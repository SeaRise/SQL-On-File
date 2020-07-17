package com.searise.sof.plan.ddl;

import com.searise.sof.catalog.Catalog;
import com.searise.sof.catalog.CatalogTable;
import com.searise.sof.plan.logic.LogicalPlan;

public class CreateTable implements LogicalPlan, DDLCommand {

    public final CatalogTable catalogTable;

    public CreateTable(CatalogTable catalogTable) {
        this.catalogTable = catalogTable;
    }

    @Override
    public void run(Catalog catalog) {
        catalog.addTable(catalogTable);
    }
}
