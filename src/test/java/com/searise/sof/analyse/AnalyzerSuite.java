package com.searise.sof.analyse;

import com.google.common.base.Preconditions;
import com.searise.sof.catalog.TestCatalog;
import com.searise.sof.core.Context;
import com.searise.sof.parser.SqlParser;
import com.searise.sof.plan.logic.LogicalPlan;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class AnalyzerSuite {
    @Test
    public void test() {
        testAnalyse("select 1 as a, a as b from a",
                "Project [literal:1:IntegerType as attribute:4:IntegerType, attribute:0:DoubleType as attribute:0:DoubleType]\n" +
                        "  Relation [a] (attribute:0:DoubleType,attribute:1:DoubleType,attribute:2:DoubleType,attribute:3:DoubleType)");

        testAnalyse("select 1 from a where !(not(true)) and (false or true) " +
                        "and 1 > 2 and 1 <  2 and ('1' = '2') and ('1' == '2') and 1 >= 2 " +
                        "and 1 !> 2 and 1 <= 2 and 1 !< 2 and 1 <> 2 and 1 != 2",
                "Project [literal:1:IntegerType]\n" +
                        "  Filter [literal:false:BooleanType]\n" +
                        "    Relation [a] (attribute:0:DoubleType,attribute:1:DoubleType,attribute:2:DoubleType,attribute:3:DoubleType)");

        testAnalyse("select a.a from a",
                "Project [attribute:0:DoubleType]\n" +
                        "  Relation [a] (attribute:0:DoubleType,attribute:1:DoubleType,attribute:2:DoubleType,attribute:3:DoubleType)");

        testAnalyse("select b.a from a as b",
                "Project [attribute:0:DoubleType]\n" +
                        "  Relation [a, b] (attribute:0:DoubleType,attribute:1:DoubleType,attribute:2:DoubleType,attribute:3:DoubleType)");

        testAnalyse("select -((((1+1)-2)/3)*4) from a",
                "Project [literal:0:IntegerType as attribute:4:IntegerType]\n" +
                        "  Relation [a] (attribute:0:DoubleType,attribute:1:DoubleType,attribute:2:DoubleType,attribute:3:DoubleType)");

        testAnalyse("select 1 as a, a as b from (select a, b from a) a",
                "Project [literal:1:IntegerType as attribute:4:IntegerType, attribute:0:DoubleType as attribute:0:DoubleType]\n" +
                        "  SubqueryAlias [a]\n" +
                        "    Project [attribute:0:DoubleType, attribute:1:DoubleType]\n" +
                        "      Relation [a] (attribute:0:DoubleType,attribute:1:DoubleType,attribute:2:DoubleType,attribute:3:DoubleType)");

        testAnalyse("select 1 from a where a > 1.0 and a < 2.0",
                "Project [literal:1:IntegerType]\n" +
                        "  Filter [attribute:0:DoubleType > literal:1.0:DoubleType:BooleanType, attribute:0:DoubleType < literal:2.0:DoubleType:BooleanType]\n" +
                        "    Relation [a] (attribute:0:DoubleType,attribute:1:DoubleType,attribute:2:DoubleType,attribute:3:DoubleType)");

        testAnalyse("select 1 from a where !(a <= 1.0 or a >= 2.0)",
                "Project [literal:1:IntegerType]\n" +
                        "  Filter [not (attribute:0:DoubleType <= literal:1.0:DoubleType:BooleanType), not (attribute:0:DoubleType >= literal:2.0:DoubleType:BooleanType)]\n" +
                        "    Relation [a] (attribute:0:DoubleType,attribute:1:DoubleType,attribute:2:DoubleType,attribute:3:DoubleType)");

        testAnalyse("select a.a, b.b, a.c, b.d from a as a join a as b on a.a = b.a and a.d > 4.0 and b.c < 11.0",
                "Project [attribute:0:DoubleType, attribute:5:DoubleType, attribute:2:DoubleType, attribute:7:DoubleType]\n" +
                        "  join on (attribute:0:DoubleType == attribute:4:DoubleType:BooleanType, attribute:3:DoubleType > literal:4.0:DoubleType:BooleanType, attribute:6:DoubleType < literal:11.0:DoubleType:BooleanType)\n" +
                        "    Relation [a, a] (attribute:0:DoubleType,attribute:1:DoubleType,attribute:2:DoubleType,attribute:3:DoubleType)\n" +
                        "    Relation [a, b] (attribute:4:DoubleType,attribute:5:DoubleType,attribute:6:DoubleType,attribute:7:DoubleType)");

        testAnalyse("select a.a, b.b, a.c, b.d from a as a join a as b on a.a = b.a where a.d > 4.0 and b.c < 11.0",
                "Project [attribute:0:DoubleType, attribute:5:DoubleType, attribute:2:DoubleType, attribute:7:DoubleType]\n" +
                        "  Filter [attribute:3:DoubleType > literal:4.0:DoubleType:BooleanType, attribute:6:DoubleType < literal:11.0:DoubleType:BooleanType]\n" +
                        "    join on (attribute:0:DoubleType == attribute:4:DoubleType:BooleanType)\n" +
                        "      Relation [a, a] (attribute:0:DoubleType,attribute:1:DoubleType,attribute:2:DoubleType,attribute:3:DoubleType)\n" +
                        "      Relation [a, b] (attribute:4:DoubleType,attribute:5:DoubleType,attribute:6:DoubleType,attribute:7:DoubleType)");

        testAnalyse(
                "select a+b, b-c, c*d, d/a, b % a, -a from (select (a+1.0) as a, (b+1.0) as b, (c+1.0) as c, (d+1.0) as d from a) a",
                "Project [(attribute:4:DoubleType + attribute:5:DoubleType) as attribute:8:DoubleType, (attribute:5:DoubleType - attribute:6:DoubleType) as attribute:9:DoubleType, (attribute:6:DoubleType * attribute:7:DoubleType) as attribute:10:DoubleType, (attribute:7:DoubleType / attribute:4:DoubleType) as attribute:11:DoubleType, (attribute:5:DoubleType % attribute:4:DoubleType) as attribute:12:DoubleType, (-attribute:4:DoubleType) as attribute:13:DoubleType]\n" +
                        "  SubqueryAlias [a]\n" +
                        "    Project [(attribute:0:DoubleType + literal:1.0:DoubleType) as attribute:4:DoubleType, (attribute:1:DoubleType + literal:1.0:DoubleType) as attribute:5:DoubleType, (attribute:2:DoubleType + literal:1.0:DoubleType) as attribute:6:DoubleType, (attribute:3:DoubleType + literal:1.0:DoubleType) as attribute:7:DoubleType]\n" +
                        "      Relation [a] (attribute:0:DoubleType,attribute:1:DoubleType,attribute:2:DoubleType,attribute:3:DoubleType)"
        );

        testAnalyse("select 1+1 > 2 as a, a as b from a where 1 > 2 and 2 < (1+3)",
                "Project [literal:false:BooleanType as attribute:5:BooleanType, attribute:0:DoubleType as attribute:0:DoubleType]\n" +
                        "  Filter [literal:false:BooleanType]\n" +
                        "    Relation [a] (attribute:0:DoubleType,attribute:1:DoubleType,attribute:2:DoubleType,attribute:3:DoubleType)");

        testAnalyse("select (a+1)+1 from a",
                "Project [((attribute:0:DoubleType + literal:1.0:DoubleType) + literal:1.0:DoubleType) as attribute:4:DoubleType]\n" +
                        "  Relation [a] (attribute:0:DoubleType,attribute:1:DoubleType,attribute:2:DoubleType,attribute:3:DoubleType)");
    }

    private void testAnalyse(String sql, String expect) {
        SqlParser sqlParser = new SqlParser(new Context());
        Analyzer analyzer = new Analyzer(new TestCatalog());
        LogicalPlan parsePlan = sqlParser.parsePlan(sql);
        String result = StringUtils.trim(analyzer.analyse(parsePlan).visitToString());
        Preconditions.checkArgument(StringUtils.equals(result, StringUtils.trim(expect)), String.format("result: %s\nexpect: %s", result, expect));
    }
}
