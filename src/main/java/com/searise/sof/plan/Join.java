package com.searise.sof.plan;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class Join implements LogicalPlan {
    @Override
    public List<LogicalPlan> children() {
        return ImmutableList.of();
    }
}
