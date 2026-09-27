package com.kingdomstudio.modules.music.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kingdomstudio.common.ResultCode;
import com.kingdomstudio.common.exception.BusinessException;
import com.kingdomstudio.modules.music.dto.LocalRunRequestDTO;
import com.kingdomstudio.modules.music.entity.PerformancePlan;
import com.kingdomstudio.modules.music.execution.ExecutionSession;
import com.kingdomstudio.modules.music.execution.ExecutionStatus;
import com.kingdomstudio.modules.music.execution.KeyInjector;
import com.kingdomstudio.modules.music.execution.WindowGuard;
import com.kingdomstudio.modules.music.vo.ExecutionResultVO;
import com.kingdomstudio.modules.music.vo.ExecutionStatusVO;
import com.kingdomstudio.modules.music.vo.RuntimePreflightVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 本机演奏的会话管理：环境自检 → 启动 → 暂停/继续 → 停止（含急停）。
 *
 * <p>这一层是「谁能真的发按键」的唯一出口，四道闸门都在这里：
 * <ol>
 *   <li><b>配置开关</b> {@code kingdom.music.local-execution.enabled}，默认关闭；</li>
 *   <li><b>显式确认</b>：请求里 {@code confirm=true} 才继续，不接受默认开启；</li>
 *   <li><b>目标窗口</b>：默认取当前前台窗口并要求使用者确认，会话期间只认这个窗口；</li>
 *   <li><b>注入方式</b>：默认 {@code robot}；{@code record} 是记录模式，一个键都不发（自检与测试用）。
 *       真实注入时如果窗口守护起不来，直接拒绝启动 —— 宁可不开，也不要在看不见窗口的情况下乱发按键。</li>
 * </ol>
 */
@Slf4j
@Service
public class ExecutionService {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	/** 自检时等前台窗口上报的时间上限 */
	private static final long WINDOW_PROBE_TIMEOUT_MS = 2500;

	/** 一次只允许一个会话：两个会话同时发按键，急停就说不清该松哪个 */
	private volatile ExecutionSession session;
	private volatile WindowGuard guard;

	private final PerformanceMacroService macroService;

	@Value("${kingdom.music.local-execution.enabled:false}")
	private boolean enabled;

	@Value("${kingdom.music.local-execution.injector:robot}")
	private String injectorMode;

	@Value("${kingdom.music.local-execution.guard-script:}")
	private String guardScript;

	@Value("${kingdom.music.local-execution.guard:auto}")
	private String guardMode;

	public ExecutionService(PerformanceMacroService macroService) {
		this.macroService = macroService;
	}

	/** 环境自检：界面在点「本地演奏」之前先看这个 */
	public RuntimePreflightVO preflight() {
		WindowGuard probe = newGuard(reason -> { });
		String currentWindow = "";
		// 可用性看的是「真的拿到了一个守护」，不是配置里写没写 auto：
		// 守门脚本找不到时会降级成空实现，那种情况下不允许真实注入
		boolean guardAvailable = !"none".equals(probe.kind());
		try {
			// 守门进程要一两秒才起来，这里等第一份窗口标题；等不到就按「读不到」处理
			long deadline = System.currentTimeMillis() + WINDOW_PROBE_TIMEOUT_MS;
			while (System.currentTimeMillis() < deadline) {
				currentWindow = probe.currentWindow();
				if (!currentWindow.isEmpty()) {
					break;
				}
				Thread.sleep(120);
			}
		} catch (InterruptedException error) {
			Thread.currentThread().interrupt();
		} finally {
			probe.close();
		}
		boolean robot = isRobot();
		boolean ready;
		String reason;
		if (!enabled) {
			ready = false;
			reason = "本机执行开关是关的：需要设置 MUSIC_LOCAL_EXECUTION_ENABLED=true（或在配置里打开）后重启后端。";
		} else if (robot && !guardAvailable) {
			ready = false;
			reason = "窗口守护被关掉了，无法保证「只对确认过的窗口生效」，因此不允许真实注入。";
		} else if (robot && currentWindow.isEmpty()) {
			ready = false;
			reason = "读不到当前前台窗口，无法确认目标窗口；请确认守门脚本 tools/ks-performance-guard.ps1 存在"
					+ "（可用 kingdom.music.local-execution.guard-script 指定绝对路径）且 PowerShell 可执行。";
		} else if (session != null && !session.getStatus().terminal()) {
			ready = false;
			reason = "已有一次演奏在进行中，先停止它再开始新的。";
		} else {
			ready = true;
			reason = "";
		}
		return RuntimePreflightVO.builder()
				.enabled(enabled)
				.currentWindow(currentWindow)
				.injector(robot ? "真实按键（java.awt.Robot）" : "记录模式（不发真实按键）")
				.guardAvailable(guardAvailable)
				.ready(ready)
				.reason(reason)
				.requirements(List.of(
						"本功能需要你主动开启：点开始之前请把目标窗口切到前台。",
						"演奏期间只对确认过的窗口生效：窗口一换就自动暂停。",
						"ESC 随时急停：立刻停止并松开所有按下的键。",
						"只发送按键，不移动鼠标、不注入任何进程、不做后台隐藏执行。"))
				.running(session != null && !session.getStatus().terminal())
				.build();
	}

