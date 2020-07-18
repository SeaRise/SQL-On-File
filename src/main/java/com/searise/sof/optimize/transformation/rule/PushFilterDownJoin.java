package com.searise.sof.optimize.transformation.rule;

import com.google.common.collect.ImmutableList;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.optimize.Group;
import com.searise.sof.optimize.GroupExpr;
import com.searise.sof.optimize.Operand;
import com.searise.sof.optimize.transformation.ExprIter;
import com.searise.sof.optimize.transformation.Pattern;
import com.searise.sof.optimize.transformation.TransformationRule;
import com.searise.sof.plan.logic.Filter;
import com.searise.sof.plan.logic.InnerJoin;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PushFilterDownJoin implements TransformationRule, PushFilterDownHelper {
    @Override
    public Pattern pattern() {
        return new Pattern(Operand.OperandFilter,
                new Pattern(Operand.OperandJoin));
    }

    @Override
    public List<GroupExpr> onTransform(ExprIter exprIter) {
        GroupExpr filterExpr = exprIter.getValue();
        Filter filter = (Filter) filterExpr.exprNode;
        GroupExpr joinExpr = exprIter.children.get(0).getValue();
        InnerJoin join = (InnerJoin) joinExpr.exprNode;

        List<Expression> combineConds = Utils.combineDistinct(filter.conditions, join.conditions);

        Set<Long> leftExprIds = joinExpr.children.get(0).schema.stream().map(a -> a.exprId).collect(Collectors.toSet());
        Pair<List<Expression>, List<Expression>> leftSplits = split(combineConds, leftExprIds);
        List<Expression> retainConds = leftSplits.getLeft();
        List<Expression> leftPushDownConds = leftSplits.getRight();
        Group left = newChildGroup(joinExpr.children.get(0), leftPushDownConds);

        Group right;
        if (retainConds.isEmpty()) {
            right = joinExpr.children.get(1);
        } else {
            Set<Long> rightExprIds = joinExpr.children.get(1).schema.stream().map(a -> a.exprId).collect(Collectors.toSet());
            Pair<List<Expression>, List<Expression>> rightSplits = split(retainConds, rightExprIds);
            retainConds = rightSplits.getLeft();
            List<Expression> rightPushDownConds = rightSplits.getRight();
            right = newChildGroup(joinExpr.children.get(1), rightPushDownConds);
        }

        InnerJoin newJoin = new InnerJoin(null, null, retainConds);
        GroupExpr newJoinExpr = new GroupExpr(newJoin, ImmutableList.of(left, right));
        return ImmutableList.of(newJoinExpr);
    }

    private Group newChildGroup(Group originGroup, List<Expression> pushDownConds) {
        if (pushDownConds.isEmpty()) {
            return originGroup;
        } else {
            Filter pushDownFilter = new Filter(pushDownConds, null);
            return Group.newGroup(pushDownFilter, ImmutableList.of(originGroup), originGroup.schema);
        }
    }
}
