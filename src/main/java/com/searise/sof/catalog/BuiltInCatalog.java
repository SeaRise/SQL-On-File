package com.searise.sof.catalog;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;

public class BuiltInCatalog implements Catalog {
    private Map<String, CatalogTable> tableMap = new HashMap<>();

    @Override
    public CatalogTable getTable(String table) {
        return Preconditions.checkNotNull(tableMap.get(table), "no such table[%s]", table);
    }
}
