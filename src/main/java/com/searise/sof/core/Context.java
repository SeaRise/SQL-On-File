package com.searise.sof.core;

import com.searise.sof.Driver;
import com.searise.sof.catalog.Catalog;
import com.searise.sof.plan.physics.PhysicalPlan;
import com.searise.sof.schedule.dag.DagScheduler;
import com.searise.sof.schedule.dag.ResultHandle;
import com.searise.sof.shuffle.MapOutputTracker;

import java.util.UUID;

public class Context {
    public final ExprIdBuilder exprIdBuilder = new ExprIdBuilder();
    public final ShuffleIdBuilder shuffleIdBuilder = new ShuffleIdBuilder();
    public final Conf conf = new Conf();
    public final String appId;

    public final Catalog catalog;
    public final Driver driver;

    public final MapOutputTracker mapOutputTracker = new MapOutputTracker();

    public final DagScheduler dagScheduler;

    public Context(Catalog catalog, Driver driver) {
        this.catalog = catalog;
        this.driver = driver;
        this.appId = UUID.randomUUID().toString();
        this.dagScheduler = new DagScheduler(this);
    }

    public void runPlan(PhysicalPlan plan, ResultHandle resultHandle) {
        dagScheduler.runPlan(plan, resultHandle);
    }
}
