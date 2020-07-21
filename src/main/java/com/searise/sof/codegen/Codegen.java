package com.searise.sof.codegen;

public interface Codegen {
    default String genCode() {
        return "";
    }
}
