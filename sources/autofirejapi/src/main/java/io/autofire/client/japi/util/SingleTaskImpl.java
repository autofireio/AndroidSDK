package io.autofire.client.japi.util;

import io.autofire.client.japi.iface.Task;
import io.autofire.client.japi.iface.TaskCallback;

public class SingleTaskImpl implements Runnable, Task {
    private TaskCallback callback;

    public SingleTaskImpl(TaskCallback callback) {
        this.callback = callback;
    }

    public void run() {
        String ret = callback.doOperation();
        callback.onResult(ret);
    }

    public void runMe() {
        SingleTaskFactoryImpl.executorService.execute(this);
    }
}
