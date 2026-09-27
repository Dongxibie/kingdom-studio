package com.kingdomstudio.modules.music.execution;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 前台窗口守门人 + ESC 急停通道。
 *
 * <p>这两件事都需要读系统的窗口状态与全球按键状态，纯 Java 做不到（JDK 没有 GetForegroundWindow）。
 * 与其为了两个只读调用引入原生桥接依赖，这里用系统自带的 PowerShell 起一个**只读**的小助手：
 *
 * <ul>
 *   <li>每 120ms 读一次前台窗口标题，变了就打印一行 {@code WINDOW &lt;title&gt;}；</li>
 *   <li>每 120ms 读一次 ESC 的按下状态，按下就打印 {@code ESC}。</li>
 * </ul>
 *
 * <p><b>它不记录任何按键</b>：脚本里只查询 ESC 这一个虚拟键的状态，其余键一个都不读；
 * 也不写文件、不联网。脚本本身随仓库提交（{@code tools/ks-performance-guard.ps1}），可以自己看。
 */
public interface WindowGuard {

	/** 最近一次读到的前台窗口标题（还没读到就返回空串） */
	String currentWindow();

	/** 最近一次读到的时间和现在差了多少毫秒（用来判断守护进程是不是还活着） */
	long lastUpdateMs();

	/** 是否是活的（在跑、且最近有上报） */
	boolean alive();

	/** 关掉守护进程 */
	void close();

	/** 实现类型：powershell 真守护 / none 空实现（不做窗口判断） */
	String kind();

	/** 空实现：不做窗口判断（在无法启动 PowerShell 的环境里降级用） */
	class NoopGuard implements WindowGuard {

		@Override
		public String currentWindow() {
			return "";
		}

		@Override
		public long lastUpdateMs() {
			return System.currentTimeMillis();
		}

		@Override
		public boolean alive() {
			return true;
		}

		@Override
		public void close() {
			// 什么都不用做
		}

		@Override
		public String kind() {
			return "none";
		}
	}

	/** PowerShell 实现 */
	@Slf4j
	class PowerShellGuard implements WindowGuard {

		private final Process process;
		private volatile String window = "";
		private volatile long updatedAt = System.currentTimeMillis();
		private volatile boolean closed = false;

		public PowerShellGuard(Path script, Consumer<String> onEmergencyStop) throws Exception {
			List<String> command = List.of("powershell", "-NoProfile", "-ExecutionPolicy", "Bypass",
					"-File", script.toString());
			ProcessBuilder builder = new ProcessBuilder(command);
			builder.redirectErrorStream(true);
			this.process = builder.start();
			Thread reader = new Thread(() -> read(process, onEmergencyStop), "ks-window-guard");
			reader.setDaemon(true);
			reader.start();
		}

		private void read(Process source, Consumer<String> onEmergencyStop) {
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(source.getInputStream(), StandardCharsets.UTF_8))) {
				String line;
				while ((line = reader.readLine()) != null) {
					updatedAt = System.currentTimeMillis();
					if (line.equals("ESC")) {
						onEmergencyStop.accept("检测到 ESC：立即停止并松开所有按键");
						continue;
					}
					if (line.startsWith("WINDOW ")) {
						window = line.substring("WINDOW ".length()).trim();
						continue;
					}
					if (line.startsWith("READY")) {
						log.info("窗口守护已就绪：{}", line);
					}
				}
			} catch (Exception error) {
				if (!closed) {
					log.warn("窗口守护读取中断：{}", error.getMessage());
				}
			}
		}

		@Override
		public String currentWindow() {
			return window;
		}

		@Override
		public long lastUpdateMs() {
			return System.currentTimeMillis() - updatedAt;
		}

		@Override
		public boolean alive() {
			return !closed && process.isAlive();
		}

		@Override
		public String kind() {
			return "powershell";
		}

		@Override
		public void close() {
			closed = true;
			process.destroy();
			try {
				if (!process.waitFor(2, java.util.concurrent.TimeUnit.SECONDS)) {
					process.destroyForcibly();
				}
			} catch (InterruptedException error) {
				Thread.currentThread().interrupt();
				process.destroyForcibly();
			}
		}
	}

	/**
	 * 找到守门脚本。
	 *
	 * <p>顺序：显式配置 → 工作目录 → 上级目录 → jar 同级目录（后端常常从别处用 jar 启动，
	 * 这时工作目录里不会有 tools/，所以还要看 jar 自己所在的位置）。
	 */
	static Path locateScript(Path configured) {
		List<Path> candidates = new ArrayList<>();
		if (configured != null) {
			candidates.add(configured);
		}
		candidates.add(Path.of("tools", "ks-performance-guard.ps1"));
		candidates.add(Path.of("..", "tools", "ks-performance-guard.ps1"));
		try {
			Path jar = Path.of(WindowGuard.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			Path jarDir = Files.isDirectory(jar) ? jar : jar.getParent();
			if (jarDir != null) {
				candidates.add(jarDir.resolve("tools").resolve("ks-performance-guard.ps1"));
				candidates.add(jarDir.resolve("..").resolve("tools").resolve("ks-performance-guard.ps1").normalize());
			}
		} catch (Exception ignored) {
			// 取不到 jar 位置就只按目录找
		}
		for (Path candidate : candidates) {
			if (candidate != null && Files.isRegularFile(candidate)) {
				return candidate.toAbsolutePath();
			}
		}
		return null;
	}
}
