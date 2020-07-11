package com.searise.sof.expression.logic;

import com.google.common.collect.ImmutableList;
import com.searise.sof.expression.Expression;
import com.searise.sof.type.DataType;

import java.util.List;

public abstract class BinaryLogic implements Expression {
    public final Expression left;
    public final Expression right;
    public final String op;

    public BinaryLogic(Expression left, Expression right, String op) {
        this.left = left;
        this.right = right;
        this.op = op;
    }

    public List<Expression> children() {
        return ImmutableList.of(left, right);
    }

    public DataType dataType() {
        return DataType.BooleanType;
    }

    @Override
    public String toString() {
        return String.format("(%s) %s (%s)", left, op, right);
    }
}
