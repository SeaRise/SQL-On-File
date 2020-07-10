package com.searise.sof.expression;

import com.searise.sof.type.DataType;

import java.util.List;

public class Literal implements Expression {
    public final DataType dataType;
    public final Object value;

    public Literal(DataType dateType, Object value) {
        this.dataType = dateType;
        this.value = value;
    }

    public String toString() {
        return value.toString();
    }

    @Override
    public Expression copyWithNewChildren(List<Expression> children) {
        return null;
    }

    public boolean resolved() {
        return true;
    }
}
