package com.searise.sof.codegen.exec;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class ExecCode {
    public final String code;
    public final String output;

    public final List<Class> importClasses;

    public final List<ParamGenerator> paramGenerators;

    public ExecCode(String code, String output, List<ParamGenerator> paramGenerators) {
        this(code, output, paramGenerators, ImmutableList.of());
    }

    public ExecCode(String code, String output, List<ParamGenerator> paramGenerators, List<Class> importClasses) {
        this.code = code;
        this.output = output;
        this.paramGenerators = paramGenerators;
        this.importClasses = importClasses;
    }

    // 专门给源头ExprCode用的.
    // 因为源头ExprCode没有children.
    public static ExecCode INIT_EXPR_CODE = new ExecCode("", "input", ImmutableList.of());
}
