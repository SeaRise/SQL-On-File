package com.searise.sof.catalog;

import com.google.common.collect.ImmutableList;

import java.util.List;

public interface Catalog {
    CatalogTable getTable(String table);

    default void addTable(CatalogTable catalogTable) {
    }

    default List<String> getAllTable() {
        return ImmutableList.of();
    }
}
