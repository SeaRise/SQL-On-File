package com.searise.sof.plan.physics;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
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

    public PhysicalNestedLoopJoin(List<BoundReference> schema, List<Expression> conditions, PhysicalPlan stream, PhysicalPlan build, Context context) {
        super(schema, stream, build, context);
        this.conditions = conditions;
    }

    @Override
    public void resolveIndex() {
        stream.resolveIndex();
        build.resolveIndex();
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

    private void pruneChild(PhysicalPlan child, List<BoundReference> useSchema) {
        List<BoundReference> childSchema = child.schema();
        Map<Long, Integer> childMap = Utils.zip(index -> childSchema.get(index).exprId, childSchema.size());
        List<BoundReference> conditionsUseSchema = Utils.toImmutableList(useSchema.stream().filter(r -> childMap.containsKey(r.exprId)));
        List<BoundReference> parentUseSchema = Utils.toImmutableList(schema.stream().filter(r -> childMap.containsKey(r.exprId)));
        child.prune(Utils.combineDistinct(conditionsUseSchema, parentUseSchema), false);
    }

    @Override
    public PhysicalNestedLoopJoin copyWithNewChildren(List<PhysicalPlan> children) {
        Preconditions.checkArgument(Objects.nonNull(children) && children.size() == 2);
        return new PhysicalNestedLoopJoin(schema, conditions, children.get(0), children.get(1), context);
    }


    @Override
    public Pair<List<Expression>, List<Expression>> joinKeys() {
        Set<Long> streamIds = stream.schema().stream().map(r -> r.exprId).collect(Collectors.toSet());
        Set<Long> buildIds = build.schema().stream().map(r -> r.exprId).collect(Collectors.toSet());
        Triple<List<Expression>, List<Expression>, List<Expression>> splits = splits(conditions, streamIds, buildIds);
        return Pair.of(splits.getLeft(), splits.getMiddle());
    }
}