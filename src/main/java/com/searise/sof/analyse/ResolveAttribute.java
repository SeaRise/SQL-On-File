package com.searise.sof.analyse;

import com.google.common.collect.ImmutableList;
import com.searise.sof.core.Context;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.Alias;
import com.searise.sof.expression.attribute.Attribute;
import com.searise.sof.expression.attribute.AttributeBase;
import com.searise.sof.expression.attribute.UnresolvedAttribute;
import com.searise.sof.plan.logic.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.searise.sof.analyse.AnalysisHelper.isEqualTo;

public class ResolveAttribute implements Rule {
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
                                map(expr -> expr.transformUp(new AttributeApplicable(ImmutableList.of(childSchema), Optional.empty(), project.context))));
                        if (!isEqualTo(project.projectList, newProjectList)) {
                            return new Project(newProjectList, project.child, project.context);
                        }
                        break;
                    case "Filter":
                        Filter filter = (Filter) p;
                        childSchema = filter.child.schema();
                        List<Expression> newConditions = Utils.toImmutableList(filter.conditions.stream().
                                map(expr -> expr.transformUp(new AttributeApplicable(ImmutableList.of(childSchema), Optional.empty(), filter.context))));
                        if (!isEqualTo(filter.conditions, newConditions)) {
                            return new Filter(newConditions, filter.child, filter.context);
                        }
                        break;
                    case "InnerJoin":
                        InnerJoin join = (InnerJoin) p;
                        Optional<List<String>> childAliases = getJoinChildAliases(join);
                        if (!childAliases.isPresent()) {
                            break;
                        }
                        List<List<Attribute>> childSchemas = ImmutableList.of(join.left.schema(), join.right.schema());
                        newConditions = Utils.toImmutableList(join.conditions.stream().
                                map(expr -> expr.transformUp(new AttributeApplicable(childSchemas, childAliases, join.context))));
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

    private Optional<List<String>> getJoinChildAliases(InnerJoin join) {
        Optional<String> left = getAlias(join.left);
        if (!left.isPresent()) {
            return Optional.empty();
        }
        Optional<String> right = getAlias(join.right);
        if (!right.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(ImmutableList.of(left.get(), right.get()));
    }

    private Optional<String> getAlias(LogicalPlan logicalPlan) {
        if (logicalPlan.getClass() == Relation.class) {
            Relation relation = (Relation) logicalPlan;
            return Optional.of(relation.referenceName.orElse(relation.tableName));
        } else if (logicalPlan.getClass() == SubqueryAlias.class) {
            SubqueryAlias subqueryAlias = (SubqueryAlias) logicalPlan;
            return Optional.of(subqueryAlias.name);
        } else {
            return Optional.empty();
        }
    }

    private class AttributeApplicable implements Applicable<Expression> {
        private final List<List<Attribute>> childSchemas;
        private final Optional<List<String>> childAliases;
        private final Context context;

        public AttributeApplicable(List<List<Attribute>> childSchemas, Optional<List<String>> childAliases, Context context) {
            this.childSchemas = childSchemas;
            this.childAliases = childAliases;
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
                    return new Alias(new Attribute(aliasName.table, aliasName.name, context.exprIdBuilder.newExprId(), alias.child.dataType()), alias.child);
                } else {
                    // just else
                }
            }
            return expression;
        }

        private Expression applyUnresolvedAttribute(UnresolvedAttribute unresolvedAttribute) {
            if (childSchemas.size() == 1) {
                return withSchema(unresolvedAttribute, childSchemas.get(0));
            } else {
                List<Attribute> combineList;
                if (unresolvedAttribute.table.isPresent()) {
                    if (!childAliases.isPresent()) {
                        return unresolvedAttribute;
                    }
                    int index = childAliases.get().indexOf(unresolvedAttribute.table.get());
                    if (!(index >= 0 && index < childAliases.get().size())) {
                        return unresolvedAttribute;
                    }
                    combineList = childSchemas.get(index);
                } else {
                    combineList = Utils.toImmutableList(childSchemas.stream().
                            flatMap((Function<List<Attribute>, Stream<Attribute>>) Collection::stream));
                }
                return withSchema(unresolvedAttribute, combineList);
            }
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
