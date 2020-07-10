package com.searise.sof.expression;

import java.util.List;
import java.util.stream.Collectors;

public class ScalarFunction implements Expression {
    public final String funcName;
    public final List<Expression> params;

    public ScalarFunction(String funcName, List<Expression> params) {
        this.funcName = funcName;
        this.params = params;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", funcName, params.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }

    @Override
    public Expression copyWithNewChildren(List<Expression> children) {
        return new ScalarFunction(funcName, children);
    }

    @Override
    public List<Expression> children() {
        return params;
    }

    public boolean resolved() {
        return false;
    }
}
