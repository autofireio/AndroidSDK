package io.autofire.client.android;

import android.app.Activity;
import android.content.Context;

import java.util.concurrent.atomic.AtomicBoolean;

import io.autofire.client.android.iface.EnvironmentImpl;
import io.autofire.client.android.iface.HTTPImpl;
import io.autofire.client.android.iface.LoggerImpl;
import io.autofire.client.android.iface.PrefsPersistenceImpl;
import io.autofire.client.japi.Initializer;
import io.autofire.client.japi.SessionManager;
import io.autofire.client.japi.event.Action;
import io.autofire.client.japi.event.Monetize;
import io.autofire.client.japi.event.Progress;
import io.autofire.client.japi.event.Resource;
import io.autofire.client.japi.util.DefaultGUIDImpl;
import io.autofire.client.japi.util.DefaultJSONEncoderImpl;
import io.autofire.client.japi.util.SingleTaskFactoryImpl;

public class Autofire {
    private static AtomicBoolean startedOnce = new AtomicBoolean(false);

    public static void setup(Context ctx) {
        SessionManager.setProviders(ctx,
                new SingleTaskFactoryImpl(),
                new LoggerImpl(),
                new EnvironmentImpl(),
                new PrefsPersistenceImpl(),
                new DefaultGUIDImpl(),
                new DefaultJSONEncoderImpl(),
                new HTTPImpl());
    }

    public static void initialize(Activity activity, String gameId) {
        if (!startedOnce.get()) {
            setup(activity.getApplicationContext());
            SessionManager.initialize(new Initializer(gameId));
            startedOnce.set(true);
        }
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
        flush();
    }
}
