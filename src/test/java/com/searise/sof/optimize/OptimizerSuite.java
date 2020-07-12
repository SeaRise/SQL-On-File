package com.searise.sof.optimize;

import com.google.common.base.Preconditions;
import com.searise.sof.analyse.Analyzer;
import com.searise.sof.analyse.TestCatalog;
import com.searise.sof.parser.SqlParser;
import com.searise.sof.plan.logic.LogicalPlan;
import com.searise.sof.plan.physics.PhysicalPlan;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static com.searise.sof.optimize.Optimizer.newOptimizer;

public class OptimizerSuite {
    @Test
    public void test() {
        doTest("select 1 as a, a as b from a",
                "PhysicalProject [1 as 4:IntegerType, 0:StringType as 5:StringType]\n" +
                        "  PhysicalScan [/|,] (StringType:0:-1,StringType:1:-1,StringType:2:-1,StringType:3:-1)");

        doTest("select 1 as a, a as b from (select a, b from a) a",
                "PhysicalProject [1 as 10:IntegerType, 6:StringType as 11:StringType]\n" +
                        "  PhysicalProject [6:StringType, 7:StringType]\n" +
                        "    PhysicalScan [/|,] (StringType:6:-1,StringType:7:-1,StringType:8:-1,StringType:9:-1)");
    }

    private void doTest(String sql, String expect) {
        LogicalPlan parsePlan = new SqlParser().parsePlan(sql);
        LogicalPlan analyzePlan = new Analyzer(new TestCatalog()).analyse(parsePlan);
        PhysicalPlan physicalPlan = newOptimizer().optimize(analyzePlan);
        String result = StringUtils.trim(physicalPlan.visitToString());
        Preconditions.checkArgument(StringUtils.equals(result, StringUtils.trim(expect)), String.format("result: %s\nexpect: %s", result, expect));
    }
}