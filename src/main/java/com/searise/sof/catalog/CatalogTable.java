package com.searise.sof.catalog;

import java.util.List;

public class CatalogTable {
    public final String table;
    public final List<StructField> structType;
    public final String filePath;
    public final String separator;

    public CatalogTable(String table, List<StructField> structType, String filePath, String separator) {
        this.table = table;
        this.structType = structType;
        this.filePath = filePath;
        this.separator = separator;
    }
}
