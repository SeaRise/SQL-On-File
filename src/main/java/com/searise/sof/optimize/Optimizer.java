package com.searise.sof.optimize;

import com.google.common.collect.ImmutableList;
import com.searise.sof.core.Utils;
import com.searise.sof.optimize.implementation.ImplementationRule;
import com.searise.sof.optimize.iter.Iterator;
import com.searise.sof.optimize.preprocess.PreprocessRule;
import com.searise.sof.optimize.transformation.ExprIter;
import com.searise.sof.optimize.transformation.Pattern;
import com.searise.sof.optimize.transformation.TransformationRule;
import com.searise.sof.plan.logic.LogicalPlan;
import com.searise.sof.plan.physics.PhysicalPlan;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        exploreGroup(rootGroup);
        return rootGroup;
    }

    private void exploreGroup(Group group) {
        if (group.explored) {
            return;
        }
        group.explored = true;
        Iterator<GroupExpr> iterator = group.iter().reset();

        while (iterator.hasNext()) {
            GroupExpr groupExpr = iterator.next();
            if (groupExpr.explored) {
                continue;
            }

            groupExpr.explored = true;
            for (Group child : groupExpr.children) {
                exploreGroup(child);
            }

            boolean isReplace = false;

            Operand operand = Operand.getOperand(groupExpr.exprNode);
            for (TransformationRule rule : transformationRuleMap.getOrDefault(operand, ImmutableList.of())) {
                Pattern pattern = rule.pattern();
                if (!pattern.operand.match(operand)) {
                    continue;
                }

                Optional<ExprIter> iterOptional = ExprIter.newExprIter(groupExpr, pattern);
                if (iterOptional.isPresent()) {
                    ExprIter iter = iterOptional.get();
                    do {
                        List<GroupExpr> replaces = Utils.checkNotNull(rule.onTransform(iter),
                                "the result of onTransform can not be null : " + rule.getClass().getSimpleName());
                        if (!replaces.isEmpty()) {
                            for (GroupExpr replace : replaces) {
                                group.insert(replace);
                            }
                            isReplace = true;
                        }
                    } while (iter.next());
                }
            }
            if (isReplace) {
                iterator.remove();
            }
        }
    }

    private PhysicalPlan onPhaseImplementation(Group rootGroup) {
        return implGroup(rootGroup);
    }

    private PhysicalPlan implGroup(Group group) {
        GroupExpr groupExpr = group.iter().reset().next();
        List<PhysicalPlan> children = Utils.toImmutableList(groupExpr.children.stream().map(this::implGroup));
        ImplementationRule implRule = implementationRuleMap.get(getOperand(groupExpr.exprNode));
        return implRule.onImplement(groupExpr, children);
    }

    private PhysicalPlan onPhaseAfterprocessing(PhysicalPlan physicalPlan) {
        physicalPlan.prune(ImmutableList.of(), true);
        physicalPlan.resolveIndex();
        return physicalPlan;
    }
}
