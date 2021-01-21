package com.searise.sof.execution;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.searise.sof.core.SofSession;
import com.searise.sof.analyse.Analyzer;
import com.searise.sof.catalog.Catalog;
import com.searise.sof.catalog.TestCatalog;
import com.searise.sof.core.SofContext;
import com.searise.sof.core.Utils;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.parser.SqlParser;
import com.searise.sof.plan.logic.LogicalPlan;
import com.searise.sof.plan.physics.PhysicalPlan;
import com.searise.sof.plan.runnable.RunnableCommand;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.List;

import static com.searise.sof.optimize.Optimizer.newOptimizer;

public class ExecutorSuite {
    @Test
    public void test() throws Exception {
        testExec("select a as a, b as b from a",
                "1.0,2.0\n" +
                        "5.0,6.0\n" +
                        "9.0,10.0");

        testExec(
                "select a+b, b-c, c*d, d/a, b % a, -a from a",
                "3.0,-1.0,12.0,4.0,0.0,-1.0\n" +
                        "11.0,-1.0,56.0,1.6,1.0,-5.0\n" +
                        "19.0,-1.0,132.0,1.3333333333333333,1.0,-9.0"
        );

        testExec(
                "select a+b+c+d from a where (b > 2.0 and c < 11.0) or !(b <= 2.0 or c >= 11.0)",
                "26.0"
        );

        testExec(
                "set sof_force_join_type=loop;select a.a, b.b, a.c, b.d from a as a join a as b on a.a = b.a and a.d > 4.0 and b.c < 11.0",
                "5.0,6.0,7.0,8.0"
        );
        testExec(
                "set sof_force_join_type=hash;select a.a, b.b, a.c, b.d from a as a join a as b on a.a = b.a and a.d > 4.0 and b.c < 11.0",
                "5.0,6.0,7.0,8.0"
        );

        testExec(
                "set sof_force_join_type=loop;select a.a, b.b, a.c, b.d from a as a join a as b on a.a = b.a where a.d > 4.0 and b.c < 11.0",
                "5.0,6.0,7.0,8.0"
        );
        testExec(
                "set sof_force_join_type=hash;select a.a, b.b, a.c, b.d from a as a join a as b on a.a = b.a where a.d > 4.0 and b.c < 11.0",
                "5.0,6.0,7.0,8.0"
        );

        testExec(
                "set sof_force_join_type=loop;select a.a, b.b, a.c, b.d from a as a join a as b",
                "1.0,2.0,3.0,4.0\n" +
                        "1.0,6.0,3.0,8.0\n" +
                        "1.0,10.0,3.0,12.0\n" +
                        "5.0,2.0,7.0,4.0\n" +
                        "5.0,6.0,7.0,8.0\n" +
                        "5.0,10.0,7.0,12.0\n" +
                        "9.0,2.0,11.0,4.0\n" +
                        "9.0,6.0,11.0,8.0\n" +
                        "9.0,10.0,11.0,12.0"
        );
        testExec(
                "set sof_force_join_type=hash;select a.a, b.b, a.c, b.d from a as a join a as b",
                "1.0,2.0,3.0,4.0\n" +
                        "1.0,6.0,3.0,8.0\n" +
                        "1.0,10.0,3.0,12.0\n" +
                        "5.0,2.0,7.0,4.0\n" +
                        "5.0,6.0,7.0,8.0\n" +
                        "5.0,10.0,7.0,12.0\n" +
                        "9.0,2.0,11.0,4.0\n" +
                        "9.0,6.0,11.0,8.0\n" +
                        "9.0,10.0,11.0,12.0"
        );

        testExec("select 1 as a, a as b from a",
                "1,1.0\n" +
                        "1,5.0\n" +
                        "1,9.0");

        testExec("select 1 as a, a as b from (select a, b from a) a",
                "1,1.0\n" +
                        "1,5.0\n" +
                        "1,9.0");

        testExec(
                "select a+b, b-c, c*d, d/a, b % a, -a from (select (a+1.0) as a, (b+1.0) as b, (c+1.0) as c, (d+1.0) as d from a) a",
                "5.0,-1.0,20.0,2.5,1.0,-2.0\n" +
                        "13.0,-1.0,72.0,1.5,1.0,-6.0\n" +
                        "21.0,-1.0,156.0,1.3,1.0,-10.0"
        );

        testExec(
                "select a, b, c, d from (select a, b, c, d from a where a > 1.0) a where a < 9.0",
                "5.0,6.0,7.0,8.0"
        );

        testExec(
                "set sof_force_join_type=hash;select a.a, b.a, c.a, d.a from a join b on a.a = b.b join c on c.c = b.b and c.c = a.a join d on d.a = a.a and d.a = c.c",
                ""
        );
        testExec(
                "set sof_force_join_type=loop;select a.a, b.a, c.a, d.a from a join b on a.a = b.b join c on c.c = b.b and c.c = a.a join d on d.a = a.a and d.a = c.c",
                ""
        );

        testExec(
                "set sof_force_join_type=hash;select a.a, b.a, c.a, d.a from a join b on a.a = b.a join c on c.c = b.c and c.a = a.a join d on d.a = a.a and d.c = c.c",
                "1.0,1.0,1.0,1.0\n" +
                        "5.0,5.0,5.0,5.0\n" +
                        "9.0,9.0,9.0,9.0"
        );
        testExec(
                "set sof_force_join_type=loop;select a.a, b.a, c.a, d.a from a join b on a.a = b.a join c on c.c = b.c and c.a = a.a join d on d.a = a.a and d.c = c.c",
                "1.0,1.0,1.0,1.0\n" +
                        "5.0,5.0,5.0,5.0\n" +
                        "9.0,9.0,9.0,9.0"
        );

        testExec("select ((a+b-c*d) > (a*b*c*5.0*d)) or (((a%b+5.0-c)/(-a)) != 0.0) from a",
                "true\n" +
                        "true\n" +
                        "true");

        testExec("select (a+1)+1 from a",
                "3.0\n" +
                        "7.0\n" +
                        "11.0");
    }

