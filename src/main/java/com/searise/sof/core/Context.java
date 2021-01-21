package com.searise.sof.core;

import com.searise.sof.catalog.Catalog;
import com.searise.sof.core.conf.Conf;
import com.searise.sof.plan.physics.PhysicalPlan;
import com.searise.sof.schedule.dag.DagScheduler;
import com.searise.sof.schedule.dag.ResultHandle;
import com.searise.sof.shuffle.MapOutputTracker;
import com.searise.sof.storge.StorageManager;

import java.util.Optional;
import java.util.UUID;

public class Context {
    public final ExprIdBuilder exprIdBuilder = new ExprIdBuilder();
    public final ShuffleIdBuilder shuffleIdBuilder = new ShuffleIdBuilder();
    public final Conf conf;
    public final String appId;

    public final Catalog catalog;
    public final Driver driver;

    public final MapOutputTracker mapOutputTracker = new MapOutputTracker();

    public final DagScheduler dagScheduler;

    public final StorageManager storageManager;

    public Context(Catalog catalog, Driver driver) {
        conf = new Conf();
        this.catalog = catalog;
        this.driver = driver;
        this.appId = UUID.randomUUID().toString();
        this.dagScheduler = new DagScheduler(this);
        this.storageManager = new StorageManager();
    }

    public void runPlan(PhysicalPlan plan, ResultHandle resultHandle) {
        dagScheduler.runPlan(plan, resultHandle);
    }

    public void stop() {
        dagScheduler.stop();
    }

    private static Optional<Context> activeContext = Optional.empty();
    public static Context getActive() {
        return activeContext.orElseGet(() -> {
            throw new SofException("no active context");
        });
    }
    public static void setActive(Context context) {
        activeContext = Optional.of(context);
    }
}
