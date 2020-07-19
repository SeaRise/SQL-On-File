package com.searise.sof.analyse;

import com.google.common.collect.ImmutableList;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.Literal;
import com.searise.sof.expression.attribute.Alias;
import com.searise.sof.plan.logic.Filter;
import com.searise.sof.plan.logic.InnerJoin;
import com.searise.sof.plan.logic.LogicalPlan;
import com.searise.sof.plan.logic.Project;

import java.util.List;
import java.util.Optional;

import static com.searise.sof.analyse.AnalysisHelper.isEqualTo;
import static com.searise.sof.core.row.EmptyRow.EMPTY_ROW;
import static com.searise.sof.expression.Expression.getBooleanLiteralValue;

public class FoldExpression implements Rule {
    @Override
    public LogicalPlan apply(LogicalPlan plan) {
        return plan.transformUp((Applicable<LogicalPlan>) p -> {
            String planType = p.getClass().getSimpleName();
            switch (planType) {
                case "Project":
                    Project project = (Project) p;
                    List<Expression> newProjectList = Utils.toImmutableList(project.projectList.stream().
                            map(expr -> expr.transformUp(new FoldApplicable())));
                    if (!isEqualTo(project.projectList, newProjectList)) {
                        return new Project(newProjectList, project.child, project.context);
                    }
                    break;
                case "Filter":
                    Filter filter = (Filter) p;
                    List<Expression> newConditions = foldConditions(filter.conditions);
                    if (!isEqualTo(filter.conditions, newConditions)) {
                        return new Filter(newConditions, filter.child, filter.context);
                    }
                    break;
                case "InnerJoin":
                    InnerJoin join = (InnerJoin) p;
                    newConditions = foldConditions(join.conditions);
                    if (!isEqualTo(join.conditions, newConditions)) {
                        return new InnerJoin(join.left, join.right, newConditions, join.context);
                    }
                    break;
                default:
            }
            return p;
        });
    }

    private List<Expression> foldConditions(List<Expression> conditions) {
        List<Expression> newConditions = Utils.toImmutableList(conditions.stream().
                map(expr -> expr.transformUp(new FoldApplicable())));
        ImmutableList.Builder<Expression> foldCondBuilder = ImmutableList.builder();
        for (Expression newCond : newConditions) {
            Optional<Boolean> bool = getBooleanLiteralValue(newCond);
            if (bool.isPresent()) {
                // 如果其中一个是false,那全部是false.
                if (!bool.get()) {
                    return ImmutableList.of(newCond);
                }
                continue;
            }
            foldCondBuilder.add(newCond);
        }
        return foldCondBuilder.build();
    }

    private class FoldApplicable implements Applicable<Expression> {

        @Override
        public Expression apply(Expression expression) {
            return expression.transformDown((Applicable<Expression>) expr -> {
                // 对于literal和Alias(literal)的情况,return expr, 避免analyse无法结束.
                if (expr.getClass() == Literal.class ||
                        expr.getClass() == Alias.class && expr.children().get(0).getClass() == Literal.class) {
                    return expr;
                } else if (expr.foldable()) {
                    Object value = expr.eval(EMPTY_ROW);
                    Literal bool = new Literal(expr.dataType(), value);
                    if (expr.getClass() == Alias.class) {
                        return new Alias(((Alias) expr).attribute, bool);
                    }
                    return bool;
                }
                // else.
                return expr;
            });
        }
    }
}
