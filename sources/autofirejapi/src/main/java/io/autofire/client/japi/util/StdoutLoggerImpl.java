package io.autofire.client.japi.util;

import io.autofire.client.japi.iface.LoggerProvider;

public class StdoutLoggerImpl implements LoggerProvider {
    public void logDebug(Object platformContext, String tag, String what) {
        System.out.println(tag + ": " + what);
    }
}
