package com.searise.sof.plan.runnable;

import com.google.common.collect.ImmutableList;
import com.searise.sof.catalog.Catalog;
import com.searise.sof.catalog.CatalogTable;
import com.searise.sof.catalog.StructField;
import com.searise.sof.core.SofContext;
import com.searise.sof.core.SofSession;
import com.searise.sof.core.Utils;
import com.searise.sof.core.conf.SofConf;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.expression.attribute.Attribute;
import com.searise.sof.plan.logic.LogicalPlan;
import com.searise.sof.plan.physics.PhysicalPlan;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class InsertOverwrite implements LogicalPlan, RunnableCommand {
    public final SofContext context;
    public final String targetTable;
    public final LogicalPlan query;
    private final long flushThreshold;

    public InsertOverwrite(SofContext context, String targetTable, LogicalPlan query) {
        this.context = context;
        this.targetTable = targetTable;
        this.query = query;
        this.flushThreshold = context.conf.getConf(SofConf.WRITE_FLUSH_THRESHOLD);
    }

    @Override
    public SofContext context() {
        return context;
    }

    @Override
    public void run(Catalog catalog) throws IOException {
        CatalogTable target = catalog.getTable(targetTable);
        LogicalPlan resolvedQuery = resolve(target, query);

        doRun(target, resolvedQuery);
    }

    private LogicalPlan resolve(CatalogTable target, LogicalPlan query) {
        LogicalPlan resolvedQuery = SofSession.getActive().analyzer.analyse(query);
        Utils.checkArgument(resolvedQuery.schema().size() == target.structType.size(),
                String.format("query field size: %d != insert table field size: %d", resolvedQuery.schema().size(), target.structType.size()));
        for (int i = 0; i < target.structType.size(); i++) {
            StructField field = target.structType.get(i);
            Attribute queryAttr = resolvedQuery.schema().get(i);
            Utils.checkArgument(resolvedQuery.schema().size() == target.structType.size(),
                    String.format("field[%d] expect: %s, actual: %s", i, field.dataType, queryAttr.dataType));
        }
        return resolvedQuery;
    }

    void doRun(CatalogTable target, LogicalPlan resolvedQuery) throws IOException {
        PhysicalPlan physicalPlan = SofSession.getActive().optimizer.optimize(resolvedQuery);

        mkdirIfNotExists(target.filePath);
        final String tmpLocation = genTmpLocation(target);
        context.runPlan(physicalPlan, (partition, iterator) -> {
            File tmpFile = genTmpFile(tmpLocation, partition);
            StringBuilder rowBuilder = new StringBuilder();
            while (iterator.hasNext()) {
                appendOrFlush(rowBuilder, target, tmpFile, iterator.next());
            }
            flush(rowBuilder, tmpFile);
        });

        cleanFiles(target.filePath);
        moveFiles(tmpLocation, target.filePath);
        deleteIfExists(tmpLocation);
    }

    private void mkdirIfNotExists(String path) throws IOException {
        File file = new File(path);
        if (!file.exists() && !file.mkdirs()) {
            throw new IOException("can not mkdir " + path);
        }
    }

    private void deleteIfExists(String path) throws IOException {
        File file = new File(path);
        if (file.exists() && !file.delete()) {
            throw new IOException("can not delete " + path);
        }
    }

    private void cleanFiles(String path) throws IOException {
        File filePath = new File(path);
        for (File file : Utils.listFiles(filePath)) {
            if (file.exists() && file.isFile() && !file.delete()) {
                throw new IOException("can not delete " + file.getPath());
            }
        }
    }

    private void appendOrFlush(StringBuilder rowBuilder, CatalogTable catalogTable, File file, InternalRow internalRow) throws IOException {
        appendText(rowBuilder, catalogTable, internalRow);
        if (rowBuilder.length() > flushThreshold) {
            flush(rowBuilder, file);
        }
    }

    private void appendText(StringBuilder rowBuilder, CatalogTable target, InternalRow row) {
        for (int index = 0; index < row.numFields() - 1; index++) {
            rowBuilder.append(row.getValue(index)).append(target.separator);
        }
        rowBuilder.append(row.getValue(row.numFields() - 1)).append("\n");
    }

    private void flush(StringBuilder rowBuilder, File file) throws IOException {
        FileUtils.writeStringToFile(file, rowBuilder.toString(), "utf-8", true);
        rowBuilder.setLength(0);
    }

    private void moveFiles(String source, String dest) throws IOException {
        File sourcePath = new File(source);
        for (File file : Utils.listFiles(sourcePath)) {
            File destFile = new File(dest + File.separator + file.getName());
            if (!file.renameTo(destFile)) {
                throw new IOException(String.format("can not rename %s to %s", file, destFile));
            }
        }
    }

    private String genTmpLocation(CatalogTable table) {
        return table.filePath + File.separator + "tmp_" + context.appId;
    }

    private File genTmpFile(String tmpLocation, int partition) {
        String tmpFileName = tmpLocation + File.separator + UUID.randomUUID().toString() + '_' + partition;
        return new File(tmpFileName);
    }

    @Override
    public List<LogicalPlan> children() {
        return ImmutableList.of(query);
    }
}
