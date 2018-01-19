package io.autofire.client.japi.util;

import io.autofire.client.japi.iface.LoggerProvider;

public class NullLoggerImpl implements LoggerProvider {
    public void logDebug(Object platformContext, String tag, String what) {
    }
}
