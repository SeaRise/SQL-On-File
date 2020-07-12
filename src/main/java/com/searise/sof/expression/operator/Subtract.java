package com.searise.sof.expression.operator;

import com.google.common.base.Preconditions;
import com.searise.sof.core.SofException;
import com.searise.sof.expression.Expression;
import com.searise.sof.type.DataType;

import java.util.List;
import java.util.Objects;

public class Subtract extends BinaryOperator {
    public Subtract(Expression left, Expression right) {
        super(left, right, "-");
    }

    @Override
    public Expression copyWithNewChildren(List<Expression> children) {
        Preconditions.checkArgument(Objects.nonNull(children) && children.size() == 2);
        return new Subtract(left, right);
    }

    @Override
    protected Object doEval(Object leftValue, Object rightValue, DataType dataType) {
        switch (dataType) {
            case IntegerType:
                return (int) leftValue - (int) rightValue;
            case DoubleType:
                return (double) leftValue - (double) rightValue;
            default:
                throw new SofException(String.format("unsupported dataType[%s] in Add", dataType()));
        }
    }
}
