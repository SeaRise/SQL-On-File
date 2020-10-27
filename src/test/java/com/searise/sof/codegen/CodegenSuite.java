package com.searise.sof.codegen;

import com.google.common.base.Preconditions;
import com.searise.sof.codegen.expr.CodeGenerator;
import com.searise.sof.codegen.expr.CodegenContext;
import com.searise.sof.core.row.ArrayRow;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.core.row.InternalRowWriter;
import com.searise.sof.expression.Cast;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.Literal;
import com.searise.sof.expression.attribute.BoundReference;
import com.searise.sof.expression.compare.*;
import com.searise.sof.expression.logic.And;
import com.searise.sof.expression.logic.Not;
import com.searise.sof.expression.logic.Or;
import com.searise.sof.expression.operator.*;
import com.searise.sof.type.DataType;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.janino.CompileException;
import org.codehaus.janino.Parser;
import org.codehaus.janino.Scanner;
import org.junit.Test;

import java.io.IOException;

import static com.searise.sof.core.row.EmptyRow.EMPTY_ROW;

public class CodegenSuite {

    @Test
    public void testLiteral() throws CompileException, Parser.ParseException, Scanner.ScanException, IOException {
        Literal literal = new Literal(DataType.IntegerType, 1);
        Expression expression = CodeGenerator.gen(literal.genCode(new CodegenContext()));
        Preconditions.checkArgument(1 == (Integer) expression.eval(EMPTY_ROW));

        literal = new Literal(DataType.IntegerType, 1);
        expression = CodeGenerator.gen(literal.genCode(new CodegenContext()));
        Preconditions.checkArgument(1 == (Integer) expression.eval(EMPTY_ROW));

        literal = new Literal(DataType.DoubleType, 1.0);
        expression = CodeGenerator.gen(literal.genCode(new CodegenContext()));
        Preconditions.checkArgument(1.0 == (Double) expression.eval(EMPTY_ROW));

        literal = new Literal(DataType.BooleanType, true);
        expression = CodeGenerator.gen(literal.genCode(new CodegenContext()));
        Preconditions.checkArgument((Boolean) expression.eval(EMPTY_ROW));

        literal = new Literal(DataType.StringType, "str");
        expression = CodeGenerator.gen(literal.genCode(new CodegenContext()));
        Preconditions.checkArgument(StringUtils.equals("str", (String) expression.eval(EMPTY_ROW)));
    }

    @Test
    public void testBoundAttribute() throws CompileException, Parser.ParseException, Scanner.ScanException, IOException {
        BoundReference boundReference = new BoundReference(DataType.IntegerType, 0);
        boundReference.resolveIndex(0);
        InternalRow row = new ArrayRow(1);
        InternalRowWriter writer = InternalRow.getWriter(0, DataType.IntegerType);
        writer.apply(row, 1);
        Expression codegen = CodeGenerator.gen(boundReference.genCode(new CodegenContext()));
        Preconditions.checkArgument(1 == (Integer) codegen.eval(row));
    }

    @Test
    public void testCast() throws CompileException, Parser.ParseException, Scanner.ScanException, IOException {
        BoundReference boundReference = new BoundReference(DataType.IntegerType, 0);
        boundReference.resolveIndex(0);
        InternalRow row = new ArrayRow(1);
        InternalRowWriter writer = InternalRow.getWriter(0, DataType.IntegerType);
        writer.apply(row, 1);
        Cast cast = new Cast(DataType.DoubleType, boundReference);
        Expression codegen = CodeGenerator.gen(cast.genCode(new CodegenContext()));
        Preconditions.checkArgument(1.0 == (double) codegen.eval(row));
    }

    @Test
    public void testOperator() throws CompileException, Parser.ParseException, Scanner.ScanException, IOException {
        Literal literal = new Literal(DataType.IntegerType, 1);
        Literal litera2 = new Literal(DataType.IntegerType, 2);

        UnaryMinus unaryMinus = new UnaryMinus(literal);
        Expression expression = CodeGenerator.gen(unaryMinus.genCode(new CodegenContext()));
        Preconditions.checkArgument(-1 == (Integer) expression.eval(EMPTY_ROW));

        Add add = new Add(unaryMinus, literal);
        expression = CodeGenerator.gen(add.genCode(new CodegenContext()));
        Preconditions.checkArgument(0 == (Integer) expression.eval(EMPTY_ROW));

        Subtract subtract = new Subtract(add, litera2);
        expression = CodeGenerator.gen(subtract.genCode(new CodegenContext()));
        Preconditions.checkArgument(-2 == (Integer) expression.eval(EMPTY_ROW));

        Multiply multiply = new Multiply(subtract, unaryMinus);
        expression = CodeGenerator.gen(multiply.genCode(new CodegenContext()));
        Preconditions.checkArgument(2 == (Integer) expression.eval(EMPTY_ROW));

        Remainder remainder = new Remainder(litera2, multiply);
        expression = CodeGenerator.gen(remainder.genCode(new CodegenContext()));
        Preconditions.checkArgument(0 == (Integer) expression.eval(EMPTY_ROW));

        Divide divide = new Divide(multiply, subtract);
        expression = CodeGenerator.gen(divide.genCode(new CodegenContext()));
        Preconditions.checkArgument(-1 == (Integer) expression.eval(EMPTY_ROW));
    }

