package com.searise.sof.plan.logic;

import com.searise.sof.catalog.CatalogTable;
import com.searise.sof.common.ExprIdBuilder;
import com.searise.sof.common.Utils;
import com.searise.sof.expression.Attribute;

import java.util.List;
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

    @Override
    public List<Attribute> schema() {
        return Utils.toImmutableList(catalogTable.structType.stream().
                map(structField -> new Attribute(tableName, structField.name, ExprIdBuilder.newExprId(), structField.dataType)));
    }
}
