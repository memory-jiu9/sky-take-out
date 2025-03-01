package com.sky.exception;

public class UserNameRepeatException extends BaseException {
    public UserNameRepeatException() {
        super();
    }

    public UserNameRepeatException(String msg) {
        super(msg);
    }
}
