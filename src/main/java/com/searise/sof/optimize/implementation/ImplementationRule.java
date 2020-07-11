package com.searise.sof.optimize.implementation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.searise.sof.optimize.GroupExpr;
import com.searise.sof.optimize.Operand;
import com.searise.sof.plan.physics.PhysicalPlan;

import java.util.List;
import java.util.Map;

public interface ImplementationRule {
    PhysicalPlan onImplement(GroupExpr groupExpr, List<PhysicalPlan> children);

    Map<Operand, List<ImplementationRule>> implementationRuleMap = ImmutableMap.of(
            Operand.OperandProject, ImmutableList.of(new ImplProject())
            ,Operand.OperandFilter, ImmutableList.of(new ImplFilter())
            ,Operand.OperandJoin, ImmutableList.of(new ImplNestedLoopJoin())
            ,Operand.OperandRelation, ImmutableList.of(new ImplRelation())
    );
}
