package com.searise.sof.optimize.preprocess;

import com.google.common.collect.ImmutableList;
import com.searise.sof.analyse.Rule;
import com.searise.sof.plan.logic.LogicalPlan;

import java.util.List;

public interface PreprocessRule extends Rule<LogicalPlan> {
    List<PreprocessRule> preprocessRules = ImmutableList.of(
            new RemoveSubqueryAlias()
            , new RemoveUselessAlias()
    );
}
