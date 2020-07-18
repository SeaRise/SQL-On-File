package com.searise.sof.analyse;

import com.searise.sof.core.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.ScalarFunction;
import com.searise.sof.expression.attribute.Alias;
import com.searise.sof.expression.attribute.UnresolvedAttribute;
import com.searise.sof.plan.logic.LogicalPlan;
import com.searise.sof.plan.logic.Project;

import java.util.List;

import static com.searise.sof.analyse.AnalysisHelper.isEqualTo;

// 给project所有的func加上alias,用于后面resolve index.
public class AddAlias implements Rule {
    private final AddAliasApplicable addAliasApplicable = new AddAliasApplicable();

    @Override
    public LogicalPlan apply(LogicalPlan plan) {
        return plan.transformUp((Applicable<LogicalPlan>) p -> {
            if (p.children().stream().allMatch(AnalysisHelper::resolved) && !p.resolved()) {
                if (p.getClass() == Project.class) {
                    Project project = (Project) p;
                    List<Expression> newProjectList = Utils.toImmutableList(project.projectList.stream().
                            map(addAliasApplicable::apply));
                    if (!isEqualTo(project.projectList, newProjectList)) {
                        return new Project(newProjectList, project.child);
                    }
                }
            }
            return p;
        });
    }

    private class AddAliasApplicable implements Applicable<Expression> {

        @Override
        public Expression apply(Expression expr) {
            if (expr.getClass() == ScalarFunction.class) {
                return new Alias(UnresolvedAttribute.UnknownUnresolvedAttribute, expr);
            }
            return expr;
        }
    }
}
