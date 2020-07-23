package com.searise.sof.plan.physics;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.searise.sof.core.Context;
import com.searise.sof.core.SofException;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.Alias;
import com.searise.sof.expression.attribute.Attribute;
import com.searise.sof.expression.attribute.BoundReference;
import com.searise.sof.optimize.afterprocess.ReferenceResolveHelper;
import com.searise.sof.optimize.afterprocess.RemoveAliasHelper;
import com.searise.sof.optimize.afterprocess.SchemaPruneHelper;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class PhysicalProject implements PhysicalPlan {
    public List<BoundReference> schema;
    public List<Expression> projectList;
    public final PhysicalPlan child;
    public final Context context;

    public PhysicalProject(List<BoundReference> schema, List<Expression> projectList, PhysicalPlan child, Context context) {
        this.schema = schema;
        this.projectList = projectList;
        this.child = child;
        this.context = context;
    }

    @Override
    public List<BoundReference> schema() {
        return schema;
    }

    @Override
    public void resolveIndex() {
        Utils.checkArgument(schema.size() == projectList.size(), "projectList.size must equal to schema.size in resolveIndex");
        // 在resolveIndex的时候,schema的数量,顺序和projectList是一致的.
        for (int i = 0; i < this.schema.size(); i++) {
            schema.get(i).resolveIndex(i);
        }
        child.resolveIndex();
        List<BoundReference> childSchema = child.schema();
        this.projectList = ReferenceResolveHelper.resolveExpression(projectList, Utils.zip(index -> childSchema.get(index).exprId, childSchema.size()));
    }

    private void resolveSchema() {
        Map<Long, Integer> exprIds = Utils.zip(index -> schema.get(index).exprId, schema.size());
        for (int i = 0; i < projectList.size(); i++) {
            Expression expression = projectList.get(i);
            if (expression.getClass() == Attribute.class) {
                resolveByAttribute((Attribute) expression, exprIds, i);
            } else if (expression.getClass() == Alias.class) {
                Alias alias = (Alias) expression;
                resolveByAttribute((Attribute) alias.attribute, exprIds, i);
            } else {
                throw new SofException("It's impossible has expression type in resolveSchema: " + expression.getClass().getSimpleName());
            }
        }
        Preconditions.checkArgument(schema.stream().allMatch(r -> r.index() >= 0));
    }

    private void resolveByAttribute(Attribute attribute, Map<Long, Integer> exprIds, int i) {
        int index = exprIds.getOrDefault(attribute.exprId, -1);
        if (index >= 0) {
            schema.get(index).resolveIndex(i);
        }
    }

    @Override
    public List<PhysicalPlan> children() {
        return ImmutableList.of(child);
    }

    @Override
    public Context context() {
        return context;
    }

    @Override
    public String toString() {
        return String.format("PhysicalProject [%s] [%s]", schemaToString(), projectList.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }

    @Override
    public void prune(List<BoundReference> father, boolean isTop) {
        // 这里需要预先resolve schema,下面会用到.
        resolveSchema();
        // project的schema和projectList数量不一定对应,而且顺序也不一定一致.所以在这里变为一致.
        // 这样projectExec就不需要schemaProjection.
        List<BoundReference> newSchema = isTop ? schema : father;
        Map<Long, BoundReference> references = schema.stream().collect(Collectors.toMap(r -> r.exprId, r -> r));
        ImmutableList.Builder<Expression> newProjectListBuilder = ImmutableList.builder();
        for (BoundReference reference : newSchema) {
            BoundReference r = references.get(reference.exprId);
            if (Objects.nonNull(r)) {
                //这样projectList的顺序,数量就和schema一致.
                newProjectListBuilder.add(projectList.get(r.index()));
            }
        }
        schema = SchemaPruneHelper.copy(newSchema);

        child.prune(SchemaPruneHelper.extractUseSchema(projectList), false);
    }

    @Override
    public void removeAlias() {
        this.projectList = RemoveAliasHelper.doRemoveAlias(projectList);
        child.removeAlias();
    }
}
