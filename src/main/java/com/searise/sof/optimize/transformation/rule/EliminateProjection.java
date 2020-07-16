package com.searise.sof.optimize.transformation.rule;

import com.google.common.collect.ImmutableList;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.attribute.Attribute;
import com.searise.sof.optimize.GroupExpr;
import com.searise.sof.optimize.Operand;
import com.searise.sof.optimize.iter.Iterator;
import com.searise.sof.optimize.transformation.ExprIter;
import com.searise.sof.optimize.transformation.Pattern;
import com.searise.sof.optimize.transformation.TransformationRule;
import com.searise.sof.plan.logic.Project;

import java.util.List;

/**
 * 单纯的select a, b, c可以被消除.
 * 列裁剪用schema,不需要project.
 */
public class EliminateProjection implements TransformationRule {
    @Override
    public Pattern pattern() {
        return new Pattern(Operand.OperandProject);
    }

    @Override
    public List<GroupExpr> onTransform(ExprIter exprIter) {
        GroupExpr groupExpr = exprIter.getValue();
        Project project = (Project) groupExpr.exprNode;
        if (project.projectList.stream().allMatch(expr -> expr.getClass() == Attribute.class)) {
            return Utils.toImmutableList(exprIter.getValue().children.stream().flatMap(group -> {
                Iterator<GroupExpr> iter = group.iter().newReadOnlyIter();
                ImmutableList.Builder<GroupExpr> builder = ImmutableList.builder();
                while (iter.hasNext()) {
                    GroupExpr next = iter.next();
                    GroupExpr newGroupExpr = new GroupExpr(next.exprNode, next.children, groupExpr.group);
                    newGroupExpr.explored = true;
                    builder.add(newGroupExpr);
                }
                return builder.build().stream();
            }));
        }
        return ImmutableList.of();
    }
}
