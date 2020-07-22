package com.searise.sof.expression.operator;

import com.google.common.base.Preconditions;
import com.searise.sof.core.SofException;
import com.searise.sof.core.row.InternalRow;
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

    @Override
    protected Object doEval(InternalRow input, DataType dataType) {
        Object leftValue = left.eval(input);
        Object rightValue = right.eval(input);
        return doEval(leftValue, rightValue, dataType);
    }

    protected abstract Object doEval(Object leftValue, Object rightValue, DataType dataType);

    protected void genCodePreCheck() {
        switch (left.dataType()) {
            case IntegerType:
            case DoubleType:
                return;
            default:
                throw new SofException(String.format("unsupported dataType[%s] in %s", dataType(), getClass().getSimpleName()));
        }
    }
}
