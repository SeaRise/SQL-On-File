package com.searise.sof.analyse;

import com.google.common.collect.ImmutableList;
import com.searise.sof.catalog.Catalog;
import com.searise.sof.core.Utils;
import com.searise.sof.plan.logic.LogicalPlan;

import java.util.List;

public class Analyzer {
    private final List<Rule> rules;

    public Analyzer(Catalog catalog) {
        rules = ImmutableList.of(
                new AddAlias()
                , new ResolveRelation(catalog)
                , new ResolveScalarFunction()
                , new FoldExpression()
                , new PushDownNot()
                , new SplitCNF()
                , new ResolveAttribute()
        );
    }

    public LogicalPlan analyse(LogicalPlan parsePlan) {
        LogicalPlan preApplyPlan = parsePlan;
        LogicalPlan applyingPlan = parsePlan;
        while (true) {
            for (Rule rule : rules) {
                applyingPlan = rule.apply(applyingPlan);
            }
            if (preApplyPlan == applyingPlan) {
                Utils.checkArgument(applyingPlan.resolved(), "logical plan has not yet resolved: \n" + applyingPlan.visitToString());
                return applyingPlan;
            }
            preApplyPlan = applyingPlan;
        }
    }
}
