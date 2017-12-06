package io.autofire.client.japi.util;

import io.autofire.client.japi.iface.Task;
import io.autofire.client.japi.iface.TaskCallback;

public class SyncTaskImpl implements Task {
    private TaskCallback callback;

    public SyncTaskImpl(TaskCallback callback) {
        this.callback = callback;
    }

    public void runMe() {
        String ret = callback.doOperation();
        callback.onResult(ret);
    }
}

