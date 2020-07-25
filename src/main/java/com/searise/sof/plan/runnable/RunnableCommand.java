package com.searise.sof.plan.runnable;

import com.searise.sof.catalog.Catalog;

import java.io.IOException;

public interface RunnableCommand {
    void run(Catalog catalog) throws Exception;
}
