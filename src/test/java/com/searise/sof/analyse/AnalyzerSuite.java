package com.searise.sof.analyse;

import com.google.common.base.Preconditions;
import com.searise.sof.catalog.TestCatalog;
import com.searise.sof.core.Context;
import com.searise.sof.parser.SqlParser;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class AnalyzerSuite {
    @Test
    public void test() {
        testAnalyse("select 1 as a, a as b from a",
                "Project [1 as 4:IntegerType, 0:DoubleType as 5:DoubleType]\n" +
                        "  Relation [a] (0:DoubleType,1:DoubleType,2:DoubleType,3:DoubleType)");

        testAnalyse("select 1 from a where !(not(true)) and (false or true) " +
                        "and 1 > 2 and 1 <  2 and '1' = '2' and '1 == 2' and 1 >= 2 " +
                        "and 1 !> 2 and 1 <= 2 and 1 !< 2 and 1 <> 2 and 1 != 2",
                "Project [1]\n" +
                        "  Filter [((((not (not (true))) and ((false) or (true))) and (1 > 2)) and (1 < 2)) and (1 = 2) = 1 == 2, 1 >= 2, 1 <= 2, 1 <= 2, 1 >= 2, not (1 = 2), not (1 = 2)]\n" +
                        "    Relation [a] (0:DoubleType,1:DoubleType,2:DoubleType,3:DoubleType)");

        testAnalyse("select a.a from a",
                "Project [0:DoubleType]\n" +
                        "  Relation [a] (0:DoubleType,1:DoubleType,2:DoubleType,3:DoubleType)");

        testAnalyse("select b.a from a as b",
                "Project [0:DoubleType]\n" +
                        "  Relation [a, b] (0:DoubleType,1:DoubleType,2:DoubleType,3:DoubleType)");

        testAnalyse("select -((((1+1)-2)/3)*4) from a",
                "Project [(-((((1 + 1) - 2) / 3) * 4))]\n" +
                        "  Relation [a] (0:DoubleType,1:DoubleType,2:DoubleType,3:DoubleType)");

        testAnalyse("select 1 as a, a as b from (select a, b from a) a",
                "Project [1 as 4:IntegerType, 0:DoubleType as 5:DoubleType]\n" +
                        "  SubqueryAlias [a]\n" +
                        "    Project [0:DoubleType, 1:DoubleType]\n" +
                        "      Relation [a] (0:DoubleType,1:DoubleType,2:DoubleType,3:DoubleType)");

        testAnalyse("select 1 from a where a > 1.0 and a < 2.0",
                "Project [1]\n" +
                        "  Filter [0:DoubleType > 1.0, 0:DoubleType < 2.0]\n" +
                        "    Relation [a] (0:DoubleType,1:DoubleType,2:DoubleType,3:DoubleType)");

        testAnalyse("select 1 from a where !(a <= 1.0 or a >= 2.0)",
                "Project [1]\n" +
                        "  Filter [not (0:DoubleType <= 1.0), not (0:DoubleType >= 2.0)]\n" +
                        "    Relation [a] (0:DoubleType,1:DoubleType,2:DoubleType,3:DoubleType)");

        testAnalyse("select a.a, b.b, a.c, b.d from a as a join a as b on a.a = b.a and a.d > 4.0 and b.c < 11.0",
                "Project [0:DoubleType, 5:DoubleType, 2:DoubleType, 7:DoubleType]\n" +
                        "  join on (0:DoubleType = 4:DoubleType, 3:DoubleType > 4.0, 6:DoubleType < 11.0)\n" +
                        "    Relation [a, a] (0:DoubleType,1:DoubleType,2:DoubleType,3:DoubleType)\n" +
                        "    Relation [a, b] (4:DoubleType,5:DoubleType,6:DoubleType,7:DoubleType)");

        testAnalyse("select a.a, b.b, a.c, b.d from a as a join a as b on a.a = b.a where a.d > 4.0 and b.c < 11.0",
                "Project [0:DoubleType, 5:DoubleType, 2:DoubleType, 7:DoubleType]\n" +
                        "  Filter [3:DoubleType > 4.0, 6:DoubleType < 11.0]\n" +
                        "    join on (0:DoubleType = 4:DoubleType)\n" +
                        "      Relation [a, a] (0:DoubleType,1:DoubleType,2:DoubleType,3:DoubleType)\n" +
                        "      Relation [a, b] (4:DoubleType,5:DoubleType,6:DoubleType,7:DoubleType)");
    }

    private void testAnalyse(String sql, String expect) {
        SqlParser sqlParser = new SqlParser(new Context());
        Analyzer analyzer = new Analyzer(new TestCatalog());
        String result = StringUtils.trim(analyzer.analyse(sqlParser.parsePlan(sql)).visitToString());
        Preconditions.checkArgument(StringUtils.equals(result, StringUtils.trim(expect)), String.format("result: %s\nexpect: %s", result, expect));
    }
}
