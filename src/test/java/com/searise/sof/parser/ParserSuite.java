package com.searise.sof.parser;

import com.google.common.base.Preconditions;
import com.searise.sof.core.Driver;
import com.searise.sof.catalog.Catalog;
import com.searise.sof.catalog.TestCatalog;
import com.searise.sof.core.Context;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class ParserSuite {

    @Test
    public void test() {
        testSql("select a.a from a",
                "Project [UnresolvedAttribute:'a.a]\n" +
                        "  UnresolvedRelation [a]");

        testSql("select 1+1, 1-1, 1*1, 1/1, 1%1, -(5) from a",
                "Project [+(literal:1:IntegerType, literal:1:IntegerType), -(literal:1:IntegerType, literal:1:IntegerType), *(literal:1:IntegerType, literal:1:IntegerType), /(literal:1:IntegerType, literal:1:IntegerType), %(literal:1:IntegerType, literal:1:IntegerType), -(literal:5:IntegerType)]\n" +
                        "  UnresolvedRelation [a]");

        testSql("select b.a from a as b",
                "Project [UnresolvedAttribute:'b.a]\n" +
                        "  UnresolvedRelation [a, b]");

        testSql("select a as c from a as b",
                "Project [UnresolvedAttribute:'a as UnresolvedAttribute:'c]\n" +
                        "  UnresolvedRelation [a, b]");

        testSql("select a, b, c from b where 'a' = 'v' and a > 'c'",
                "Project [UnresolvedAttribute:'a, UnresolvedAttribute:'b, UnresolvedAttribute:'c]\n" +
                        "  Filter [and(=(literal:a:StringType, literal:v:StringType), >(UnresolvedAttribute:'a, literal:c:StringType))]\n" +
                        "    UnresolvedRelation [b]");

        testSql("select 'a', 0.5, c from b where 'a' >= 'v' or a > 'c'",
                "Project [literal:a:StringType, literal:0.5:DoubleType, UnresolvedAttribute:'c]\n" +
                        "  Filter [or(>=(literal:a:StringType, literal:v:StringType), >(UnresolvedAttribute:'a, literal:c:StringType))]\n" +
                        "    UnresolvedRelation [b]");

        testSql("select a, b, c from (select a from b) a where 'a' = 'v' and a = 'c'",
                "Project [UnresolvedAttribute:'a, UnresolvedAttribute:'b, UnresolvedAttribute:'c]\n" +
                        "  Filter [and(=(literal:a:StringType, literal:v:StringType), =(UnresolvedAttribute:'a, literal:c:StringType))]\n" +
                        "    SubqueryAlias [a]\n" +
                        "      Project [UnresolvedAttribute:'a]\n" +
                        "        UnresolvedRelation [b]");

        testSql("select a, b, c from (select a from b, c, d) a where 'a' = 'v' and a = 'c'",
                "Project [UnresolvedAttribute:'a, UnresolvedAttribute:'b, UnresolvedAttribute:'c]\n" +
                        "  Filter [and(=(literal:a:StringType, literal:v:StringType), =(UnresolvedAttribute:'a, literal:c:StringType))]\n" +
                        "    SubqueryAlias [a]\n" +
                        "      Project [UnresolvedAttribute:'a]\n" +
                        "        join\n" +
                        "          join\n" +
                        "            UnresolvedRelation [b]\n" +
                        "            UnresolvedRelation [c]\n" +
                        "          UnresolvedRelation [d]");

        testSql("select a, b, c from a join b on a.a = b.b join c on a.a = c.c",
                "Project [UnresolvedAttribute:'a, UnresolvedAttribute:'b, UnresolvedAttribute:'c]\n" +
                        "  join on (=(UnresolvedAttribute:'a.a, UnresolvedAttribute:'c.c))\n" +
                        "    join on (=(UnresolvedAttribute:'a.a, UnresolvedAttribute:'b.b))\n" +
                        "      UnresolvedRelation [a]\n" +
                        "      UnresolvedRelation [b]\n" +
                        "    UnresolvedRelation [c]");

        testSql("select a, b, c from a join b join c",
                "Project [UnresolvedAttribute:'a, UnresolvedAttribute:'b, UnresolvedAttribute:'c]\n" +
                        "  join\n" +
                        "    UnresolvedRelation [a]\n" +
                        "    join\n" +
                        "      UnresolvedRelation [b]\n" +
                        "      UnresolvedRelation [c]");

        testSql(
                "select a+b, b-c, c*d, d/a, b % a, -a from (select (a+1.0) as a, (b+1.0) as b, (c+1.0) as c, (d+1.0) as d from a) a",
                "Project [+(UnresolvedAttribute:'a, UnresolvedAttribute:'b), -(UnresolvedAttribute:'b, UnresolvedAttribute:'c), *(UnresolvedAttribute:'c, UnresolvedAttribute:'d), /(UnresolvedAttribute:'d, UnresolvedAttribute:'a), %(UnresolvedAttribute:'b, UnresolvedAttribute:'a), -(UnresolvedAttribute:'a)]\n" +
                        "  SubqueryAlias [a]\n" +
                        "    Project [+(UnresolvedAttribute:'a, literal:1.0:DoubleType) as UnresolvedAttribute:'a, +(UnresolvedAttribute:'b, literal:1.0:DoubleType) as UnresolvedAttribute:'b, +(UnresolvedAttribute:'c, literal:1.0:DoubleType) as UnresolvedAttribute:'c, +(UnresolvedAttribute:'d, literal:1.0:DoubleType) as UnresolvedAttribute:'d]\n" +
                        "      UnresolvedRelation [a]"
        );
    }

    private void testSql(String sql, String expect) {
        Catalog catalog = new TestCatalog();
        SqlParser sqlParser = new SqlParser(new Context(catalog, new Driver()));
        String result = StringUtils.trim(sqlParser.parsePlan(sql).visitToString());
        Preconditions.checkArgument(StringUtils.equals(result, StringUtils.trim(expect)), String.format("result: %s\nexpect: %s", result, expect));
    }
}
