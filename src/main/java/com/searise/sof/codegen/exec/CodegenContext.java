package com.searise.sof.codegen.exec;

import java.util.concurrent.atomic.AtomicLong;

public class CodegenContext {
    private AtomicLong suffixId = new AtomicLong();

    private long getId() {
        return suffixId.getAndIncrement();
    }

    public String genVar(String prefix, String var) {
        return prefix + "_" + var + "_" + getId();
    }
}
