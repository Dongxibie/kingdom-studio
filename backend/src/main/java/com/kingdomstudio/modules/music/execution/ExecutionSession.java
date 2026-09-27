package com.kingdomstudio.modules.music.execution;

import com.kingdomstudio.modules.music.vo.ExecutionResultVO;
import com.kingdomstudio.modules.music.vo.ExecutionStatusVO;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 一次本机演奏会话：状态机 + 执行线程 + 随时可停。
 *
 * <p>这里集中了所有「演奏过程中会不会出事」的判断，逐条对应需求里的安全要求：
 *
 * <ol>
 *   <li><b>用户主动开启</b>：构造之前必须已经通过「开关 + 显式确认 + 目标窗口」三道检查（在 ExecutionService 里做）；</li>
 *   <li><b>可以随时停止</b>：{@link #stop} 置位后 10ms 内退出循环，并在 finally 里松开所有按住的键；</li>
 *   <li><b>ESC 急停</b>：由 {@link WindowGuard} 上报，走的是同一个 {@link #stop}，不另开一条路径；</li>
 *   <li><b>只对确认过的窗口生效</b>：每条命令前比对前台窗口标题，对不上就<b>自动暂停</b>并说明原因；</li>
 *   <li><b>状态可见</b>：进度、当前按键、剩余时间、按住的键、窗口标题都通过 {@link #snapshot()} 对外暴露；</li>
 *   <li><b>异常也释放</b>：无论正常结束、停止还是抛异常，finally 都会松开按住的键并关掉守护进程。</li>
 * </ol>
 *
 * <p>执行线程是独立的一条，接口线程只读状态，不阻塞。
 */
@Slf4j
public class ExecutionSession {

	/** 等待过程中检查停止/暂停/窗口的间隔 */
	private static final int TICK_MS = 10;

	private final Long taskId;
	private final String taskName;
	private final String profileName;
	private final List<ExecutionResultVO.Command> commands;
	private final KeyInjector injector;
	private final WindowGuard guard;
	/** 用户确认过的目标窗口标题（空表示没有约束） */
	private final String targetWindow;
	private final int duration;

	private final Set<String> heldKeys = ConcurrentHashMap.newKeySet();
	private final List<String> warnings = Collections.synchronizedList(new ArrayList<>());

	private volatile ExecutionStatus status = ExecutionStatus.READY;
	private volatile int position;
	private volatile String currentKey = "";
	private volatile String stopReason = "";
	private volatile long startedAtMs;
	private volatile long elapsedMs;
	private volatile long pausedAccumulatedMs;
	private volatile long pausedAtMs;
	private volatile boolean stopRequested;
	private volatile boolean pauseRequested;
	private volatile Thread worker;

	public ExecutionSession(Long taskId, String taskName, String profileName,
			List<ExecutionResultVO.Command> commands, KeyInjector injector, WindowGuard guard,
			String targetWindow, int duration) {
		this.taskId = taskId;
		this.taskName = taskName;
		this.profileName = profileName;
		this.commands = List.copyOf(commands);
		this.injector = injector;
		this.guard = guard;
		this.targetWindow = targetWindow == null ? "" : targetWindow;
		this.duration = duration;
	}

	public Long getTaskId() {
		return taskId;
	}

	public ExecutionStatus getStatus() {
		return status;
	}

	/** 开始演奏：只有「就绪」状态能开 */
	public synchronized void start() {
		if (status != ExecutionStatus.READY) {
			throw new IllegalStateException("当前状态是「" + status.label() + "」，不能再开始");
		}
		status = ExecutionStatus.RUNNING;
		startedAtMs = System.nanoTime() / 1_000_000;
		worker = new Thread(this::run, "ks-performance-" + taskId);
		worker.setDaemon(true);
		worker.start();
		log.info("本机演奏开始：task={} 命令={} 目标窗口={} 注入方式={}",
				taskId, commands.size(), targetWindow, injector.describe());
	}

	/** 主循环：按时间轴发按键，途中响应暂停 / 停止 / 窗口变化 */
	private void run() {
		try {
			for (int index = 0; index < commands.size(); index++) {
				ExecutionResultVO.Command command = commands.get(index);
				if (stopRequested) {
					break;
				}
				// 对齐到这条命令的时间点（暂停期间的时间不计入）
				waitUntil(command.getAtMs());
				if (stopRequested) {
					break;
				}
				awaitResume();
				if (stopRequested) {
					break;
				}
				if (focusChanged()) {
					autoPauseByFocus();
					awaitResume();
					if (stopRequested) {
						break;
					}
				}
				send(command);
				position = index + 1;
			}
			if (!stopRequested && position >= commands.size()) {
				status = ExecutionStatus.FINISHED;
				log.info("本机演奏完成：task={} 命令={}", taskId, commands.size());
			}
		} catch (InterruptedException error) {
			Thread.currentThread().interrupt();
			stopReason = "执行线程被中断";
			status = ExecutionStatus.STOPPED;
		} catch (Exception error) {
			// 异常退出也必须把按住的键放开
			warnings.add("执行中断：" + error.getMessage());
			stopReason = "执行中断：" + error.getMessage();
			status = ExecutionStatus.STOPPED;
			log.warn("本机演奏异常退出：task={} {}", taskId, error.getMessage());
		} finally {
			releaseHeld();
			guard.close();
			if (status == ExecutionStatus.RUNNING || status == ExecutionStatus.PAUSED) {
				status = ExecutionStatus.STOPPED;
			}
		}
	}

	private void send(ExecutionResultVO.Command command) {
		currentKey = command.getKey();
		int delay = injector.keyDelayMs();
		if ("DOWN".equals(command.getAction())) {
			injector.keyDown(command.getKey());
			heldKeys.add(command.getKey());
		} else {
			injector.keyUp(command.getKey());
			heldKeys.remove(command.getKey());
		}
		if (delay > 0) {
			sleep(delay);
		}
	}

	/** 等到这条命令的时间点：暂停的时间要扣掉，所以用「排定时间 + 累计暂停」做基准 */
	private void waitUntil(int atMs) throws InterruptedException {
		while (true) {
			long target = atMs + pausedAccumulatedMs;
			long now = nowMs() - startedAtMs;
			if (now >= target) {
				return;
			}
			if (stopRequested) {
				return;
			}
			if (pauseRequested) {
				awaitResume();
				continue;
			}
			if (focusChanged()) {
				autoPauseByFocus();
				continue;
			}
			sleep((int) Math.min(TICK_MS, target - now));
		}
	}

	/** 暂停中就在这里等；恢复时把这段暂停时长记进累计值，后面的时间轴自动顺延 */
	private void awaitResume() throws InterruptedException {
		if (!pauseRequested) {
			return;
		}
		long at = nowMs();
		status = ExecutionStatus.PAUSED;
		while (pauseRequested && !stopRequested) {
			sleep(TICK_MS * 5);
		}
		pausedAccumulatedMs += nowMs() - at;
		if (!stopRequested) {
			status = ExecutionStatus.RUNNING;
		}
	}

	private boolean focusChanged() {
		if (targetWindow.isEmpty()) {
			return false;
		}
		String current = guard.currentWindow();
		if (current.isEmpty()) {
			return false;
		}
		return !current.equals(targetWindow);
	}

	private void autoPauseByFocus() {
		if (pauseRequested) {
			return;
		}
		String current = guard.currentWindow();
		String note = "前台窗口已切到「" + current + "」，已自动暂停：本机演奏只对确认过的窗口「" + targetWindow + "」生效。";
		log.info("本机演奏自动暂停：task={} {}", taskId, note);
		pause(note);
	}

	/**
	 * 暂停：状态立刻变「已暂停」，并**马上松开所有按住的键**。
	 *
	 * <p>不等工作线程走到检查点再松：目标程序里留着一个按住的键，比时间轴晚几十毫秒严重得多。
	 * 时间轴本身由 {@link #awaitResume()} 记账，累计的暂停时长会在恢复后自动补回来。
	 */
	public synchronized void pause(String reason) {
		if (status != ExecutionStatus.RUNNING) {
			return;
		}
		pauseRequested = true;
		status = ExecutionStatus.PAUSED;
		if (reason != null && !reason.isBlank() && !warnings.contains(reason)) {
			warnings.add(reason);
		}
		releaseHeld();
	}

	/** 继续：状态立刻回到「演奏中」，时间轴由工作线程记账（暂停时长会补回） */
	public synchronized void resume() {
		if (status != ExecutionStatus.PAUSED) {
			return;
		}
		pauseRequested = false;
		status = ExecutionStatus.RUNNING;
	}

	/** 停止（人工或急停）：置位后执行线程会在 10ms 内退出，finally 负责松开所有键 */
	public synchronized void stop(String reason) {
		if (status.terminal()) {
			return;
		}
		stopRequested = true;
		pauseRequested = false;
		stopReason = reason == null ? "已停止" : reason;
		status = ExecutionStatus.STOPPED;
		releaseHeld();
		log.info("本机演奏停止：task={} 原因={}", taskId, stopReason);
	}

	private void releaseHeld() {
		if (heldKeys.isEmpty()) {
			return;
		}
		List<String> keys = new ArrayList<>(heldKeys);
		try {
			injector.releaseAll(keys);
		} catch (Exception error) {
			log.warn("释放按键失败（继续收尾）：{}", error.getMessage());
		} finally {
			heldKeys.clear();
		}
	}

	/** 等执行线程收尾，供停止接口确认「已经真的停了」 */
	public boolean awaitTermination(long timeoutMs) {
		Thread thread = worker;
		if (thread == null) {
			return true;
		}
		try {
			thread.join(timeoutMs);
			return !thread.isAlive();
		} catch (InterruptedException error) {
			Thread.currentThread().interrupt();
			return false;
		}
	}

	public ExecutionStatusVO snapshot() {
		int executed = position;
		int progress = commands.isEmpty() ? 100 : (int) Math.round(executed * 100.0 / commands.size());
		long now = status == ExecutionStatus.RUNNING || status == ExecutionStatus.PAUSED ? nowMs() - startedAtMs : elapsedMs;
		elapsedMs = now;
		int elapsed = (int) Math.max(0, now - pausedAccumulatedMs);
		return ExecutionStatusVO.builder()
				.taskId(taskId)
				.taskName(taskName)
				.profileName(profileName)
				.status(status.name())
				.statusLabel(status.label())
				.progress(progress)
				.currentKey(currentKey)
				.executedCount(executed)
				.commandCount(commands.size())
				.elapsedMs(elapsed)
				.duration(duration)
				.remainingMs(Math.max(0, duration - elapsed))
				.heldKeys(new ArrayList<>(heldKeys))
				.targetWindow(targetWindow)
				.currentWindow(guard.currentWindow())
				.guardAlive(guard.alive())
				.injector(injector.describe())
				.warnings(new ArrayList<>(warnings))
				.stopReason(stopReason)
				.startedAt(LocalDateTime.now())
				.build();
	}

	private void sleep(int ms) {
		try {
			TimeUnit.MILLISECONDS.sleep(Math.max(1, ms));
		} catch (InterruptedException error) {
			Thread.currentThread().interrupt();
		}
	}

	private long nowMs() {
		return System.nanoTime() / 1_000_000;
	}
}
