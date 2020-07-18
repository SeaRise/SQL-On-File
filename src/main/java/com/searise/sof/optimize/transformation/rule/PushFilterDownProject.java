package com.searise.sof.optimize.transformation.rule;

import com.google.common.collect.ImmutableList;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.Attribute;
import com.searise.sof.optimize.GroupExpr;
import com.searise.sof.optimize.Operand;
import com.searise.sof.optimize.transformation.ExprIter;
import com.searise.sof.optimize.transformation.Pattern;
import com.searise.sof.optimize.transformation.TransformationRule;
import com.searise.sof.plan.logic.Filter;
import com.searise.sof.plan.logic.Project;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PushFilterDownProject implements TransformationRule {
    @Override
    public Pattern pattern() {
        return new Pattern(Operand.OperandFilter,
                new Pattern(Operand.OperandProject));
    }

    @Override
    public List<GroupExpr> onTransform(ExprIter exprIter) {
        GroupExpr projectExpr = exprIter.getValue();
        Project project = (Project) projectExpr.exprNode;
        GroupExpr filterExpr = exprIter.children.get(0).getValue();
        Filter filter = (Filter) filterExpr.exprNode;

        Set<Long> projectExprIds = projectExpr.group.schema.stream().map(a -> a.exprId).collect(Collectors.toSet());

        ImmutableList.Builder<Expression> retainBuilder = ImmutableList.builder();
        ImmutableList.Builder<Expression> pushDownBuilder = ImmutableList.builder();
        for (Expression condition : filter.conditions) {
            boolean isRetain = false;
            List<Attribute> useAttributes = Expression.getUseAttributes(condition);
            for (Attribute useAttribute : useAttributes) {
                if (projectExprIds.contains(useAttribute.exprId)) {
                    isRetain = true;
                    break;
                }
            }
            if (isRetain) {
                retainBuilder.add(condition);
            } else {
                pushDownBuilder.add(condition);
            }
        }

        List<Expression> pushDownConds = pushDownBuilder.build();
        if (pushDownConds.isEmpty()) {
            return ImmutableList.of();
        }

        List<Expression> retainConds = retainBuilder.build();


        return null;
    }
}
