package com.searise.sof.expression;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.searise.sof.codegen.expr.CodegenContext;
import com.searise.sof.codegen.expr.ExprCode;
import com.searise.sof.core.SofException;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.type.DataType;
import com.searise.sof.type.TypeGroup;

import java.util.List;
import java.util.Objects;

public class Cast implements Expression {
    public final Expression child;
    public final DataType dataType;

    public Cast(DataType dataType, Expression child) {
        this.dataType = dataType;
        this.child = child;
    }

    @Override
    public String toString() {
        return String.format("cast(%s as %s)", child, dataType.name);
    }

    @Override
    public Expression copyWithNewChildren(List<Expression> children) {
        Preconditions.checkArgument(Objects.nonNull(children) && children.size() == 1);
        return new Cast(dataType, children.get(0));
    }

    @Override
    public List<Expression> children() {
        return ImmutableList.of(child);
    }

    public boolean resolved() {
        return child.resolved();
    }

    public DataType dataType() {
        return dataType;
    }

    public boolean foldable() {
        return child.foldable();
    }

    public Object eval(InternalRow input) {
        Object childValue = child.eval(input);
        DataType fromType = child.dataType();
        switch (fromType) {
            case StringType:
                switch (dataType) {
                    case IntegerType:
                        return Integer.valueOf(childValue.toString());
                    case DoubleType:
                        return Double.valueOf(childValue.toString());
                }
            case BooleanType:
                if (dataType == DataType.StringType) {
                    return childValue.toString();
                }
            case IntegerType:
                return Double.valueOf(childValue.toString());
            default:
        }
        throw new SofException(String.format("Cannot cast %s to %s", fromType, dataType));
    }

    @Override
    public ExprCode genCode(CodegenContext codegenContext) {
        if (!resolved()) {
            throw new SofException(String.format("unresolved expression %s can not call genCode", getClass().getSimpleName()));
        }
        ExprCode childExprCode = child.genCode(codegenContext);
        DataType fromType = child.dataType();
        switch (fromType) {
            case StringType:
                switch (dataType) {
                    case IntegerType:
                        String code = String.format("Integer.valueOf(%s)", childExprCode.code);
                        return new ExprCode(code, childExprCode.params, childExprCode.paramNames, dataType);
                    case DoubleType:
                        code = String.format("Double.valueOf(%s)", childExprCode.code);
                        return new ExprCode(code, childExprCode.params, childExprCode.paramNames, dataType);
                }
            case BooleanType:
                if (dataType == DataType.StringType) {
                    String code = String.format("Boolean.toString(%s)", childExprCode.code);
                    return new ExprCode(code, childExprCode.params, childExprCode.paramNames, dataType);
                }
            case IntegerType:
                String code = String.format("Double.valueOf(Integer.toString(%s))", childExprCode.code);
                return new ExprCode(code, childExprCode.params, childExprCode.paramNames, dataType);
            default:
        }
        return Expression.fallback(this, codegenContext);
    }

    public static Expression buildCast(Expression expression, DataType toDataType) {
        DataType fromType = expression.dataType();
        if (fromType == toDataType) {
            return expression;
        }
        if (!TypeGroup.canCast(fromType, toDataType)) {
            throw new SofException(String.format("Cannot cast %s to %s", fromType, toDataType));
        }
        return new Cast(toDataType, expression);
    }
}