package com.searise.sof.optimize;

import com.searise.sof.core.SofException;
import com.searise.sof.plan.logic.*;

public enum Operand {
    OperandJoin(InnerJoin.class),
    OperandProject(Project.class),
    OperandFilter(Filter.class),
    OperandRelation(Relation.class);

    private final Class<? extends LogicalPlan> planClass;

    Operand(Class<? extends LogicalPlan> planClass) {
        this.planClass = planClass;
    }

    public static Operand getOperand(LogicalPlan logicalPlan) {
        for (Operand operand : Operand.values()) {
            if (operand.planClass == logicalPlan.getClass()) {
                return operand;
            }
        }
        throw new SofException(String.format("unsupported logical plan: %s", logicalPlan.getClass().getSimpleName()));
    }
}