    private void testExec(String sql, String expect) throws Exception {
        Catalog catalog = new TestCatalog();
        SofContext context = SofContext.getOrCreate();
        List<String> splits = Utils.split(sql);
        for (int i = 0; i < splits.size() - 1; i++) {
            LogicalPlan parsePlan = new SqlParser(context).parsePlan(splits.get(i));
            if (parsePlan instanceof RunnableCommand) {
                RunnableCommand command = (RunnableCommand) parsePlan;
                command.run(catalog);
            }
        }

        LogicalPlan parsePlan = new SqlParser(context).parsePlan(splits.get(splits.size() - 1));
        LogicalPlan analyzePlan = new Analyzer(new TestCatalog()).analyse(parsePlan);
        PhysicalPlan physicalPlan = newOptimizer().optimize(analyzePlan);

        Preconditions.checkArgument(physicalPlan.partitions() > 0);

        StringBuffer resultBuilder = new StringBuffer();
        context.runPlan(physicalPlan, (partition, iterator) -> {
            StringBuilder partitionResultBuilder = new StringBuilder();
            while (iterator.hasNext()) {
                InternalRow row = iterator.next();
                for (int index = 0; index < row.numFields() - 1; index++) {
                    partitionResultBuilder.append(row.getValue(index)).append(",");
                }
                partitionResultBuilder.append(row.getValue(row.numFields() - 1)).append("\n");
            }
            resultBuilder.append(partitionResultBuilder.toString());
        });

        String result = StringUtils.trim(resultBuilder.toString());
        Preconditions.checkArgument(compareResult(result, StringUtils.trim(expect)), String.format("result: %s\nexpect: %s", result, expect));
        context.stop();
    }

    private boolean compareResult(String result, String expect) {
        if (StringUtils.isBlank(result) && StringUtils.isBlank(expect)) {
            return true;
        } else if (StringUtils.isBlank(result)) {
            return false;
        } else if (StringUtils.isBlank(expect)) {
            return false;
        } else {
            List<String> results = ImmutableList.copyOf(StringUtils.split(result, "\n"));
            List<String> expects = ImmutableList.copyOf(StringUtils.split(expect, "\n"));
            if (results.size() != expects.size()) {
                return false;
            }
            return expects.containsAll(results);
        }
    }
}
