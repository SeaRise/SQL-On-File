package com.searise.sof.optimize.transformation.rule.join;

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
import com.searise.sof.plan.logic.MultiJoin;

import java.util.List;

/**
 * join                       multi-join
 * |            \      ---->     |   |   \  \
 * multi-join   multi-join           a   b    c  d
 * |     \      |      \
 * a     b      c      d
 */
public class MergeJoinTwoSideMultiJoin implements TransformationRule {
    @Override
    public Pattern pattern() {
        return new Pattern(Operand.OperandJoin,
                new Pattern(Operand.OperandMultiJoin)
                , new Pattern(Operand.OperandMultiJoin)
        );
    }

    @Override
    public List<GroupExpr> onTransform(ExprIter exprIter) {
        GroupExpr joinExpr = exprIter.getValue();
        InnerJoin join = (InnerJoin) joinExpr.exprNode;

        GroupExpr leftMultiJoinExpr = exprIter.children.get(0).getValue();
        MultiJoin leftMultiJoin = (MultiJoin) leftMultiJoinExpr.exprNode;
        GroupExpr rightMultiJoinExpr = exprIter.children.get(1).getValue();
        MultiJoin rightMultiJoin = (MultiJoin) rightMultiJoinExpr.exprNode;

        List<Expression> newConditions = Utils.combineDistinct(leftMultiJoin.conditions, rightMultiJoin.conditions, join.conditions);
        List<Group> newChildren = Utils.combineDistinct(leftMultiJoinExpr.children, rightMultiJoinExpr.children, joinExpr.children);
        MultiJoin newMultiJoin = new MultiJoin(newConditions, join.context);
        GroupExpr newMultiJoinExpr = new GroupExpr(newMultiJoin, newChildren);
        return ImmutableList.of(newMultiJoinExpr);
    }
}