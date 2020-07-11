package com.searise.sof.analyse;

import com.google.common.collect.ImmutableList;
import com.searise.sof.catalog.Catalog;
import com.searise.sof.catalog.CatalogTable;
import com.searise.sof.catalog.StructField;
import com.searise.sof.type.DataType;

import static com.searise.sof.type.DataType.StringType;

public class TestCatalog implements Catalog {
    @Override
    public CatalogTable getTable(String table) {
        return new CatalogTable(table,
                ImmutableList.of(
                        new StructField("a", StringType)
                        ,new StructField("b", StringType)
                        ,new StructField("c", StringType)
                        ,new StructField("d", StringType)
                ),
                "/", ",");
    }
}
