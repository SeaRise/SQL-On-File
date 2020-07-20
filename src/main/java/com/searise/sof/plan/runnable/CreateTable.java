package com.searise.sof.plan.runnable;

import com.searise.sof.catalog.Catalog;
import com.searise.sof.catalog.CatalogTable;
import com.searise.sof.core.Context;
import com.searise.sof.plan.logic.LogicalPlan;

public class CreateTable implements LogicalPlan, RunnableCommand {

    public final CatalogTable catalogTable;
    public final Context context;

    public CreateTable(CatalogTable catalogTable, Context context) {
        this.catalogTable = catalogTable;
        this.context = context;
    }

    @Override
    public void run(Catalog catalog) {
        catalog.addTable(catalogTable);
    }

    @Override
    public Context context() {
        return context;
    }
}
