package com.searise.sof.core;

public class SofException extends RuntimeException {
    public SofException(String message) {
        super(message);
    }

    public SofException(String message, Throwable e) {
        super(message, e);
    }
}
