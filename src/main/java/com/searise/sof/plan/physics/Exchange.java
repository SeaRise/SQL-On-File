package com.searise.sof.plan.physics;

import com.google.common.collect.ImmutableList;
import com.searise.sof.core.conf.SofConf;
import com.searise.sof.core.SofContext;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.BoundReference;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Exchange implements PhysicalPlan {
    // 并非children
    // Exchange.children().size() == 0
    public final PhysicalPlan mapPlan;
    public final SofContext context;
    public final List<Expression> keys;

    public final long shuffleId;

    public Exchange(PhysicalPlan mapPlan, SofContext context, List<Expression> keys) {
        this.mapPlan = mapPlan;
        this.context = context;
        this.keys = keys;

        this.shuffleId = context.shuffleIdBuilder.newShuffleId();
    }

    @Override
    public List<BoundReference> schema() {
        return mapPlan.schema();
    }

    @Override
    public void resolveIndex() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void prune(List<BoundReference> father, boolean isTop) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SofContext context() {
        return context;
    }

    @Override
    public String toString() {
        return String.format("Exchange [%s] [%s] [%s]", schemaToString(),
                keys.stream().map(Object::toString).collect(Collectors.joining(", ")), shuffleId);
    }

    @Override
    public List<PhysicalPlan> toStringChildren() {
        return ImmutableList.of(mapPlan);
    }

    @Override
    public Exchange copyWithNewChildren(List<PhysicalPlan> children) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int partitions() {
        if (mapPlan.partitions() <= 0) {
            return 0;
        }

        // 对于没有key的,只有一个partition.
        if (Objects.isNull(keys) || keys.size() <= 0) {
            return 1;
        }
        // 对于常量值的keys, 只有一个partition.
        if (keys.stream().allMatch(Expression::foldable)) {
            return 1;
        }
        return context.conf.getConf(SofConf.MAX_PARALLELISM);
    }
}