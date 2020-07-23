package com.searise.sof.plan.physics;

import com.google.common.collect.ImmutableList;
import com.searise.sof.core.Context;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.Attribute;
import com.searise.sof.expression.attribute.BoundReference;
import com.searise.sof.expression.compare.EqualTo;
import com.searise.sof.optimize.afterprocess.ReferenceResolveHelper;
import com.searise.sof.optimize.afterprocess.SchemaPruneHelper;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PhysicalHashJoin implements PhysicalPlan {
    public List<BoundReference> schema;
    public List<Expression> otherConditions;
    public List<Expression> streamJoinKeys;
    public List<Expression> buildJoinKeys;
    public final PhysicalPlan stream;
    public final PhysicalPlan build;
    public final Context context;

    public PhysicalHashJoin(List<BoundReference> schema, List<Expression> conditions, PhysicalPlan stream, PhysicalPlan build, Context context) {
        this.schema = schema;
        this.stream = stream;
        this.build = build;
        this.context = context;

        Set<Long> streamIds = stream.schema().stream().map(r -> r.exprId).collect(Collectors.toSet());
        Set<Long> buildIds = build.schema().stream().map(r -> r.exprId).collect(Collectors.toSet());
        Triple<List<Expression>, List<Expression>, List<Expression>> splits = splits(conditions, streamIds, buildIds);
        streamJoinKeys = splits.getLeft();
        buildJoinKeys = splits.getMiddle();
        otherConditions = splits.getRight();
        Utils.checkArgument(streamJoinKeys.size() == buildJoinKeys.size(), "streamJoinKeys.size must equal to buildJoinKeys.size");
    }

    // streamKeys, buildKeys, otherConditions
    private Triple<List<Expression>, List<Expression>, List<Expression>> splits(List<Expression> conditions, Set<Long> streamIds, Set<Long> buildIds) {
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
}