package com.searise.sof.plan.runnable;

import com.searise.sof.catalog.Catalog;
import com.searise.sof.catalog.CatalogTable;
import com.searise.sof.catalog.StructField;
import com.searise.sof.core.SofContext;
import com.searise.sof.core.SofSession;
import com.searise.sof.core.Utils;
import com.searise.sof.plan.logic.LogicalPlan;

import java.util.List;
import java.util.stream.Collectors;

public class CreateTableAsSelect implements LogicalPlan, RunnableCommand {
    public final SofContext context;
    public final String targetTable;
    public final String filePath;
    public final String separator;
    public final LogicalPlan query;

    public CreateTableAsSelect(SofContext context, String targetTable, String filePath, String separator, LogicalPlan query) {
        this.context = context;
        this.targetTable = targetTable;
        this.filePath = filePath;
        this.separator = separator;
        this.query = query;
    }

    @Override
    public SofContext context() {
        return context;
    }

    @Override
    public void run(Catalog catalog) throws Exception {
        LogicalPlan resolvedQuery = SofSession.getActive().analyzer.analyse(query);
        List<StructField> structType = Utils.toImmutableList(resolvedQuery.schema().stream().
                map(attr -> new StructField(attr.name, attr.dataType)));
        Utils.checkArgument(structType.stream().map(f -> f.name).collect(Collectors.toSet()).size() == structType.size(),
                "repeat field name in query.");
        CatalogTable target = new CatalogTable(targetTable, structType, filePath, separator);
        catalog.addTable(target);

        InsertOverwrite insertOverwrite = new InsertOverwrite(context, targetTable, query);
        insertOverwrite.doRun(target, resolvedQuery);
    }
}
