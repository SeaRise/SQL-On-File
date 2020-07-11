package com.searise.sof.analyse;

import com.google.common.base.Preconditions;
import com.searise.sof.parser.SqlParser;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class AnalyzerSuite {
    @Test
    public void test() {
        testAnalyse("select 1 from a",
                "Project [1]\n" +
                        "  Relation [a]");

        testAnalyse("select 1 from a where !(not(true)) and (false or true) " +
                        "and 1 > 2 and 1 <  2 and '1' = '2' and '1 == 2' and 1 >= 2 " +
                        "and 1 !> 2 and 1 <= 2 and 1 !< 2 and 1 <> 2 and 1 != 2",
                "Project [1]\n" +
                        "  Filter [((((((((((not (not (true))) and ((false) or (true))) and (1 > 2)) and (1 < 2)) and (1 = 2) = 1 == 2) and (1 >= 2)) and (1 <= 2)) and (1 <= 2)) and (1 >= 2)) and (not (1 = 2))) and (not (1 = 2))]\n" +
                        "    Relation [a]");

        testAnalyse("select a.a from a",
                "Project [0:StringType]\n" +
                        "  Relation [a]");

        testAnalyse("select b.a from a as b",
                "Project [4:StringType]\n" +
                        "  Relation [a, b]");
    }

    private void testAnalyse(String sql, String expect) {
        SqlParser sqlParser = new SqlParser();
        Analyzer analyzer = new Analyzer(new TestCatalog());
        String result = StringUtils.trim(analyzer.analyse(sqlParser.parsePlan(sql)).visitToString());
        Preconditions.checkArgument(StringUtils.equals(result, StringUtils.trim(expect)), String.format("result: %s\nexpect: %s", result, expect));
    }
}
