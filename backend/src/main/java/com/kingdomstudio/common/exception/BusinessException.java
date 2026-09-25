package com.kingdomstudio.common.exception;

import com.kingdomstudio.common.ResultCode;
import lombok.Getter;

/**
 * 业务异常：由 Service 层主动抛出，交给 {@link GlobalExceptionHandler} 统一转换。
 *
 * <p>示例：{@code throw new BusinessException(ResultCode.NOT_FOUND, "项目不存在");}
 */
@Getter
public class BusinessException extends RuntimeException {

	private final Integer code;

	public BusinessException(String message) {
		super(message);
		this.code = ResultCode.BUSINESS_ERROR.getCode();
	}

	public BusinessException(ResultCode resultCode) {
		super(resultCode.getMessage());
		this.code = resultCode.getCode();
	}

	public BusinessException(ResultCode resultCode, String message) {
		super(message);
		this.code = resultCode.getCode();
	}
}
