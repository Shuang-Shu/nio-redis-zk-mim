package com.mdc.mim.common.dto;

public enum ResponsesCodeEnum {
    SUCCESS(200), FAILED(500);

    ResponsesCodeEnum(int code) {
        this.code = code;
    }

    int code;

    public int getCode() {
        return code;
    }
}
