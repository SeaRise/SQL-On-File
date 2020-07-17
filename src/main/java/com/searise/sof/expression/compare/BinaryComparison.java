package com.searise.sof.expression.compare;

import com.searise.sof.core.SofException;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.expression.Binary;
import com.searise.sof.expression.Expression;
import com.searise.sof.type.DataType;
import org.apache.commons.lang3.StringUtils;

public abstract class BinaryComparison extends Binary {

    public BinaryComparison(Expression left, Expression right, String op) {
        super(left, right, op);
    }

    public DataType dataType() {
        return DataType.BooleanType;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", left, op, right);
    }

    @Override
    protected Object doEval(InternalRow input, DataType inputDataType) {
        Object leftValue = left.eval(input);
        Object rightValue = right.eval(input);
        int compareToResult;
        switch (inputDataType) {
            case IntegerType:
                compareToResult = Integer.compare((int) leftValue, (int) rightValue);
                break;
            case DoubleType:
                compareToResult = Double.compare((double) leftValue, (double) rightValue);
                break;
            case StringType:
                compareToResult = StringUtils.compare((String) leftValue, (String) rightValue);
                break;
            case BooleanType:
                boolean leftBoolean = (boolean) leftValue;
                boolean rightBoolean = (boolean) rightValue;
                compareToResult = leftBoolean == rightBoolean ? 0 : leftBoolean ? 1 : -1;
                break;
            default:
                throw new SofException(String.format("unsupported dataType[%s] in Add", dataType()));
        }
        return handleCompareToResult(compareToResult);
    }

    protected abstract boolean handleCompareToResult(int result);
}
