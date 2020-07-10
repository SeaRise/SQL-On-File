package com.searise.sof.analyse;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.searise.sof.catalog.Catalog;
import com.searise.sof.plan.logic.LogicalPlan;

import java.util.List;

public class Analyzer {
    private final List<Rule> rules;
    public Analyzer(Catalog catalog) {
        rules = ImmutableList.of(new ResolveReferences(catalog));
    }

    public LogicalPlan analyse(LogicalPlan parsePlan) {
        LogicalPlan preApplyPlan = parsePlan;
        LogicalPlan applyingPlan = parsePlan;
        while (true) {
            for (Rule rule : rules) {
                applyingPlan = rule.apply(applyingPlan);
            }
            if (preApplyPlan == applyingPlan) {
                Preconditions.checkArgument(applyingPlan.resolved(), "logical plan has not yet resolved");
                return applyingPlan;
            }
            preApplyPlan = applyingPlan;
        }
    }
}
