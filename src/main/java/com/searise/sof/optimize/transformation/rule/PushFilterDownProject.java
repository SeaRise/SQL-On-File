package com.searise.sof.optimize.transformation.rule;

import com.searise.sof.optimize.GroupExpr;
import com.searise.sof.optimize.Operand;
import com.searise.sof.optimize.transformation.ExprIter;
import com.searise.sof.optimize.transformation.Pattern;
import com.searise.sof.optimize.transformation.TransformationRule;
import com.searise.sof.plan.logic.Filter;
import com.searise.sof.plan.logic.Project;

import java.util.List;

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

        

        return null;
    }
}
