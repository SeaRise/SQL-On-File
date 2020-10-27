package com.searise.sof.expression;

import com.google.common.collect.ImmutableList;
import com.searise.sof.codegen.expr.CodegenContext;
import com.searise.sof.codegen.expr.ExprCode;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.type.DataType;

public class Literal implements Expression {
    public final DataType dataType;
    public final Object value;

    public Literal(DataType dateType, Object value) {
        this.dataType = dateType;
        this.value = value;
    }

    public String toString() {
        return String.format("literal:%s:%s", value, dataType);
    }

    public boolean resolved() {
        return true;
    }

    public DataType dataType() {
        return dataType;
    }

    public Object eval(InternalRow input) {
        return value;
    }

    public boolean foldable() {
        return true;
    }

    @Override
    public ExprCode genCode(CodegenContext codegenContext) {
        String code = dataType == DataType.StringType ? String.format("\"%s\"", value.toString()) : value.toString();
        return new ExprCode(code, ImmutableList.of(), ImmutableList.of(), dataType);
    }
}
