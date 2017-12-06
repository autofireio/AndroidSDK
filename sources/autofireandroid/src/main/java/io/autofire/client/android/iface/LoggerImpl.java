package io.autofire.client.android.iface;

import android.util.Log;

import io.autofire.client.japi.iface.LoggerProvider;

public class LoggerImpl implements LoggerProvider{
    public void logDebug(Object platformContext, String tag, String what) {
        Log.d(tag, what);
    }
}
