package com.kingdomstudio.modules.desktop.service;

import com.kingdomstudio.common.ResultCode;
import com.kingdomstudio.common.exception.BusinessException;
import com.kingdomstudio.modules.desktop.vo.AgentStatusVO;
import com.kingdomstudio.modules.desktop.vo.DispatchPlanVO;
import com.kingdomstudio.modules.desktop.vo.KeyCommandVO;
import com.kingdomstudio.modules.desktop.vo.ProtocolVO;
import com.kingdomstudio.modules.music.vo.KeySequenceVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 桌面代理（模拟实现）。
 *
 * <p><b>这个类不会按下任何一个键。</b> 它只做三件事：
 * <ol>
 *   <li>把按键序列翻译成「待发送的命令流」（按下 / 松开成对、带相对时间）；</li>
 *   <li>按一组硬规则校验这份命令流，把不可行的部分拦下来并说明原因；</li>
 *   <li>提供协议描述与状态，让前端和未来的真实代理照着实现。</li>
 * </ol>
 *
 * <p>为什么先只做模拟：真实系统输入意味着程序去操作别人的键盘，
 * 一旦出错可能落到任意窗口上。先把协议、命令流与安全检查做扎实，
 * 等有明确的单人使用场景与急停手段（例如按住 ESC 立即中止）再考虑接真实执行。
 */
@Slf4j
@Service
public class DesktopAgentService {

	/** 协议版本：改动消息结构时必须一起改，客户端据此判断兼容性 */
	public static final String PROTOCOL_VERSION = "1.0";

	/** 代理要连的地址（与项目 context-path 保持一致） */
	public static final String ENDPOINT = "/api/desktop/ws";

	/** 同一次动作里，按下与松开之间的时间不能短于这个值：太短等于没按下 */
	public static final int MIN_HOLD_MS = 40;

	/** 单次按住的上限：超过就当数据有问题，截断并说明 */
	public static final int MAX_HOLD_MS = 2_000;

	/** 同一个键两次按下之间的最小间隔：物理上不可能更快地重复按同一个键 */
	public static final int MIN_REPRESS_GAP_MS = 60;

	/** 一次派发的命令总数上限，防止误把整份超长曲谱丢进来 */
	public static final int MAX_COMMANDS = 4_000;

	private static final List<String> NOTES = List.of(
			"本期只做模拟：不会产生任何真实的键盘/鼠标输入。",
			"接真实执行前需要：明确的单人使用场景、可见的急停方式（如按住 ESC 立即中止）、以及只在前台窗口生效的约束。",
			"按键序列由服务端按乐器档案重新计算，前端不能直接提交按键，避免越权构造输入。");

	public AgentStatusVO status() {
		return AgentStatusVO.builder()
				.available(false)
				.mode("MOCK")
				.protocolVersion(PROTOCOL_VERSION)
				.endpoint(ENDPOINT)
				.sessions(0)
				.notes(NOTES)
				.build();
	}

	/** 协议描述：字段、消息类型与安全约束 */
	public ProtocolVO protocol() {
		List<ProtocolVO.FieldDoc> envelope = List.of(
				field("type", "string", "消息类型，见 messages"),
				field("sessionId", "string", "会话 id，由服务端在 dispatch 时生成"),
				field("atMs", "number", "相对曲首的毫秒时间；代理按它在本地计时"),
				field("payload", "object", "具体内容，随 type 变化"));

		List<ProtocolVO.MessageDoc> messages = List.of(
				message("AGENT_HELLO", "AGENT_TO_CLIENT",
						"代理上线时先自报家门：版本、操作系统、是否具备输入权限",
						"{\"type\":\"AGENT_HELLO\",\"payload\":{\"version\":\"1.0\",\"os\":\"Windows 11\",\"canInjectInput\":true}}"),
				message("SESSION_OPEN", "CLIENT_TO_AGENT",
						"开始一次演奏会话，携带完整按键命令流",
						"{\"type\":\"SESSION_OPEN\",\"sessionId\":\"s-1\",\"payload\":{\"commands\":[{\"seq\":1,\"key\":\"Z\",\"action\":\"PRESS\",\"atMs\":0,\"holdMs\":480}]}}"),
				message("SESSION_TICK", "AGENT_TO_CLIENT",
						"代理每完成一批命令回报进度，便于界面同步",
						"{\"type\":\"SESSION_TICK\",\"sessionId\":\"s-1\",\"payload\":{\"done\":12,\"total\":40,\"atMs\":6000}}"),
				message("SESSION_ABORT", "CLIENT_TO_AGENT",
						"中止会话：代理必须立刻松开所有已按下的键",
						"{\"type\":\"SESSION_ABORT\",\"sessionId\":\"s-1\",\"payload\":{\"reason\":\"user-cancel\"}}"),
				message("AGENT_ERROR", "AGENT_TO_CLIENT",
						"出错回报：键位不支持、权限不足、被急停等",
						"{\"type\":\"AGENT_ERROR\",\"sessionId\":\"s-1\",\"payload\":{\"code\":\"NO_INPUT_PERMISSION\",\"message\":\"代理未获得输入权限\"}}"));

		List<String> safety = List.of(
				"真实执行必须在用户明确点击「开始演奏」后才允许，且界面全程显示进行中状态。",
				"急停：代理本地监听 ESC，按住即中止并松开所有键；服务端也可以随时下发 SESSION_ABORT。",
				"只在前台目标窗口生效，不做后台注入、不做跨窗口广播。",
				"单次会话命令数上限 " + MAX_COMMANDS + " 条、单次按住上限 " + MAX_HOLD_MS + "ms，超限即拒绝而不是降级执行。",
				"不记录、不上传任何按键之外的用户输入内容。");

		return ProtocolVO.builder()
				.version(PROTOCOL_VERSION)
				.transport("WebSocket")
				.endpoint(ENDPOINT)
				.envelope(envelope)
				.messages(messages)
				.safety(safety)
				.build();
	}

