package com.searise.sof.plan.ddl;

import com.searise.sof.catalog.Catalog;

public interface DDLCommand {
    void run(Catalog catalog);
}
