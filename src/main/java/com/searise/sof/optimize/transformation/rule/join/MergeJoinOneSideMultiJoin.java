package com.searise.sof.optimize.transformation.rule.join;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.optimize.Group;
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
 *         join                     multi-join
 *    |            \     ---->     |    |   \
 * multi-join       b              c    d    b
 *  |     \
 *  c     d
 *
 *  b != join/multi-join
 * */
public class MergeJoinOneSideMultiJoin implements TransformationRule {
    private final int multiJoinIndex;

    public MergeJoinOneSideMultiJoin(int multiJoinIndex) {
        Preconditions.checkArgument(multiJoinIndex == 0 || multiJoinIndex == 1);
        this.multiJoinIndex = multiJoinIndex;
    }

    @Override
    public Pattern pattern() {
        if (multiJoinIndex == 0) {
            return new Pattern(Operand.OperandJoin,
                    new Pattern(Operand.OperandMultiJoin)
                    , new Pattern(Operand.OperandAny)
            );
        } else {
            return new Pattern(Operand.OperandJoin,
                    new Pattern(Operand.OperandAny)
                    , new Pattern(Operand.OperandMultiJoin)
            );
        }
    }

    @Override
    public List<GroupExpr> onTransform(ExprIter exprIter) {
        GroupExpr joinExpr = exprIter.getValue();
        InnerJoin join = (InnerJoin) joinExpr.exprNode;

        GroupExpr otherExpr = exprIter.children.get(1-multiJoinIndex).getValue();
        LogicalPlan otherPlan = otherExpr.exprNode;
        if (otherPlan.getClass() == MultiJoin.class || otherPlan.getClass() == InnerJoin.class) {
            return ImmutableList.of();
        }
        GroupExpr multiJoinExpr = exprIter.children.get(multiJoinIndex).getValue();
        MultiJoin multiJoin = (MultiJoin) multiJoinExpr.exprNode;

        List<Expression> newConditions = Utils.combineDistinct(multiJoin.conditions, join.conditions);
        List<Group> newChildren = Utils.combineDistinct(multiJoinExpr.children, joinExpr.children);
        MultiJoin newMultiJoin = new MultiJoin(newConditions, join.context);
        GroupExpr newMultiJoinExpr = new GroupExpr(newMultiJoin, newChildren);
        return ImmutableList.of(newMultiJoinExpr);
    }
}