	/** 开始本机演奏 */
	public ExecutionStatusVO start(Long taskId, LocalRunRequestDTO request) {
		RuntimePreflightVO preflight = preflight();
		if (Boolean.FALSE.equals(request == null ? null : request.getConfirm())) {
			throw new BusinessException(ResultCode.BAD_REQUEST, "需要显式确认（confirm=true）才能开始本机演奏");
		}
		if (request == null || request.getConfirm() == null) {
			throw new BusinessException(ResultCode.BAD_REQUEST, "需要显式确认（confirm=true）才能开始本机演奏");
		}
		if (!preflight.getReady()) {
			throw new BusinessException(ResultCode.BAD_REQUEST, preflight.getReason());
		}
		PerformancePlan entity = macroService.requireLatest(taskId);
		List<ExecutionResultVO.Command> commands = toCommands(entity);
		if (commands.isEmpty()) {
			throw new BusinessException(ResultCode.BAD_REQUEST, "这份演奏计划里没有任何命令，不能开始演奏");
		}
		String target = request.getTargetWindow() == null || request.getTargetWindow().isBlank()
				? preflight.getCurrentWindow() : request.getTargetWindow().trim();
		if (isRobot() && !target.equals(preflight.getCurrentWindow())) {
			throw new BusinessException(ResultCode.BAD_REQUEST,
					"目标窗口「" + target + "」不是当前前台窗口（当前是「" + preflight.getCurrentWindow()
							+ "」）：请先切到目标窗口再开始，避免按键落到别的程序里。");
		}
		stop("开始新的演奏前，先停掉上一个会话", false);
		KeyInjector injector = isRobot() ? new KeyInjector.RobotInjector() : new KeyInjector.RecordingInjector();
		WindowGuard newGuard = newGuard(this::emergencyStop);
		ExecutionSession created = new ExecutionSession(taskId, entity.getTaskName(), entity.getProfileName(),
				commands, injector, newGuard, target, entity.getDuration() == null ? 0 : entity.getDuration());
		this.guard = newGuard;
		this.session = created;
		created.start();
		return created.snapshot();
	}

	public ExecutionStatusVO status() {
		ExecutionSession current = session;
		if (current == null) {
			return ExecutionStatusVO.builder()
					.status(ExecutionStatus.READY.name())
					.statusLabel(ExecutionStatus.READY.label())
					.progress(0).executedCount(0).commandCount(0)
					.elapsedMs(0).duration(0).remainingMs(0)
					.heldKeys(List.of()).warnings(List.of()).stopReason("")
					.guardAlive(false)
					.injector(isRobot() ? "真实按键（java.awt.Robot）" : "记录模式（不发真实按键）")
					.build();
		}
		return current.snapshot();
	}

	public ExecutionStatusVO pause() {
		ExecutionSession current = requireSession();
		current.pause("已手动暂停");
		return current.snapshot();
	}

