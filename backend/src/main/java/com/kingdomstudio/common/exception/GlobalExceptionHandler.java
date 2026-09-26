package com.kingdomstudio.common.exception;

import com.kingdomstudio.common.Result;
import com.kingdomstudio.common.ResultCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理：把各类异常统一转换成 {@link Result}，避免异常细节泄漏给前端。
 *
 * <p>HTTP 状态码统一返回 200，业务状态放在 {@code code} 字段里 —— 这是前后端
 * 约定好的「统一响应格式」的常见做法，前端只需判断 code。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	/** 业务异常：Service 层主动抛出，属于预期内的错误 */
	@ExceptionHandler(BusinessException.class)
	public Result<Void> handleBusinessException(BusinessException e) {
		log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
		return Result.fail(e.getCode(), e.getMessage());
	}

	/** @Valid 校验 RequestBody 失败 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
		String message = e.getBindingResult().getFieldErrors().stream()
				.map(FieldError::getDefaultMessage)
				.collect(Collectors.joining("; "));
		log.warn("参数校验失败: {}", message);
		return Result.fail(ResultCode.BAD_REQUEST, message.isEmpty() ? ResultCode.BAD_REQUEST.getMessage() : message);
	}

	/** 表单/查询参数绑定校验失败 */
	@ExceptionHandler(BindException.class)
	public Result<Void> handleBindException(BindException e) {
		String message = e.getFieldErrors().stream()
				.map(FieldError::getDefaultMessage)
				.collect(Collectors.joining("; "));
		log.warn("参数绑定失败: {}", message);
		return Result.fail(ResultCode.BAD_REQUEST, message.isEmpty() ? ResultCode.BAD_REQUEST.getMessage() : message);
	}

	/** 方法参数上的 @Validated 校验失败（如 @RequestParam 加约束） */
	@ExceptionHandler(ConstraintViolationException.class)
	public Result<Void> handleConstraintViolation(ConstraintViolationException e) {
		log.warn("参数约束校验失败: {}", e.getMessage());
		return Result.fail(ResultCode.BAD_REQUEST, e.getMessage());
	}

	/** 缺少必填请求参数 */
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public Result<Void> handleMissingParameter(MissingServletRequestParameterException e) {
		return Result.fail(ResultCode.BAD_REQUEST, "缺少必填参数：" + e.getParameterName());
	}

	/** 请求体不是合法 JSON */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public Result<Void> handleNotReadable(HttpMessageNotReadableException e) {
		log.warn("请求体解析失败: {}", e.getMessage());
		return Result.fail(ResultCode.BAD_REQUEST, "请求体格式有误，请检查 JSON 是否合法");
	}

	/** 请求方法不支持 */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public Result<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
		return Result.fail(ResultCode.METHOD_NOT_ALLOWED, "不支持的请求方法：" + e.getMethod());
	}

	/** 静态资源/接口路径不存在 */
	@ExceptionHandler(NoResourceFoundException.class)
	public Result<Void> handleNoResourceFound(NoResourceFoundException e) {
		return Result.fail(ResultCode.NOT_FOUND, "接口不存在：" + e.getResourcePath());
	}

	/** Spring Security 拒绝访问（Phase 2 接入 JWT 后生效） */
	@ExceptionHandler(AccessDeniedException.class)
	public Result<Void> handleAccessDenied(AccessDeniedException e) {
		log.warn("访问被拒绝: {}", e.getMessage());
		return Result.fail(ResultCode.FORBIDDEN);
	}

	/**
	 * 上传文件超过大小上限。
	 *
	 * <p>上限在 application.yml 的 spring.servlet.multipart 里配置（当前 2MB，与前端选文件时的校验一致）。
	 * 以前这类异常会落到兜底分支，用户只看到「服务器内部错误」，不知道是文件太大。
	 */
	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public Result<Void> handleMaxUploadSize(MaxUploadSizeExceededException e) {
		log.warn("上传超过大小上限: {}", e.getMessage());
		return Result.fail(ResultCode.BAD_REQUEST, "文件超过大小上限（单文件 2MB），请压缩或裁剪后再试");
	}

	/** 文件已收到但读取失败（连接中断、临时目录不可写等），与「文件太大」分开提示 */
	@ExceptionHandler(MultipartException.class)
	public Result<Void> handleMultipart(MultipartException e) {
		log.warn("上传请求解析失败: {}", e.getMessage());
		return Result.fail(ResultCode.BAD_REQUEST, "上传请求解析失败，请重新选择文件后再试");
	}

	/**
	 * 唯一键冲突：多为「同一个资源重复提交」或「删除后又用相同的名称/来源新建」。
	 *
	 * <p>数据库唯一键是最后一道防线，命中说明业务层的前置判重没拦住（并发或逻辑删除后的重建），
	 * 属于可预期的客户端问题，应当返回可读提示而不是 500。
	 */
	@ExceptionHandler(DuplicateKeyException.class)
	public Result<Void> handleDuplicateKey(DuplicateKeyException e) {
		log.warn("唯一键冲突: {}", e.getMessage());
		return Result.fail(ResultCode.BAD_REQUEST, "已存在相同的记录（名称或来源重复），请修改后重试");
	}

	/**
	 * 其他数据完整性错误：字段超长、取值不满足约束等。
	 *
	 * <p>把「库层面的约束」翻译成「用户能看懂的一句话」；具体是哪条约束只写进日志，不返给前端。
	 */
	@ExceptionHandler(DataIntegrityViolationException.class)
	public Result<Void> handleDataIntegrity(DataIntegrityViolationException e) {
		log.warn("数据完整性约束被违反: {}", e.getMostSpecificCause().getMessage());
		return Result.fail(ResultCode.BAD_REQUEST, "数据不符合约束（字段过长或取值不合法），请检查后重试");
	}

	/** 兜底：未预期的异常，记录堆栈但不把细节返回给前端 */
	@ExceptionHandler(Exception.class)
	public Result<Void> handleException(Exception e) {
		log.error("系统异常", e);
		return Result.fail(ResultCode.INTERNAL_ERROR);
	}
}
