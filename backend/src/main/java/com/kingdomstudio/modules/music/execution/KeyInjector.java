package com.kingdomstudio.modules.music.execution;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.List;

/**
 * 按键注入通道。
 *
 * <p>抽成接口的唯一目的是**让测试不需要真的按键**：单测注入记录式实现，
 * 就能把「顺序对不对、暂停会不会漏按、停止有没有松开」全部断言清楚，
 * 同时保证测试永远不会往你的桌面发按键。
 *
 * <p>真实实现只有一种：{@link Robot}（JDK 自带，不需要任何原生依赖）。
 * 它把按键送进系统输入队列，因此**谁在前台谁收到**——这也正是「只对前台窗口生效」的由来，
 * 上层会用 {@link WindowGuard} 守住「前台窗口是不是用户确认过的那一个」。
 */
public interface KeyInjector {

	/** 按下 */
	void keyDown(String key);

	/** 松开 */
	void keyUp(String key);

	/** 松开一批键（急停与暂停时用，重复松开是无害的） */
	default void releaseAll(Collection<String> keys) {
		if (keys == null) {
			return;
		}
		keys.forEach(this::keyUp);
	}

	/** 给状态面板看的说明：是真实注入还是记录模式 */
	String describe();

	/** 建议的按键间隔（毫秒）：给系统与目标程序留出反应时间 */
	int keyDelayMs();

	/** 真实实现：java.awt.Robot */
	class RobotInjector implements KeyInjector {

		private final Robot robot;

		public RobotInjector() {
			try {
				this.robot = new Robot();
				this.robot.setAutoDelay(0);
				this.robot.setAutoWaitForIdle(false);
			} catch (Exception error) {
				throw new IllegalStateException("当前环境无法初始化 AWT Robot（需要图形会话）：" + error.getMessage(), error);
			}
		}

		@Override
		public void keyDown(String key) {
			int code = keyCodeOf(key);
			robot.keyPress(code);
		}

		@Override
		public void keyUp(String key) {
			int code = keyCodeOf(key);
			robot.keyRelease(code);
		}

		@Override
		public String describe() {
			return "真实按键（java.awt.Robot → 系统输入队列，只影响前台窗口）";
		}

		@Override
		public int keyDelayMs() {
			return 12;
		}

		/** 单个字符 → 虚拟键码；非单键直接拒绝（协议只传单键） */
		static int keyCodeOf(String key) {
			if (key == null || key.length() != 1) {
				throw new IllegalArgumentException("只能发送单个字符的键：" + key);
			}
			int code = KeyEvent.getExtendedKeyCodeForChar(key.charAt(0));
			if (code == KeyEvent.VK_UNDEFINED) {
				throw new IllegalArgumentException("无法映射这个键：" + key);
			}
			return code;
		}
	}

	/** 记录实现：只把动作记下来，一个键都不发（测试与 dry-run 用） */
	class RecordingInjector implements KeyInjector {

		private final List<String> actions = java.util.Collections.synchronizedList(new java.util.ArrayList<>());

		@Override
		public void keyDown(String key) {
			actions.add("DOWN " + key);
		}

		@Override
		public void keyUp(String key) {
			actions.add("UP " + key);
		}

		@Override
		public String describe() {
			return "记录模式（不发真实按键，用于自检与测试）";
		}

		@Override
		public int keyDelayMs() {
			return 0;
		}

		/** 已记录的动作，形如 DOWN Z / UP Z */
		public List<String> actions() {
			return List.copyOf(actions);
		}
	}
}
