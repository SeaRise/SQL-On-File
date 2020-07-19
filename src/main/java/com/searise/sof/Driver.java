package com.searise.sof;

import com.searise.sof.analyse.Analyzer;
import com.searise.sof.catalog.BuiltInCatalog;
import com.searise.sof.catalog.Catalog;
import com.searise.sof.core.Context;
import com.searise.sof.core.Utils;
import com.searise.sof.execution.Builder;
import com.searise.sof.execution.Executor;
import com.searise.sof.execution.ResultExec;
import com.searise.sof.optimize.Optimizer;
import com.searise.sof.parser.SqlParser;
import com.searise.sof.plan.logic.LogicalPlan;
import com.searise.sof.plan.physics.PhysicalPlan;
import com.searise.sof.plan.runnable.RunnableCommand;

import java.io.IOException;

import static com.searise.sof.optimize.Optimizer.newOptimizer;

public class Driver {
    private final Catalog catalog = new BuiltInCatalog();
    private final Context context = new Context();
    private final SqlParser sqlParser = new SqlParser(context);
    private final Analyzer analyzer = new Analyzer(catalog);
    private final Optimizer optimizer = newOptimizer();
    private final Builder builder = new Builder();

    public void compile(String sqls) throws IOException {
        for (String sql : Utils.split(Utils.removeComments(sqls))) {
            doCompile(sql);
        }
    }

    private void doCompile(String sql) {
        LogicalPlan parsePlan = sqlParser.parsePlan(sql);
        if (parsePlan instanceof RunnableCommand) {
            RunnableCommand command = (RunnableCommand) parsePlan;
            command.run(catalog);
            return;
        }
        LogicalPlan analyzePlan = analyzer.analyse(parsePlan);
        PhysicalPlan physicalPlan = optimizer.optimize(analyzePlan);
        Executor executor = builder.build(physicalPlan);
        execute(executor);
    }

    private void execute(Executor executor) {
        executor.open();
        executor.close();
        if (executor.getClass() == ResultExec.class) {
            Utils.println("result:");
            Utils.println(((ResultExec) executor).result());
        }
    }
}
