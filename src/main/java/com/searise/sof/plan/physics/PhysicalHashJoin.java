package com.searise.sof.plan.physics;

import com.google.common.base.Preconditions;
import com.searise.sof.core.SofContext;
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
import java.util.stream.Collectors;

public class PhysicalHashJoin extends PhysicalJoin {
    public List<Expression> otherConditions;
    public List<Expression> streamJoinKeys;
    public List<Expression> buildJoinKeys;

    public PhysicalHashJoin(List<BoundReference> schema, List<Expression> conditions, PhysicalPlan stream, PhysicalPlan build, SofContext context) {
        super(schema, stream, build, context);

        Triple<List<Expression>, List<Expression>, List<Expression>> splits = splits(conditions);
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
            SofContext context) {
        super(schema, stream, build, context);
        this.streamJoinKeys = streamJoinKeys;
        this.buildJoinKeys = buildJoinKeys;
        this.otherConditions = otherConditions;
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