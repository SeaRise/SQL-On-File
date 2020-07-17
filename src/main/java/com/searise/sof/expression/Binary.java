package com.searise.sof.expression;

import com.google.common.collect.ImmutableList;
import com.searise.sof.core.Utils;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.type.DataType;

import java.util.List;

public abstract class Binary implements Expression {
    public final Expression left;
    public final Expression right;
    public final String op;

    public Binary(Expression left, Expression right, String op) {
        this.left = left;
        this.right = right;
        this.op = op;
    }

    public Object eval(InternalRow input) {
        DataType inputDataType = left.dataType();
        Utils.checkArgument(inputDataType == right.dataType(),
                "left.dataType must equal to right.dataType in binary expression " + getClass().getSimpleName());
        return doEval(input, inputDataType);
    }

    protected abstract Object doEval(InternalRow input, DataType inputDataType);

    @Override
    public List<Expression> children() {
        return ImmutableList.of(left, right);
    }
}
