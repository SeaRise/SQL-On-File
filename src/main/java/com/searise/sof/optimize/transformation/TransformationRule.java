package com.searise.sof.optimize.transformation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.searise.sof.optimize.GroupExpr;
import com.searise.sof.optimize.Operand;
import com.searise.sof.optimize.transformation.rule.EliminateProjection;

import java.util.List;
import java.util.Map;

public interface TransformationRule {
    Pattern pattern();

    // 需要确保转换后plan更优.
    List<GroupExpr> onTransform(ExprIter exprIter);

    Map<Operand, List<TransformationRule>> transformationRuleMap = ImmutableMap.of(
            Operand.OperandProject, ImmutableList.of(new EliminateProjection())
    );
}