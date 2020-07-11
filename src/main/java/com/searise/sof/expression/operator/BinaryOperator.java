package com.searise.sof.expression.operator;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.searise.sof.expression.Expression;
import com.searise.sof.type.DataType;

import java.util.List;

public abstract class BinaryOperator implements Expression {
    public final Expression left;
    public final Expression right;
    public final String op;

    public BinaryOperator(Expression left, Expression right, String op) {
        this.left = left;
        this.right = right;
        this.op = op;
    }

    @Override
    public List<Expression> children() {
        return ImmutableList.of(left, right);
    }

    @Override
    public DataType dataType() {
        Preconditions.checkArgument(resolved() && left.dataType() == right.dataType());
        return left.dataType();
    }

    @Override
    public String toString() {
        return String.format("(%s %s %s)", left, op, right);
    }
}
