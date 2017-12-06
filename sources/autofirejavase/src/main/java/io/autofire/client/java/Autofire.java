package io.autofire.client.java;

import io.autofire.client.japi.Initializer;
import io.autofire.client.japi.SessionManager;
import io.autofire.client.japi.event.Action;
import io.autofire.client.japi.event.Monetize;
import io.autofire.client.japi.event.Progress;
import io.autofire.client.japi.event.Resource;
import io.autofire.client.japi.util.DefaultGUIDImpl;
import io.autofire.client.japi.util.DefaultJSONEncoderImpl;
import io.autofire.client.japi.util.SingleTaskFactoryImpl;
import io.autofire.client.java.iface.EnvironmentImpl;
import io.autofire.client.java.iface.HTTPImpl;
import io.autofire.client.java.iface.HomePersistenceImpl;
import io.autofire.client.java.iface.LoggerImpl;

public class Autofire {
    public static void setup(String version) {
        EnvironmentImpl.setVersion(version);
        SessionManager.setProviders(null,
                new SingleTaskFactoryImpl(),
                new LoggerImpl(),
                new EnvironmentImpl(),
                new HomePersistenceImpl(),
                new DefaultGUIDImpl(),
                new DefaultJSONEncoderImpl(),
                new HTTPImpl());
    }

    public static void initialize(String gameId, String version) {
        setup(version);
        SessionManager.initialize(new Initializer(gameId));
    }

    public static void progress(String level, int score) {
        SessionManager.sendEvent(new Progress(level, score));
    }

    public static void monetize(String item, int ac, int qty) {
        SessionManager.sendEvent(new Monetize(item, ac, qty));
    }

    public static void monetize(String item, int ac) {
        monetize(item, ac, 1);
    }

    public static void resource(String name, int qty) {
        SessionManager.sendEvent(new Resource(name, qty));
    }

    public static void action(String what) {
        SessionManager.sendEvent(new Action(what));
    }

    public static void flush() {
        SessionManager.flushEvents();
    }

    public static void finish() {
        SessionManager.deinitialize(null);
        SessionManager.shutdown();
    }
}
