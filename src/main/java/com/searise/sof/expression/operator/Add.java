package com.searise.sof.expression.operator;

import com.google.common.base.Preconditions;
import com.searise.sof.expression.Expression;

import java.util.List;
import java.util.Objects;

public class Add extends BinaryOperator {
    public Add(Expression left, Expression right) {
        super(left, right, "+");
    }

    @Override
    public Expression copyWithNewChildren(List<Expression> children) {
        Preconditions.checkArgument(Objects.nonNull(children) && children.size() == 2);
        return new Add(left, right);
    }
}
