package com.searise.sof.core;

import com.searise.sof.codegen.expr.CodeGenerator;
import com.searise.sof.core.conf.Conf;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.expression.Expression;

import java.util.List;

public class Predication {
    private final List<Expression> conditions;
    public final Context context;

    public Predication(List<Expression> conditions, Context context) {
        this.context = context;

        if (context.conf.getConf(Conf.CODEGEN_EXPRESSION)) {
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
