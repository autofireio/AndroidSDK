package io.autofire.client.japi;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import io.autofire.client.japi.event.GameEvent;
import io.autofire.client.japi.event.Utils;
import io.autofire.client.japi.iface.EnvironmentProvider;

final class Header implements Serializable {
    public static final String PLATFORM_KEY = "platform";
    public static final String OS_KEY = "os";
    public static final String MODEL_KEY = "model";
    public static final String LOCALE_KEY = "locale";
    public static final String VERSION_KEY = "version";

    public String autofireVersion;
    public String platform;
    public String os;
    public String model;
    public String locale;
    public String version;
    public long initTimestamp;
    public String atLevel;
    public HashMap<String, String> features;

    public Header(Object platformContext, EnvironmentProvider env,
                  Map<String, String> features,
                  long initTimestamp, String atLevel) {
        String v;
        String vv;

        this.autofireVersion = Version.VERSION;
        v = features.get(PLATFORM_KEY);
        if (v != null) {
            vv = v;
            features.remove(PLATFORM_KEY);
        } else
            vv = env.getPlatform(platformContext);
        this.platform = GameEvent.sanitizeNominalValue(vv);
        v = features.get(OS_KEY);
        if (v != null) {
            vv = v;
            features.remove(OS_KEY);
        } else
            vv = env.getOs(platformContext);
        this.os = GameEvent.sanitizeNominalValue(vv);
        v = features.get(MODEL_KEY);
        if (v != null) {
            vv = v;
            features.remove(MODEL_KEY);
        } else
            vv = env.getModel(platformContext);
        this.model = GameEvent.sanitizeNominalValue(vv);
        v = features.get(LOCALE_KEY);
        if (v != null) {
            vv = v;
            features.remove(LOCALE_KEY);
        } else
            vv = env.getLocale(platformContext);
        this.locale = GameEvent.sanitizeNominalValue(vv);
        v = features.get(VERSION_KEY);
        if (v != null) {
            vv = v;
            features.remove(VERSION_KEY);
        } else
            vv = env.getVersion(platformContext);
        this.version = GameEvent.sanitizeNominalValue(vv);
        this.initTimestamp = initTimestamp;
        this.atLevel = GameEvent.sanitizeNominalValue(atLevel);

        this.features = new HashMap<String, String>(features);
    }

    private boolean tryAdd(Map<String, String> to, String key, String value) {
        if (!Utils.isNullOrEmpty(value) && !value.equals(GameEvent.EMPTY_STRING)) {
            to.put(key, value);

            return true;
        }

        return false;
    }

    Map<String, String> toRaw() {
        HashMap<String, String> result = new HashMap<String, String>(features);
        tryAdd(result, "autofireVersion", autofireVersion);
        tryAdd(result, PLATFORM_KEY, platform);
        tryAdd(result, OS_KEY, os);
        tryAdd(result, MODEL_KEY, model);
        tryAdd(result, LOCALE_KEY, locale);
        tryAdd(result, VERSION_KEY, version);
        tryAdd(result, "initTimestamp", GameEvent.toISO8601String(initTimestamp));
        tryAdd(result, "atLevel", atLevel);

        return result;
    }
}
