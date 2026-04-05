package com.zhangsihan.carbonfootprint.common;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    BAD_REQUEST("BAD_REQUEST", "请求参数有误", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("UNAUTHORIZED", "请先登录", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("FORBIDDEN", "没有访问权限", HttpStatus.FORBIDDEN),
    NOT_FOUND("NOT_FOUND", "资源不存在", HttpStatus.NOT_FOUND),
    CONFLICT("CONFLICT", "数据已存在", HttpStatus.CONFLICT),
    INTERNAL_ERROR("INTERNAL_ERROR", "服务器开小差了", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
