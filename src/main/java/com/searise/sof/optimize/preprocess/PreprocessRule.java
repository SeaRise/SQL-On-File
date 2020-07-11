package com.searise.sof.optimize.preprocess;

import com.google.common.collect.ImmutableList;
import com.searise.sof.analyse.Rule;

import java.util.List;

public interface PreprocessRule extends Rule {
    List<PreprocessRule> preprocessRules = ImmutableList.of(
            new RemoveSubqueryAlias()
    );
}
