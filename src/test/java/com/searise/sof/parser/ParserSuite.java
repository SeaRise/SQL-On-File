package com.searise.sof.parser;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class ParserSuite {

    @Test
    public void test() {
        testSql("select a.a from a",
                "Project ['a.a]\n" +
                        "  UnresolvedRelation [a]");

        testSql("select 1+1, 1-1, 1*1, 1/1, 1%1, -(5) from a",
                "Project [+(1, 1), -(1, 1), *(1, 1), /(1, 1), %(1, 1), -(5)]\n" +
                        "  UnresolvedRelation [a]");

        testSql("select b.a from a as b",
                "Project ['b.a]\n" +
                        "  UnresolvedRelation [a, b]");

        testSql("select a as c from a as b",
                "Project ['a as 'c]\n" +
                        "  UnresolvedRelation [a, b]");

        testSql("select a, b, c from b where 'a' = 'v' and a > 'c'",
                "Project ['a, 'b, 'c]\n" +
                        "  Filter [and(=(a, v), >('a, c))]\n" +
                        "    UnresolvedRelation [b]\n");

        testSql("select 'a', 0.5, c from b where 'a' >= 'v' or a > 'c'",
                "Project [a, 0.5, 'c]\n" +
                        "  Filter [or(>=(a, v), >('a, c))]\n" +
                        "    UnresolvedRelation [b]\n");

        testSql("select a, b, c from (select a from b) a where 'a' = 'v' and a = 'c'",
                "Project ['a, 'b, 'c]\n" +
                        "  Filter [and(=(a, v), =('a, c))]\n" +
                        "    SubqueryAlias [a]\n" +
                        "      Project ['a]\n" +
                        "        UnresolvedRelation [b]");

        testSql("select a, b, c from (select a from b, c, d) a where 'a' = 'v' and a = 'c'",
                "Project ['a, 'b, 'c]\n" +
                        "  Filter [and(=(a, v), =('a, c))]\n" +
                        "    SubqueryAlias [a]\n" +
                        "      Project ['a]\n" +
                        "        join\n" +
                        "          join\n" +
                        "            UnresolvedRelation [b]\n" +
                        "            UnresolvedRelation [c]\n" +
                        "          UnresolvedRelation [d]\n");

        testSql("select a, b, c from a join b on a.a = b.b join c on a.a = c.c",
                "Project ['a, 'b, 'c]\n" +
                        "  join on (=('a.a, 'c.c))\n" +
                        "    join on (=('a.a, 'b.b))\n" +
                        "      UnresolvedRelation [a]\n" +
                        "      UnresolvedRelation [b]\n" +
                        "    UnresolvedRelation [c]");
    }

    private void testSql(String sql, String expect) {
        SqlParser sqlParser = new SqlParser();
        String result = StringUtils.trim(sqlParser.parsePlan(sql).visitToString());
        Preconditions.checkArgument(StringUtils.equals(result, StringUtils.trim(expect)), String.format("result: %s\nexpect: %s", result, expect));
    }
}
