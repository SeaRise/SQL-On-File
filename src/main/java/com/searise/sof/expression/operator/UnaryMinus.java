package com.searise.sof.expression.operator;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.searise.sof.expression.Expression;
import com.searise.sof.type.DataType;

import java.util.List;
import java.util.Objects;

public class UnaryMinus implements Expression {
    public final Expression child;
    public final String op = "-";

    public UnaryMinus(Expression child) {
        this.child = child; }

    @Override
    public List<Expression> children() {
        return ImmutableList.of(child);
    }

    @Override
    public DataType dataType() {
        return child.dataType();
    }

    @Override
    public String toString() {
        return String.format("(%s%s)", op, child);
    }

    @Override
    public Expression copyWithNewChildren(List<Expression> children) {
        Preconditions.checkArgument(Objects.nonNull(children) && children.size() == 1);
        return new UnaryMinus(children.get(0));
    }
}
