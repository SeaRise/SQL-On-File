package com.searise.sof.stats;

import com.searise.sof.core.SofException;
import com.searise.sof.core.Utils;
import com.searise.sof.optimize.Group;
import com.searise.sof.optimize.GroupExpr;
import com.searise.sof.optimize.iter.Iterator;
import com.searise.sof.plan.logic.*;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

public class SizeInBytesStatsVisitor {
    private SizeInBytesStatsVisitor() {
    }

    public static Statistics visit(Group group) {
        if (group.stats.isPresent()) {
            return group.stats.get();
        }

        Iterator<GroupExpr> iterator = group.iter().newReadOnlyIter();
        if (!iterator.hasNext()) {
            return Statistics.EMPTY;
        }
        Statistics stats = visit(iterator.next());
        group.stats = Optional.of(stats);
        return stats;
    }

    private static Statistics visit(GroupExpr groupExpr) {
        List<Statistics> children = Utils.toImmutableList(groupExpr.children.stream().map(SizeInBytesStatsVisitor::visit));
        LogicalPlan logicalPlan = groupExpr.exprNode;
        return visit(logicalPlan, children);
    }

    private static Statistics visit(LogicalPlan logicalPlan, List<Statistics> children) {
        String planType = logicalPlan.getClass().getSimpleName();
        switch (planType) {
            case "Project":
                return visitProject((Project) logicalPlan, children);
            case "Filter":
                return visitFilter((Filter) logicalPlan, children);
            case "InnerJoin":
                return visitInnerJoin((InnerJoin) logicalPlan, children);
            case "Relation":
                return visitRelation((Relation) logicalPlan, children);
            default:
                throw new SofException(String.format("unsupported plan type `%s` in SizeInBytesStatsVisitor", planType));
        }
    }

    private static Statistics visitProject(Project project, List<Statistics> children) {
        return children.get(0);
    }

    private static Statistics visitInnerJoin(InnerJoin join, List<Statistics> children) {
        double selectivity = ConditionEstimation.estimateSelectivity(join.conditions);

        BigInteger leftChildSizeInBytes = children.get(0).sizeInBytes;
        BigInteger rightChildSizeInBytes = children.get(1).sizeInBytes;
        BigInteger childSizeInBytes = leftChildSizeInBytes.multiply(rightChildSizeInBytes);

        BigInteger sizeInBytes = ceil(new BigDecimal(childSizeInBytes).multiply(new BigDecimal(selectivity)));
        return new Statistics(sizeInBytes);
    }

    private static Statistics visitFilter(Filter filter, List<Statistics> children) {
        double selectivity = ConditionEstimation.estimateSelectivity(filter.conditions);
        BigInteger childSizeInBytes = children.get(0).sizeInBytes;
        BigInteger sizeInBytes = ceil(new BigDecimal(childSizeInBytes).multiply(new BigDecimal(selectivity)));
        return new Statistics(sizeInBytes);
    }

    private static BigInteger ceil(BigDecimal bigDecimal) {
        return bigDecimal.setScale(0, RoundingMode.CEILING).toBigInteger();
    }

    private static Statistics visitRelation(Relation relation, List<Statistics> children) {
        String filePath = relation.catalogTable.filePath;
        BigInteger sizeInBytes = new BigInteger("0");
        for (File file : Utils.listFiles(new File(filePath))) {
            sizeInBytes = sizeInBytes.add(new BigInteger(file.length() + ""));
        }
        return new Statistics(sizeInBytes);
    }
}
