package com.kingdomstudio.modules.music.share;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 演奏码的单元测试。
 *
 * <p>演奏码是被人念着抄着传播的东西，所以三件事必须稳：格式固定、序号有序、用户抄错能挡下来。
 */
class ShareCodeTest {

	@Test
	@DisplayName("按 id 生成有序编号：第一条是 A001")
	void shouldGenerateSequentialCodes() {
		assertEquals("KS-MUSIC-2026-A001", ShareCode.of(1, 2026));
		assertEquals("KS-MUSIC-2026-A002", ShareCode.of(2, 2026));
		assertEquals("KS-MUSIC-2026-A999", ShareCode.of(999, 2026));
	}

	@Test
	@DisplayName("每满 999 换一个字母，编号回到 001")
	void shouldRollOverLetters() {
		assertEquals("KS-MUSIC-2026-B001", ShareCode.of(1000, 2026));
		assertEquals("KS-MUSIC-2026-B002", ShareCode.of(1001, 2026));
	}

	@Test
	@DisplayName("非法 id 也能生成：0 与负数按第一条处理，不抛异常")
	void shouldHandleIllegalId() {
		assertEquals("KS-MUSIC-2026-A001", ShareCode.of(0, 2026));
		assertEquals("KS-MUSIC-2026-A001", ShareCode.of(-5, 2026));
	}

	@Test
	@DisplayName("格式校验：只认出本系统的码")
	void shouldValidateFormat() {
		assertTrue(ShareCode.matches("KS-MUSIC-2026-A001"));
		assertTrue(ShareCode.matches(" ks-music-2026-b013 "));
		assertFalse(ShareCode.matches("KS-MUSIC-26-A1"));
		assertFalse(ShareCode.matches("A001"));
		assertFalse(ShareCode.matches(null));
	}

	@Test
	@DisplayName("用户输入规整：去空格转大写，允许只抄后半段")
	void shouldNormalizeInput() {
		assertEquals("KS-MUSIC-2026-A001", ShareCode.normalize(" ks-music-2026-a001 ", 2026));
		assertEquals("KS-MUSIC-2026-A001", ShareCode.normalize("KS-MUSIC-2026-A001", 2026));
		assertEquals("KS-MUSIC-2026-A007", ShareCode.normalize("a007", 2026), "只抄了 A007 也能补全");
		assertEquals("KS-MUSIC-2025-C012", ShareCode.normalize("2025-C012", 2026), "带了年份就用写的年份");
		assertEquals("", ShareCode.normalize("   ", 2026));
		assertEquals("随便写的", ShareCode.normalize("随便写的", 2026), "认不出来就原样返回，交给后端报错");
	}
}
