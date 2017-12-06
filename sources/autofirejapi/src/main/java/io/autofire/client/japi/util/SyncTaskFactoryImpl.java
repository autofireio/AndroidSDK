package io.autofire.client.japi.util;

import io.autofire.client.japi.iface.Task;
import io.autofire.client.japi.iface.TaskCallback;
import io.autofire.client.japi.iface.TaskFactory;

public class SyncTaskFactoryImpl implements TaskFactory {
    public void start() {
    }

    public void shutdown() {
    }

    public Task mk(String name, TaskCallback callback) {
        return new SyncTaskImpl(callback);
    }
}
