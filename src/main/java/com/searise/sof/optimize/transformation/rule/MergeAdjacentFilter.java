package com.searise.sof.optimize.transformation.rule;

import com.google.common.collect.ImmutableList;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.optimize.GroupExpr;
import com.searise.sof.optimize.Operand;
import com.searise.sof.optimize.transformation.ExprIter;
import com.searise.sof.optimize.transformation.Pattern;
import com.searise.sof.optimize.transformation.TransformationRule;
import com.searise.sof.plan.logic.Filter;

import java.util.List;

public class MergeAdjacentFilter implements TransformationRule {
    @Override
    public Pattern pattern() {
        return new Pattern(Operand.OperandFilter,
                new Pattern(Operand.OperandFilter));
    }

    @Override
    public List<GroupExpr> onTransform(ExprIter exprIter) {
        GroupExpr topFilterExpr = exprIter.getValue();
        Filter topFilter = (Filter) topFilterExpr.exprNode;
        GroupExpr bottomFilterExpr = exprIter.children.get(0).getValue();
        Filter bottomFilter = (Filter) bottomFilterExpr.exprNode;

        List<Expression> mergeConditions = Utils.combineDistinct(topFilter.conditions, bottomFilter.conditions);
        Filter mergeFilter = new Filter(mergeConditions, null, topFilter.context);
        GroupExpr mergeFilterExpr = new GroupExpr(mergeFilter, bottomFilterExpr.children);
        return ImmutableList.of(mergeFilterExpr);
    }
}
