package com.kingdomstudio.modules.music.parser;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 简谱文本解析。
 *
 * <p>支持的是「手写简谱最常见的那一部分」写法，够用且规则明确：
 * <pre>
 *   1=C 4/4 BPM=96        ← 可选的头行：调号、拍号、速度
 *   5 5 6 5 | 1' 7 6 5 -  | 0 3_ 3_ 4 3 |
 * </pre>
 * <ul>
 *   <li>数字 <code>1-7</code> 是音级，<code>0</code> 是休止；</li>
 *   <li><code>'</code> 每多一个升高八度，<code>,</code> 每多一个降低八度；</li>
 *   <li><code>#</code> / <code>b</code> 前缀是变化音（升 / 降半音）；</li>
 *   <li><code>-</code> 延长一拍，<code>.</code> 附点（时值 ×1.5），<code>_</code> 减半（每多一个再减半）；</li>
 *   <li><code>|</code> 小节线只是断句，不产生音符。</li>
 * </ul>
 *
 * <p>调号只按 C 大调处理：<code>1=D</code> 这种会把整行换算成对应调的自然音阶，
 * 因为「简谱的 1 到底唱哪个音」全靠调号决定，忽略它会导致整首曲子移调。
 */
@Component
public class JianpuParser {

	/** 一拍 = 60000 / BPM 毫秒；BPM 默认 96（简谱教材里最常见的速度） */
	private static final int DEFAULT_BPM = 96;

	/** 大调音阶在半音上的位置：1 2 3 4 5 6 7 → 0 2 4 5 7 9 11 */
	private static final int[] MAJOR_SCALE = {0, 2, 4, 5, 7, 9, 11};

	/** 音名 → 半音序号，用于解析 1=C 这类调号 */
	private static final String[] KEY_LETTERS = {"C", "D", "E", "F", "G", "A", "B"};

	public ParsedSong parse(String text, String title) {
		String source = text == null ? "" : text;
		String[] lines = source.split("\\r?\\n");

		int bpm = DEFAULT_BPM;
		String timeSignature = "4/4";
		int keyRoot = 0; // C
		List<String> body = new ArrayList<>();
		for (String rawLine : lines) {
			String line = rawLine.trim();
			if (line.isEmpty()) {
				continue;
			}
			// 头行形如「1=C 4/4 BPM=96」：只在行首符合约定时才当配置读，
			// 否则「1 2 / 3 4」这种用斜杠断拍的谱面行会被整行当成配置吃掉
			if (isHeader(line)) {
				Header header = readHeader(line);
				if (header.bpm() > 0) {
					bpm = header.bpm();
				}
				keyRoot = header.keyRoot();
				if (header.timeSignature() != null) {
					timeSignature = header.timeSignature();
				}
				String rest = stripHeader(line);
				if (!rest.isBlank()) {
					body.add(rest);
				}
				continue;
			}
			// 行内的 // 或 # 之后当注释（# 前面必须有空格，否则会吃掉升号）
			int comment = line.indexOf("//");
			if (comment >= 0) {
				line = line.substring(0, comment).trim();
			}
			line = line.replaceAll("\\s+#\\s.*$", "").trim();
			if (!line.isEmpty()) {
				body.add(line);
			}
		}

		int beatMs = (int) Math.round(60_000d / bpm);
		List<ParsedSong.Note> notes = new ArrayList<>();
		int cursorMs = 0;
		int barMs = beatMs * numerator(timeSignature);
		for (String line : body) {
			for (Token token : tokenize(line)) {
				if (token.isRest()) {
					cursorMs += token.durationBeats * beatMs;
					continue;
				}
				int pitch = toPitch(token, keyRoot);
				if (pitch >= 0 && pitch <= 127) {
					notes.add(new ParsedSong.Note(pitch, token.velocity,
							cursorMs, Math.max(1, (int) Math.round(token.durationBeats * beatMs)), 0));
				}
				cursorMs += (int) Math.round(token.durationBeats * beatMs);
			}
			// 行尾没写满一小节也补到小节线，换行即换行，读起来才对得上谱面
			if (cursorMs % barMs != 0) {
				cursorMs += barMs - (cursorMs % barMs);
			}
		}

		List<ParsedSong.Note> sorted = ParsedSong.sort(notes);
		int durationMs = sorted.stream().mapToInt(ParsedSong.Note::endMs).max().orElse(0);
		String firstLine = body.isEmpty() ? "" : body.get(0);
		return new ParsedSong(
				title == null || title.isBlank() ? "未命名简谱" : title,
				"JIANPU",
				firstLine.length() > 120 ? firstLine.substring(0, 120) : firstLine,
				bpm,
				timeSignature,
				durationMs,
				sorted);
	}

	/** 头行：调号、速度、或行首拍号三者占其一 */
	private boolean isHeader(String line) {
		String upper = line.trim().toUpperCase(Locale.ROOT);
		return upper.startsWith("1=") || upper.contains("BPM") || upper.matches("^\\d{1,2}\\s*/\\s*\\d{1,2}\\b.*");
	}

	/** 头行内容；缺失的项用 null / 0 表示「这一行没说」 */
	record Header(int bpm, int keyRoot, String timeSignature) {
	}

