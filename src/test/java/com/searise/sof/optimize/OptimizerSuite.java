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
                "PhysicalProject [IntegerType:exprId->4:index->0,StringType:exprId->5:index->1] [1, StringType:exprId->0:index->0]\n" +
                        "  PhysicalScan [StringType:exprId->0:index->0] [/|,] (0:StringType,1:StringType,2:StringType,3:StringType)");

        doTest("select 1 as a, a as b from (select a, b from a) a",
                "PhysicalProject [IntegerType:exprId->10:index->0,StringType:exprId->11:index->1] [1, StringType:exprId->6:index->0]\n" +
                        "  PhysicalScan [StringType:exprId->6:index->0] [/|,] (6:StringType,7:StringType,8:StringType,9:StringType)");

        doTest("select a, b from a",
                "PhysicalScan [StringType:exprId->12:index->0,StringType:exprId->13:index->1] [/|,] (12:StringType,13:StringType,14:StringType,15:StringType)");

        doTest("select a from (select a from a) b where a < 10.0",
                "PhysicalFilter [StringType:exprId->16:index->0] [StringType:exprId->16:index->0 < 10.0]\n" +
                        "  PhysicalScan [StringType:exprId->16:index->0] [/|,] (16:StringType,17:StringType,18:StringType,19:StringType)");
    }

    private void doTest(String sql, String expect) {
        LogicalPlan parsePlan = new SqlParser().parsePlan(sql);
        LogicalPlan analyzePlan = new Analyzer(new TestCatalog()).analyse(parsePlan);
        PhysicalPlan physicalPlan = newOptimizer().optimize(analyzePlan);
        String result = StringUtils.trim(physicalPlan.visitToString());
        Preconditions.checkArgument(StringUtils.equals(result, StringUtils.trim(expect)), String.format("result: %s\nexpect: %s", result, expect));
    }
}
