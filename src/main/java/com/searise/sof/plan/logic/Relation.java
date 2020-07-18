package com.searise.sof.plan.logic;

import com.searise.sof.catalog.CatalogTable;
import com.searise.sof.core.ExprIdBuilder;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.attribute.Attribute;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Relation extends UnresolvedRelation {
    public final CatalogTable catalogTable;
    private List<Attribute> schema;

    public Relation(CatalogTable catalogTable, String tableName, Optional<String> referenceName) {
        super(tableName, referenceName);
        this.catalogTable = catalogTable;
        this.schema = Utils.toImmutableList(catalogTable.structType.stream().
                map(structField -> new Attribute(Optional.of(referenceName.orElse(tableName)),
                        structField.name, ExprIdBuilder.newExprId(), structField.dataType)));
    }

    @Override
    public String toString() {
        String aliasToString = referenceName.map(s -> ", " + s).orElse("");
        return String.format("Relation [%s%s] (%s)", tableName, aliasToString,
                schema.stream().map(Attribute::toString).collect(Collectors.joining(",")));
    }

    @Override
    public boolean resolved() {
        return true;
    }

    @Override
    public List<Attribute> schema() {
        return this.schema;
    }
}
