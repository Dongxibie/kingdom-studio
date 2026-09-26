package com.kingdomstudio.common.exception;

import com.kingdomstudio.common.Result;
import com.kingdomstudio.common.ResultCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 全局异常处理测试：重点验证「以前会变成服务器内部错误的可预期问题，现在有可读的中文提示」。
 *
 * <p>这是发布前的回归点：上传超限、唯一键冲突、字段超长以前都落兜底分支，用户只看到「服务器内部错误」。
 */
class GlobalExceptionHandlerTest {

	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

	@Test
	@DisplayName("上传超限：提示文件太大，而不是服务器内部错误")
	void shouldReportUploadTooLarge() {
		Result<Void> result = handler.handleMaxUploadSize(new MaxUploadSizeExceededException(2 * 1024 * 1024));

		assertEquals(ResultCode.BAD_REQUEST.getCode(), result.getCode());
		assertNotNull(result.getMessage());
		assertTrue(result.getMessage().contains("超过大小上限"), result.getMessage());
	}

	@Test
	@DisplayName("唯一键冲突：提示已存在相同记录")
	void shouldReportDuplicateKey() {
		Result<Void> result = handler.handleDuplicateKey(new DuplicateKeyException("Duplicate entry 'abc' for key 'uk_motion_resource_hash'"));

		assertEquals(ResultCode.BAD_REQUEST.getCode(), result.getCode());
		assertTrue(result.getMessage().contains("已存在相同的记录"), result.getMessage());
		// 具体的键名是库内部信息，不该出现在给前端的提示里
		assertTrue(!result.getMessage().contains("uk_motion_resource_hash"), result.getMessage());
	}

	@Test
	@DisplayName("数据完整性：提示字段过长或取值不合法")
	void shouldReportDataIntegrity() {
		Result<Void> result = handler.handleDataIntegrity(
				new DataIntegrityViolationException("Data too long for column 'key_layout' at row 1"));

		assertEquals(ResultCode.BAD_REQUEST.getCode(), result.getCode());
		assertTrue(result.getMessage().contains("数据不符合约束"), result.getMessage());
		assertTrue(!result.getMessage().contains("key_layout"), "列名不应返回给前端：" + result.getMessage());
	}
}
