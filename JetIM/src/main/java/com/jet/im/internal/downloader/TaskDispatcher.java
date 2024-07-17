package com.jet.im.internal.downloader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @author lvhongzhen
 */
public class TaskDispatcher {
    private static final String TAG = "TaskDispatcher";
    private static final String DOWNLOAD = "download";
    private static final int MAX_RUNNING_TASK = 4;
    private final Map<String, List<Task>> taskMap = new HashMap<>(10);
    private final ThreadPoolExecutor executorService;

    public TaskDispatcher() {
        executorService =
                new ThreadPoolExecutor(
                        MAX_RUNNING_TASK,
                        MAX_RUNNING_TASK,
                        60,
                        TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>());
        executorService.allowCoreThreadTimeOut(true);
    }

    public synchronized void enqueue(BaseRequest request) {
        String tag = request.getTag();
        Task task = new Task(this, request);

        if (taskMap.containsKey(tag)) {
            taskMap.get(tag).add(task);
        } else {
            ArrayList<Task> deque = new ArrayList<>(4);
            deque.add(task);
            taskMap.put(tag, deque);
        }

        Future<?> submit = executorService.submit(task);
        task.setFuture(submit);
    }

    public synchronized boolean cancel(String tag) {
        if (!taskMap.containsKey(tag)) {
            return false;
        }

        List<Task> taskList = taskMap.get(tag);

        for (Task task : taskList) {
            task.cancel();
        }

        taskList.clear();
        taskMap.remove(tag);
        return true;
    }

    public synchronized void addTag(String tag) {
        if (!taskMap.containsKey(tag)) {
            taskMap.put(tag, new ArrayList<>(4));
        }
    }

    public synchronized boolean pause(String tag) {
        if (!taskMap.containsKey(tag)) {
            return false;
        }

        List<Task> taskList = taskMap.get(tag);

        for (Task task : taskList) {
            task.pause();
        }

        taskList.clear();
        taskMap.remove(tag);
        return true;
    }

    public synchronized void cancelAll() {
        for (Map.Entry<String, List<Task>> entry : taskMap.entrySet()) {
            List<Task> taskList = entry.getValue();

            for (Task task : taskList) {
                task.cancel();
            }

            taskList.clear();
        }

        taskMap.clear();
    }

    public synchronized void pauseAll() {
        for (Map.Entry<String, List<Task>> entry : taskMap.entrySet()) {
            List<Task> taskList = entry.getValue();

            for (Task task : taskList) {
                task.pause();
            }

            taskList.clear();
        }

        taskMap.clear();
    }

    public synchronized void finish(Task task) {
        String tag = task.getTag();

        if (!taskMap.containsKey(tag)) {
            return;
        }

        List<Task> taskList = taskMap.get(tag);
        taskList.remove(task);

        if (taskList.isEmpty()) {
            taskMap.remove(tag);
        }
    }

    public boolean existsTask(String tag) {
        return taskMap.containsKey(tag);
    }

    public List<Task> getTask(String tag) {
        return taskMap.get(tag);
    }

    public void execute(Runnable r) {
        executorService.execute(r);
    }
}
