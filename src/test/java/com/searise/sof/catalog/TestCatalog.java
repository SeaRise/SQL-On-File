package com.searise.sof.catalog;

import com.google.common.collect.ImmutableList;

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
                "src\\test\\resources\\in", ",");
    }
}
