package com.searise.sof.optimize;

import com.google.common.base.Preconditions;
import com.searise.sof.analyse.Analyzer;
import com.searise.sof.catalog.Catalog;
import com.searise.sof.catalog.TestCatalog;
import com.searise.sof.catalog.TestContext;
import com.searise.sof.core.SofContext;
import com.searise.sof.core.Utils;
import com.searise.sof.parser.SqlParser;
import com.searise.sof.plan.logic.LogicalPlan;
import com.searise.sof.plan.physics.PhysicalPlan;
import com.searise.sof.plan.runnable.RunnableCommand;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.List;

import static com.searise.sof.optimize.Optimizer.newOptimizer;

public class OptimizerSuite {
    @Test
    public void test() throws Exception {
        doTest("select a as a, b as b from a",
                "PhysicalScan [DoubleType:exprId->0:index->0,DoubleType:exprId->1:index->1] [src\\test\\resources\\in|,] (attribute:0:DoubleType,attribute:1:DoubleType,attribute:2:DoubleType,attribute:3:DoubleType)");

        doTest("select 1 as a, a as b from a",
                "PhysicalProject [IntegerType:exprId->4:index->0,DoubleType:exprId->0:index->1] [literal:1:IntegerType, DoubleType:exprId->0:index->0]\n" +
                        "  PhysicalScan [DoubleType:exprId->0:index->0] [src\\test\\resources\\in|,] (attribute:0:DoubleType,attribute:1:DoubleType,attribute:2:DoubleType,attribute:3:DoubleType)");

        doTest("select 1 as a, a as b from (select a, b from a) a",
                "PhysicalProject [IntegerType:exprId->4:index->0,DoubleType:exprId->0:index->1] [literal:1:IntegerType, DoubleType:exprId->0:index->0]\n" +
                        "  PhysicalScan [DoubleType:exprId->0:index->0] [src\\test\\resources\\in|,] (attribute:0:DoubleType,attribute:1:DoubleType,attribute:2:DoubleType,attribute:3:DoubleType)");

        doTest("select a, b from a",
                "PhysicalScan [DoubleType:exprId->0:index->0,DoubleType:exprId->1:index->1] [src\\test\\resources\\in|,] (attribute:0:DoubleType,attribute:1:DoubleType,attribute:2:DoubleType,attribute:3:DoubleType)");

        doTest("select a from (select a from a) b where a < 10.0",
                "PhysicalFilter [DoubleType:exprId->0:index->0] [DoubleType:exprId->0:index->0 < literal:10.0:DoubleType:BooleanType]\n" +
                        "  PhysicalScan [DoubleType:exprId->0:index->0] [src\\test\\resources\\in|,] (attribute:0:DoubleType,attribute:1:DoubleType,attribute:2:DoubleType,attribute:3:DoubleType)");

        doTest(
                "set sof_force_join_type=loop;select a.a, b.b, a.c, b.d from a as a join a as b on a.a = b.a where a.d > 4.0 and b.c < 11.0",
                "PhysicalNestedLoopJoin [DoubleType:exprId->0:index->0,DoubleType:exprId->5:index->3,DoubleType:exprId->2:index->1,DoubleType:exprId->7:index->4] [DoubleType:exprId->0:index->0 == DoubleType:exprId->4:index->2:BooleanType]\n" +
                        "  Exchange [DoubleType:exprId->0:index->1,DoubleType:exprId->2:index->2] [DoubleType:exprId->0:index->0] [0]\n" +
                        "    PhysicalFilter [DoubleType:exprId->0:index->1,DoubleType:exprId->2:index->2] [DoubleType:exprId->3:index->0 > literal:4.0:DoubleType:BooleanType]\n" +
                        "      PhysicalScan [DoubleType:exprId->3:index->3,DoubleType:exprId->0:index->0,DoubleType:exprId->2:index->2] [src\\test\\resources\\in|,] (attribute:0:DoubleType,attribute:1:DoubleType,attribute:2:DoubleType,attribute:3:DoubleType)\n" +
                        "  Exchange [DoubleType:exprId->4:index->1,DoubleType:exprId->5:index->2,DoubleType:exprId->7:index->3] [DoubleType:exprId->4:index->0] [1]\n" +
                        "    PhysicalFilter [DoubleType:exprId->4:index->1,DoubleType:exprId->5:index->2,DoubleType:exprId->7:index->3] [DoubleType:exprId->6:index->0 < literal:11.0:DoubleType:BooleanType]\n" +
                        "      PhysicalScan [DoubleType:exprId->6:index->2,DoubleType:exprId->4:index->0,DoubleType:exprId->5:index->1,DoubleType:exprId->7:index->3] [src\\test\\resources\\in|,] (attribute:4:DoubleType,attribute:5:DoubleType,attribute:6:DoubleType,attribute:7:DoubleType)"
        );
        doTest(
                "set sof_force_join_type=hash;select a.a, b.b, a.c, b.d from a as a join a as b on a.a = b.a where a.d > 4.0 and b.c < 11.0",
                "PhysicalHashJoin [DoubleType:exprId->0:index->0,DoubleType:exprId->5:index->3,DoubleType:exprId->2:index->1,DoubleType:exprId->7:index->4], stream: [DoubleType:exprId->0:index->0], build: [DoubleType:exprId->4:index->0], others: [] \n" +
                        "  Exchange [DoubleType:exprId->0:index->1,DoubleType:exprId->2:index->2] [DoubleType:exprId->0:index->0] [0]\n" +
                        "    PhysicalFilter [DoubleType:exprId->0:index->1,DoubleType:exprId->2:index->2] [DoubleType:exprId->3:index->0 > literal:4.0:DoubleType:BooleanType]\n" +
                        "      PhysicalScan [DoubleType:exprId->3:index->3,DoubleType:exprId->0:index->0,DoubleType:exprId->2:index->2] [src\\test\\resources\\in|,] (attribute:0:DoubleType,attribute:1:DoubleType,attribute:2:DoubleType,attribute:3:DoubleType)\n" +
                        "  Exchange [DoubleType:exprId->4:index->1,DoubleType:exprId->5:index->2,DoubleType:exprId->7:index->3] [DoubleType:exprId->4:index->0] [1]\n" +
                        "    PhysicalFilter [DoubleType:exprId->4:index->1,DoubleType:exprId->5:index->2,DoubleType:exprId->7:index->3] [DoubleType:exprId->6:index->0 < literal:11.0:DoubleType:BooleanType]\n" +
                        "      PhysicalScan [DoubleType:exprId->6:index->2,DoubleType:exprId->4:index->0,DoubleType:exprId->5:index->1,DoubleType:exprId->7:index->3] [src\\test\\resources\\in|,] (attribute:4:DoubleType,attribute:5:DoubleType,attribute:6:DoubleType,attribute:7:DoubleType)"
        );

        doTest(
                "set sof_force_join_type=loop;select a.a, b.b, a.c, b.d from a as a join a as b",
                "PhysicalNestedLoopJoin [DoubleType:exprId->0:index->0,DoubleType:exprId->5:index->2,DoubleType:exprId->2:index->1,DoubleType:exprId->7:index->3] []\n" +
                        "  PhysicalScan [DoubleType:exprId->0:index->0,DoubleType:exprId->2:index->2] [src\\test\\resources\\in|,] (attribute:0:DoubleType,attribute:1:DoubleType,attribute:2:DoubleType,attribute:3:DoubleType)\n" +
                        "  PhysicalScan [DoubleType:exprId->5:index->1,DoubleType:exprId->7:index->3] [src\\test\\resources\\in|,] (attribute:4:DoubleType,attribute:5:DoubleType,attribute:6:DoubleType,attribute:7:DoubleType)"
        );
        doTest(
                "set sof_force_join_type=hash;select a.a, b.b, a.c, b.d from a as a join a as b",
                "PhysicalHashJoin [DoubleType:exprId->0:index->0,DoubleType:exprId->5:index->2,DoubleType:exprId->2:index->1,DoubleType:exprId->7:index->3], stream: [], build: [], others: [] \n" +
                        "  PhysicalScan [DoubleType:exprId->0:index->0,DoubleType:exprId->2:index->2] [src\\test\\resources\\in|,] (attribute:0:DoubleType,attribute:1:DoubleType,attribute:2:DoubleType,attribute:3:DoubleType)\n" +
                        "  PhysicalScan [DoubleType:exprId->5:index->1,DoubleType:exprId->7:index->3] [src\\test\\resources\\in|,] (attribute:4:DoubleType,attribute:5:DoubleType,attribute:6:DoubleType,attribute:7:DoubleType)"
        );

        doTest(
                "select a+b, b-c, c*d, d/a, b % a, -a from (select (a+1.0) as a, (b+1.0) as b, (c+1.0) as c, (d+1.0) as d from a) a",
                "PhysicalProject [DoubleType:exprId->8:index->0,DoubleType:exprId->9:index->1,DoubleType:exprId->10:index->2,DoubleType:exprId->11:index->3,DoubleType:exprId->12:index->4,DoubleType:exprId->13:index->5] [((DoubleType:exprId->0:index->0 + literal:1.0:DoubleType) + (DoubleType:exprId->1:index->1 + literal:1.0:DoubleType)), ((DoubleType:exprId->1:index->1 + literal:1.0:DoubleType) - (DoubleType:exprId->2:index->2 + literal:1.0:DoubleType)), ((DoubleType:exprId->2:index->2 + literal:1.0:DoubleType) * (DoubleType:exprId->3:index->3 + literal:1.0:DoubleType)), ((DoubleType:exprId->3:index->3 + literal:1.0:DoubleType) / (DoubleType:exprId->0:index->0 + literal:1.0:DoubleType)), ((DoubleType:exprId->1:index->1 + literal:1.0:DoubleType) % (DoubleType:exprId->0:index->0 + literal:1.0:DoubleType)), (-(DoubleType:exprId->0:index->0 + literal:1.0:DoubleType))]\n" +
                        "  PhysicalScan [DoubleType:exprId->0:index->0,DoubleType:exprId->1:index->1,DoubleType:exprId->2:index->2,DoubleType:exprId->3:index->3] [src\\test\\resources\\in|,] (attribute:0:DoubleType,attribute:1:DoubleType,attribute:2:DoubleType,attribute:3:DoubleType)"
        );

        doTest(
                "select a, b, c, d from (select a, b, c, d from a where a > 1.0) a where a < 9.0",
                "PhysicalFilter [DoubleType:exprId->0:index->0,DoubleType:exprId->1:index->1,DoubleType:exprId->2:index->2,DoubleType:exprId->3:index->3] [DoubleType:exprId->0:index->0 < literal:9.0:DoubleType:BooleanType, DoubleType:exprId->0:index->0 > literal:1.0:DoubleType:BooleanType]\n" +
                        "  PhysicalScan [DoubleType:exprId->0:index->0,DoubleType:exprId->1:index->1,DoubleType:exprId->2:index->2,DoubleType:exprId->3:index->3] [src\\test\\resources\\in|,] (attribute:0:DoubleType,attribute:1:DoubleType,attribute:2:DoubleType,attribute:3:DoubleType)"
        );

        doTest(
                "select a.a, b.a, c.a, d.a, e.a, f.a, g.a from a join b join c join d join e join f join g",
                "PhysicalHashJoin [DoubleType:exprId->0:index->6,DoubleType:exprId->4:index->0,DoubleType:exprId->8:index->1,DoubleType:exprId->12:index->2,DoubleType:exprId->16:index->3,DoubleType:exprId->20:index->4,DoubleType:exprId->24:index->5], stream: [], build: [], others: [] \n" +
                        "  PhysicalHashJoin [DoubleType:exprId->4:index->5,DoubleType:exprId->8:index->0,DoubleType:exprId->12:index->1,DoubleType:exprId->16:index->2,DoubleType:exprId->20:index->3,DoubleType:exprId->24:index->4], stream: [], build: [], others: [] \n" +
                        "    PhysicalHashJoin [DoubleType:exprId->8:index->4,DoubleType:exprId->12:index->0,DoubleType:exprId->16:index->1,DoubleType:exprId->20:index->2,DoubleType:exprId->24:index->3], stream: [], build: [], others: [] \n" +
                        "      PhysicalHashJoin [DoubleType:exprId->12:index->3,DoubleType:exprId->16:index->0,DoubleType:exprId->20:index->1,DoubleType:exprId->24:index->2], stream: [], build: [], others: [] \n" +
                        "        PhysicalHashJoin [DoubleType:exprId->16:index->2,DoubleType:exprId->20:index->0,DoubleType:exprId->24:index->1], stream: [], build: [], others: [] \n" +
                        "          PhysicalHashJoin [DoubleType:exprId->20:index->0,DoubleType:exprId->24:index->1], stream: [], build: [], others: [] \n" +
                        "            PhysicalScan [DoubleType:exprId->20:index->0] [src\\test\\resources\\in|,] (attribute:20:DoubleType,attribute:21:DoubleType,attribute:22:DoubleType,attribute:23:DoubleType)\n" +
                        "            PhysicalScan [DoubleType:exprId->24:index->0] [src\\test\\resources\\in|,] (attribute:24:DoubleType,attribute:25:DoubleType,attribute:26:DoubleType,attribute:27:DoubleType)\n" +
                        "          PhysicalScan [DoubleType:exprId->16:index->0] [src\\test\\resources\\in|,] (attribute:16:DoubleType,attribute:17:DoubleType,attribute:18:DoubleType,attribute:19:DoubleType)\n" +
                        "        PhysicalScan [DoubleType:exprId->12:index->0] [src\\test\\resources\\in|,] (attribute:12:DoubleType,attribute:13:DoubleType,attribute:14:DoubleType,attribute:15:DoubleType)\n" +
                        "      PhysicalScan [DoubleType:exprId->8:index->0] [src\\test\\resources\\in|,] (attribute:8:DoubleType,attribute:9:DoubleType,attribute:10:DoubleType,attribute:11:DoubleType)\n" +
                        "    PhysicalScan [DoubleType:exprId->4:index->0] [src\\test\\resources\\in|,] (attribute:4:DoubleType,attribute:5:DoubleType,attribute:6:DoubleType,attribute:7:DoubleType)\n" +
                        "  PhysicalScan [DoubleType:exprId->0:index->0] [src\\test\\resources\\in|,] (attribute:0:DoubleType,attribute:1:DoubleType,attribute:2:DoubleType,attribute:3:DoubleType)"
        );

        doTest(
                "select a.a, b.a, c.a, d.a from a join b on a.a = b.b join c on c.c = b.b and c.c = a.a join d on d.a = a.a and d.a = c.c",
                "PhysicalHashJoin [DoubleType:exprId->0:index->0,DoubleType:exprId->4:index->2,DoubleType:exprId->8:index->3,DoubleType:exprId->12:index->4], stream: [DoubleType:exprId->0:index->0, DoubleType:exprId->10:index->1], build: [DoubleType:exprId->12:index->0, DoubleType:exprId->12:index->0], others: [] \n" +
                        "  Exchange [DoubleType:exprId->0:index->1,DoubleType:exprId->10:index->3,DoubleType:exprId->4:index->2,DoubleType:exprId->8:index->4] [DoubleType:exprId->0:index->0, DoubleType:exprId->10:index->1] [4]\n" +
                        "    PhysicalHashJoin [DoubleType:exprId->0:index->1,DoubleType:exprId->10:index->3,DoubleType:exprId->4:index->2,DoubleType:exprId->8:index->4], stream: [DoubleType:exprId->5:index->0, DoubleType:exprId->0:index->1], build: [DoubleType:exprId->10:index->0, DoubleType:exprId->10:index->0], others: [] \n" +
                        "      Exchange [DoubleType:exprId->5:index->1,DoubleType:exprId->0:index->0,DoubleType:exprId->4:index->2] [DoubleType:exprId->5:index->0, DoubleType:exprId->0:index->1] [2]\n" +
                        "        PhysicalHashJoin [DoubleType:exprId->5:index->1,DoubleType:exprId->0:index->0,DoubleType:exprId->4:index->2], stream: [DoubleType:exprId->0:index->0], build: [DoubleType:exprId->5:index->0], others: [] \n" +
                        "          Exchange [DoubleType:exprId->0:index->0] [DoubleType:exprId->0:index->0] [0]\n" +
                        "            PhysicalScan [DoubleType:exprId->0:index->0] [src\\test\\resources\\in|,] (attribute:0:DoubleType,attribute:1:DoubleType,attribute:2:DoubleType,attribute:3:DoubleType)\n" +
                        "          Exchange [DoubleType:exprId->5:index->1,DoubleType:exprId->4:index->0] [DoubleType:exprId->5:index->0] [1]\n" +
                        "            PhysicalScan [DoubleType:exprId->5:index->1,DoubleType:exprId->4:index->0] [src\\test\\resources\\in|,] (attribute:4:DoubleType,attribute:5:DoubleType,attribute:6:DoubleType,attribute:7:DoubleType)\n" +
                        "      Exchange [DoubleType:exprId->10:index->2,DoubleType:exprId->8:index->0] [DoubleType:exprId->10:index->0, DoubleType:exprId->10:index->0] [3]\n" +
                        "        PhysicalScan [DoubleType:exprId->10:index->2,DoubleType:exprId->8:index->0] [src\\test\\resources\\in|,] (attribute:8:DoubleType,attribute:9:DoubleType,attribute:10:DoubleType,attribute:11:DoubleType)\n" +
                        "  Exchange [DoubleType:exprId->12:index->0] [DoubleType:exprId->12:index->0, DoubleType:exprId->12:index->0] [5]\n" +
                        "    PhysicalScan [DoubleType:exprId->12:index->0] [src\\test\\resources\\in|,] (attribute:12:DoubleType,attribute:13:DoubleType,attribute:14:DoubleType,attribute:15:DoubleType)"
        );
    }

    private void doTest(String sql, String expect) throws Exception {
        try (SofContext context = TestContext.newTestContext()) {
            Catalog catalog = new TestCatalog();

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
            String result = StringUtils.trim(physicalPlan.visitToString());
            Preconditions.checkArgument(StringUtils.equals(result, StringUtils.trim(expect)), String.format("result: %s\nexpect: %s", result, expect));
        }
    }
}
