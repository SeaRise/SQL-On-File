package com.searise.sof.optimize.transformation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.searise.sof.optimize.GroupExpr;
import com.searise.sof.optimize.Operand;
import com.searise.sof.optimize.transformation.rule.*;

import java.util.List;
import java.util.Map;

public interface TransformationRule {
    Pattern pattern();

    // 需要确保转换后plan更优.
    List<GroupExpr> onTransform(ExprIter exprIter);

    Map<Operand, List<TransformationRule>> defaultTransformationRuleMap = ImmutableMap.of(
            Operand.OperandProject, ImmutableList.of(new EliminateProject(), new MergeAdjacentProject())
            , Operand.OperandFilter, ImmutableList.of(new PushFilterDownProject(), new PushFilterDownJoin(), new MergeAdjacentFilter())
    );

    // todo
    //  简单的join reorder
    List<Map<Operand, List<TransformationRule>>> transformationRuleBatches = ImmutableList.of(
            defaultTransformationRuleMap
    );
}