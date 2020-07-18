package com.searise.sof.optimize;

import com.google.common.base.Preconditions;
import com.searise.sof.analyse.Analyzer;
import com.searise.sof.catalog.TestCatalog;
import com.searise.sof.core.Context;
import com.searise.sof.core.ExprIdBuilder;
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
                "PhysicalProject [IntegerType:exprId->4:index->0,DoubleType:exprId->5:index->1] [1, DoubleType:exprId->0:index->0]\n" +
                        "  PhysicalScan [DoubleType:exprId->0:index->0] [src\\test\\resources\\input.txt|,] (0:DoubleType,1:DoubleType,2:DoubleType,3:DoubleType)");

        doTest("select 1 as a, a as b from (select a, b from a) a",
                "PhysicalProject [IntegerType:exprId->4:index->0,DoubleType:exprId->5:index->1] [1, DoubleType:exprId->0:index->0]\n" +
                        "  PhysicalScan [DoubleType:exprId->0:index->0] [src\\test\\resources\\input.txt|,] (0:DoubleType,1:DoubleType,2:DoubleType,3:DoubleType)");

        doTest("select a, b from a",
                "PhysicalScan [DoubleType:exprId->0:index->0,DoubleType:exprId->1:index->1] [src\\test\\resources\\input.txt|,] (0:DoubleType,1:DoubleType,2:DoubleType,3:DoubleType)");

        doTest("select a from (select a from a) b where a < 10.0",
                "PhysicalFilter [DoubleType:exprId->0:index->0] [DoubleType:exprId->0:index->0 < 10.0]\n" +
                        "  PhysicalScan [DoubleType:exprId->0:index->0] [src\\test\\resources\\input.txt|,] (0:DoubleType,1:DoubleType,2:DoubleType,3:DoubleType)");
    }

    private void doTest(String sql, String expect) {
        LogicalPlan parsePlan = new SqlParser(new Context()).parsePlan(sql);
        LogicalPlan analyzePlan = new Analyzer(new TestCatalog()).analyse(parsePlan);
        PhysicalPlan physicalPlan = newOptimizer().optimize(analyzePlan);
        String result = StringUtils.trim(physicalPlan.visitToString());
        Preconditions.checkArgument(StringUtils.equals(result, StringUtils.trim(expect)), String.format("result: %s\nexpect: %s", result, expect));
    }
}
