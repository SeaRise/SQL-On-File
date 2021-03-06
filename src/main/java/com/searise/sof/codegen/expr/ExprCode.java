package com.searise.sof.codegen.expr;

import com.google.common.collect.ImmutableList;
import com.searise.sof.expression.Expression;
import com.searise.sof.type.DataType;

import java.util.List;

public class ExprCode {
    public final String code;
    public final List<Expression> params;
    public final List<String> paramNames;
    public final DataType dataType;
    public final List<Class> importClasses;

    public ExprCode(String code, List<Expression> params, List<String> paramNames, DataType dataType) {
        this(code, params, paramNames, dataType, ImmutableList.of());
    }

    public ExprCode(String code, List<Expression> params, List<String> paramNames, DataType dataType, List<Class> importClasses) {
        this.code = code;
        this.params = params;
        this.paramNames = paramNames;
        this.dataType = dataType;
        this.importClasses = importClasses;
    }
}
