package com.searise.sof.expression.operator;

import com.google.common.base.Preconditions;
import com.searise.sof.expression.Binary;
import com.searise.sof.expression.Expression;
import com.searise.sof.type.DataType;

public abstract class BinaryOperator extends Binary {

    public BinaryOperator(Expression left, Expression right, String op) {
        super(left, right, op);
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
