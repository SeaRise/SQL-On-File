package com.searise.sof.plan.physics;

import com.searise.sof.core.Context;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.attribute.Attribute;
import com.searise.sof.expression.attribute.BoundReference;
import com.searise.sof.optimize.afterprocess.ReferenceResolveHelper;
import com.searise.sof.optimize.afterprocess.SchemaPruneHelper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PhysicalScan implements PhysicalPlan {
    public List<BoundReference> schema;
    public final List<Attribute> structType;
    public final String filePath;
    public final String separator;
    public final Context context;

    public PhysicalScan(List<BoundReference> schema, List<Attribute> structType, String filePath, String separator, Context context) {
        this.schema = schema;
        this.structType = structType;
        this.filePath = filePath;
        this.separator = separator;
        this.context = context;
    }

    @Override
    public List<BoundReference> schema() {
        return schema;
    }

    @Override
    public void resolveIndex() {
        Map<Long, Integer> inputs = Utils.zip(index -> structType.get(index).exprId, structType.size());
        ReferenceResolveHelper.resolveSchema(schema, inputs);
    }

    @Override
    public String toString() {
        return String.format("PhysicalScan [%s] [%s|%s] (%s)", schemaToString(), filePath, separator,
                structType.stream().map(Attribute::toString).collect(Collectors.joining(",")));
    }

    @Override
    public void prune(List<BoundReference> father, boolean isTop) {
        schema = isTop ? schema : SchemaPruneHelper.copy(father);
    }

    @Override
    public Context context() {
        return context;
    }

    @Override
    public int partitions() {
        return 1;
    }
}
