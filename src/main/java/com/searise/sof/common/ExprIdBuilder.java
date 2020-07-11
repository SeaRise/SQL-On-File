package com.searise.sof.common;

import java.util.concurrent.atomic.AtomicLong;

public class ExprIdBuilder {
    private ExprIdBuilder() {}

    private static AtomicLong curId = new java.util.concurrent.atomic.AtomicLong();
    public static long newExprId() {
        return curId.getAndIncrement();
    }
}
