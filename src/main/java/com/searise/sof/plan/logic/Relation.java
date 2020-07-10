package com.searise.sof.plan.logic;

import com.searise.sof.catalog.CatalogTable;

import java.util.Optional;

public class Relation extends UnresolvedRelation {
    public final CatalogTable catalogTable;

    public Relation(CatalogTable catalogTable, String tableName, Optional<String> referenceName) {
        super(tableName, referenceName);
        this.catalogTable = catalogTable;
    }

    @Override
    public String toString() {
        String aliasToString = referenceName.map(s -> ", " + s).orElse("");
        return String.format("Relation [%s%s]", tableName, aliasToString);
    }

    @Override
    public boolean resolved() {
        return true;
    }
}
