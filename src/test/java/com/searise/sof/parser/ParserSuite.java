package com.searise.sof.parser;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class ParserSuite {

    @Test
    public void test() {
        testSql("select a, b, c from b where 'a' = 'v' and a > 'c'",
                "Project ['a, 'b, 'c]\n" +
                        "  Filter [and(=(a, v), >('a, c))]\n" +
                        "    Relation [b]\n");

        testSql("select 'a', 0.5, c from b where 'a' >= 'v' or a > 'c'",
                "Project [a, 0.5, 'c]\n" +
                        "  Filter [or(>=(a, v), >('a, c))]\n" +
                        "    Relation [b]\n");

        testSql("select a, b, c from (select a from b) a where 'a' = 'v' and a = 'c'",
                "Project ['a, 'b, 'c]\n" +
                        "  Filter [and(=(a, v), =('a, c))]\n" +
                        "    SubqueryAlias [a]\n" +
                        "      Project ['a]\n" +
                        "        Relation [b]");

        testSql("select a, b, c from (select a from b, c, d) a where 'a' = 'v' and a = 'c'",
                "Project ['a, 'b, 'c]\n" +
                        "  Filter [and(=(a, v), =('a, c))]\n" +
                        "    SubqueryAlias [a]\n" +
                        "      Project ['a]\n" +
                        "        join\n" +
                        "          join\n" +
                        "            Relation [b]\n" +
                        "            Relation [c]\n" +
                        "          Relation [d]\n");
    }

    private void testSql(String sql, String expect) {
        SqlParser sqlParser = new SqlParser();
        String result = StringUtils.trim(sqlParser.parsePlan(sql).visitToString());
        Preconditions.checkArgument(StringUtils.equals(result, StringUtils.trim(expect)), "result: %s\nexpect: %s");
    }
}