	/**
	 * 生成派发计划（模拟）。
	 *
	 * <p>规则都写在代码里而不是配置里：这几条是安全底线，
	 * 不应该出现「改个配置就能绕过」的情况。
	 */
	public DispatchPlanVO plan(KeySequenceVO sequence, String clientId) {
		if (sequence == null || sequence.getStrokes() == null || sequence.getStrokes().isEmpty()) {
			throw new BusinessException(ResultCode.BAD_REQUEST, "这首曲子没有任何可派发的按键，请先完成映射");
		}
		List<String> warnings = new ArrayList<>();
		List<KeyCommandVO> commands = new ArrayList<>();
		// 记录每个键当前的松开时间：用来判断「同一个键还没松开又要按下」的情况
		Map<String, Integer> heldUntil = new HashMap<>();
		int seq = 1;

		for (KeySequenceVO.Stroke stroke : sequence.getStrokes()) {
			int hold = stroke.getDurationMs() == null ? MIN_HOLD_MS : stroke.getDurationMs();
			boolean adjusted = Boolean.TRUE.equals(stroke.getAdjusted());
			if (hold < MIN_HOLD_MS) {
				warnings.add("第 " + stroke.getSeq() + " 次按键只有 " + hold + "ms，短于最短按键时长 "
						+ MIN_HOLD_MS + "ms，已按最短时长发送。");
				hold = MIN_HOLD_MS;
			}
			if (hold > MAX_HOLD_MS) {
				warnings.add("第 " + stroke.getSeq() + " 次按键长达 " + hold + "ms，超过单次上限 "
						+ MAX_HOLD_MS + "ms，已截断。");
				hold = MAX_HOLD_MS;
			}
			for (String key : stroke.getKeys()) {
				if (!isSendableKey(key)) {
					warnings.add("第 " + stroke.getSeq() + " 次按键的「" + key + "」不是单个字符，已跳过：协议只传单键。");
					continue;
				}
				int at = stroke.getStartMs() == null ? 0 : stroke.getStartMs();
				Integer release = heldUntil.get(key);
				if (release != null && at < release + MIN_REPRESS_GAP_MS) {
					// 物理上不可能在没松开时再次按下同一个键：顺延到上一次松开之后
					int shifted = release + MIN_REPRESS_GAP_MS;
					warnings.add("键 " + key + " 在 " + at + "ms 处被重复按下，但上一次要到 " + release
							+ "ms 才松开，已顺延到 " + shifted + "ms。");
					at = shifted;
				}
				int releaseAt = at + hold;
				commands.add(KeyCommandVO.builder()
						.seq(seq++)
						.key(key)
						.action("PRESS")
						.atMs(at)
						.holdMs(hold)
						.strokeSeq(stroke.getSeq())
						.adjusted(adjusted)
						.build());
				commands.add(KeyCommandVO.builder()
						.seq(seq++)
						.key(key)
						.action("RELEASE")
						.atMs(releaseAt)
						.strokeSeq(stroke.getSeq())
						.adjusted(adjusted)
						.build());
				heldUntil.put(key, releaseAt);
				if (commands.size() > MAX_COMMANDS) {
					throw new BusinessException(ResultCode.BAD_REQUEST,
							"命令数超过上限（" + MAX_COMMANDS + " 条），请先裁短曲子再派发");
				}
			}
		}

		commands.sort(Comparator.comparingInt(KeyCommandVO::getAtMs).thenComparingInt(KeyCommandVO::getSeq));
		String sessionId = "mock-" + UUID.randomUUID().toString().substring(0, 8);
		log.info("生成派发计划（模拟）：session={} 任务={} 命令={} 警告={}",
				sessionId, sequence.getTaskName(), commands.size(), warnings.size());

		List<String> notes = new ArrayList<>(NOTES);
		if (clientId != null && !clientId.isBlank()) {
			notes.add(0, "请求方：" + clientId + "；本次为模拟派发，未产生任何真实输入。");
		}

		return DispatchPlanVO.builder()
				.sessionId(sessionId)
				.mode("MOCK")
				.taskId(sequence.getTaskId())
				.taskName(sequence.getTaskName())
				.profileId(sequence.getProfileId())
				.profileName(sequence.getProfileName())
				.durationMs(sequence.getStrokes().stream()
						.mapToInt(stroke -> (stroke.getStartMs() == null ? 0 : stroke.getStartMs())
								+ (stroke.getDurationMs() == null ? 0 : stroke.getDurationMs()))
						.max().orElse(0))
				.accepted(commands.size())
				.warnings(warnings)
				.commands(commands)
				.notes(notes)
				.build();
	}

	/** 协议只传单键：多字符的键（例如 "F5"）不在这套协议的语义里 */
	boolean isSendableKey(String key) {
		return key != null && key.length() == 1 && !Character.isWhitespace(key.charAt(0));
	}

	private ProtocolVO.FieldDoc field(String name, String type, String description) {
		return ProtocolVO.FieldDoc.builder().name(name).type(type).description(description).build();
	}

	private ProtocolVO.MessageDoc message(String type, String direction, String description, String example) {
		return ProtocolVO.MessageDoc.builder()
				.type(type)
				.direction(direction)
				.description(description)
				.example(example)
				.build();
	}
}
