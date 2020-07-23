package com.searise.sof.plan.physics;

import com.google.common.collect.ImmutableList;
import com.searise.sof.core.Context;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.BoundReference;
import com.searise.sof.optimize.afterprocess.ReferenceResolveHelper;
import com.searise.sof.optimize.afterprocess.SchemaPruneHelper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PhysicalNestedLoopJoin implements PhysicalPlan {
    public List<BoundReference> schema;
    public List<Expression> conditions;
    public final PhysicalPlan stream;
    public final PhysicalPlan build;
    public final Context context;

    public PhysicalNestedLoopJoin(List<BoundReference> schema, List<Expression> conditions, PhysicalPlan stream, PhysicalPlan build, Context context) {
        this.schema = schema;
        this.conditions = conditions;
        this.stream = stream;
        this.build = build;
        this.context = context;
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
    public Context context() {
        return context;
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
}