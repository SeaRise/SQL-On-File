package com.searise.sof.optimize.transformation;

import com.google.common.base.Preconditions;
import com.searise.sof.core.SofSession;
import com.searise.sof.analyse.Analyzer;
import com.searise.sof.catalog.TestCatalog;
import com.searise.sof.core.SofContext;
import com.searise.sof.optimize.Group;
import com.searise.sof.optimize.GroupExpr;
import com.searise.sof.optimize.Operand;
import com.searise.sof.optimize.preprocess.PreprocessRule;
import com.searise.sof.parser.SqlParser;
import com.searise.sof.plan.logic.LogicalPlan;
import org.junit.Test;

import java.util.Optional;

public class ExprIterSuite {
    @Test
    public void testSql() {
        doTestSql(
                "select a from a",
                new Pattern(Operand.OperandProject,
                        new Pattern(Operand.OperandRelation)
                ),
                1);

        doTestSql(
                "select a.a from a join c on a.a = c.c",
                new Pattern(Operand.OperandProject,
                        new Pattern(Operand.OperandJoin,
                                new Pattern(Operand.OperandRelation),
                                new Pattern(Operand.OperandRelation)
                        )
                ),
                1);

        doTestSql(
                "select a from a where a > 10",
                new Pattern(Operand.OperandProject,
                        new Pattern(Operand.OperandFilter,
                                new Pattern(Operand.OperandRelation))
                ),
                1);
    }

    private void doTestSql(String sql, Pattern pattern, int count) {
        LogicalPlan parsePlan = new SqlParser(SofContext.getOrCreate()).parsePlan(sql);
        LogicalPlan logicalPlan = new Analyzer(new TestCatalog()).analyse(parsePlan);
        for (PreprocessRule preprocessRule : PreprocessRule.preprocessRules) {
            logicalPlan = preprocessRule.apply(logicalPlan);
        }
        Group rootGroup = Group.convert2Group(logicalPlan);
        GroupExpr groupExpr = rootGroup.iter().next();
        Optional<ExprIter> iter = ExprIter.newExprIter(groupExpr, pattern);
        Preconditions.checkArgument(iter.isPresent());
        int matchCount = 0;
        do {
            matchCount++;
        } while (iter.get().next());
        Preconditions.checkArgument(matchCount == count, String.format("expect: %d, result: %d", count, matchCount));
    }
}