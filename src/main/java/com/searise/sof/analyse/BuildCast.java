package com.searise.sof.analyse;

import com.searise.sof.core.Utils;
import com.searise.sof.expression.Cast;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.ScalarFunction;
import com.searise.sof.plan.logic.Filter;
import com.searise.sof.plan.logic.InnerJoin;
import com.searise.sof.plan.logic.LogicalPlan;
import com.searise.sof.plan.logic.Project;
import com.searise.sof.type.DataType;
import com.searise.sof.type.TypeGroup;

import java.util.List;

import static com.searise.sof.analyse.AnalysisHelper.isEqualTo;

public class BuildCast implements Rule<LogicalPlan> {
    @Override
    public LogicalPlan apply(LogicalPlan plan) {
        return plan.transformUp((Applicable<LogicalPlan>) p -> {
            if (p.children().stream().allMatch(AnalysisHelper::resolved) && !p.resolved()) {
                String planType = p.getClass().getSimpleName();
                switch (planType) {
                    case "Project":
                        Project project = (Project) p;
                        List<Expression> newProjectList = Utils.toImmutableList(project.projectList.stream().
                                map(expr -> expr.transformUp(new BuildCastApplicable())));
                        if (!isEqualTo(project.projectList, newProjectList)) {
                            return new Project(newProjectList, project.child, project.context);
                        }
                        break;
                    case "Filter":
                        Filter filter = (Filter) p;
                        List<Expression> newConditions = Utils.toImmutableList(filter.conditions.stream().
                                map(expr -> expr.transformUp(new BuildCastApplicable())));
                        if (!isEqualTo(filter.conditions, newConditions)) {
                            return new Filter(newConditions, filter.child, filter.context);
                        }
                        break;
                    case "InnerJoin":
                        InnerJoin join = (InnerJoin) p;
                        newConditions = Utils.toImmutableList(join.conditions.stream().
                                map(expr -> expr.transformUp(new BuildCastApplicable())));
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

    private class BuildCastApplicable implements Applicable<Expression> {

        @Override
        public Expression apply(Expression expression) {
            if (expression.getClass() == ScalarFunction.class &&
                    expression.children().stream().allMatch(AnalysisHelper::resolved)) {
                ScalarFunction func = (ScalarFunction) expression;
                List<DataType> dataTypes = Utils.toImmutableList(func.params.stream().map(Expression::dataType));
                DataType topDataType = TypeGroup.getTopType(dataTypes);
                List<Expression> newParams = Utils.toImmutableList(func.params.stream().map(p -> Cast.buildCast(p, topDataType)));
                return new ScalarFunction(func.funcName, newParams);
            }
            return expression;
        }
    }
}
