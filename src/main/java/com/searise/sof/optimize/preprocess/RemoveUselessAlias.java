package com.searise.sof.optimize.preprocess;

import com.searise.sof.analyse.Applicable;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.Alias;
import com.searise.sof.expression.attribute.Attribute;
import com.searise.sof.plan.logic.LogicalPlan;
import com.searise.sof.plan.logic.Project;

import java.util.List;

import static com.searise.sof.analyse.AnalysisHelper.isEqualTo;

public class RemoveUselessAlias implements PreprocessRule {
    @Override
    public LogicalPlan apply(LogicalPlan plan) {
        return plan.transformDown((Applicable<LogicalPlan>) p -> {
            if (p.getClass() == Project.class) {
                Project project = (Project) p;
                List<Expression> newProjectList = Utils.toImmutableList(project.projectList.stream().map(expr ->
                        expr.getClass() == Alias.class && expr.children().get(0).getClass() == Attribute.class ? expr.children().get(0) : expr)
                );
                if (!isEqualTo(project.projectList, newProjectList)) {
                    return new Project(newProjectList, project.child, project.context);
                }
            }
            return p;
        });
    }
}
