package com.searise.sof.expression;

import com.google.common.collect.ImmutableList;
import com.searise.sof.analyse.AnalysisHelper;
import com.searise.sof.analyse.Applicable;
import com.searise.sof.codegen.expr.Codegen;
import com.searise.sof.codegen.expr.CodegenContext;
import com.searise.sof.codegen.expr.ExprCode;
import com.searise.sof.core.SofException;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.expression.attribute.Attribute;
import com.searise.sof.type.DataType;

import java.util.List;
import java.util.Optional;

public interface Expression extends AnalysisHelper<Expression>, Codegen {
    default boolean resolved() {
        return children().stream().allMatch(Expression::resolved);
    }

    default DataType dataType() {
        throw new SofException(String.format("unresolved expression %s can not call dataType", getClass().getSimpleName()));
    }

    default Object eval(InternalRow input) {
        throw new SofException(String.format("%s can not support eval", getClass().getSimpleName()));
    }

    default boolean foldable() {
        if (!resolved()) {
            return false;
        }

        List<Expression> children = children();
        if (children.isEmpty()) {
            return false;
        }
        return children.stream().allMatch(Expression::foldable);
    }

    static List<Attribute> getUseAttributes(Expression expression) {
        ImmutableList.Builder<Attribute> builder = ImmutableList.builder();
        expression.transformUp((Applicable<Expression>) expr -> {
            if (expr.getClass() == Attribute.class) {
                builder.add((Attribute) expr);
            }
            return expr;
        });
        return builder.build();
    }

    static Optional<Boolean> getBooleanLiteralValue(Expression expression) {
        if (expression.getClass() == Literal.class) {
            Literal literal = (Literal) expression;
            if (literal.dataType == DataType.BooleanType) {
                return Optional.of(((boolean) literal.value));
            }
        }
        return Optional.empty();
    }

    default ExprCode genCode(CodegenContext codegenContext) {
        if (!resolved()) {
            throw new SofException(String.format("unresolved expression %s can not call genCode", getClass().getSimpleName()));
        }

        return fallback(this, codegenContext);
    }

    static ExprCode fallback(Expression expression, CodegenContext codegenContext) {
        String val = codegenContext.genExprName("self");
        String code = String.format("(%s) %s.eval(%s)", expression.dataType().javaType, val, codegenContext.inputVal);
        return new ExprCode(code, ImmutableList.of(expression), ImmutableList.of(val), expression.dataType());
    }
}
