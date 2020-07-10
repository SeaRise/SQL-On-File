package com.searise.sof.catalog;

import com.searise.sof.type.DataType;

public class StructField {
    public final String name;
    public final DataType dataType;

    public StructField(String name, DataType dataType) {
        this.name = name;
        this.dataType = dataType;
    }
}
