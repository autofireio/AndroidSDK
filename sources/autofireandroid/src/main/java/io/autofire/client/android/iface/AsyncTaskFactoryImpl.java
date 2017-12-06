package io.autofire.client.android.iface;

import io.autofire.client.japi.iface.Task;
import io.autofire.client.japi.iface.TaskCallback;
import io.autofire.client.japi.iface.TaskFactory;

public class AsyncTaskFactoryImpl implements TaskFactory {
    public void start() {
    }

    public void shutdown() {
    }

    public Task mk(String name, TaskCallback callback) {
        return new AsyncTaskImpl(callback);
    }
}
