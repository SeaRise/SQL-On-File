package com.searise.sof.plan.runnable;

import com.searise.sof.catalog.Catalog;
import com.searise.sof.core.Context;
import com.searise.sof.plan.logic.LogicalPlan;
import org.apache.commons.lang3.StringUtils;

import static com.searise.sof.core.Conf.MAX_PARALLELISM;

public class SetCommand implements LogicalPlan, RunnableCommand {
    private final String key;
    private final String value;
    private final Context context;

    public SetCommand(String key, String value, Context context) {
        this.key = key;
        this.value = value;
        this.context = context;
    }

    @Override
    public void run(Catalog catalog) {
        context.conf.setConf(key, value);
        if (StringUtils.equals(MAX_PARALLELISM, key)) {
            context.dagScheduler.taskScheduler.setParallelism(context.conf.getIntConf(MAX_PARALLELISM));
        }
    }

    @Override
    public Context context() {
        return context;
    }
}
