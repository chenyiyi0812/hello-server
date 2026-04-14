package com.example.demo.common;

public enum ResultCode {
    SUCCESS(200, "操作成功"),
    USER_HAS_EXISTED(400, "用户已存在"),
    USER_NOT_EXIST(404, "用户不存在"),
    PASSWORD_ERROR(401, "密码错误"),
    OPERATION_FAILED(500, "操作失败");

    private int code;
    private String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}