package com.searise.sof.codegen;

import java.util.concurrent.atomic.AtomicLong;

public class CodegenContext {
    public final String inputVal = "input";

    private AtomicLong suffixId = new java.util.concurrent.atomic.AtomicLong();

    public String genExprName(String originName) {
        return originName + "_" + suffixId.getAndIncrement();
    }
}
