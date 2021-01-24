package com.searise.sof.plan.physics;

import com.google.common.collect.ImmutableList;
import com.searise.sof.core.SofContext;
import com.searise.sof.core.SofException;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.attribute.Attribute;
import com.searise.sof.expression.attribute.BoundReference;
import com.searise.sof.expression.compare.EqualTo;
import com.searise.sof.optimize.afterprocess.ReferenceResolveHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class PhysicalJoin implements PhysicalPlan {
    public List<BoundReference> schema;
    public final PhysicalPlan stream;
    public final PhysicalPlan build;
    public final SofContext context;

    public PhysicalJoin(List<BoundReference> schema, PhysicalPlan stream, PhysicalPlan build, SofContext context) {
        this.schema = schema;
        this.stream = stream;
        this.build = build;
        this.context = context;
    }

    @Override
    public SofContext context() {
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

    protected void pruneChild(PhysicalPlan child, List<BoundReference> useSchema) {
        List<BoundReference> childSchema = child.schema();
        Map<Long, Integer> childMap = Utils.zip(index -> childSchema.get(index).exprId, childSchema.size());
        List<BoundReference> conditionsUseSchema = Utils.toImmutableList(useSchema.stream().filter(r -> childMap.containsKey(r.exprId)));
        List<BoundReference> parentUseSchema = Utils.toImmutableList(schema.stream().filter(r -> childMap.containsKey(r.exprId)));
        child.prune(Utils.combineDistinct(conditionsUseSchema, parentUseSchema), false);
    }

    // streamKeys, buildKeys, otherConditions
    protected Triple<List<Expression>, List<Expression>, List<Expression>> splits(List<Expression> conditions) {
        Set<Long> streamIds = stream.schema().stream().map(r -> r.exprId).collect(Collectors.toSet());
        Set<Long> buildIds = build.schema().stream().map(r -> r.exprId).collect(Collectors.toSet());

        ImmutableList.Builder<Expression> otherCondBuilder = ImmutableList.builder();
        ImmutableList.Builder<Expression> streamKeyBuilder = ImmutableList.builder();
        ImmutableList.Builder<Expression> buildKeyBuilder = ImmutableList.builder();
        for (Expression condition : conditions) {
            boolean isOtherCond = true;
            if (condition.getClass() == EqualTo.class) {
                EqualTo equalTo = (EqualTo) condition;
                if (belongTo(equalTo.left, streamIds) && belongTo(equalTo.right, buildIds)) {
                    streamKeyBuilder.add(equalTo.left);
                    buildKeyBuilder.add(equalTo.right);
                    isOtherCond = false;
                } else if (belongTo(equalTo.right, streamIds) && belongTo(equalTo.left, buildIds)) {
                    streamKeyBuilder.add(equalTo.right);
                    buildKeyBuilder.add(equalTo.left);
                    isOtherCond = false;
                } else {
                    // just else.
                }
            }
            if (isOtherCond) {
                otherCondBuilder.add(condition);
            }
        }
        return Triple.of(streamKeyBuilder.build(), buildKeyBuilder.build(), otherCondBuilder.build());
    }

    private boolean belongTo(Expression expression, Set<Long> parentExprIds) {
        if (!expression.resolved()) {
            throw new SofException(String.format("%s has not resolved", expression));
        }

        if (expression instanceof BoundReference) {
            throw new SofException("Tt is illegal to call splits() for BoundReference");
        }

        if (expression instanceof Attribute) {
            return parentExprIds.contains(((Attribute) expression).exprId);
        }

        if (expression.children().isEmpty()) {
            return false;
        }

        for (Expression child : expression.children()) {
            if (!belongTo(child, parentExprIds)) {
                return false;
            }
        }
        return true;
    }

    public abstract Pair<List<Expression>, List<Expression>> joinKeys();

    protected Pair<List<Expression>, List<Expression>> resolveJoinKeys(List<Expression> streamJoinKeys, List<Expression> buildJoinKeys) {
        Map<Long, Integer> streamInputs = Utils.zip(index -> stream.schema().get(index).exprId, stream.schema().size());
        streamJoinKeys = ReferenceResolveHelper.resolveExpression(streamJoinKeys, streamInputs);

        Map<Long, Integer> buildInputs = Utils.zip(index -> build.schema().get(index).exprId, build.schema().size());
        buildJoinKeys = ReferenceResolveHelper.resolveExpression(buildJoinKeys, buildInputs);

        return Pair.of(streamJoinKeys, buildJoinKeys);
    }
}
