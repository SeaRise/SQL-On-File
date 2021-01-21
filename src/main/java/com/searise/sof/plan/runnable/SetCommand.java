package com.searise.sof.plan.runnable;

import com.searise.sof.catalog.Catalog;
import com.searise.sof.core.SofContext;
import com.searise.sof.plan.logic.LogicalPlan;
import org.apache.commons.lang3.StringUtils;

import static com.searise.sof.core.conf.SofConf.MAX_PARALLELISM;

public class SetCommand implements LogicalPlan, RunnableCommand {
    private final String key;
    private final String value;
    private final SofContext context;

    public SetCommand(String key, String value, SofContext context) {
        this.key = key;
        this.value = value;
        this.context = context;
    }

    @Override
    public void run(Catalog catalog) {
        context.conf.setConf(key, value);
        if (StringUtils.equals(MAX_PARALLELISM.getName(), key)) {
            context.dagScheduler.taskScheduler.setParallelism(context.conf.getConf(MAX_PARALLELISM));
        }
    }

    @Override
    public SofContext context() {
        return context;
    }
}
