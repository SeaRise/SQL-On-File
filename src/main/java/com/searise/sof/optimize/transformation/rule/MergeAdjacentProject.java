package com.searise.sof.optimize.transformation.rule;

import com.google.common.collect.ImmutableList;
import com.searise.sof.analyse.Applicable;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.Alias;
import com.searise.sof.expression.attribute.Attribute;
import com.searise.sof.optimize.GroupExpr;
import com.searise.sof.optimize.Operand;
import com.searise.sof.optimize.transformation.ExprIter;
import com.searise.sof.optimize.transformation.Pattern;
import com.searise.sof.optimize.transformation.TransformationRule;
import com.searise.sof.plan.logic.Project;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MergeAdjacentProject implements TransformationRule {
    @Override
    public Pattern pattern() {
        return new Pattern(Operand.OperandProject,
                new Pattern(Operand.OperandProject));
    }

    @Override
    public List<GroupExpr> onTransform(ExprIter exprIter) {
        GroupExpr topProjectExpr = exprIter.getValue();
        Project topProject = (Project) topProjectExpr.exprNode;
        // 避免和EliminateProject重复,导致产生多余的分支.
        if (topProject.projectList.stream().allMatch(expr -> expr.getClass() == Attribute.class)) {
            return ImmutableList.of();
        }

        GroupExpr bottomProjectExpr = exprIter.children.get(0).getValue();
        Project bottomProject = (Project) bottomProjectExpr.exprNode;

        Map<Long, Expression> replace = bottomProject.projectList.stream().
                filter(e -> e.getClass() == Alias.class).
                collect(Collectors.toMap(
                        expr -> ((Attribute) ((Alias) expr).attribute).exprId,
                        expr -> ((Alias) expr).child
                ));

        List<Expression> newProjectList = Utils.toImmutableList(topProject.projectList.stream().map(expr ->
                expr.transformUp((Applicable<Expression>) expression -> {
                    if (expression.getClass() == Attribute.class) {
                        Attribute attribute = (Attribute) expression;
                        return replace.getOrDefault(attribute.exprId, attribute);
                    }
                    return expression;
                })
        ));

        Project newProject = new Project(newProjectList, null, topProject.context);
        GroupExpr newProjectExpr = new GroupExpr(newProject, bottomProjectExpr.children);
        return ImmutableList.of(newProjectExpr);
    }
}