	public ExecutionStatusVO resume() {
		ExecutionSession current = requireSession();
		current.resume();
		return current.snapshot();
	}

	/** 停止：界面上的「停止」与 ESC 急停走同一条路径 */
	public ExecutionStatusVO stop() {
		return stop("已手动停止", true);
	}

	/** 急停：由窗口守护上报的 ESC 触发 */
	public void emergencyStop(String reason) {
		log.warn("急停触发：{}", reason);
		stop(reason, true);
	}

	private ExecutionStatusVO stop(String reason, boolean await) {
		ExecutionSession current = session;
		if (current == null) {
			return status();
		}
		current.stop(reason);
		if (await) {
			current.awaitTermination(1500);
		}
		WindowGuard currentGuard = guard;
		if (currentGuard != null) {
			currentGuard.close();
			guard = null;
		}
		return current.snapshot();
	}

	private ExecutionSession requireSession() {
		ExecutionSession current = session;
		if (current == null || current.getStatus().terminal()) {
			throw new BusinessException(ResultCode.BAD_REQUEST, "当前没有正在进行的演奏");
		}
		return current;
	}

	private boolean isRobot() {
		return injectorMode == null || !"record".equalsIgnoreCase(injectorMode);
	}

	/** 建一个窗口守护：守门脚本找不到、或明确配 none 时退化成不做窗口判断的实现 */
	private WindowGuard newGuard(java.util.function.Consumer<String> onEmergencyStop) {
		if (guardMode != null && "none".equalsIgnoreCase(guardMode)) {
			return new WindowGuard.NoopGuard();
		}
		Path script = WindowGuard.locateScript(guardScript == null || guardScript.isBlank()
				? null : Path.of(guardScript));
		if (script == null) {
			log.warn("找不到窗口守门脚本，窗口判断降级为空实现");
			return new WindowGuard.NoopGuard();
		}
		try {
			return new WindowGuard.PowerShellGuard(script, onEmergencyStop);
		} catch (Exception error) {
			log.warn("窗口守护启动失败，降级为空实现：{}", error.getMessage());
			return new WindowGuard.NoopGuard();
		}
	}

	private List<ExecutionResultVO.Command> toCommands(PerformancePlan entity) {
		try {
			List<Map<String, Object>> rows = OBJECT_MAPPER.readValue(entity.getNotes(),
					new TypeReference<List<Map<String, Object>>>() {
					});
			List<ExecutionResultVO.Command> commands = new ArrayList<>();
			int seq = 1;
			Map<String, Integer> downAt = new java.util.HashMap<>();
			for (Map<String, Object> row : rows) {
				String key = String.valueOf(row.get("key"));
				String action = String.valueOf(row.get("action"));
				int timestamp = ((Number) row.getOrDefault("timestamp", 0)).intValue();
				Integer hold = null;
				if ("UP".equals(action)) {
					Integer start = downAt.remove(key);
					hold = start == null ? null : timestamp - start;
				} else {
					downAt.put(key, timestamp);
				}
				commands.add(ExecutionResultVO.Command.builder()
						.seq(seq++).key(key).action(action).atMs(timestamp).holdMs(hold)
						.strokeSeq(((Number) row.getOrDefault("strokeSeq", 0)).intValue())
						.build());
			}
			return commands;
		} catch (Exception error) {
			throw new BusinessException(ResultCode.INTERNAL_ERROR, "演奏计划解析失败：" + error.getMessage());
		}
	}

	/** 给执行模式清单用：本机演奏是否可用 */
	public Map<String, Object> localModeState() {
		RuntimePreflightVO preflight = preflight();
		return Map.of(
				"mode", "LOCAL",
				"label", "本机演奏",
				"available", preflight.getReady(),
				"reason", preflight.getReady() ? "" : preflight.getReason());
	}

	/** 便于测试：直接读当前会话状态名 */
	ExecutionStatus currentStatus() {
		ExecutionSession current = session;
		return current == null ? ExecutionStatus.READY : current.getStatus();
	}

	String normalizeMode(String mode) {
		return mode == null ? "" : mode.trim().toUpperCase(Locale.ROOT);
	}
}
