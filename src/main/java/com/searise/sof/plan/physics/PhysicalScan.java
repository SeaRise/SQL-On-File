package com.searise.sof.plan.physics;

import com.searise.sof.expression.attribute.BoundReference;

import java.util.List;
import java.util.stream.Collectors;

public class PhysicalScan implements PhysicalPlan {
    public final List<BoundReference> schema;
    public final String filePath;
    public final String separator;

    public PhysicalScan(List<BoundReference> schema, String filePath, String separator) {
        this.schema = schema;
        this.filePath = filePath;
        this.separator = separator;
    }

    @Override
    public List<BoundReference> schema() {
        return schema;
    }

    @Override
    public String toString() {
        return String.format("PhysicalScan [%s|%s] (%s)", filePath, separator,
                schema.stream().map(BoundReference::toString).collect(Collectors.joining(",")));
    }
}
