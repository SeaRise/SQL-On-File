package com.searise.sof.plan;

import java.util.List;
import java.util.stream.Collectors;

public interface QueryPlan<T extends QueryPlan> {
    List<T> children();

    default String visitToString() {
        return visitToString("");
    }

    default String visitToString(String preString) {
        String nextPreString = preString + "  ";
        return preString + toString() + "\n" + children().stream().map(child -> child.visitToString(nextPreString)).collect(Collectors.joining());
    }
}
