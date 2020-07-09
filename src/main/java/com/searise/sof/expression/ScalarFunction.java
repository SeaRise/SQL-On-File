package com.searise.sof.expression;

import java.util.List;

public class ScalarFunction implements Expression {
    public final String funcName;
    public final List<Expression> params;

    public ScalarFunction(String funcName, List<Expression> params) {
        this.funcName = funcName;
        this.params = params;
    }
}
