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
                "PhysicalProject [1 as 4:IntegerType, StringType:0:0 as 5:StringType]\n" +
                        "  PhysicalScan [/|,] (StringType:0:0,StringType:1:1,StringType:2:2,StringType:3:3)");

        doTest("select 1 as a, a as b from (select a, b from a) a",
                "PhysicalProject [1 as 10:IntegerType, StringType:6:0 as 11:StringType]\n" +
                        "  PhysicalScan [/|,] (StringType:6:0,StringType:7:1)");

        doTest("select a, b from a",
                "PhysicalScan [/|,] (StringType:12:0,StringType:13:1)");
    }

    private void doTest(String sql, String expect) {
        LogicalPlan parsePlan = new SqlParser().parsePlan(sql);
        LogicalPlan analyzePlan = new Analyzer(new TestCatalog()).analyse(parsePlan);
        PhysicalPlan physicalPlan = newOptimizer().optimize(analyzePlan);
        String result = StringUtils.trim(physicalPlan.visitToString());
        Preconditions.checkArgument(StringUtils.equals(result, StringUtils.trim(expect)), String.format("result: %s\nexpect: %s", result, expect));
    }
}
