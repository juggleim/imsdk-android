package com.juggle.im.internal.downloader;

import java.util.concurrent.Future;

/** @author lvhongzhen */
public class Task implements Runnable {
    private TaskDispatcher dispatcher;
    private BaseRequest request;
    private Future<?> future;

    public Task(TaskDispatcher dispatcher, BaseRequest request) {
        this.dispatcher = dispatcher;
        this.request = request;
    }

    @Override
    public void run() {
        request.run();
        dispatcher.finish(this);
    }

    public String getTag() {
        return request.getTag();
    }

    public void cancel() {
        request.cancel();
        future.cancel(false);
        request.requestCallback.onCancel(request.getTag());
    }

    public void pause() {
        request.cancel();
        future.cancel(false);
    }

    public void setFuture(Future<?> future) {
        this.future = future;
    }

    public BaseRequest getRequest() {
        return request;
    }
}
