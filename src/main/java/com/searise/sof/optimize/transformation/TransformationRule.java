package com.searise.sof.optimize.transformation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.searise.sof.optimize.GroupExpr;
import com.searise.sof.optimize.Operand;
import com.searise.sof.optimize.transformation.rule.*;
import com.searise.sof.optimize.transformation.rule.join.*;

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

    Map<Operand, List<TransformationRule>> prepareJoinReorderTransformationRuleMap = ImmutableMap.of(
            Operand.OperandJoin,
            ImmutableList.of(
                    new TransformLeafJoinToMultiJoin(), new MergeJoinOneSideMultiJoin(0),
                    new MergeJoinOneSideMultiJoin(1), new MergeJoinTwoSideMultiJoin()
            )
    );

    Map<Operand, List<TransformationRule>> joinReorderTransformationRuleMap = ImmutableMap.of(
            Operand.OperandMultiJoin, ImmutableList.of(new DPJoinReorder(), new GreedyJoinReorder())
    );

    // todo
    //  简单的join reorder
    List<Map<Operand, List<TransformationRule>>> transformationRuleBatches = ImmutableList.of(
            defaultTransformationRuleMap
            , prepareJoinReorderTransformationRuleMap
            , joinReorderTransformationRuleMap
    );
}