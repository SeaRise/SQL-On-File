package com.searise.sof.plan.physics;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.searise.sof.core.Context;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.Attribute;
import com.searise.sof.expression.attribute.BoundReference;
import com.searise.sof.expression.compare.EqualTo;
import com.searise.sof.optimize.afterprocess.ReferenceResolveHelper;
import com.searise.sof.optimize.afterprocess.SchemaPruneHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class PhysicalHashJoin extends PhysicalJoin {
    public List<Expression> otherConditions;
    public List<Expression> streamJoinKeys;
    public List<Expression> buildJoinKeys;

    public PhysicalHashJoin(List<BoundReference> schema, List<Expression> conditions, PhysicalPlan stream, PhysicalPlan build, Context context) {
        super(schema, stream, build, context);

        Set<Long> streamIds = stream.schema().stream().map(r -> r.exprId).collect(Collectors.toSet());
        Set<Long> buildIds = build.schema().stream().map(r -> r.exprId).collect(Collectors.toSet());
        Triple<List<Expression>, List<Expression>, List<Expression>> splits = splits(conditions, streamIds, buildIds);
        streamJoinKeys = splits.getLeft();
        buildJoinKeys = splits.getMiddle();
        otherConditions = splits.getRight();
        Utils.checkArgument(streamJoinKeys.size() == buildJoinKeys.size(), "streamJoinKeys.size must equal to buildJoinKeys.size");
    }

    private PhysicalHashJoin(
            List<BoundReference> schema,
            PhysicalPlan stream,
            PhysicalPlan build,
            List<Expression> otherConditions,
            List<Expression> streamJoinKeys,
            List<Expression> buildJoinKeys,
            Context context) {
        super(schema, stream, build, context);
        this.streamJoinKeys = streamJoinKeys;
        this.buildJoinKeys = buildJoinKeys;
        this.otherConditions = otherConditions;
    }

    @Override
    public void resolveIndex() {
        stream.resolveIndex();
        build.resolveIndex();

        Map<Long, Integer> streamInputs = Utils.zip(index -> stream.schema().get(index).exprId, stream.schema().size());
        this.streamJoinKeys = ReferenceResolveHelper.resolveExpression(streamJoinKeys, streamInputs);

        Map<Long, Integer> buildInputs = Utils.zip(index -> build.schema().get(index).exprId, build.schema().size());
        this.buildJoinKeys = ReferenceResolveHelper.resolveExpression(buildJoinKeys, buildInputs);

        List<BoundReference> childSchema = Utils.combineDistinct(stream.schema(), build.schema());
        Map<Long, Integer> inputs = Utils.zip(index -> childSchema.get(index).exprId, childSchema.size());
        this.otherConditions = ReferenceResolveHelper.resolveExpression(otherConditions, inputs);

        ReferenceResolveHelper.resolveSchema(schema, inputs);
    }

    @Override
    public String toString() {
        return String.format("PhysicalHashJoin [%s], stream: [%s], build: [%s], others: [%s] ", schemaToString(),
                streamJoinKeys.stream().map(Object::toString).collect(Collectors.joining(", "))
                , buildJoinKeys.stream().map(Object::toString).collect(Collectors.joining(", "))
                , otherConditions.stream().map(Object::toString).collect(Collectors.joining(", "))
        );
    }

    @Override
    public void prune(List<BoundReference> father, boolean isTop) {
        schema = isTop ? SchemaPruneHelper.copy(schema) : SchemaPruneHelper.copy(father);

        List<BoundReference> useSchema = Utils.combineDistinct(
                SchemaPruneHelper.extractUseSchema(streamJoinKeys)
                , SchemaPruneHelper.extractUseSchema(buildJoinKeys)
                , SchemaPruneHelper.extractUseSchema(otherConditions)
        );
        pruneChild(stream, useSchema);
        pruneChild(build, useSchema);
    }

    private void pruneChild(PhysicalPlan child, List<BoundReference> useSchema) {
        List<BoundReference> childSchema = child.schema();
        Map<Long, Integer> childMap = Utils.zip(index -> childSchema.get(index).exprId, childSchema.size());
        List<BoundReference> conditionsUseSchema = Utils.toImmutableList(useSchema.stream().filter(r -> childMap.containsKey(r.exprId)));
        List<BoundReference> parentUseSchema = Utils.toImmutableList(schema.stream().filter(r -> childMap.containsKey(r.exprId)));
        child.prune(Utils.combineDistinct(conditionsUseSchema, parentUseSchema), false);
    }

    @Override
    public PhysicalHashJoin copyWithNewChildren(List<PhysicalPlan> children) {
        Preconditions.checkArgument(Objects.nonNull(children) && children.size() == 2);
        return new PhysicalHashJoin(schema, children.get(0), children.get(1), otherConditions, streamJoinKeys, buildJoinKeys, context);
    }

    @Override
    public Pair<List<Expression>, List<Expression>> joinKeys() {
        return Pair.of(streamJoinKeys, buildJoinKeys);
    }
}