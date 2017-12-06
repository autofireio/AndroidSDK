package io.autofire.client.japi.iface;

public interface EnvironmentProvider {
    String getPlatform(Object platformContext);

    String getOs(Object platformContext);

    String getModel(Object platformContext);

    String getLocale(Object platformContext);

    String getVersion(Object platformContext);
}
