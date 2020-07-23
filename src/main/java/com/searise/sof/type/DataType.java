package com.searise.sof.type;

import com.searise.sof.core.SofException;
import org.apache.commons.lang3.StringUtils;

// todo 支持dataType convert
//  现在是强类型限制,在analyse那里加上一个cast算子,
//  用于转换类型
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