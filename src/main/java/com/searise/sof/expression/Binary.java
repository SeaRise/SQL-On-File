package com.searise.sof.expression;

import com.google.common.collect.ImmutableList;
import com.searise.sof.codegen.expr.CodegenContext;
import com.searise.sof.codegen.expr.ExprCode;
import com.searise.sof.core.SofException;
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
                String.format("left(%s).dataType[%s] must equal to right(%s).dataType[%s] in binary expression: %s",
                        left, left.dataType(), right, right.dataType(), getClass().getSimpleName()));
        return doEval(input, inputDataType);
    }

    protected abstract Object doEval(InternalRow input, DataType inputDataType);

    public ExprCode genCode(CodegenContext codegenContext) {
        if (!resolved()) {
            throw new SofException(String.format("unresolved expression %s can not call genCode", getClass().getSimpleName()));
        }

        Utils.checkArgument(left.dataType() == right.dataType(),
                String.format("left(%s).dataType[%s] must equal to right(%s).dataType[%s] in binary expression: %s",
                        left, left.dataType(), right, right.dataType(), getClass().getSimpleName()));
        genCodePreCheck();
        return doGenCode(codegenContext);
    }

    protected ExprCode doGenCode(CodegenContext codegenContext) {
        ExprCode leftExprCode = left.genCode(codegenContext);
        ExprCode rightExprCode = right.genCode(codegenContext);
        String code = String.format("(%s) %s (%s)", leftExprCode.code, op, rightExprCode.code);
        return new ExprCode(code, Utils.combine(leftExprCode.params, rightExprCode.params),
                Utils.combine(leftExprCode.paramNames, rightExprCode.paramNames), dataType());
    }

    protected abstract void genCodePreCheck();

    @Override
    public List<Expression> children() {
        return ImmutableList.of(left, right);
    }
}
