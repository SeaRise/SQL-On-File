package com.searise.sof.plan.physics;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.searise.sof.core.SofContext;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.BoundReference;
import com.searise.sof.optimize.afterprocess.ReferenceResolveHelper;
import com.searise.sof.optimize.afterprocess.SchemaPruneHelper;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class PhysicalFilter implements PhysicalPlan {
    public List<BoundReference> schema;
    public List<Expression> conditions;
    public final PhysicalPlan child;
    public final SofContext context;

    public PhysicalFilter(List<BoundReference> schema, List<Expression> conditions, PhysicalPlan child, SofContext context) {
        this.schema = schema;
        this.conditions = conditions;
        this.child = child;
        this.context = context;
    }

    @Override
    public List<BoundReference> schema() {
        return schema;
    }

    @Override
    public List<PhysicalPlan> children() {
        return ImmutableList.of(child);
    }

    @Override
    public SofContext context() {
        return context;
    }

    @Override
    public void resolveIndex() {
        child.resolveIndex();
        List<BoundReference> childSchema = child.schema();
        Map<Long, Integer> inputs = Utils.zip(index -> childSchema.get(index).exprId, childSchema.size());
        this.conditions = ReferenceResolveHelper.resolveExpression(conditions, inputs);
        ReferenceResolveHelper.resolveSchema(schema, inputs);
    }

    @Override
    public String toString() {
        return String.format("PhysicalFilter [%s] [%s]", schemaToString(), conditions.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }

    @Override
    public void prune(List<BoundReference> father, boolean isTop) {
        schema = isTop ? SchemaPruneHelper.copy(schema) : SchemaPruneHelper.copy(father);
        child.prune(Utils.combineDistinct(SchemaPruneHelper.extractUseSchema(conditions), schema), false);
    }

    @Override
    public PhysicalFilter copyWithNewChildren(List<PhysicalPlan> children) {
        Preconditions.checkArgument(Objects.nonNull(children) && children.size() == 1);
        return new PhysicalFilter(schema, conditions, children.get(0), context);
    }
}
