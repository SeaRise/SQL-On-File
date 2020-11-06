package com.searise.sof.plan.physics;

import com.google.common.base.Preconditions;
import com.searise.sof.core.Context;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.BoundReference;
import com.searise.sof.optimize.afterprocess.ReferenceResolveHelper;
import com.searise.sof.optimize.afterprocess.SchemaPruneHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class PhysicalNestedLoopJoin extends PhysicalJoin {
    public List<Expression> conditions;
    private List<Expression> streamJoinKeys;
    private List<Expression> buildJoinKeys;

    public PhysicalNestedLoopJoin(List<BoundReference> schema, List<Expression> conditions, PhysicalPlan stream, PhysicalPlan build, Context context) {
        super(schema, stream, build, context);
        this.conditions = conditions;

        Triple<List<Expression>, List<Expression>, List<Expression>> splits = splits(conditions);
        streamJoinKeys = splits.getLeft();
        buildJoinKeys = splits.getMiddle();
    }

    private PhysicalNestedLoopJoin(
            List<BoundReference> schema, List<Expression> conditions,
            List<Expression> streamJoinKeys, List<Expression> buildJoinKeys,
            PhysicalPlan stream, PhysicalPlan build, Context context) {
        super(schema, stream, build, context);
        this.conditions = conditions;

        this.streamJoinKeys = streamJoinKeys;
        this.buildJoinKeys = buildJoinKeys;
    }

    @Override
    public void resolveIndex() {
        stream.resolveIndex();
        build.resolveIndex();

        Pair<List<Expression>, List<Expression>> resolveKeys = resolveJoinKeys(streamJoinKeys, buildJoinKeys);
        this.streamJoinKeys = resolveKeys.getLeft();
        this.buildJoinKeys = resolveKeys.getRight();

        List<BoundReference> childSchema = Utils.combineDistinct(stream.schema(), build.schema());
        Map<Long, Integer> inputs = Utils.zip(index -> childSchema.get(index).exprId, childSchema.size());
        this.conditions = ReferenceResolveHelper.resolveExpression(conditions, inputs);
        ReferenceResolveHelper.resolveSchema(schema, inputs);
    }

    @Override
    public String toString() {
        return String.format("PhysicalNestedLoopJoin [%s] [%s]", schemaToString(), conditions.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }

    @Override
    public void prune(List<BoundReference> father, boolean isTop) {
        schema = isTop ? SchemaPruneHelper.copy(schema) : SchemaPruneHelper.copy(father);

        List<BoundReference> useSchema = SchemaPruneHelper.extractUseSchema(conditions);
        pruneChild(stream, useSchema);
        pruneChild(build, useSchema);
    }

    @Override
    public PhysicalNestedLoopJoin copyWithNewChildren(List<PhysicalPlan> children) {
        Preconditions.checkArgument(Objects.nonNull(children) && children.size() == 2);
        return new PhysicalNestedLoopJoin(schema, conditions, streamJoinKeys, buildJoinKeys, children.get(0), children.get(1), context);
    }

    @Override
    public Pair<List<Expression>, List<Expression>> joinKeys() {
        return Pair.of(streamJoinKeys, buildJoinKeys);
    }
}