    @Test
    public void testLogic() throws CompileException, Parser.ParseException, Scanner.ScanException, IOException {
        BoundReference left = new BoundReference(DataType.BooleanType, -1);
        left.resolveIndex(0);
        BoundReference right = new BoundReference(DataType.BooleanType, -1);
        right.resolveIndex(1);
        InternalRow row = new ArrayRow(2);
        InternalRowWriter writer = InternalRow.getWriter(0, DataType.BooleanType);
        writer.apply(row, true);
        writer = InternalRow.getWriter(1, DataType.BooleanType);
        writer.apply(row, false);

        Not not = new Not(right);
        Expression expression = CodeGenerator.gen(not.genCode(new CodegenContext()));
        Preconditions.checkArgument((boolean) expression.eval(row));

        And and = new And(left, not);
        expression = CodeGenerator.gen(and.genCode(new CodegenContext()));
        Preconditions.checkArgument((boolean) expression.eval(row));

        Or or = new Or(left, right);
        expression = CodeGenerator.gen(or.genCode(new CodegenContext()));
        Preconditions.checkArgument((boolean) expression.eval(row));
    }

    @Test
    public void testCompare() throws CompileException, Parser.ParseException, Scanner.ScanException, IOException {
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
        Expression expression = CodeGenerator.gen(equalTo.genCode(new CodegenContext()));
        Preconditions.checkArgument(expression.eval(row).equals(false));

        GreaterThan greaterThan = new GreaterThan(left, right);
        expression = CodeGenerator.gen(greaterThan.genCode(new CodegenContext()));
        Preconditions.checkArgument(expression.eval(row).equals(false));

        GreaterThanOrEqual greaterThanOrEqual = new GreaterThanOrEqual(left, right);
        expression = CodeGenerator.gen(greaterThanOrEqual.genCode(new CodegenContext()));
        Preconditions.checkArgument(expression.eval(row).equals(false));

        LessThan lessThan = new LessThan(left, right);
        expression = CodeGenerator.gen(lessThan.genCode(new CodegenContext()));
        Preconditions.checkArgument(expression.eval(row).equals(true));

        LessThanOrEqual lessThanOrEqual = new LessThanOrEqual(left, right);
        expression = CodeGenerator.gen(lessThanOrEqual.genCode(new CodegenContext()));
        Preconditions.checkArgument(expression.eval(row).equals(true));
    }

    @Test
    public void testCompareStr() throws CompileException, Parser.ParseException, Scanner.ScanException, IOException {
        BoundReference left = new BoundReference(DataType.StringType, -1);
        left.resolveIndex(0);
        BoundReference right = new BoundReference(DataType.StringType, -1);
        right.resolveIndex(1);
        InternalRow row = new ArrayRow(2);
        InternalRowWriter writer = InternalRow.getWriter(0, DataType.StringType);
        writer.apply(row, "a");
        writer = InternalRow.getWriter(1, DataType.StringType);
        writer.apply(row, "b");

        EqualTo equalTo = new EqualTo(left, right);
        Expression expression = CodeGenerator.gen(equalTo.genCode(new CodegenContext()));
        Preconditions.checkArgument(expression.eval(row).equals(false));

        GreaterThan greaterThan = new GreaterThan(left, right);
        expression = CodeGenerator.gen(greaterThan.genCode(new CodegenContext()));
        Preconditions.checkArgument(expression.eval(row).equals(false));

        GreaterThanOrEqual greaterThanOrEqual = new GreaterThanOrEqual(left, right);
        expression = CodeGenerator.gen(greaterThanOrEqual.genCode(new CodegenContext()));
        Preconditions.checkArgument(expression.eval(row).equals(false));

        LessThan lessThan = new LessThan(left, right);
        expression = CodeGenerator.gen(lessThan.genCode(new CodegenContext()));
        Preconditions.checkArgument(expression.eval(row).equals(true));

        LessThanOrEqual lessThanOrEqual = new LessThanOrEqual(left, right);
        expression = CodeGenerator.gen(lessThanOrEqual.genCode(new CodegenContext()));
        Preconditions.checkArgument(expression.eval(row).equals(true));
    }
}
