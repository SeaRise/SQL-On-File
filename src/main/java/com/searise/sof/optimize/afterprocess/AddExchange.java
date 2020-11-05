package com.searise.sof.optimize.afterprocess;

import com.google.common.collect.ImmutableList;
import com.searise.sof.analyse.Applicable;
import com.searise.sof.analyse.Rule;
import com.searise.sof.core.Context;
import com.searise.sof.expression.Expression;
import com.searise.sof.plan.physics.Exchange;
import com.searise.sof.plan.physics.PhysicalJoin;
import com.searise.sof.plan.physics.PhysicalPlan;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class AddExchange implements Rule<PhysicalPlan> {
    @Override
    public PhysicalPlan apply(PhysicalPlan plan) {
        return plan.transformDown((Applicable<PhysicalPlan>) p -> {
            if (p instanceof PhysicalJoin) {
                PhysicalJoin join = (PhysicalJoin) p;
                Pair<List<Expression>, List<Expression>> joinKeys = join.joinKeys();
                PhysicalPlan newStream = newExchange(join.stream, join.context, joinKeys.getLeft());
                PhysicalPlan newBuild = newExchange(join.build, join.context, joinKeys.getRight());
                return join.copyWithNewChildren(ImmutableList.of(newStream, newBuild));
            }
            return p;
        });
    }

    private PhysicalPlan newExchange(PhysicalPlan plan, Context context, List<Expression> keys) {
        // partitions <= 0没有shuffle的必要.
        if (plan.partitions() <= 0) {
            return plan;
        }
        Exchange exchange = new Exchange(plan, context, keys);
        if (exchange.partitions() == 1 && plan.partitions() == 1) {
            // exchange和plan的partitions都为1, 说明没有shuffle.
            return plan;
        }
        return exchange;
    }
}
