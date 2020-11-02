package com.searise.sof;

import com.searise.sof.analyse.Analyzer;
import com.searise.sof.catalog.BuiltInCatalog;
import com.searise.sof.catalog.Catalog;
import com.searise.sof.core.Context;
import com.searise.sof.core.Utils;
import com.searise.sof.execution.Builder;
import com.searise.sof.execution.Executor;
import com.searise.sof.execution.ResultExec;
import com.searise.sof.execution.RowIterator;
import com.searise.sof.optimize.Optimizer;
import com.searise.sof.parser.SqlParser;
import com.searise.sof.plan.logic.LogicalPlan;
import com.searise.sof.plan.physics.PhysicalPlan;
import com.searise.sof.plan.runnable.RunnableCommand;

import static com.searise.sof.optimize.Optimizer.newOptimizer;

public class Driver {
    public final Catalog catalog = new BuiltInCatalog();
    public final Context context = new Context(catalog, this);
    public final SqlParser sqlParser = new SqlParser(context);
    public final Analyzer analyzer = new Analyzer(catalog);
    public final Optimizer optimizer = newOptimizer();
    public final Builder builder = new Builder(context);

    public void compile(String sqls) throws Exception {
        for (String sql : Utils.split(Utils.removeComments(sqls))) {
            doCompile(sql);
        }
    }

    private void doCompile(String sql) throws Exception {
        LogicalPlan parsePlan = sqlParser.parsePlan(sql);
        if (parsePlan instanceof RunnableCommand) {
            RunnableCommand command = (RunnableCommand) parsePlan;
            command.run(catalog);
            Utils.println("ok");
            return;
        }
        LogicalPlan analyzePlan = analyzer.analyse(parsePlan);
        PhysicalPlan physicalPlan = optimizer.optimize(analyzePlan);
        Executor executor = builder.build(physicalPlan);
        execute(executor);
    }

    private void execute(Executor executor) {
        if (executor.getClass() != ResultExec.class) {
            executor = new ResultExec(executor, context);
        }

        RowIterator rowIterator = executor.compute(0);
        rowIterator.open();
        rowIterator.close();
        Utils.println("ok");
        Utils.println(((ResultExec) executor).result());
    }
}