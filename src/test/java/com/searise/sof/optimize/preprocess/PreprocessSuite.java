package com.searise.sof.optimize.preprocess;

import com.searise.sof.analyse.Analyzer;
import com.searise.sof.analyse.Applicable;
import com.searise.sof.catalog.TestCatalog;
import com.searise.sof.core.Context;
import com.searise.sof.parser.SqlParser;
import com.searise.sof.plan.logic.LogicalPlan;
import com.searise.sof.plan.logic.SubqueryAlias;
import org.junit.Test;

public class PreprocessSuite {
    @Test
    public void testRemoveSubqueryAlias() {
        doTestRemoveSubqueryAlias("select 1 as a, a as b from (select a, b from a) a");
        doTestRemoveSubqueryAlias("select 1 as a, a.a as b from (select a, b from a) a join c on a.a = c.a");
        doTestRemoveSubqueryAlias("select a from (select a from (select a from a) a) a join (select a from (select a from a) a) c");
    }

    private void doTestRemoveSubqueryAlias(String sql) {
        LogicalPlan parsePlan = new SqlParser(new Context()).parsePlan(sql);
        LogicalPlan analyzePlan = new Analyzer(new TestCatalog()).analyse(parsePlan);
        LogicalPlan logicalPlan = new RemoveSubqueryAlias().apply(analyzePlan);
        logicalPlan.transformDown((Applicable<LogicalPlan>) plan -> {
            if (plan.getClass() == SubqueryAlias.class) {
                throw new IllegalArgumentException();
            }
            return plan;
        });
    }
}
