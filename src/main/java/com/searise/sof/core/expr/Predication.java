package com.searise.sof.core.expr;

import com.searise.sof.codegen.expr.CodeGenerator;
import com.searise.sof.core.SofContext;
import com.searise.sof.core.Utils;
import com.searise.sof.core.conf.SofConf;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.expression.Expression;

import java.util.List;

public class Predication {
    private final List<Expression> conditions;
    public final SofContext context;

    public Predication(List<Expression> conditions, SofContext context) {
        this.context = context;

        if (context.conf.getConf(SofConf.CODEGEN_EXPRESSION)) {
            this.conditions = Utils.toImmutableList(conditions.stream().map(CodeGenerator::tryCodegen));
        } else {
            this.conditions = conditions;
        }
    }

    public boolean apply(InternalRow input) {
        for (Expression condition : conditions) {
            if (!(boolean) condition.eval(input)) {
                return false;
            }
        }
        return true;
    }
}
