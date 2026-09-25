package com.kingdomstudio.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 统一响应结果。
 *
 * <pre>
 * {
 *   "code": 200,
 *   "message": "success",
 *   "data": {}
 * }
 * </pre>
 *
 * @param <T> 业务数据类型
 */
@Getter
@Schema(description = "统一响应结果")
public class Result<T> {

	@Schema(description = "业务状态码，200 表示成功", example = "200")
	private final Integer code;

	@Schema(description = "提示信息", example = "success")
	private final String message;

	@Schema(description = "业务数据，失败时为 null")
	private final T data;

	private Result(Integer code, String message, T data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}

	public static <T> Result<T> success() {
		return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
	}

	public static <T> Result<T> success(T data) {
		return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
	}

	public static <T> Result<T> success(String message, T data) {
		return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
	}

	public static <T> Result<T> fail(ResultCode resultCode) {
		return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
	}

	public static <T> Result<T> fail(ResultCode resultCode, String message) {
		return new Result<>(resultCode.getCode(), message, null);
	}

	public static <T> Result<T> fail(Integer code, String message) {
		return new Result<>(code, message, null);
	}

	public static <T> Result<T> fail(String message) {
		return new Result<>(ResultCode.BUSINESS_ERROR.getCode(), message, null);
	}
}
