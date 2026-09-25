package com.kingdomstudio.modules.music.parser;

/**
 * 音高号 ↔ 音名互转。
 *
 * <p>音乐里有两套说法容易混：
 * <ul>
 *   <li><b>音高号</b>：MIDI 的 0-127 整数，60 = 中央 C，相邻整数差半音；</li>
 *   <li><b>音名</b>：给人看的「C4 / #F5 / bB3」，其中数字是八度（科学音高记法）。</li>
 * </ul>
 * 库里两列都存：音高号用来算映射，音名用来显示，避免前后端各写一套换算。
 */
public final class PitchNames {

	private static final String[] NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

	/** 每八度十二个半音 */
	public static final int SEMITONES_PER_OCTAVE = 12;

	private PitchNames() {
	}

	/**
	 * 音高号 → 音名。
	 *
	 * <p>八度一律用 C 分界：60 = C4、59 = B3、61 = C#4。
	 */
	public static String name(int pitch) {
		if (pitch < 0 || pitch > 127) {
			return "?";
		}
		int octave = pitch / SEMITONES_PER_OCTAVE - 1;
		return NAMES[pitch % SEMITONES_PER_OCTAVE] + octave;
	}

	/** 是否是变化音（黑键）：C#、D#、F#、G#、A# */
	public static boolean isBlackKey(int pitch) {
		return switch (pitch % SEMITONES_PER_OCTAVE) {
			case 1, 3, 6, 8, 10 -> true;
			default -> false;
		};
	}

	/** 在 C 大调里的音级序号（C=0, D=1 ... B=6）；变化音返回 -1 */
	public static int majorDegree(int pitch) {
		return switch (pitch % SEMITONES_PER_OCTAVE) {
			case 0 -> 0;
			case 2 -> 1;
			case 4 -> 2;
			case 5 -> 3;
			case 7 -> 4;
			case 9 -> 5;
			case 11 -> 6;
			default -> -1;
		};
	}

	/** 音名 → 音高号；解析失败返回 -1 */
	public static int toPitch(String name) {
		if (name == null || name.isBlank()) {
			return -1;
		}
		String value = name.trim().toUpperCase(java.util.Locale.ROOT);
		int index = 0;
		boolean sharp = false;
		boolean flat = false;
		if (value.startsWith("#")) {
			sharp = true;
			index = 1;
		} else if (value.startsWith("B") && value.length() > 1 && !Character.isDigit(value.charAt(1))) {
			flat = true;
			index = 1;
		}
		if (index >= value.length() - 1) {
			return -1;
		}
		String letter = value.substring(index, index + 1);
		int base = -1;
		for (int i = 0; i < NAMES.length; i++) {
			if (NAMES[i].equals(letter)) {
				base = i;
				break;
			}
		}
		if (base < 0) {
			return -1;
		}
		try {
			int octave = Integer.parseInt(value.substring(index + 1));
			int pitch = (octave + 1) * SEMITONES_PER_OCTAVE + base;
			if (sharp) {
				pitch++;
			}
			if (flat) {
				pitch--;
			}
			return (pitch < 0 || pitch > 127) ? -1 : pitch;
		} catch (NumberFormatException e) {
			return -1;
		}
	}
}
