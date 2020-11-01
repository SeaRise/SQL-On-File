package com.searise.sof.core;

import com.searise.sof.Driver;
import com.searise.sof.catalog.Catalog;

import java.util.UUID;

public class Context {
    public final ExprIdBuilder exprIdBuilder = new ExprIdBuilder();
    public final ShuffleIdBuilder shuffleIdBuilder = new ShuffleIdBuilder();
    public final Conf conf = new Conf();
    public final String appId;

    public final Catalog catalog;
    public final Driver driver;
    public Context(Catalog catalog, Driver driver) {
        this.catalog = catalog;
        this.driver = driver;
        this.appId = UUID.randomUUID().toString();
    }
}
