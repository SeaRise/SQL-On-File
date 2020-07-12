package com.searise.sof.expression.compare;

import com.google.common.base.Preconditions;
import com.searise.sof.expression.Expression;

import java.util.List;
import java.util.Objects;

public class GreaterThanOrEqual extends BinaryComparison {
    public GreaterThanOrEqual(Expression left, Expression right) {
        super(left, right, ">=");
    }

    public Expression copyWithNewChildren(List<Expression> children) {
        Preconditions.checkArgument(Objects.nonNull(children) && children.size() == 2);
        return new GreaterThanOrEqual(children.get(0), children.get(1));
    }

    @Override
    protected boolean handleCompareToResult(int result) {
        return result >= 0;
    }
}
