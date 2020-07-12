package com.searise.sof.execution;

import com.google.common.collect.ImmutableList;
import com.searise.sof.catalog.Catalog;
import com.searise.sof.catalog.CatalogTable;
import com.searise.sof.catalog.StructField;

import static com.searise.sof.type.DataType.DoubleType;

public class TestCatalog implements Catalog {
    @Override
    public CatalogTable getTable(String table) {
        return new CatalogTable(table,
                ImmutableList.of(
                        new StructField("a", DoubleType)
                        , new StructField("b", DoubleType)
                        , new StructField("c", DoubleType)
                        , new StructField("d", DoubleType)
                ),
                "src\\test\\resources\\input.txt", ",");
    }
}
