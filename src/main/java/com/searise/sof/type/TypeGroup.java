package com.searise.sof.type;

import com.google.common.collect.ImmutableSet;
import com.searise.sof.core.SofException;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.searise.sof.type.DataType.*;

public class TypeGroup {
    private TypeGroup() {
    }

    private static Set<DataType> getTypeGroup(DataType dataType) {
        switch (dataType) {
            case DoubleType:
                return ImmutableSet.of();
            case IntegerType:
                return ImmutableSet.of(DoubleType);
            case StringType:
                return ImmutableSet.of(IntegerType, DoubleType);
            case BooleanType:
                return ImmutableSet.of(StringType);
            default:
                return ImmutableSet.of();
        }
    }

    public static boolean canCast(DataType fromType, DataType toType) {
        return fromType == toType || getTypeGroup(fromType).contains(toType);
    }

    public static DataType getTopType(List<DataType> dataTypes) {
        Optional<DataType> topOptional = dataTypes.stream().max(Comparator.comparingInt(o -> o.priority));
        if (!topOptional.isPresent()) {
            throw new SofException("can not find top data type from " + dataTypes);
        }

        DataType top = topOptional.get();
        for (DataType dataType : dataTypes) {
            if (!canCast(dataType, top)) {
                throw new SofException(String.format("Cannot cast %s to %s", dataType, top));
            }
        }
        return top;
    }
}
