package io.autofire.client.japi.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.autofire.client.japi.iface.Task;
import io.autofire.client.japi.iface.TaskCallback;
import io.autofire.client.japi.iface.TaskFactory;

public class SingleTaskFactoryImpl implements TaskFactory {
    static ExecutorService executorService;

    private static final int TIMEOUT_SEC = 4;

    // reference:
    //   https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html
    private static void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(TIMEOUT_SEC, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(TIMEOUT_SEC, TimeUnit.SECONDS))
                    System.err.println("Autofire ExecutorService: Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    public void start() {
        shutdown();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void shutdown() {
        if (executorService != null) {
            shutdownAndAwaitTermination(executorService);
            executorService = null;
        }
    }

    public Task mk(String name, TaskCallback callback) {
        if (executorService == null || executorService.isShutdown())
            start();

        return new SingleTaskImpl(callback);
    }
}
