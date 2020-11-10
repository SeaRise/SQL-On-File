package com.searise.sof.codegen.exec;

import com.searise.sof.execution.Executor;

// 只支持单children的算子.
public abstract class Codegen implements Executor {
    protected String variablePrefix;

    {
        String planType = this.getClass().getSimpleName();
        switch (planType) {
            case "FilterExec":
                variablePrefix = "filter";
                break;
            case "ProjectExec":
                variablePrefix = "project";
                break;
            default:
                variablePrefix = planType;
        }
    }

    public Executor child() {
        return children().get(0);
    }

    public abstract ExecCode genCode(CodegenContext context, ExecCode child);
}
