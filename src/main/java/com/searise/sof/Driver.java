package com.searise.sof;

import com.searise.sof.analyse.Analyzer;
import com.searise.sof.catalog.BuiltInCatalog;
import com.searise.sof.catalog.Catalog;
import com.searise.sof.execution.Builder;
import com.searise.sof.execution.Executor;
import com.searise.sof.optimize.Optimizer;
import com.searise.sof.parser.SqlParser;
import com.searise.sof.plan.logic.LogicalPlan;
import com.searise.sof.plan.physics.PhysicalPlan;

public class Driver {
    private final Catalog catalog = new BuiltInCatalog();

    public void compile(String sql) {
        LogicalPlan parsePlan = new SqlParser().parsePlan(sql);
        LogicalPlan analyzePlan = new Analyzer(catalog).analyse(parsePlan);
        PhysicalPlan physicalPlan = new Optimizer().optimize(analyzePlan);
        Executor executor = new Builder().build(physicalPlan);
        executor.execute();
    }
}
