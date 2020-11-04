package com.searise.sof.plan.runnable;

import com.google.common.collect.ImmutableList;
import com.searise.sof.catalog.Catalog;
import com.searise.sof.catalog.CatalogTable;
import com.searise.sof.catalog.StructField;
import com.searise.sof.core.Conf;
import com.searise.sof.core.Context;
import com.searise.sof.core.SofException;
import com.searise.sof.core.Utils;
import com.searise.sof.core.row.InternalRow;
import com.searise.sof.execution.Executor;
import com.searise.sof.execution.RowIterator;
import com.searise.sof.expression.attribute.Attribute;
import com.searise.sof.plan.logic.LogicalPlan;
import com.searise.sof.plan.physics.PhysicalPlan;
import com.searise.sof.schedule.dag.ResultHandle;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static com.searise.sof.core.row.EmptyRow.EMPTY_ROW;

public class InsertOverwrite implements LogicalPlan, RunnableCommand {
    public final Context context;
    public final String targetTable;
    public final LogicalPlan query;
    private final int flushThreshold;

    public InsertOverwrite(Context context, String targetTable, LogicalPlan query) {
        this.context = context;
        this.targetTable = targetTable;
        this.query = query;
        this.flushThreshold = context.conf.getIntConf(Conf.WRITE_FLUSH_THRESHOLD);
    }

    @Override
    public Context context() {
        return context;
    }

    @Override
    public void run(Catalog catalog) {
        CatalogTable target = catalog.getTable(targetTable);
        LogicalPlan resolvedQuery = resolve(target, query);

        doRun(target, resolvedQuery);
    }


    private Executor compileQuery(CatalogTable target, LogicalPlan query) {
        PhysicalPlan physicalPlan = context.driver.optimizer.optimize(query);
        return context.driver.builder.build(physicalPlan);
    }

    private LogicalPlan resolve(CatalogTable target, LogicalPlan query) {
        LogicalPlan resolvedQuery = context.driver.analyzer.analyse(query);
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

    void doRun(CatalogTable target, LogicalPlan resolvedQuery) {
        PhysicalPlan physicalPlan = context.driver.optimizer.optimize(resolvedQuery);
        context.runPlan(physicalPlan, (partition, iterator) -> {
            File tmpFile = genTmpFile(target, partition);
            StringBuilder rowBuilder = new StringBuilder();
            while (iterator.hasNext()) {
                appendOrFlush(rowBuilder, target, tmpFile, iterator.next());
            }
            flush(rowBuilder, tmpFile);
            moveFile(tmpFile, target);
        });
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
        rowBuilder.append(row.getValue(row.numFields()-1)).append("\n");
    }

    private void flush(StringBuilder rowBuilder, File file) throws IOException {
        FileUtils.writeStringToFile(file, rowBuilder.toString(), "utf-8", true);
        rowBuilder.setLength(0);
    }

    private void moveFile(File tmpFile, CatalogTable target) {
        File file = new File(target.filePath);
        boolean remove = !file.exists() || file.delete();
        Utils.checkArgument(remove, "can't remove origin file: " + target.filePath);

        if (!tmpFile.renameTo(file)) {
            throw new SofException(String.format("can't move %s to %s", tmpFile.getAbsolutePath(), file.getAbsolutePath()));
        }
        Utils.println(String.format("overwrite %d bytes to %s", file.length(), file.getAbsolutePath()));
    }

    private File genTmpFile(CatalogTable table, int partition) {
        String[] splits = StringUtils.split(table.filePath, File.separatorChar);

        StringBuilder absoluteTmpFileNameBuilder = new StringBuilder();
        for (int i = 0; i < splits.length - 1; i++) {
            absoluteTmpFileNameBuilder.append(splits[i]).append(File.separatorChar);
        }
        String tmpFileName = UUID.randomUUID().toString() + '_' + splits[splits.length - 1];
        absoluteTmpFileNameBuilder.append(tmpFileName);
        String absoluteTmpFileName = absoluteTmpFileNameBuilder.toString();

        return new File(absoluteTmpFileName);
    }

    @Override
    public List<LogicalPlan> children() {
        return ImmutableList.of(query);
    }
}
