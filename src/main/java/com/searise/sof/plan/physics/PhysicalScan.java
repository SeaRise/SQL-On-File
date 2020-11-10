package com.searise.sof.plan.physics;

import com.searise.sof.core.Context;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.attribute.Attribute;
import com.searise.sof.expression.attribute.BoundReference;
import com.searise.sof.optimize.afterprocess.ReferenceResolveHelper;
import com.searise.sof.optimize.afterprocess.SchemaPruneHelper;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PhysicalScan implements PhysicalPlan {
    public List<BoundReference> schema;
    public final List<Attribute> structType;
    public final String filePath;
    public final String separator;
    public final Context context;

    public final List<String> splits;

    public PhysicalScan(List<BoundReference> schema, List<Attribute> structType, String filePath, String separator, Context context) {
        this.schema = schema;
        this.structType = structType;
        this.filePath = filePath;
        this.separator = separator;
        this.context = context;
        this.splits = getSplits();
    }

    // filePath是一级目录
    // 下面的都是文件.
    private List<String> getSplits() {
        Utils.checkArgument(StringUtils.isNotBlank(filePath), "filePath is blank!");
        File file = new File(filePath);
        Utils.checkArgument(!file.exists() || file.isDirectory(), String.format("path(%s) must be directory!", filePath));
        return getSplits(filePath);
    }

    private List<String> getSplits(String filePath) {
        return Utils.toImmutableList(Utils.listFiles(new File(filePath)).stream().
                filter(f -> f.exists() && f.isFile()).map(File::getPath));
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
        return splits.size();
    }
}
