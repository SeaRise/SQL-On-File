package com.searise.sof.plan.runnable;

import com.searise.sof.catalog.Catalog;

public interface RunnableCommand {
    void run(Catalog catalog);
}
