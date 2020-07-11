package com.searise.sof.expression.compare;

import com.google.common.base.Preconditions;
import com.searise.sof.expression.Expression;

import java.util.List;
import java.util.Objects;

public class EqualTo extends BinaryComparison {
    public EqualTo(Expression left, Expression right) {
        super(left, right, "=");
    }

    public Expression copyWithNewChildren(List<Expression> children) {
        Preconditions.checkArgument(Objects.nonNull(children) && children.size() == 2);
        return new EqualTo(left, right);
    }
}
