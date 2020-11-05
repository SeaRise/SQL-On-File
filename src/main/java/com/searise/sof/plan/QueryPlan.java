package com.searise.sof.plan;

import com.google.common.collect.ImmutableList;
import com.searise.sof.analyse.AnalysisHelper;
import com.searise.sof.core.Context;

import java.util.List;
import java.util.stream.Collectors;

public interface QueryPlan<T extends QueryPlan> extends AnalysisHelper<T> {
    default List<T> children() {
        return ImmutableList.of();
    }

    default String visitToString() {
        return visitToString("");
    }

    default String visitToString(String preString) {
        String nextPreString = preString + "  ";
        return preString + toString() + "\n" + toStringChildren().stream().map(child -> child.visitToString(nextPreString)).collect(Collectors.joining());
    }

    default List<T> toStringChildren() {
        return children();
    }

    Context context();
}
