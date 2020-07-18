package com.searise.sof.analyse;

import com.google.common.base.Preconditions;
import com.searise.sof.parser.SqlParser;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class AnalyzerSuite {
    @Test
    public void test() {
        testAnalyse("select 1 as a, a as b from a",
                "Project [1 as 4:IntegerType, 0:StringType as 5:StringType]\n" +
                        "  Relation [a] (0:StringType,1:StringType,2:StringType,3:StringType)");

        testAnalyse("select 1 from a where !(not(true)) and (false or true) " +
                        "and 1 > 2 and 1 <  2 and '1' = '2' and '1 == 2' and 1 >= 2 " +
                        "and 1 !> 2 and 1 <= 2 and 1 !< 2 and 1 <> 2 and 1 != 2",
                "Project [1]\n" +
                        "  Filter [((((not (not (true))) and ((false) or (true))) and (1 > 2)) and (1 < 2)) and (1 = 2) = 1 == 2, 1 >= 2, 1 <= 2, 1 <= 2, 1 >= 2, not (1 = 2), not (1 = 2)]\n" +
                        "    Relation [a] (6:StringType,7:StringType,8:StringType,9:StringType)");

        testAnalyse("select a.a from a",
                "Project [12:StringType]\n" +
                        "  Relation [a] (12:StringType,13:StringType,14:StringType,15:StringType)");

        testAnalyse("select b.a from a as b",
                "Project [16:StringType]\n" +
                        "  Relation [a, b] (16:StringType,17:StringType,18:StringType,19:StringType)");

        testAnalyse("select -((((1+1)-2)/3)*4) from a",
                "Project [(-((((1 + 1) - 2) / 3) * 4))]\n" +
                        "  Relation [a] (20:StringType,21:StringType,22:StringType,23:StringType)");

        testAnalyse("select 1 as a, a as b from (select a, b from a) a",
                "Project [1 as 29:IntegerType, 25:StringType as 30:StringType]\n" +
                        "  SubqueryAlias [a]\n" +
                        "    Project [25:StringType, 26:StringType]\n" +
                        "      Relation [a] (25:StringType,26:StringType,27:StringType,28:StringType)");

        testAnalyse("select 1 from a where a > 1.0 and a < 2.0",
                "Project [1]\n" +
                        "  Filter [31:StringType > 1.0, 31:StringType < 2.0]\n" +
                        "    Relation [a] (31:StringType,32:StringType,33:StringType,34:StringType)");

        testAnalyse("select 1 from a where !(a <= 1.0 or a >= 2.0)",
                "Project [1]\n" +
                        "  Filter [not (37:StringType <= 1.0), not (37:StringType >= 2.0)]\n" +
                        "    Relation [a] (37:StringType,38:StringType,39:StringType,40:StringType)");

        testAnalyse("select a.a, b.b, a.c, b.d from a as a join a as b on a.a = b.a and a.d > 4.0 and b.c < 11.0",
                "Project [44:StringType, 49:StringType, 46:StringType, 51:StringType]\n" +
                        "  join on (44:StringType = 48:StringType, 47:StringType > 4.0, 50:StringType < 11.0)\n" +
                        "    Relation [a, a] (44:StringType,45:StringType,46:StringType,47:StringType)\n" +
                        "    Relation [a, b] (48:StringType,49:StringType,50:StringType,51:StringType)");

        testAnalyse("select a.a, b.b, a.c, b.d from a as a join a as b on a.a = b.a where a.d > 4.0 and b.c < 11.0",
                "Project [52:StringType, 57:StringType, 54:StringType, 59:StringType]\n" +
                        "  Filter [55:StringType > 4.0, 58:StringType < 11.0]\n" +
                        "    join on (52:StringType = 56:StringType)\n" +
                        "      Relation [a, a] (52:StringType,53:StringType,54:StringType,55:StringType)\n" +
                        "      Relation [a, b] (56:StringType,57:StringType,58:StringType,59:StringType)");
    }

    private void testAnalyse(String sql, String expect) {
        SqlParser sqlParser = new SqlParser();
        Analyzer analyzer = new Analyzer(new TestCatalog());
        String result = StringUtils.trim(analyzer.analyse(sqlParser.parsePlan(sql)).visitToString());
        Preconditions.checkArgument(StringUtils.equals(result, StringUtils.trim(expect)), String.format("result: %s\nexpect: %s", result, expect));
    }
}
