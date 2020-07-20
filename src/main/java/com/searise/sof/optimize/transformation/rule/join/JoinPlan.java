package com.searise.sof.optimize.transformation.rule.join;

import com.searise.sof.optimize.Group;
import com.searise.sof.stats.SizeInBytesStatsVisitor;

import java.math.BigInteger;
import java.util.Set;

public class JoinPlan {
    public final Group group;
    public final BigInteger cost;
    public final Set<Integer> leaves;

    public JoinPlan(Group group, BigInteger cost, Set<Integer> leaves) {
        this.group = group;
        this.cost = cost;
        this.leaves = leaves;
    }

    public BigInteger sizeInBytes() {
        return SizeInBytesStatsVisitor.visit(group).sizeInBytes;
    }
}
