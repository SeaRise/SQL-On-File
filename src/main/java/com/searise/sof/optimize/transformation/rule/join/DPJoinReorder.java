package com.searise.sof.optimize.transformation.rule.join;

import com.google.common.collect.ImmutableList;
import com.searise.sof.core.Conf;
import com.searise.sof.optimize.GroupExpr;
import com.searise.sof.optimize.Operand;
import com.searise.sof.optimize.transformation.ExprIter;
import com.searise.sof.optimize.transformation.Pattern;
import com.searise.sof.optimize.transformation.TransformationRule;

import java.util.List;

public class DPJoinReorder implements TransformationRule {
    @Override
    public Pattern pattern() {
        return new Pattern(Operand.OperandMultiJoin);
    }

    @Override
    public List<GroupExpr> onTransform(ExprIter exprIter) {
        int greedyJoinReorderThreshold = exprIter.getValue().exprNode.context().conf.getIntConf(Conf.GREEDY_JOIN_REORDER_THRESHOLD);
        if (exprIter.getValue().children.size() >= greedyJoinReorderThreshold) {
            return ImmutableList.of();
        }

        return ImmutableList.of();
    }
}
