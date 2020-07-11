package com.searise.sof.analyse;

import com.searise.sof.common.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.ScalarFunction;
import com.searise.sof.expression.compare.*;
import com.searise.sof.expression.logic.And;
import com.searise.sof.expression.logic.Not;
import com.searise.sof.expression.logic.Or;
import com.searise.sof.expression.operator.*;
import com.searise.sof.plan.logic.Filter;
import com.searise.sof.plan.logic.InnerJoin;
import com.searise.sof.plan.logic.LogicalPlan;
import com.searise.sof.plan.logic.Project;
import com.searise.sof.type.DataType;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static com.searise.sof.analyse.AnalysisHelper.isEqualTo;
import static com.searise.sof.expression.Function.*;
import static com.searise.sof.type.DataType.*;

public class ResolveScalarFunction implements Rule {
    @Override
    public LogicalPlan apply(LogicalPlan plan) {
        return plan.transformUp((Applicable<LogicalPlan>) p -> {
            if (p.children().stream().allMatch(AnalysisHelper::resolved) && !p.resolved()) {
                String planType = p.getClass().getSimpleName();
                switch (planType) {
                    case "Project":
                        Project project = (Project) p;
                        List<Expression> newProjectList = Utils.toImmutableList(project.projectList.stream().
                                map(expr -> expr.transformUp(new ScalarFunctionApplicable())));
                        if (!isEqualTo(project.projectList, newProjectList)) {
                            return new Project(newProjectList, project.child);
                        }
                        break;
                    case "Filter":
                        Filter filter = (Filter) p;
                        List<Expression> newConditions = Utils.toImmutableList(filter.conditions.stream().
                            map(expr -> expr.transformUp(new ScalarFunctionApplicable())));
                        if (!isEqualTo(filter.conditions, newConditions)) {
                            return new Filter(newConditions, filter.child);
                        }
                        break;
                    case "InnerJoin":
                        InnerJoin join = (InnerJoin) p;
                        newConditions = Utils.toImmutableList(join.conditions.stream().
                                map(expr -> expr.transformUp(new ScalarFunctionApplicable())));
                        if (!isEqualTo(join.conditions, newConditions)) {
                            return new InnerJoin(join.left, join.right, newConditions);
                        }
                        break;
                    default:
                }
            }
            return p;
        });
    }

    private class ScalarFunctionApplicable implements Applicable<Expression> {

        @Override
        public Expression apply(Expression expression) {
            if (expression.getClass() == ScalarFunction.class &&
                    expression.children().stream().allMatch(AnalysisHelper::resolved) &&
                    !expression.resolved()) {
                ScalarFunction func = (ScalarFunction) expression;
                switch (func.funcName) {
                    // logic
                    case AND:
                    case OR:
                        if (func.children().size() == 2 &&
                                func.children().stream().allMatch(child -> child.dataType() == BooleanType)) {
                            if (StringUtils.equals(func.funcName, AND)) {
                                return new And(func.children().get(0), func.children().get(1));
                            } else {
                                return new Or(func.children().get(0), func.children().get(1));
                            }
                        }
                    case NOT_1:
                    case NOT_2:
                        if (func.children().size() == 1 &&
                                func.children().stream().allMatch(child -> child.dataType() == BooleanType)) {
                            return new Not(func.children().get(0));
                        }

                    // compare
                    case EQUAL_TO_1:
                    case EQUAL_TO_2:
                        if (checkBinary(func)) {
                            return new EqualTo(func.children().get(0), func.children().get(1));
                        }
                    case LESS_THAN:
                        if (checkBinary(func)) {
                            return new LessThan(func.children().get(0), func.children().get(1));
                        }
                    case LESS_THAN_OR_EQUAL_1:
                    case LESS_THAN_OR_EQUAL_2:
                        if (checkBinary(func)) {
                            return new LessThanOrEqual(func.children().get(0), func.children().get(1));
                        }
                    case GREATER_THAN:
                        if (checkBinary(func)) {
                            return new GreaterThan(func.children().get(0), func.children().get(1));
                        }
                    case GREATER_THAN_OR_EQUAL_1:
                    case GREATER_THAN_OR_EQUAL_2:
                        if (checkBinary(func)) {
                            return new GreaterThanOrEqual(func.children().get(0), func.children().get(1));
                        }
                    case NOT_EQUAL_TO_1:
                    case NOT_EQUAL_TO_2:
                        if (checkBinary(func)) {
                            return new Not(new EqualTo(func.children().get(0), func.children().get(1)));
                        }

                    // operator
                    case PLUS:
                        if (checkBinary(func) && isOperatorDateType(func.children().get(0).dataType())) {
                            return new Add(func.children().get(0), func.children().get(1));
                        }
                    case MINUS:
                        if (isOperatorDateType(func.children().get(0).dataType())) {
                            if (checkBinary(func)) {
                                return new Divide(func.children().get(0), func.children().get(1));
                            } else if (func.children().size() == 1) {
                                return new UnaryMinus(func.children().get(0));
                            }
                        }
                    case ASTERISK:
                        if (checkBinary(func) && isOperatorDateType(func.children().get(0).dataType())) {
                            return new Multiply(func.children().get(0), func.children().get(1));
                        }
                    case SLASH:
                        if (checkBinary(func) && isOperatorDateType(func.children().get(0).dataType())) {
                            return new Subtract(func.children().get(0), func.children().get(1));
                        }
                    case PERCENT:
                        if (checkBinary(func) && isOperatorDateType(func.children().get(0).dataType())) {
                            return new Remainder(func.children().get(0), func.children().get(1));
                        }

                    default:
                }
            }
            return expression;
        }
    }

    private boolean isOperatorDateType(DataType dataType) {
        return dataType == IntegerType || dataType == FloatType;
    }

    // Strong type match
    private boolean checkBinary(ScalarFunction func) {
        return (func.children().size() == 2 &&
                func.children().get(0).dataType() == func.children().get(0).dataType());
    }
}