	private Header readHeader(String line) {
		String upper = line.toUpperCase(Locale.ROOT);
		int bpm = 0;
		if (upper.contains("BPM")) {
			String tail = upper.substring(upper.indexOf("BPM") + 3).replace("=", "").trim();
			String digits = tail.replaceAll("^[^0-9]*([0-9]{2,3}).*$", "$1");
			try {
				bpm = Integer.parseInt(digits);
			} catch (NumberFormatException ignored) {
				bpm = 0;
			}
		}

		int keyRoot = 0;
		int keyIndex = upper.indexOf("1=");
		if (keyIndex >= 0) {
			int cursor = keyIndex + 2;
			while (cursor < upper.length() && upper.charAt(cursor) == ' ') {
				cursor++;
			}
			if (cursor < upper.length()) {
				String letter = upper.substring(cursor, cursor + 1);
				for (int i = 0; i < KEY_LETTERS.length; i++) {
					if (KEY_LETTERS[i].equals(letter)) {
						// 调号决定「1」落在哪个半音：C=0 D=2 E=4 F=5 G=7 A=9 B=11
						keyRoot = MAJOR_SCALE[i];
						break;
					}
				}
				String tail = upper.substring(Math.min(cursor + 1, upper.length()));
				if (tail.startsWith("#")) {
					keyRoot++;
				} else if (tail.startsWith("B")) {
					keyRoot--;
				}
			}
		}

		String timeSignature = null;
		java.util.regex.Matcher matcher = java.util.regex.Pattern
				.compile("(?:^|\\s)(\\d{1,2})\\s*/\\s*(\\d{1,2})(?:\\s|$)")
				.matcher(line);
		if (matcher.find()) {
			int numerator = Integer.parseInt(matcher.group(1));
			int denominator = Integer.parseInt(matcher.group(2));
			if (numerator >= 1 && numerator <= 16 && denominator >= 1 && denominator <= 16) {
				timeSignature = numerator + "/" + denominator;
			}
		}
		return new Header(bpm, keyRoot, timeSignature);
	}

	/** 头行里去掉调号 / 拍号 / BPM 之后剩下的谱面内容 */
	private String stripHeader(String line) {
		return line.replaceAll("(?i)1\\s*=\\s*[A-G][#b]?", "")
				.replaceAll("(?i)BPM\\s*=?\\s*\\d+", "")
				.replaceAll("(^|\\s)\\d{1,2}\\s*/\\s*\\d{1,2}(?=\\s|$)", " ")
				.trim();
	}

	private int numerator(String timeSignature) {
		int slash = timeSignature.indexOf('/');
		if (slash <= 0) {
			return 4;
		}
		try {
			return Math.max(1, Integer.parseInt(timeSignature.substring(0, slash)));
		} catch (NumberFormatException e) {
			return 4;
		}
	}

	/** 一个音或一个休止 */
	record Token(int degree, int accidental, int octaveShift, double durationBeats, int velocity) {
		boolean isRest() {
			return degree == 0;
		}
	}

	List<Token> tokenize(String line) {
		List<Token> tokens = new ArrayList<>();
		int index = 0;
		while (index < line.length()) {
			char ch = line.charAt(index);
			if (ch == '|' || ch == '/' || ch == ' ') {
				index++;
				continue;
			}
			if (ch == '-') {
				// 延长线：给上一个音加一拍，没有上一个音就当休止
				if (tokens.isEmpty()) {
					tokens.add(new Token(0, 0, 0, 1, 90));
				} else {
					Token last = tokens.remove(tokens.size() - 1);
					tokens.add(new Token(last.degree(), last.accidental(), last.octaveShift(),
							last.durationBeats() + 1, last.velocity()));
				}
				index++;
				continue;
			}
			int accidental = 0;
			if (ch == '#') {
				accidental = 1;
				index++;
			} else if (ch == 'b' || ch == 'B') {
				accidental = -1;
				index++;
			}
			if (index >= line.length()) {
				break;
			}
			char digit = line.charAt(index);
			if (digit < '0' || digit > '7') {
				index++;
				continue;
			}
			int degree = digit - '0';
			index++;
			int octaveShift = 0;
			int velocity = 90;
			double beats = 1;
			boolean running = true;
			while (index < line.length() && running) {
				char mark = line.charAt(index);
				switch (mark) {
					case '\'' -> {
						octaveShift++;
						index++;
					}
					case ',' -> {
						octaveShift--;
						index++;
					}
					case '.' -> {
						beats *= 1.5;
						index++;
					}
					case '_' -> {
						beats /= 2;
						index++;
					}
					case '>' -> {
						velocity = 110;
						index++;
					}
					case '<' -> {
						velocity = 70;
						index++;
					}
					default -> running = false;
				}
			}
			tokens.add(new Token(degree, accidental, octaveShift, beats, velocity));
		}
		return tokens;
	}

	/** 音级 + 调号 → 音高号；1 = 中央 C 所在八度（C4 = 60） */
	int toPitch(Token token, int keyRoot) {
		if (token.isRest()) {
			return -1;
		}
		int degree = token.degree();
		int steps = (degree - 1) / 7;
		int within = (degree - 1) % 7;
		// 调号决定「1」落在哪个半音上，音阶内部仍按大调音程走
		int pitch = 60 + keyRoot + MAJOR_SCALE[within] + steps * 12 + token.octaveShift() * 12 + token.accidental();
		return Math.max(0, Math.min(127, pitch));
	}
}
