package com.searise.sof.core;

import com.searise.sof.core.row.InternalRow;
import com.searise.sof.expression.Expression;

import java.util.List;

public class Predication {
    private final List<Expression> conditions;

    public Predication(List<Expression> conditions) {
        this.conditions = conditions;
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
