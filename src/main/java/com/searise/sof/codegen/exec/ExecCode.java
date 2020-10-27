package com.searise.sof.codegen.exec;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class ExecCode {
    public final String code;
    public final String output;

    public final List<Object> params;
    public final List<String> paramNames;
    public final List<Class> paramClasses;

    public final List<Class> importClasses;

    public ExecCode(String code, String output, List<Object> params, List<String> paramNames, List<Class> paramClasses) {
        this(code, output, params, paramNames, paramClasses, ImmutableList.of());
    }

    public ExecCode(String code, String output, List<Object> params, List<String> paramNames, List<Class> paramClasses, List<Class> importClasses) {
        this.code = code;
        this.output = output;
        this.params = params;
        this.paramNames = paramNames;
        this.paramClasses = paramClasses;
        this.importClasses = importClasses;
    }

    // 专门给源头ExprCode用的.
    // 因为源头ExprCode没有children.
    public static ExecCode INIT_EXPR_CODE = new ExecCode("", "input", ImmutableList.of(), ImmutableList.of(), ImmutableList.of());
}
