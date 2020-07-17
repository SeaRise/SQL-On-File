package com.searise.sof.type;

import com.searise.sof.core.SofException;
import org.apache.commons.lang3.StringUtils;

// todo 支持dataType convert
//  现在是强类型限制,在analyse那里加上一个cast算子,
//  用于转换类型
public enum DataType {
    StringType("string"),
    BooleanType("bool"),
    IntegerType("int"),
    DoubleType("double");

    public final String name;

    DataType(String name) {
        this.name = name;
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