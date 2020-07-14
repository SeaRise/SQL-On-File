package com.searise.sof.optimize.transformation;

import com.google.common.collect.ImmutableList;
import com.searise.sof.optimize.Operand;

import java.util.List;

public class Pattern {
    public final Operand operand;
    public final List<Pattern> children;

    public Pattern(Operand operand, List<Pattern> children) {
        this.operand = operand;
        this.children = children;
    }

    public Pattern(Operand operand) {
        this(operand, ImmutableList.of());
    }
}
