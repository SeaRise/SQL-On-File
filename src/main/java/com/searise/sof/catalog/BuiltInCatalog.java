package com.searise.sof.catalog;

import com.google.common.base.Preconditions;
import com.searise.sof.core.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuiltInCatalog implements Catalog {
    private Map<String, CatalogTable> tableMap = new HashMap<>();

    @Override
    public CatalogTable getTable(String table) {
        return Preconditions.checkNotNull(tableMap.get(table), "no such table[%s]", table);
    }

    @Override
    public void addTable(CatalogTable catalogTable) {
        Utils.checkArgument(!tableMap.containsKey(catalogTable.table), String.format("table %s has exists", catalogTable.table));
        tableMap.put(catalogTable.table, catalogTable);
    }

    @Override
    public List<String> getAllTable() {
        return Utils.toImmutableList(tableMap.keySet().stream());
    }
}
