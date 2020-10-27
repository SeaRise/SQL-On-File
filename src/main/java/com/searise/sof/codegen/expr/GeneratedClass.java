package com.searise.sof.codegen.expr;

import com.searise.sof.expression.Expression;
import com.searise.sof.type.DataType;

public interface GeneratedClass {
    Expression generate(DataType dataType, Expression[] params);
}
