package com.searise.sof.expression.compare;

import com.google.common.collect.ImmutableList;
import com.searise.sof.codegen.expr.CodeGenerator;
import com.searise.sof.codegen.expr.CodegenContext;
import com.searise.sof.codegen.expr.ExprCode;
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
        return String.format("%s %s %s:%s", left, op, right, dataType());
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

    protected void genCodePreCheck() {
    }

    @Override
    protected ExprCode doGenCode(CodegenContext codegenContext) {
        switch (left.dataType()) {
            case IntegerType:
            case DoubleType:
                return super.doGenCode(codegenContext);
            case StringType:
            case BooleanType:
                // 遇到StringType和BooleanType就替换codegen的children.
                // boolean和string的compare没办法直接用BinaryComparison的op.
                // 如true > false.
                // 这里为了方便,就只codegen children.
                return withCodegenChildren(codegenContext);
            default:
                throw new SofException(String.format("unsupported dataType[%s] in %s", dataType(), getClass().getSimpleName()));
        }
    }

    private ExprCode withCodegenChildren(CodegenContext codegenContext) {
        Expression newExpr;
        try {
            ExprCode leftExprCode = left.genCode(codegenContext);
            ExprCode rightExprCode = right.genCode(codegenContext);
            Expression newLeft = CodeGenerator.gen(leftExprCode);
            Expression newRight = CodeGenerator.gen(rightExprCode);
            newExpr = this.copyWithNewChildren(ImmutableList.of(newLeft, newRight));

            ImmutableList.Builder<String> paramNamesBuilder = ImmutableList.builder();
            String val = codegenContext.genExprName("self");
            paramNamesBuilder.add(val).addAll(leftExprCode.paramNames).addAll(rightExprCode.paramNames);

            ImmutableList.Builder<Expression> paramsBuilder = ImmutableList.builder();
            paramsBuilder.add(newExpr).addAll(leftExprCode.params).addAll(rightExprCode.params);

            String code = String.format("(%s) %s.eval(%s)", dataType().javaType, val, codegenContext.inputVal);
            return new ExprCode(code, paramsBuilder.build(), paramNamesBuilder.build(), dataType());
        } catch (Exception e) {
            return Expression.fallback(this, codegenContext);
        }
    }
}
