package com.searise.sof.optimize.transformation.rule;

import com.google.common.collect.ImmutableList;
import com.searise.sof.expression.Expression;
import com.searise.sof.optimize.Group;
import com.searise.sof.optimize.GroupExpr;
import com.searise.sof.optimize.Operand;
import com.searise.sof.optimize.transformation.ExprIter;
import com.searise.sof.optimize.transformation.Pattern;
import com.searise.sof.optimize.transformation.TransformationRule;
import com.searise.sof.plan.logic.Filter;
import com.searise.sof.plan.logic.Project;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PushFilterDownProject implements TransformationRule, PushFilterDownHelper {
    @Override
    public Pattern pattern() {
        return new Pattern(Operand.OperandFilter,
                new Pattern(Operand.OperandProject));
    }

    @Override
    public List<GroupExpr> onTransform(ExprIter exprIter) {
        GroupExpr filterExpr = exprIter.getValue();
        Filter filter = (Filter) filterExpr.exprNode;
        GroupExpr projectExpr = exprIter.children.get(0).getValue();
        Project project = (Project) projectExpr.exprNode;


        Set<Long> exprIds = projectExpr.children.get(0).schema.stream().map(a -> a.exprId).collect(Collectors.toSet());
        Pair<List<Expression>, List<Expression>> splits = split(filter.conditions, exprIds);
        List<Expression> retainConds = splits.getLeft();
        List<Expression> pushDownConds = splits.getRight();

        if (pushDownConds.isEmpty()) {
            return ImmutableList.of();
        }
        Filter pushDownFilter = new Filter(pushDownConds, null);
        Group pushDownFilterGroup = Group.newGroup(pushDownFilter, projectExpr.children, projectExpr.group.schema);

        if (retainConds.isEmpty()) {
            return ImmutableList.of(new GroupExpr(project, ImmutableList.of(pushDownFilterGroup)));
        }
        Group newProjectGroup = Group.newGroup(project, ImmutableList.of(pushDownFilterGroup), projectExpr.group.schema);
        Filter retainFilter = new Filter(retainConds, null);
        GroupExpr retainFilterExpr = new GroupExpr(retainFilter, ImmutableList.of(newProjectGroup));
        return ImmutableList.of(retainFilterExpr);
    }
}
