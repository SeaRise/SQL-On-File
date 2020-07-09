package com.searise.sof.analyse;

import com.searise.sof.catalog.Catalog;
import com.searise.sof.plan.logic.LogicalPlan;

public class Analyzer {
    private final Catalog catalog;
    public Analyzer(Catalog catalog) {
        this.catalog = catalog;
    }

    public LogicalPlan analyse(LogicalPlan parsePlan) {
        return parsePlan;
    }
}
