package com.jet.im.kit.utils;


import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Wrapper class for {@link ScheduledExecutorService} that supports {@link #cancelAllJobs()}
 * operation. To support this, we keep internal variable {@link #futures}.
 * <p>
 * Note that jobs registered via {@link #invokeAny} calls are not cancelled by
 * {@link #cancelAllJobs()} - since the jobs are automatically cancelled for these methods. See
 * {@link java.util.concurrent.ExecutorService#invokeAny}.
 * <p>
 * All access to the internal variable {@link #futures} must be synchronized.
 */
public class ClearableScheduledExecutorService implements ScheduledExecutorService {
    private final ScheduledExecutorService scheduledExecutorService;
    private final List<Future<?>> futures = new ArrayList<>(); // this is thread-unsafe variable.

    public ClearableScheduledExecutorService() {
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    private synchronized <T> Future<T> addFuture(Future<T> future) {
        this.futures.add(future);
        return future;
    }

    private synchronized <T> ScheduledFuture<T> addScheduledFuture(ScheduledFuture<T> scheduledFuture) {
        this.futures.add(scheduledFuture);
        return scheduledFuture;
    }

    private synchronized <T> List<Future<T>> addFutures(List<Future<T>> futures) {
        this.futures.addAll(futures);
        return futures;
    }

    public synchronized void cancelAllJobs() {
        cancelAllJobs(false);
    }

    public synchronized void cancelAllJobs(boolean mayInterruptIfRunning) {
        for (Future<?> future : futures) {
            future.cancel(mayInterruptIfRunning);
        }
        futures.clear();
    }

    @NonNull
    @Override
    public ScheduledFuture<?> schedule(@NonNull Runnable command, long delay, @NonNull TimeUnit unit) {
        return addScheduledFuture(scheduledExecutorService.schedule(command, delay, unit));
    }

    @NonNull
    @Override
    public <V> ScheduledFuture<V> schedule(@NonNull Callable<V> callable, long delay, @NonNull TimeUnit unit) {
        return addScheduledFuture(scheduledExecutorService.schedule(callable, delay, unit));
    }

    @NonNull
    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(@NonNull Runnable command, long initialDelay, long period, @NonNull TimeUnit unit) {
        return addScheduledFuture(scheduledExecutorService.scheduleAtFixedRate(command, initialDelay, period, unit));
    }

    @NonNull
    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(@NonNull Runnable command, long initialDelay, long delay, @NonNull TimeUnit unit) {
        return addScheduledFuture(scheduledExecutorService.scheduleWithFixedDelay(command, initialDelay, delay, unit));
    }

    @Override
    public void shutdown() {
        scheduledExecutorService.shutdown();
    }

    @NonNull
    @Override
    public List<Runnable> shutdownNow() {
        return scheduledExecutorService.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return scheduledExecutorService.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return scheduledExecutorService.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, @NonNull TimeUnit unit) throws InterruptedException {
        return scheduledExecutorService.awaitTermination(timeout, unit);
    }

    @NonNull
    @Override
    public <T> Future<T> submit(@NonNull Callable<T> task) {
        return addFuture(scheduledExecutorService.submit(task));
    }

    @NonNull
    @Override
    public <T> Future<T> submit(@NonNull Runnable task, T result) {
        return addFuture(scheduledExecutorService.submit(task, result));
    }

    @NonNull
    @Override
    public Future<?> submit(@NonNull Runnable task) {
        return addFuture(scheduledExecutorService.submit(task));

    }

    @NonNull
    @Override
    public <T> List<Future<T>> invokeAll(@NonNull Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return addFutures(scheduledExecutorService.invokeAll(tasks));
    }

    @NonNull
    @Override
    public <T> List<Future<T>> invokeAll(@NonNull Collection<? extends Callable<T>> tasks, long timeout, @NonNull TimeUnit unit) throws InterruptedException {
        return addFutures(scheduledExecutorService.invokeAll(tasks, timeout, unit));
    }

    @NonNull
    @Override
    public <T> T invokeAny(@NonNull Collection<? extends Callable<T>> tasks) throws ExecutionException, InterruptedException {
        return scheduledExecutorService.invokeAny(tasks);
    }

    @NonNull
    @Override
    public <T> T invokeAny(@NonNull Collection<? extends Callable<T>> tasks, long timeout, @NonNull TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
        return scheduledExecutorService.invokeAny(tasks, timeout, unit);
    }

    @NonNull
    @Override
    public void execute(@NonNull Runnable command) {
        addFuture(scheduledExecutorService.schedule(command, 0L, TimeUnit.MILLISECONDS));
    }
}
