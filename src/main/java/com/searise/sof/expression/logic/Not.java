package com.searise.sof.expression.logic;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.searise.sof.core.Utils;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.expression.Expression;
import com.searise.sof.type.DataType;

import java.util.List;
import java.util.Objects;

import static com.searise.sof.type.DataType.BooleanType;

public class Not implements Expression {
    public final Expression child;

    public Not(Expression child) {
        this.child = child;
    }

    public Expression copyWithNewChildren(List<Expression> children) {
        Preconditions.checkArgument(Objects.nonNull(children) && children.size() == 1);
        return new Not(child);
    }

    public List<Expression> children() {
        return ImmutableList.of(child);
    }

    public DataType dataType() {
        return DataType.BooleanType;
    }

    @Override
    public String toString() {
        return String.format("not (%s)", child);
    }

    @Override
    public Object eval(InternalRow input) {
        DataType inputDataType = child.dataType();
        Utils.checkArgument(inputDataType == BooleanType,
                "input dataType of `or` must be boolean or not " + inputDataType);
        boolean childBoolean = (boolean) child.eval(input);
        return !childBoolean;
    }
}
