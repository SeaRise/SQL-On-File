package com.searise.sof.analyse;

import com.searise.sof.core.SofContext;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.Alias;
import com.searise.sof.expression.attribute.Attribute;
import com.searise.sof.expression.attribute.AttributeBase;
import com.searise.sof.expression.attribute.UnresolvedAttribute;
import com.searise.sof.plan.logic.Filter;
import com.searise.sof.plan.logic.InnerJoin;
import com.searise.sof.plan.logic.LogicalPlan;
import com.searise.sof.plan.logic.Project;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

import static com.searise.sof.analyse.AnalysisHelper.isEqualTo;

public class ResolveAttribute implements Rule<LogicalPlan> {
    @Override
    public LogicalPlan apply(LogicalPlan plan) {
        return plan.transformUp((Applicable<LogicalPlan>) p -> {
            if (p.children().stream().allMatch(AnalysisHelper::resolved) && !p.resolved()) {
                String planType = p.getClass().getSimpleName();
                switch (planType) {
                    case "Project":
                        Project project = (Project) p;
                        List<Attribute> childSchema = project.child.schema();
                        List<Expression> newProjectList = Utils.toImmutableList(project.projectList.stream().
                                map(expr -> expr.transformUp(new AttributeApplicable(childSchema, project.context))));
                        if (!isEqualTo(project.projectList, newProjectList)) {
                            return new Project(newProjectList, project.child, project.context);
                        }
                        break;
                    case "Filter":
                        Filter filter = (Filter) p;
                        childSchema = filter.child.schema();
                        List<Expression> newConditions = Utils.toImmutableList(filter.conditions.stream().
                                map(expr -> expr.transformUp(new AttributeApplicable(childSchema, filter.context))));
                        if (!isEqualTo(filter.conditions, newConditions)) {
                            return new Filter(newConditions, filter.child, filter.context);
                        }
                        break;
                    case "InnerJoin":
                        InnerJoin join = (InnerJoin) p;
                        List<Attribute> childrenSchema = Utils.combineDistinct(join.left.schema(), join.right.schema());
                        newConditions = Utils.toImmutableList(join.conditions.stream().
                                map(expr -> expr.transformUp(new AttributeApplicable(childrenSchema, join.context))));
                        if (!isEqualTo(join.conditions, newConditions)) {
                            return new InnerJoin(join.left, join.right, newConditions, join.context);
                        }
                        break;
                    default:
                }
            }
            return p;
        });
    }

    private class AttributeApplicable implements Applicable<Expression> {
        private final List<Attribute> childrenSchema;
        private final SofContext context;

        public AttributeApplicable(List<Attribute> childrenSchema, SofContext context) {
            this.childrenSchema = childrenSchema;
            this.context = context;
        }

        @Override
        public Expression apply(Expression expression) {
            if (expression.children().stream().allMatch(AnalysisHelper::resolved) &&
                    !expression.resolved()) {
                if (expression.getClass() == UnresolvedAttribute.class) {
                    return applyUnresolvedAttribute((UnresolvedAttribute) expression);
                } else if (expression.getClass() == Alias.class) {
                    Alias alias = (Alias) expression;
                    AttributeBase aliasName = alias.attribute;
                    long aliasExprId;
                    // 如果是attr as alias, 那么alias和attr可以共用exprId, 后面可以消除alias.
                    if (alias.child.getClass() == Attribute.class) {
                        aliasExprId = ((Attribute) alias.child).exprId;
                    } else {
                        aliasExprId = context.exprIdBuilder.newExprId();
                    }
                    return new Alias(new Attribute(aliasName.table, aliasName.name, aliasExprId, alias.child.dataType()), alias.child);
                } else {
                    // just else
                }
            }
            return expression;
        }

        private Expression applyUnresolvedAttribute(UnresolvedAttribute unresolvedAttribute) {
            return withSchema(unresolvedAttribute, childrenSchema);
        }

        private Expression withSchema(UnresolvedAttribute unresolvedAttribute, List<Attribute> schema) {
            for (Attribute attribute : schema) {
                if (StringUtils.equals(attribute.name, unresolvedAttribute.name)) {
                    Optional<String> table1 = unresolvedAttribute.table;
                    Optional<String> table2 = attribute.table;
                    if (!table1.isPresent() || !table2.isPresent()) {
                        return attribute;
                    } else if (StringUtils.equals(table1.get(), table2.get())) {
                        return attribute;
                    } else {
                        // just else.
                    }
                }
            }
            return unresolvedAttribute;
        }
    }
}
