package io.autofire.client.java.iface;

import java.util.Locale;

import io.autofire.client.japi.iface.EnvironmentProvider;

public class EnvironmentImpl implements EnvironmentProvider {
    private static String version = "Unknown";

    public static void setVersion(String version) {
        EnvironmentImpl.version = version;
    }

    public String getPlatform(Object platformContext) {
        return "JavaSE";
    }

    public String getOs(Object platformContext) {
        return System.getProperty("os.name") + " " +
                System.getProperty("os.arch") + " " +
                System.getProperty("os.version");
    }

    public String getModel(Object platformContext) {
        return System.getProperty("java.version");
    }

    public String getLocale(Object platformContext) {
        return Locale.getDefault().toString();
    }

    public String getVersion(Object platformContext) {
        return version;
    }
}
