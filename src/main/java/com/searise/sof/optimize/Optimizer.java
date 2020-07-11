package com.searise.sof.optimize;

import com.searise.sof.common.SofException;
import com.searise.sof.common.Utils;
import com.searise.sof.optimize.implementation.ImplementationRule;
import com.searise.sof.optimize.preprocess.PreprocessRule;
import com.searise.sof.plan.logic.LogicalPlan;
import com.searise.sof.plan.physics.PhysicalPlan;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Optimizer {
    public static Optimizer newOptimizer() {
        return new Optimizer(
                PreprocessRule.preprocessRules,
                ImplementationRule.implementationRuleMap
        );
    }

    private final List<PreprocessRule> preprocessRules;
    private final Map<Operand, List<ImplementationRule>> implementationRuleMap;
    public Optimizer(List<PreprocessRule> preprocessRules, Map<Operand, List<ImplementationRule>> implementationRuleMap) {
        this.preprocessRules = preprocessRules;
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
        return rootGroup;
    }

    private PhysicalPlan onPhaseImplementation(Group rootGroup) {
        return implGroup(rootGroup);
    }

    private PhysicalPlan implGroup(Group group) {
        Iterator<GroupExpr> iterator = group.iterator();
        if (iterator.hasNext()) {
            GroupExpr groupExpr = iterator.next();
            List<PhysicalPlan> children = Utils.toImmutableList(groupExpr.children.stream().map(this::implGroup));
            ImplementationRule implRule = implementationRuleMap.get(Operand.getOperand(groupExpr.exprNode)).get(0);
            return implRule.onImplement(groupExpr, children);
        }
        throw new SofException("has no physical plan impl");
    }

    private PhysicalPlan onPhaseAfterprocessing(PhysicalPlan physicalPlan) {
        return physicalPlan;
    }
}
