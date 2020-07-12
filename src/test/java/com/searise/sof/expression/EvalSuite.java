package com.searise.sof.expression;

import com.google.common.base.Preconditions;
import com.searise.sof.core.row.ArrayRow;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.core.row.InternalRowWriter;
import com.searise.sof.expression.attribute.BoundReference;
import com.searise.sof.expression.compare.*;
import com.searise.sof.expression.logic.And;
import com.searise.sof.expression.logic.Not;
import com.searise.sof.expression.logic.Or;
import com.searise.sof.expression.operator.*;
import com.searise.sof.type.DataType;
import org.junit.Test;

public class EvalSuite {
    @Test
    public void testCompare() {
        BoundReference left = new BoundReference(DataType.DoubleType, -1);
        left.resolveIndex(0);
        BoundReference right = new BoundReference(DataType.DoubleType, -1);
        right.resolveIndex(1);
        InternalRow row = new ArrayRow(2);
        InternalRowWriter writer = InternalRow.getWriter(0, DataType.DoubleType);
        writer.apply(row, 1.0);
        writer = InternalRow.getWriter(1, DataType.DoubleType);
        writer.apply(row, 2.0);

        EqualTo equalTo = new EqualTo(left, right);
        Preconditions.checkArgument(equalTo.eval(row).equals(false));

        GreaterThan greaterThan = new GreaterThan(left, right);
        Preconditions.checkArgument(greaterThan.eval(row).equals(false));

        GreaterThanOrEqual greaterThanOrEqual = new GreaterThanOrEqual(left, right);
        Preconditions.checkArgument(greaterThanOrEqual.eval(row).equals(false));

        LessThan lessThan = new LessThan(left, right);
        Preconditions.checkArgument(lessThan.eval(row).equals(true));

        LessThanOrEqual lessThanOrEqual = new LessThanOrEqual(left, right);
        Preconditions.checkArgument(lessThanOrEqual.eval(row).equals(true));
    }

    @Test
    public void testOperator() {
        BoundReference left = new BoundReference(DataType.DoubleType, -1);
        left.resolveIndex(0);
        BoundReference right = new BoundReference(DataType.DoubleType, -1);
        right.resolveIndex(1);
        InternalRow row = new ArrayRow(2);
        InternalRowWriter writer = InternalRow.getWriter(0, DataType.DoubleType);
        writer.apply(row, 1.0);
        writer = InternalRow.getWriter(1, DataType.DoubleType);
        writer.apply(row, 2.0);

        Add add = new Add(left, right);
        Preconditions.checkArgument(add.eval(row).equals(3.0));

        Divide divide = new Divide(left, right);
        Preconditions.checkArgument(divide.eval(row).equals(0.5));

        Multiply multiply = new Multiply(left, right);
        Preconditions.checkArgument(multiply.eval(row).equals(2.0));

        Remainder remainder = new Remainder(left, right);
        Preconditions.checkArgument(remainder.eval(row).equals(1.0 % 2.0));

        Subtract subtract = new Subtract(left, right);
        Preconditions.checkArgument(subtract.eval(row).equals(1.0 - 2.0));

        UnaryMinus unaryMinus = new UnaryMinus(left);
        Preconditions.checkArgument(unaryMinus.eval(row).equals(-1.0));
    }

    @Test
    public void testLogic() {
        BoundReference left = new BoundReference(DataType.BooleanType, -1);
        left.resolveIndex(0);
        BoundReference right = new BoundReference(DataType.BooleanType, -1);
        right.resolveIndex(1);
        InternalRow row = new ArrayRow(2);
        InternalRowWriter writer = InternalRow.getWriter(0, DataType.BooleanType);
        writer.apply(row, true);
        writer = InternalRow.getWriter(1, DataType.BooleanType);
        writer.apply(row, false);

        And and = new And(left, right);
        Preconditions.checkArgument(and.eval(row).equals(false));

        Or or = new Or(left, right);
        Preconditions.checkArgument(or.eval(row).equals(true));

        Not not = new Not(left);
        Preconditions.checkArgument(not.eval(row).equals(false));
    }
}
