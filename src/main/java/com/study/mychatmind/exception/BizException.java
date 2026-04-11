package com.study.mychatmind.exception;

import lombok.Getter;

@Getter
public class BizException extends RuntimeException {

    private int code;

    public BizException(String message) {
        super(message);
        this.code = 400;
    }
}
