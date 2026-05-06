package com.zhangsihan.carbonfootprint.common;

import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException exception) {
        return ResponseEntity.status(exception.getErrorCode().getHttpStatus())
                .body(ApiResponse.failure(exception.getErrorCode().getCode(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ":" + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(ApiResponse.failure(ErrorCode.BAD_REQUEST.getCode(), message));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBind(BindException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ":" + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(ApiResponse.failure(ErrorCode.BAD_REQUEST.getCode(), message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraint(ConstraintViolationException exception) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure(ErrorCode.BAD_REQUEST.getCode(), exception.getMessage()));
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MissingServletRequestPartException.class})
    public ResponseEntity<ApiResponse<Void>> handleMissingRequestPart(Exception exception) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure(ErrorCode.BAD_REQUEST.getCode(), "请求参数不完整，请检查表单内容"));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSize(MaxUploadSizeExceededException exception) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure(ErrorCode.BAD_REQUEST.getCode(), "上传文件过大，请压缩后重试"));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiResponse<Void>> handleMultipart(MultipartException exception) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure(ErrorCode.BAD_REQUEST.getCode(), "文件上传失败，请重新选择图片"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException exception) {
        return ResponseEntity.status(ErrorCode.FORBIDDEN.getHttpStatus())
                .body(ApiResponse.failure(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleOther(Exception exception) {
        // Keep the real stack trace in server logs, but do not expose internal details to clients.
        log.error("Unhandled server exception", exception);
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getHttpStatus())
                .body(ApiResponse.failure(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getMessage()));
    }
}
