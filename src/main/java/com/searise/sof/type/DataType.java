package com.searise.sof.type;

// todo 支持dataType convert
//  现在是强类型限制,在analyse那里加上一个cast算子,
//  用于转换类型
public enum DataType {
    StringType,
    BooleanType,
    IntegerType,
    DoubleType
}