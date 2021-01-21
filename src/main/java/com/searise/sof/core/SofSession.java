package com.searise.sof.core;

import com.searise.sof.analyse.Analyzer;
import com.searise.sof.catalog.BuiltInCatalog;
import com.searise.sof.catalog.Catalog;
import com.searise.sof.core.conf.SofConf;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.execution.ExecBuilder;
import com.searise.sof.optimize.Optimizer;
import com.searise.sof.parser.SqlParser;
import com.searise.sof.plan.logic.LogicalPlan;
import com.searise.sof.plan.physics.PhysicalPlan;
import com.searise.sof.plan.runnable.RunnableCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.searise.sof.optimize.Optimizer.newOptimizer;

public class SofSession {
    public final Catalog catalog;
    public final SofContext context;
    public final SqlParser sqlParser;
    public final Analyzer analyzer;
    public final Optimizer optimizer;
    public final ExecBuilder builder;

    private SofSession(SofConf conf) {
        catalog = new BuiltInCatalog();
        context = SofContext.getOrCreate(conf);
        sqlParser = new SqlParser(context);
        analyzer = new Analyzer(catalog);
        optimizer = newOptimizer();
        builder = new ExecBuilder(context);
    }

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
        unActiveIfIs(this);
        context.stop();
    }

    private static Optional<SofSession> activeSession = Optional.empty();
    public static synchronized SofSession getActive() {
        return activeSession.orElseGet(() -> {
            throw new SofException("no active session");
        });
    }
    private static synchronized void unActiveIfIs(SofSession session) {
        if (activeSession.isPresent() && activeSession.get() == session) {
            activeSession = Optional.empty();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        Map<String, String> options = new HashMap<>();

        public Builder config(String key, String value) {
            this.options.put(key, value);
            return this;
        }

        public SofSession build() {
            SofConf conf = new SofConf();
            options.forEach(conf::setConf);
            SofSession session = new SofSession(conf);
            synchronized (SofSession.class) {
                activeSession = Optional.of(session);
            }
            return session;
        }
    }
}