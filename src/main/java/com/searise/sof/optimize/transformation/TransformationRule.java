package com.searise.sof.optimize.transformation;

import com.google.common.collect.ImmutableMap;
import com.searise.sof.optimize.GroupExpr;
import com.searise.sof.optimize.Operand;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TransformationRule {
    Pattern pattern();

    Optional<GroupExpr> onTransform(GroupExpr groupExpr);

    Map<Operand, List<TransformationRule>> transformationRuleMap = ImmutableMap.of();
}