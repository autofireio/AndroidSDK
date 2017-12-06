package io.autofire.client.japi.iface;

public interface TaskFactory {
    void start();

    void shutdown();

    Task mk(String name, TaskCallback callback);
}
