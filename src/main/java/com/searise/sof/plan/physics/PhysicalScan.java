package com.searise.sof.plan.physics;

import com.searise.sof.core.Utils;
import com.searise.sof.expression.attribute.Attribute;
import com.searise.sof.expression.attribute.BoundReference;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PhysicalScan implements PhysicalPlan {
    public List<BoundReference> schema;
    public final List<Attribute> structType;
    public final String filePath;
    public final String separator;

    public PhysicalScan(List<BoundReference> schema, List<Attribute> structType, String filePath, String separator) {
        this.schema = schema;
        this.structType = structType;
        this.filePath = filePath;
        this.separator = separator;
    }

    @Override
    public List<BoundReference> schema() {
        return schema;
    }

    @Override
    public void resolveIndex() {
        Map<Long, Integer> inputs = Utils.zip(index -> structType.get(index).exprId, structType.size());
        ReferenceResolver.resolveSchema(schema, inputs);
    }

    @Override
    public String toString() {
        return String.format("PhysicalScan [%s] [%s|%s] (%s)", schemaToString(), filePath, separator,
                structType.stream().map(Attribute::toString).collect(Collectors.joining(",")));
    }

    @Override
    public void prune(List<BoundReference> father, boolean isTop) {
        if (!isTop) {
            schema = Utils.copy(father);
        }
    }
}
