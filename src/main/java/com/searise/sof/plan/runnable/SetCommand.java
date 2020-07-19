package com.searise.sof.plan.runnable;

import com.searise.sof.catalog.Catalog;
import com.searise.sof.core.Context;
import com.searise.sof.plan.logic.LogicalPlan;

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
    }
}
