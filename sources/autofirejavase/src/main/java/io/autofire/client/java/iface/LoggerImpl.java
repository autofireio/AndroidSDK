package io.autofire.client.java.iface;

import java.util.logging.Logger;

import io.autofire.client.japi.iface.LoggerProvider;

public class LoggerImpl implements LoggerProvider {
    static final Logger LOGGER = Logger.getLogger(LoggerImpl.class.getName());

    public void logDebug(Object platformContext, String tag, String what) {
        LOGGER.info(tag + ": " + what);
    }
}
