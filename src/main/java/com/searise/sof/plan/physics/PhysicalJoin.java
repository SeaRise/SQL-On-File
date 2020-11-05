package com.searise.sof.plan.physics;

import com.google.common.collect.ImmutableList;
import com.searise.sof.core.Context;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.Attribute;
import com.searise.sof.expression.attribute.BoundReference;
import com.searise.sof.expression.compare.EqualTo;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.Set;

public abstract class PhysicalJoin implements PhysicalPlan {
    public List<BoundReference> schema;
    public final PhysicalPlan stream;
    public final PhysicalPlan build;
    public final Context context;

    public PhysicalJoin(List<BoundReference> schema, PhysicalPlan stream, PhysicalPlan build, Context context) {
        this.schema = schema;
        this.stream = stream;
        this.build = build;
        this.context = context;
    }

    @Override
    public Context context() {
        return context;
    }

    @Override
    public List<BoundReference> schema() {
        return schema;
    }

    @Override
    public List<PhysicalPlan> children() {
        return ImmutableList.of(stream, build);
    }

    // streamKeys, buildKeys, otherConditions
    protected Triple<List<Expression>, List<Expression>, List<Expression>> splits(List<Expression> conditions, Set<Long> streamIds, Set<Long> buildIds) {
        ImmutableList.Builder<Expression> otherCondBuilder = ImmutableList.builder();
        ImmutableList.Builder<Expression> streamKeyBuilder = ImmutableList.builder();
        ImmutableList.Builder<Expression> buildKeyBuilder = ImmutableList.builder();
        for (Expression condition : conditions) {
            boolean isOtherCond = true;
            if (condition.getClass() == EqualTo.class) {
                EqualTo equalTo = (EqualTo) condition;
                if (equalTo.left.getClass() == Attribute.class && equalTo.right.getClass() == Attribute.class) {
                    Attribute attr1 = (Attribute) equalTo.left;
                    Attribute attr2 = (Attribute) equalTo.right;
                    if (streamIds.contains(attr1.exprId) && buildIds.contains(attr2.exprId)) {
                        streamKeyBuilder.add(attr1);
                        buildKeyBuilder.add(attr2);
                        isOtherCond = false;
                    } else if (streamIds.contains(attr2.exprId) && buildIds.contains(attr1.exprId)) {
                        streamKeyBuilder.add(attr2);
                        buildKeyBuilder.add(attr1);
                        isOtherCond = false;
                    } else {
                        // just else.
                    }
                }
            }
            if (isOtherCond) {
                otherCondBuilder.add(condition);
            }
        }
        return Triple.of(streamKeyBuilder.build(), buildKeyBuilder.build(), otherCondBuilder.build());
    }

    public abstract Pair<List<Expression>, List<Expression>> joinKeys();
}
