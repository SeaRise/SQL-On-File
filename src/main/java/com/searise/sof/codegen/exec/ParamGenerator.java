package com.searise.sof.codegen.exec;

public interface ParamGenerator {
    String name();
    Class clazz();
    Object gen();
}
