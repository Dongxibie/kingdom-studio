package com.kingdomstudio.modules.music.execution;

import com.kingdomstudio.modules.music.vo.ExecutionResultVO;
import com.kingdomstudio.modules.music.vo.ExecutionStatusVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 本机会话的单元测试。
 *
 * <p>全部用**记录式注入器**（一个真键都不发）与**假窗口守护**，
 * 所以这些用例可以在任何机器上跑，不会往桌面发按键。
 * 覆盖需求里点名要测的五件事：执行顺序、暂停恢复、停止释放、异常退出、空计划，
 * 外加两条安全行为：ESC 急停、窗口一换自动暂停。
 */
class ExecutionSessionTest {

	/** 假窗口守护：窗口标题可以随时改，也可以手动触发急停 */
	static class FakeGuard implements WindowGuard {

		volatile String window = "游戏窗口";
		volatile Consumer<String> onStop = reason -> { };
		volatile boolean closed = false;

		@Override
		public String currentWindow() {
			return window;
		}

		@Override
		public long lastUpdateMs() {
			return 0;
		}

		@Override
		public boolean alive() {
			return !closed;
		}

		@Override
		public void close() {
			closed = true;
		}

		@Override
		public String kind() {
			return "fake";
		}

		void pressEscape() {
			onStop.accept("检测到 ESC：立即停止并松开所有按键");
		}
	}

	private ExecutionResultVO.Command command(String key, String action, int atMs) {
		return ExecutionResultVO.Command.builder().seq(0).key(key).action(action).atMs(atMs).build();
	}

	private ExecutionSession session(List<ExecutionResultVO.Command> commands, KeyInjector injector,
			FakeGuard guard, String targetWindow) {
		guard.onStop = reason -> { };
		return new ExecutionSession(1L, "小星星", "光遇式 15 键", commands, injector, guard, targetWindow, 200);
	}

	@Test
	@DisplayName("执行顺序：按下先于松开，事件按时间轴依次发出，最后落在「已完成」")
	void playsCommandsInOrder() throws Exception {
		KeyInjector.RecordingInjector injector = new KeyInjector.RecordingInjector();
		FakeGuard guard = new FakeGuard();
		ExecutionSession session = session(List.of(
				command("Z", "DOWN", 0),
				command("Z", "UP", 30),
				command("X", "DOWN", 40),
				command("X", "UP", 70)), injector, guard, "游戏窗口");

		session.start();
		assertTrue(session.awaitTermination(3000), "应当在超时前结束");

		assertEquals(List.of("DOWN Z", "UP Z", "DOWN X", "UP X"), injector.actions());
		assertEquals(ExecutionStatus.FINISHED, session.getStatus());
		ExecutionStatusVO snapshot = session.snapshot();
		assertEquals(100, snapshot.getProgress());
		assertEquals(4, snapshot.getExecutedCount());
		assertTrue(snapshot.getHeldKeys().isEmpty(), "演奏结束后不该有按住的键");
	}

	@Test
	@DisplayName("暂停恢复：暂停会松开按住的键，时间轴停住，继续后接着弹完")
	void pauseReleasesKeysAndKeepsTimeline() throws Exception {
		KeyInjector.RecordingInjector injector = new KeyInjector.RecordingInjector();
		FakeGuard guard = new FakeGuard();
		ExecutionSession session = session(List.of(
				command("Z", "DOWN", 0),
				command("Z", "UP", 120),
				command("X", "DOWN", 150),
				command("X", "UP", 220)), injector, guard, "游戏窗口");

		session.start();
		// 等第一个按下发生
		Thread.sleep(40);
		session.pause("测试暂停");
		Thread.sleep(60);

		assertEquals(ExecutionStatus.PAUSED, session.getStatus());
		assertTrue(injector.actions().contains("UP Z"), "暂停必须松开按住的键：" + injector.actions());
		assertTrue(session.snapshot().getHeldKeys().isEmpty());
		int positionWhilePaused = session.snapshot().getExecutedCount();
		Thread.sleep(80);
		assertEquals(positionWhilePaused, session.snapshot().getExecutedCount(), "暂停期间不该继续发按键");

		session.resume();
		assertTrue(session.awaitTermination(3000));
		assertEquals(ExecutionStatus.FINISHED, session.getStatus());
		assertEquals(4, session.snapshot().getExecutedCount());
	}

	@Test
	@DisplayName("停止：立刻停下、松开所有按住的键、状态落在「已停止」")
	void stopReleasesHeldKeys() throws Exception {
		KeyInjector.RecordingInjector injector = new KeyInjector.RecordingInjector();
		FakeGuard guard = new FakeGuard();
		ExecutionSession session = session(List.of(
				command("Z", "DOWN", 0),
				command("Z", "UP", 4000)), injector, guard, "游戏窗口");

		session.start();
		Thread.sleep(60);
		session.stop("测试停止");
		assertTrue(session.awaitTermination(2000), "停止后执行线程应当很快收尾");

		assertEquals(ExecutionStatus.STOPPED, session.getStatus());
		assertTrue(injector.actions().contains("DOWN Z"));
		assertTrue(injector.actions().contains("UP Z"), "停止时必须松开按住的键：" + injector.actions());
		ExecutionStatusVO snapshot = session.snapshot();
		assertTrue(snapshot.getHeldKeys().isEmpty());
		assertEquals("测试停止", snapshot.getStopReason());
	}

