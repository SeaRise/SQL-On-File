package com.searise.sof.type;

import com.searise.sof.core.SofException;
import org.apache.commons.lang3.StringUtils;

public enum DataType {
    BooleanType(0, "bool", "Boolean"),
    StringType(1, "string", "String"),
    IntegerType(2, "int", "Integer"),
    DoubleType(3, "double", "Double");

    public final int priority;
    public final String name;
    public final String javaType;

    DataType(int priority, String name, String javaType) {
        this.priority = priority;
        this.name = name;
        this.javaType = javaType;
    }

    public static DataType getType(String name) {
        for (DataType dataType : values()) {
            if (StringUtils.equals(dataType.name, name)) {
                return dataType;
            }
        }
        throw new SofException("no such data type: " + name);
    }
}