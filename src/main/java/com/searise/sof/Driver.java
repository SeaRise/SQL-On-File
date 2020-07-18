package com.searise.sof;

import com.searise.sof.analyse.Analyzer;
import com.searise.sof.catalog.BuiltInCatalog;
import com.searise.sof.catalog.Catalog;
import com.searise.sof.core.Context;
import com.searise.sof.core.Utils;
import com.searise.sof.execution.Builder;
import com.searise.sof.execution.Executor;
import com.searise.sof.execution.ResultExec;
import com.searise.sof.parser.SqlParser;
import com.searise.sof.plan.ddl.DDLCommand;
import com.searise.sof.plan.logic.LogicalPlan;
import com.searise.sof.plan.physics.PhysicalPlan;

import java.io.IOException;

import static com.searise.sof.optimize.Optimizer.newOptimizer;

public class Driver {
    private final Catalog catalog = new BuiltInCatalog();
    private final Context context = new Context();

    public void compile(String sqls) throws IOException {
        for (String sql : Utils.split(Utils.removeComments(sqls))) {
            doCompile(sql);
        }
    }

    private void doCompile(String sql) {
        LogicalPlan parsePlan = new SqlParser(context).parsePlan(sql);
        if (parsePlan instanceof DDLCommand) {
            DDLCommand command = (DDLCommand) parsePlan;
            command.run(catalog);
            return;
        }
        LogicalPlan analyzePlan = new Analyzer(catalog).analyse(parsePlan);
        PhysicalPlan physicalPlan = newOptimizer().optimize(analyzePlan);
        Executor executor = new Builder().build(physicalPlan);
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
