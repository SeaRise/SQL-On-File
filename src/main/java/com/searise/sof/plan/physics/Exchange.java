package com.searise.sof.plan.physics;

import com.searise.sof.core.Conf;
import com.searise.sof.core.Context;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.BoundReference;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Exchange implements PhysicalPlan {
    // 并非children
    // Exchange.children().size() == 0
    public final PhysicalPlan mapPlan;
    public final Context context;
    public final List<Expression> keys;

    public final long shuffleId;

    public Exchange(PhysicalPlan mapPlan, Context context, List<Expression> keys) {
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
    public Context context() {
        return context;
    }

    @Override
    public String toString() {
        return String.format("Exchange [%s] [%s]", schemaToString(),
                keys.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }

    @Override
    public Exchange copyWithNewChildren(List<PhysicalPlan> children) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int partitions() {
        // 对于没有key的,只有一个partition.
        if (Objects.isNull(keys) || keys.size() <= 0) {
            return 1;
        }
        return context.conf.getIntConf(Conf.MAX_PARALLELISM);
    }
}