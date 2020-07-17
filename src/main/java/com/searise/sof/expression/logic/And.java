package com.searise.sof.expression.logic;

import com.google.common.base.Preconditions;
import com.searise.sof.core.Utils;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.expression.Expression;
import com.searise.sof.type.DataType;

import java.util.List;
import java.util.Objects;

import static com.searise.sof.type.DataType.BooleanType;

public class And extends BinaryLogic {
    public And(Expression left, Expression right) {
        super(left, right, "and");
    }

    public Expression copyWithNewChildren(List<Expression> children) {
        Preconditions.checkArgument(Objects.nonNull(children) &&
                children.size() == 2 &&
                children.get(0).dataType() == BooleanType);
        return new And(children.get(0), children.get(1));
    }

    @Override
    protected Object doEval(InternalRow input, DataType dataType) {
        Utils.checkArgument(dataType == BooleanType,
                "input dataType of `and` must be boolean or not " + dataType);

        if (!((boolean) left.eval(input))) {
            return false;
        }
        return right.eval(input);
    }
}