	@Test
	@DisplayName("异常退出：注入过程中抛异常，也要松开已按下的键并落到「已停止」")
	void exceptionStillReleasesKeys() throws Exception {
		List<String> actions = new ArrayList<>();
		KeyInjector broken = new KeyInjector() {
			private int count = 0;

			@Override
			public void keyDown(String key) {
				count++;
				if (count >= 2) {
					throw new IllegalStateException("模拟注入失败");
				}
				actions.add("DOWN " + key);
			}

			@Override
			public void keyUp(String key) {
				actions.add("UP " + key);
			}

			@Override
			public String describe() {
				return "会失败的注入器";
			}

			@Override
			public int keyDelayMs() {
				return 0;
			}
		};
		FakeGuard guard = new FakeGuard();
		ExecutionSession session = session(List.of(
				command("Z", "DOWN", 0),
				command("X", "DOWN", 10),
				command("Z", "UP", 20)), broken, guard, "游戏窗口");

		session.start();
		assertTrue(session.awaitTermination(3000));

		assertEquals(ExecutionStatus.STOPPED, session.getStatus());
		assertTrue(session.snapshot().getStopReason().contains("执行中断"));
		assertTrue(actions.contains("UP Z"), "异常退出也必须松开按住的键：" + actions);
		assertTrue(session.snapshot().getHeldKeys().isEmpty());
	}

	@Test
	@DisplayName("空计划：没有命令直接算完成，不会卡住也不会报错")
	void emptyPlanFinishesImmediately() throws Exception {
		KeyInjector.RecordingInjector injector = new KeyInjector.RecordingInjector();
		FakeGuard guard = new FakeGuard();
		ExecutionSession session = session(List.of(), injector, guard, "游戏窗口");

		session.start();
		assertTrue(session.awaitTermination(2000));

		assertEquals(ExecutionStatus.FINISHED, session.getStatus());
		assertTrue(injector.actions().isEmpty());
		assertEquals(100, session.snapshot().getProgress());
	}

	@Test
	@DisplayName("ESC 急停：守护上报 ESC 后立即停止并松开按键")
	void escapeStopsImmediately() throws Exception {
		KeyInjector.RecordingInjector injector = new KeyInjector.RecordingInjector();
		FakeGuard guard = new FakeGuard();
		ExecutionSession session = session(List.of(
				command("Z", "DOWN", 0),
				command("Z", "UP", 5000)), injector, guard, "游戏窗口");
		guard.onStop = reason -> session.stop(reason);

		session.start();
		Thread.sleep(50);
		guard.pressEscape();

		assertEquals(ExecutionStatus.STOPPED, session.getStatus());
		assertTrue(session.snapshot().getStopReason().contains("ESC"));
		assertTrue(injector.actions().contains("UP Z"), "急停必须松开按住的键：" + injector.actions());
	}

	@Test
	@DisplayName("窗口切换：前台窗口不是确认过的那一个就自动暂停，切回去能继续")
	void focusChangeAutoPauses() throws Exception {
		KeyInjector.RecordingInjector injector = new KeyInjector.RecordingInjector();
		FakeGuard guard = new FakeGuard();
		ExecutionSession session = session(List.of(
				command("Z", "DOWN", 0),
				command("Z", "UP", 60),
				command("X", "DOWN", 90),
				command("X", "UP", 150)), injector, guard, "游戏窗口");

		session.start();
		Thread.sleep(20);
		guard.window = "别的窗口";
		Thread.sleep(120);

		assertEquals(ExecutionStatus.PAUSED, session.getStatus());
		assertTrue(session.snapshot().getWarnings().stream().anyMatch(item -> item.contains("自动暂停")),
				session.snapshot().getWarnings().toString());
		assertTrue(session.snapshot().getHeldKeys().isEmpty(), "自动暂停时也要松开按键");

		guard.window = "游戏窗口";
		session.resume();
		assertTrue(session.awaitTermination(3000));
		assertEquals(ExecutionStatus.FINISHED, session.getStatus());
	}

	@Test
	@DisplayName("没有确认过窗口时不判断窗口，照常执行（记录模式的自检场景）")
	void emptyTargetWindowSkipsFocusCheck() throws Exception {
		KeyInjector.RecordingInjector injector = new KeyInjector.RecordingInjector();
		FakeGuard guard = new FakeGuard();
		ExecutionSession session = session(List.of(
				command("Z", "DOWN", 0),
				command("Z", "UP", 20)), injector, guard, "");

		session.start();
		assertTrue(session.awaitTermination(2000));
		assertEquals(ExecutionStatus.FINISHED, session.getStatus());
		assertFalse(session.snapshot().getWarnings().stream().anyMatch(item -> item.contains("自动暂停")));
	}
}
