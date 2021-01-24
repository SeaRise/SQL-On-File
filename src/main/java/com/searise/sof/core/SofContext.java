package com.searise.sof.core;

import com.searise.sof.core.conf.SofConf;
import com.searise.sof.core.id.ExprIdBuilder;
import com.searise.sof.core.id.ShuffleIdBuilder;
import com.searise.sof.plan.physics.PhysicalPlan;
import com.searise.sof.schedule.dag.DagScheduler;
import com.searise.sof.schedule.dag.ResultHandle;
import com.searise.sof.shuffle.MapOutputTracker;
import com.searise.sof.storge.StorageManager;

import java.util.Optional;
import java.util.UUID;

public class SofContext implements AutoCloseable {
    public final ExprIdBuilder exprIdBuilder = new ExprIdBuilder();
    public final ShuffleIdBuilder shuffleIdBuilder = new ShuffleIdBuilder();
    public final SofConf conf;
    public final String appId;

    public final MapOutputTracker mapOutputTracker = new MapOutputTracker();

    public final DagScheduler dagScheduler;

    public final StorageManager storageManager;

    private SofContext(SofConf conf) {
        this.conf = conf;
        this.appId = UUID.randomUUID().toString();
        this.dagScheduler = new DagScheduler(this);
        this.storageManager = new StorageManager(this);
    }

    public void runPlan(PhysicalPlan plan, ResultHandle resultHandle) {
        dagScheduler.runPlan(plan, resultHandle);
    }

    @Override
    public void close() {
        unActiveIfIs(this);
        dagScheduler.stop();
    }

    private static Optional<SofContext> activeContext = Optional.empty();

    public static synchronized SofContext getActive() {
        return activeContext.orElseGet(() -> {
            throw new SofException("no active context");
        });
    }

    private static synchronized void unActiveIfIs(SofContext context) {
        if (activeContext.isPresent() && activeContext.get() == context) {
            activeContext = Optional.empty();
        }
    }

    public static synchronized SofContext getOrCreate(SofConf conf) {
        if (!activeContext.isPresent()) {
            activeContext = Optional.of(new SofContext(conf));
        }
        return getActive();
    }

    public static SofContext getOrCreate() {
        return getOrCreate(new SofConf());
    }
}
