package com.searise.sof.optimize.transformation.rule.join;

import com.google.common.collect.ImmutableList;
import com.searise.sof.optimize.GroupExpr;
import com.searise.sof.optimize.Operand;
import com.searise.sof.optimize.transformation.ExprIter;
import com.searise.sof.optimize.transformation.Pattern;
import com.searise.sof.optimize.transformation.TransformationRule;
import com.searise.sof.plan.logic.InnerJoin;
import com.searise.sof.plan.logic.LogicalPlan;
import com.searise.sof.plan.logic.MultiJoin;

import java.util.List;

/**
 *     join               multi-join
 *    |   \     ---->     |        \
 *   a     b              a        b
 *
 *   a,b != join/multi-join
 * */
public class TransformLeafJoinToMultiJoin implements TransformationRule {
    @Override
    public Pattern pattern() {
        return new Pattern(Operand.OperandMultiJoin,
                new Pattern(Operand.OperandAny)
                , new Pattern(Operand.OperandAny)
        );
    }

    @Override
    public List<GroupExpr> onTransform(ExprIter exprIter) {
        GroupExpr joinExpr = exprIter.getValue();
        InnerJoin join = (InnerJoin) joinExpr.exprNode;

        GroupExpr leftExpr = exprIter.children.get(0).getValue();
        LogicalPlan leftPlan = leftExpr.exprNode;
        GroupExpr rightExpr = exprIter.children.get(1).getValue();
        LogicalPlan rightPlan = rightExpr.exprNode;

        if (leftPlan.getClass() == MultiJoin.class || leftPlan.getClass() == InnerJoin.class ||
                rightPlan.getClass() == MultiJoin.class || rightPlan.getClass() == InnerJoin.class) {
            return ImmutableList.of();
        }

        MultiJoin multiJoin = new MultiJoin(join.conditions);
        GroupExpr multiJoinExpr = new GroupExpr(multiJoin, joinExpr.children);
        return ImmutableList.of(multiJoinExpr);
    }
}
