package com.searise.sof.optimize.implementation;

import com.google.common.collect.ImmutableMap;
import com.searise.sof.optimize.GroupExpr;
import com.searise.sof.optimize.Operand;
import com.searise.sof.plan.physics.PhysicalPlan;

import java.util.List;
import java.util.Map;

public interface ImplementationRule {
    PhysicalPlan onImplement(GroupExpr groupExpr, List<PhysicalPlan> children);

    Map<Operand, ImplementationRule> implementationRuleMap = ImmutableMap.of(
            Operand.OperandProject, new ImplProject()
            , Operand.OperandFilter, new ImplFilter()
            , Operand.OperandJoin, new ImplJoin()
            , Operand.OperandRelation, new ImplRelation()
    );
}
