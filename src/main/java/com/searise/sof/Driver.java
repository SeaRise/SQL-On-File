package com.searise.sof;

import com.searise.sof.analyse.Analyzer;
import com.searise.sof.catalog.BuiltInCatalog;
import com.searise.sof.catalog.Catalog;
import com.searise.sof.core.Context;
import com.searise.sof.core.Utils;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.execution.Builder;
import com.searise.sof.optimize.Optimizer;
import com.searise.sof.parser.SqlParser;
import com.searise.sof.plan.logic.LogicalPlan;
import com.searise.sof.plan.physics.PhysicalPlan;
import com.searise.sof.plan.runnable.RunnableCommand;

import java.util.concurrent.atomic.AtomicInteger;

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
        execute(physicalPlan);
    }

    private void execute(PhysicalPlan plan) {
        StringBuffer resultBuilder = new StringBuffer();
        AtomicInteger totalRowCount = new AtomicInteger(0);
        long start = System.currentTimeMillis();

        context.runPlan(plan, (partition, iterator) -> {
            StringBuilder partitionResultBuilder = new StringBuilder();
            int rowCount = 0;
            while (iterator.hasNext()) {
                InternalRow row = iterator.next();
                for (int index = 0; index < row.numFields() - 1; index++) {
                    partitionResultBuilder.append(row.getValue(index)).append("\t");
                }
                partitionResultBuilder.append(row.getValue(row.numFields() - 1)).append("\n");
                rowCount++;
            }
            totalRowCount.addAndGet(rowCount);
            resultBuilder.append(partitionResultBuilder.toString());
        });

        long end = System.currentTimeMillis();
        long useSecond = (end - start) / 1000;
        resultBuilder.append("Time taken: ").append(useSecond).
                append(" seconds, Fetched: ").append(totalRowCount.get()).
                append(" row(s)");

        Utils.println("ok");
        Utils.println(resultBuilder.toString());
    }

    public void stop() {
        context.stop();
    }
}