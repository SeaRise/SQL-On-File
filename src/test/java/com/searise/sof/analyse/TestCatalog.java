package com.searise.sof.analyse;

import com.google.common.collect.ImmutableList;
import com.searise.sof.catalog.Catalog;
import com.searise.sof.catalog.CatalogTable;

public class TestCatalog implements Catalog {
    @Override
    public CatalogTable getTable(String table) {
        return new CatalogTable(table, ImmutableList.of(), "/", ",");
    }
}
