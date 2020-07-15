package com.searise.sof.execution;

import com.google.common.base.Preconditions;
import com.searise.sof.analyse.Analyzer;
import com.searise.sof.parser.SqlParser;
import com.searise.sof.plan.logic.LogicalPlan;
import com.searise.sof.plan.physics.PhysicalPlan;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static com.searise.sof.optimize.Optimizer.newOptimizer;

public class ExecutorSuite {
    @Test
    public void test() {
//        testExec(
//                "select a+b, b-c, c*d, d/a, b % a, -a from a",
//                "3.0,-1.0,12.0,4.0,0.0,-1.0\n" +
//                        "11.0,-1.0,56.0,1.6,1.0,-5.0\n" +
//                        "19.0,-1.0,132.0,1.3333333333333333,1.0,-9.0"
//        );
//
//        testExec(
//                "select a+b+c+d from a where (b > 2.0 and c < 11.0) or !(b <= 2.0 or c >= 11.0)",
//                "26.0"
//        );

        testExec(
                "select a.a, b.b, a.c, b.d from a as a join a as b on a.a = b.a and a.d > 4.0 and b.c < 11.0",
                "5.0,6.0,7.0,8.0"
        );

//        testExec(
//                "select a.a, b.b, a.c, b.d from a as a join a as b on a.a = b.a where a.d > 4.0 and b.c < 11.0",
//                "5.0,6.0,7.0,8.0"
//        );
    }

    private void testExec(String sql, String expect) {
        LogicalPlan parsePlan = new SqlParser().parsePlan(sql);
        LogicalPlan analyzePlan = new Analyzer(new TestCatalog()).analyse(parsePlan);
        PhysicalPlan physicalPlan = newOptimizer().optimize(analyzePlan);
        System.out.println(physicalPlan.visitToString());
        ResultExec executor = (ResultExec) new Builder().build(physicalPlan);
        executor.open();
        executor.close();
        String result = StringUtils.trim(executor.result());
        Preconditions.checkArgument(StringUtils.equals(result, StringUtils.trim(expect)), String.format("result: %s\nexpect: %s", result, expect));
    }
}
