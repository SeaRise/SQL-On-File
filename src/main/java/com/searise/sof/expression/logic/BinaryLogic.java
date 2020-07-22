package com.searise.sof.expression.logic;

import com.searise.sof.core.SofException;
import com.searise.sof.expression.Binary;
import com.searise.sof.expression.Expression;
import com.searise.sof.type.DataType;

public abstract class BinaryLogic extends Binary {
    public BinaryLogic(Expression left, Expression right, String op) {
        super(left, right, op);
    }

    public DataType dataType() {
        return DataType.BooleanType;
    }

    @Override
    public String toString() {
        return String.format("(%s) %s (%s)", left, op, right);
    }

    protected void genCodePreCheck() {
        if (left.dataType() == DataType.BooleanType) {
            return;
        }
        throw new SofException(String.format("unsupported dataType[%s] in %s", dataType(), getClass().getSimpleName()));
    }
}
