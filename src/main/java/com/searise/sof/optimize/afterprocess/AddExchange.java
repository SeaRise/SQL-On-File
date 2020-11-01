package com.searise.sof.optimize.afterprocess;

import com.google.common.collect.ImmutableList;
import com.searise.sof.analyse.Applicable;
import com.searise.sof.analyse.Rule;
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
                Exchange streamExchange = new Exchange(join.stream, join.context, joinKeys.getLeft());
                Exchange buildExchange = new Exchange(join.build, join.context, joinKeys.getRight());
                return join.copyWithNewChildren(ImmutableList.of(streamExchange, buildExchange));
            }
            return p;
        });
    }
}
