package com.searise.sof.plan.logic;

import com.google.common.collect.ImmutableList;
import com.searise.sof.analyse.AnalysisHelper;
import com.searise.sof.core.Context;
import com.searise.sof.expression.attribute.Attribute;
import com.searise.sof.plan.QueryPlan;

import java.util.List;

public interface LogicalPlan extends QueryPlan<LogicalPlan>, AnalysisHelper<LogicalPlan> {
    default List<Attribute> schema() {
        return ImmutableList.of();
    }

    @Override
    default List<LogicalPlan> children() {
        return ImmutableList.of();
    }

    Context context();
}
