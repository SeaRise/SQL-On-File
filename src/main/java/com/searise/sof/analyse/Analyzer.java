package com.searise.sof.analyse;

import com.google.common.collect.ImmutableList;
import com.searise.sof.catalog.Catalog;
import com.searise.sof.core.Utils;
import com.searise.sof.plan.logic.LogicalPlan;

import java.util.List;

public class Analyzer {
    private final List<List<Rule>> ruleBatches;

    public Analyzer(Catalog catalog) {
        ruleBatches = ImmutableList.of(
                ImmutableList.of(
                        new AddAlias()
                        , new ResolveRelation(catalog)
                        , new ResolveAttribute()
                        , new BuildCast()
                        , new ResolveScalarFunction()
                )
                , ImmutableList.of(new FoldExpression(), new PushDownNot(), new SplitCNF())
        );
    }

    public LogicalPlan analyse(LogicalPlan parsePlan) {
        AnalysisHelper preApplyPlan = parsePlan;
        AnalysisHelper applyingPlan = parsePlan;
        for (List<Rule> batch : ruleBatches) {
            while (true) {
                for (Rule rule : batch) {
                    applyingPlan = rule.apply(applyingPlan);
                }
                if (preApplyPlan == applyingPlan) {
                    break;
                }
                preApplyPlan = applyingPlan;
            }
        }

        LogicalPlan analyzedPlan = (LogicalPlan) applyingPlan;
        Utils.checkArgument(applyingPlan.resolved(), "logical plan has not yet resolved: \n" + analyzedPlan.visitToString());
        return analyzedPlan;
    }
}
