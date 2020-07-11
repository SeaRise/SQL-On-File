package com.searise.sof.expression.logic;

import com.google.common.base.Preconditions;
import com.searise.sof.expression.Expression;

import java.util.List;
import java.util.Objects;

public class And extends LogicalBinary {
    public And(Expression left, Expression right) {
        super(left, right, "and");
    }

    public Expression copyWithNewChildren(List<Expression> children) {
        Preconditions.checkArgument(Objects.nonNull(children) && children.size() == 2);
        return new And(left, right);
    }
}
