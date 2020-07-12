package com.searise.sof.optimize;

import com.searise.sof.core.Utils;
import com.searise.sof.optimize.implementation.ImplementationRule;
import com.searise.sof.optimize.preprocess.PreprocessRule;
import com.searise.sof.optimize.transformation.TransformationRule;
import com.searise.sof.plan.logic.LogicalPlan;
import com.searise.sof.plan.physics.PhysicalPlan;

import java.util.List;
import java.util.Map;

import static com.searise.sof.optimize.Operand.getOperand;

public class Optimizer {
    public static Optimizer newOptimizer() {
        return new Optimizer(
                PreprocessRule.preprocessRules
                , TransformationRule.transformationRuleMap
                , ImplementationRule.implementationRuleMap
        );
    }

    private final List<PreprocessRule> preprocessRules;
    private final Map<Operand, List<TransformationRule>> transformationRuleMap;
    private final Map<Operand, ImplementationRule> implementationRuleMap;

    public Optimizer(List<PreprocessRule> preprocessRules,
                     Map<Operand, List<TransformationRule>> transformationRuleMap,
                     Map<Operand, ImplementationRule> implementationRuleMap) {
        this.preprocessRules = preprocessRules;
        this.transformationRuleMap = transformationRuleMap;
        this.implementationRuleMap = implementationRuleMap;
    }

    public PhysicalPlan optimize(LogicalPlan logicalPlan) {
        logicalPlan = onPhasePreprocessing(logicalPlan);
        Group rootGroup = Group.convert2Group(logicalPlan);
        rootGroup = onPhaseExploration(rootGroup);
        PhysicalPlan bestPhysicalPlan = onPhaseImplementation(rootGroup);
        bestPhysicalPlan = onPhaseAfterprocessing(bestPhysicalPlan);
        return bestPhysicalPlan;
    }

    private LogicalPlan onPhasePreprocessing(LogicalPlan logicalPlan) {
        for (PreprocessRule preprocessRule : preprocessRules) {
            logicalPlan = preprocessRule.apply(logicalPlan);
        }
        return logicalPlan;
    }

    private Group onPhaseExploration(Group rootGroup) {
        return exploreGroup(rootGroup);
    }

    private Group exploreGroup(Group group) {
        return group;
    }

    private PhysicalPlan onPhaseImplementation(Group rootGroup) {
        return implGroup(rootGroup);
    }

    private PhysicalPlan implGroup(Group group) {
        GroupExpr groupExpr = group.groupExpr();
        List<PhysicalPlan> children = Utils.toImmutableList(groupExpr.children.stream().map(this::implGroup));
        ImplementationRule implRule = implementationRuleMap.get(getOperand(groupExpr.exprNode));
        return implRule.onImplement(groupExpr, children);
    }

    private PhysicalPlan onPhaseAfterprocessing(PhysicalPlan physicalPlan) {
        physicalPlan.resolveSchema();
        return physicalPlan;
    }
}
