package com.searise.sof.optimize.transformation.rule.join;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.searise.sof.core.SofContext;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.Attribute;
import com.searise.sof.optimize.Group;
import com.searise.sof.optimize.GroupExpr;
import com.searise.sof.optimize.Operand;
import com.searise.sof.optimize.transformation.ExprIter;
import com.searise.sof.optimize.transformation.Pattern;
import com.searise.sof.optimize.transformation.TransformationRule;
import com.searise.sof.plan.logic.InnerJoin;
import com.searise.sof.plan.logic.MultiJoin;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 类似赫夫曼树的实现.
 * 贪心策略是每次循环选择cost最小的join plan.
 */
public class GreedyJoinReorder implements TransformationRule {
    @Override
    public Pattern pattern() {
        return new Pattern(Operand.OperandMultiJoin);
    }

    @Override
    public List<GroupExpr> onTransform(ExprIter exprIter) {
        GroupExpr groupExpr = exprIter.getValue();
        MultiJoin multiJoin = (MultiJoin) groupExpr.exprNode;
        GroupExpr reorderGroupExpr = reorder(multiJoin.context, multiJoin.conditions, groupExpr.children);
        return ImmutableList.of(reorderGroupExpr);
    }

    private GroupExpr reorder(SofContext context, List<Expression> conditions, List<Group> leaves) {
        List<JoinPlan> joinPlanPool = new ArrayList<>(leaves.size());
        for (int i = 0; i < leaves.size(); i++) {
            joinPlanPool.add(new JoinPlan(leaves.get(i), BigInteger.ZERO, ImmutableSet.of(i)));
        }

        List<Expression> retainConditions = conditions;
        while (!retainConditions.isEmpty()) {
            List<Expression> newRetainConditions = ImmutableList.of();
            JoinPlan newJoinPlan = null;
            List<Integer> removePlanIndexes = ImmutableList.of();
            for (int outer = 0; outer < joinPlanPool.size() - 1; outer++) {
                for (int inner = outer + 1; inner < joinPlanPool.size(); inner++) {
                    JoinPlan outerPlan = joinPlanPool.get(outer);
                    JoinPlan innerPlan = joinPlanPool.get(inner);
                    Optional<Pair<JoinPlan, List<Expression>>> result = buildJoinPlan(context, outerPlan, innerPlan, retainConditions);
                    if (result.isPresent()) {
                        JoinPlan buildJoinPlan = result.get().getLeft();
                        if (Objects.isNull(newJoinPlan) || buildJoinPlan.cost.compareTo(newJoinPlan.cost) <= 0) {
                            newJoinPlan = buildJoinPlan;
                            newRetainConditions = result.get().getRight();
                            //先大再小.
                            removePlanIndexes = ImmutableList.of(inner, outer);
                        }
                    }
                }
            }
            Utils.checkNotNull(newJoinPlan, "It's impossible has null newJoinPlan in reorder");
            retainConditions = newRetainConditions;
            for (Integer removePlanIndex : removePlanIndexes) {
                joinPlanPool.remove(removePlanIndex.intValue());
            }
            joinPlanPool.add(newJoinPlan);
        }

        Preconditions.checkArgument(joinPlanPool.size() > 0);
        if (joinPlanPool.size() > 1) {
            return buildCrossJoin(joinPlanPool, context);
        }
        return joinPlanPool.get(0).group.iter().newReadOnlyIter().next();
    }

    private GroupExpr buildCrossJoin(List<JoinPlan> joinPlanPool, SofContext context) {
        List<Group> sortedList = joinPlanPool.stream().
                sorted((o1, o2) -> Integer.compare(o2.leaves.size(), o1.leaves.size())).
                map(p -> p.group).
                collect(Collectors.toList());
        Group head = sortedList.get(0);
        for (int i = 1; i < sortedList.size(); i++) {
            Group group = sortedList.get(i);
            InnerJoin join = new InnerJoin(null, null, ImmutableList.of(), context);
            head = Group.newGroup(join, ImmutableList.of(head, group), Utils.combineDistinct(head.schema, group.schema));
        }
        return head.iter().newReadOnlyIter().next();
    }

    private Optional<Pair<JoinPlan, List<Expression>>> buildJoinPlan(SofContext context, JoinPlan plan1, JoinPlan plan2, List<Expression> conditions) {
        if (plan1.leaves.stream().anyMatch(plan2.leaves::contains)) {
            return Optional.empty();
        }

        ImmutableSet.Builder<Long> schemaBuilder = ImmutableSet.builder();
        for (Attribute attribute : plan1.group.schema) {
            schemaBuilder.add(attribute.exprId);
        }
        for (Attribute attribute : plan2.group.schema) {
            schemaBuilder.add(attribute.exprId);
        }
        Set<Long> schema = schemaBuilder.build();

        ImmutableList.Builder<Expression> relatedBuilder = ImmutableList.builder();
        ImmutableList.Builder<Expression> retainBuilder = ImmutableList.builder();
        for (Expression condition : conditions) {
            List<Attribute> useAttributes = Expression.getUseAttributes(condition);
            if (useAttributes.stream().allMatch(u -> schema.contains(u.exprId))) {
                relatedBuilder.add(condition);
            } else {
                retainBuilder.add(condition);
            }
        }
        List<Expression> relatedConditions = relatedBuilder.build();
        if (relatedConditions.isEmpty()) {
            // 不生成笛卡尔积.
            return Optional.empty();
        }
        List<Expression> retainConditions = retainBuilder.build();

        BigInteger selfCost = plan1.sizeInBytes().add(plan2.sizeInBytes());
        BigInteger newCost = selfCost.add(plan1.cost).add(plan2.cost);
        InnerJoin join = new InnerJoin(null, null, relatedConditions, context);
        // 构造左深树.
        List<Group> newChildren = plan1.leaves.size() > plan2.leaves.size() ?
                ImmutableList.of(plan1.group, plan2.group) :
                ImmutableList.of(plan2.group, plan1.group);
        Group group = Group.newGroup(join, newChildren, Utils.combineDistinct(plan1.group.schema, plan2.group.schema));
        JoinPlan newJoinPlan = new JoinPlan(group, newCost, Utils.combineDistinct(plan1.leaves, plan2.leaves));

        return Optional.of(Pair.of(newJoinPlan, retainConditions));
    }
}
