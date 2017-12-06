package io.autofire.client.japi.util;

import io.autofire.client.japi.iface.EnvironmentProvider;

public class DummyEnvironmentImpl implements EnvironmentProvider {
    public String getPlatform(Object platformContext) {
        return "";
    }

    public String getOs(Object platformContext) {
        return "";
    }

    public String getModel(Object platformContext) {
        return "";
    }

    public String getLocale(Object platformContext) {
        return "";
    }

    public String getVersion(Object platformContext) {
        return "";
    }
}
