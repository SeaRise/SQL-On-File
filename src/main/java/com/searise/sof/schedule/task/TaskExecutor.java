package com.searise.sof.schedule.task;

import com.searise.sof.core.Conf;
import com.searise.sof.core.Context;
import com.searise.sof.core.Utils;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskExecutor {
    private final Queue<Task> pendingQueue = new LinkedList<>();

    private int maxRunningNum;

    private int runningNum = 0;

    private ExecutorService executor = Executors.newCachedThreadPool();

    private final TaskScheduler taskScheduler;

    public TaskExecutor(Context context, TaskScheduler taskScheduler) {
        this.maxRunningNum = context.conf.getIntConf(Conf.MAX_PARALLELISM);
        this.taskScheduler = taskScheduler;
    }

    public synchronized void setMaxRunningNum(final int maxRunningNum) {
        int preMaxRunningNum = this.maxRunningNum;
        this.maxRunningNum = maxRunningNum;
        if (preMaxRunningNum < maxRunningNum) {
            refresh();
        }
    }

    public synchronized void submit(Task task) {
        if (runningNum < maxRunningNum) {
            if (pendingQueue.isEmpty()) {
                runningNum++;
                executor.submit(newRunnable(task));
            } else {
                pendingQueue.add(task);
                refresh();
            }
        } else {
            pendingQueue.add(task);
        }
    }

    private synchronized void refresh() {
        while (runningNum < maxRunningNum) {
            Task next = pendingQueue.poll();
            if (Objects.isNull(next)) {
                return;
            }
            runningNum++;
            executor.submit(newRunnable(next));
        }
    }

    // 为了复用调用reUseCurThread的线程，如果用refresh，在cacheThreadPool的机制下，这条线程会被回收.
    // 注意不能在synchronized下调用这个方法
    private Optional<Task> reUseCurThread() {
        Task nextFirst;
        synchronized (this) {
            if (runningNum >= maxRunningNum) {
                return Optional.empty();
            }

            nextFirst = pendingQueue.poll();
            if (Objects.isNull(nextFirst)) {
                return Optional.empty();
            }
            runningNum++;

            refresh();
        }
        return Optional.of(nextFirst);
    }

    private Runnable newRunnable(Task task) {
        return () -> {
            doRun(task);

            // reUseCurThread().ifPresent(task1 -> newRunnable(task1).run());
            // 会造成oom, 因为jvm没有尾递归优化, 方法栈会一直增长.
            Optional<Task> next = reUseCurThread();
            while (next.isPresent()) {
                doRun(next.get());
                next = reUseCurThread();
            }
        };
    }

    private void doRun(Task task) {
        try {
            task.runTask();
            taskScheduler.statusUpdate(TaskResult.success(task.stageId, task.partition));
        } catch (Throwable e) {
            Utils.println(e.getMessage());
            taskScheduler.statusUpdate(TaskResult.fail(e, task.stageId, task.partition));
        } finally {
            synchronized (this) {
                runningNum--;
            }
        }
    }

    public synchronized void stop() {
        pendingQueue.clear();
        executor.shutdownNow();
    }

    public synchronized void clear() {
        pendingQueue.clear();
        executor.shutdownNow();
        executor = Executors.newCachedThreadPool();
    }
}
