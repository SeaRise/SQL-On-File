package com.searise.sof.core;

import java.util.concurrent.atomic.AtomicLong;

public class ExprIdBuilder {
    public ExprIdBuilder() {
    }

    private AtomicLong curId = new java.util.concurrent.atomic.AtomicLong();

    public long newExprId() {
        return curId.getAndIncrement();
    }
}
