package com.searise.sof.expression.logic;

import com.google.common.base.Preconditions;
import com.searise.sof.expression.Expression;

import java.util.List;
import java.util.Objects;

public class Or extends LogicalBinary {
    public Or(Expression left, Expression right) {
        super(left, right, "or");
    }

    public Expression copyWithNewChildren(List<Expression> children) {
        Preconditions.checkArgument(Objects.nonNull(children) && children.size() == 2);
        return new Or(left, right);
    }
}
