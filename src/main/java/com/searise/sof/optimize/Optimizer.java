package com.searise.sof.optimize;

import com.google.common.collect.ImmutableList;
import com.searise.sof.core.SofException;
import com.searise.sof.core.Utils;
import com.searise.sof.optimize.afterprocess.AddExchange;
import com.searise.sof.optimize.implementation.ImplementationRule;
import com.searise.sof.optimize.iter.Iterator;
import com.searise.sof.optimize.preprocess.PreprocessRule;
import com.searise.sof.optimize.transformation.ExprIter;
import com.searise.sof.optimize.transformation.Pattern;
import com.searise.sof.optimize.transformation.TransformationRule;
import com.searise.sof.plan.logic.LogicalPlan;
import com.searise.sof.plan.physics.PhysicalPlan;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.searise.sof.optimize.Operand.getOperand;

public class Optimizer {
    public static Optimizer newOptimizer() {
        return new Optimizer(
                PreprocessRule.preprocessRules
                , TransformationRule.transformationRuleBatches
                , ImplementationRule.implementationRuleMap
        );
    }

    private final List<PreprocessRule> preprocessRules;
    private final List<Map<Operand, List<TransformationRule>>> transformationRuleBatches;
    private final Map<Operand, ImplementationRule> implementationRuleMap;

    public Optimizer(List<PreprocessRule> preprocessRules,
                     List<Map<Operand, List<TransformationRule>>> transformationRuleBatches,
                     Map<Operand, ImplementationRule> implementationRuleMap) {
        this.preprocessRules = preprocessRules;
        this.transformationRuleBatches = transformationRuleBatches;
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
        for (int batchIndex = 0; batchIndex < transformationRuleBatches.size(); batchIndex++) {
            exploreGroup(rootGroup, transformationRuleBatches.get(batchIndex), batchIndex);
        }
        return rootGroup;
    }

    private void exploreGroup(Group group, Map<Operand, List<TransformationRule>> batch, int batchIndex) {
        if (group.explored.isExplored(batchIndex)) {
            return;
        }
        group.explored.explore(batchIndex);
        Iterator<GroupExpr> iterator = group.iter().reset();

        while (iterator.hasNext()) {
            GroupExpr groupExpr = iterator.next();
            if (groupExpr.explored.isExplored(batchIndex)) {
                continue;
            }

            groupExpr.explored.explore(batchIndex);
            for (Group child : groupExpr.children) {
                exploreGroup(child, batch, batchIndex);
            }

            boolean isReplace = false;

            Operand operand = Operand.getOperand(groupExpr.exprNode);
            for (TransformationRule rule : batch.getOrDefault(operand, ImmutableList.of())) {
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
        return implGroup(rootGroup).getLeft();
    }

    private Pair<PhysicalPlan, Integer> implGroup(Group group) {
        if (group.impl.isPresent()) {
            return group.impl.get();
        }

        // todo 不是用level,而是用plan的cost来做剪枝.
        Optional<Pair<PhysicalPlan, Integer>> bestPlan = Optional.empty();
        Iterator<GroupExpr> iterator = group.iter().newReadOnlyIter();
        while (iterator.hasNext()) {
            GroupExpr groupExpr = iterator.next();
            List<Pair<PhysicalPlan, Integer>> children = Utils.toImmutableList(groupExpr.children.stream().map(this::implGroup));
            int childrenLevel = children.stream().map(Pair::getRight).reduce((a, b) -> a + b).orElse(0);
            int level = childrenLevel + 1;
            if (!bestPlan.isPresent() || bestPlan.get().getRight() > level) {
                ImplementationRule implRule = implementationRuleMap.get(getOperand(groupExpr.exprNode));
                List<PhysicalPlan> implChildren = Utils.toImmutableList(children.stream().map(Pair::getLeft));
                PhysicalPlan impl = implRule.onImplement(groupExpr, implChildren);
                bestPlan = Optional.of(Pair.of(impl, level));
            }
        }

        group.impl = bestPlan;
        return bestPlan.orElseGet(() -> {
            throw new SofException("can't find best plan in implGroup");
        });
    }

    private PhysicalPlan onPhaseAfterprocessing(PhysicalPlan physicalPlan) {
        physicalPlan.prune(ImmutableList.of(), true);
        physicalPlan.resolveIndex();
        physicalPlan.removeAlias();
//        return new AddExchange().apply(physicalPlan);
        return physicalPlan;
    }
}
