package com.kingdomstudio.common;

import lombok.Getter;

/**
 * 业务状态码。
 *
 * <p>约定：200 成功；4xx 客户端问题；5xx 服务端问题；6xx 业务规则问题。
 */
@Getter
public enum ResultCode {

	SUCCESS(200, "success"),
	BAD_REQUEST(400, "请求参数有误"),
	UNAUTHORIZED(401, "未登录或登录状态已过期"),
	FORBIDDEN(403, "没有访问权限"),
	NOT_FOUND(404, "请求的资源不存在"),
	METHOD_NOT_ALLOWED(405, "请求方法不支持"),
	INTERNAL_ERROR(500, "服务器内部错误"),
	BUSINESS_ERROR(600, "业务处理失败");

	private final Integer code;
	private final String message;

	ResultCode(Integer code, String message) {
		this.code = code;
		this.message = message;
	}
}
