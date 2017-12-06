package io.autofire.client.japi.event;

import java.util.HashMap;
import java.util.Map;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public abstract class GameEvent {
    public static final String DATETIME_FORMATTER = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final DateFormat dateFormat = new SimpleDateFormat(DATETIME_FORMATTER, Locale.US);

    private static final char SEPARATOR = '/';
    private static final char ASSIGNMENT = '=';
    private static final char TAB = '\t';
    private static final int MAX_NAME_LEN = 32;
    private static final int MAX_KEY_LEN = 64;
    private static final int MAX_NOMINAL_VALUE_LEN = 64;
    public static final String EMPTY_STRING = "_EMPTY";

    private static final int MAX_FEATURES = 0;

    String name;
    long timestamp;
    Map<String, String> nominals = new HashMap<String, String>();
    Map<String, Integer> integrals = new HashMap<String, Integer>();
    Map<String, Double> fractionals = new HashMap<String, Double>();

    public long getTimestamp() {
        return timestamp;
    }

    public static long nowFromEpoch() {
        return System.currentTimeMillis() / 1000L;
    }

    public static String toISO8601String(long unixTime) {
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date(unixTime * 1000L);
        return dateFormat.format(date);
    }

    private static String onNonEmpty(String str) {
        if (Utils.isNullOrEmpty(str))
            return EMPTY_STRING;
        else
            return str;
    }

    private static String left(String input, int length) {
        if (length >= input.length())
            return input;
        else
            return input.substring(0, length);
    }

    private static String sanitize(String str, int len) {
        str = str.replaceAll("[^\u0020-\u007F]+", "");
        return onNonEmpty(left(str.trim(), len))
                .replace(SEPARATOR, '_')
                .replace(ASSIGNMENT, '_')
                .replace(TAB, ' ');
    }

    public static String sanitizeName(String name) {
        return sanitize(name, MAX_NAME_LEN);
    }

    public static String sanitizeKey(String key) {
        return sanitize(key, MAX_KEY_LEN);
    }

    public static String sanitizeNominalValue(String value) {
        return sanitize(value, MAX_NOMINAL_VALUE_LEN);
    }

    protected GameEvent(String name, long timestamp) {
        this.name = sanitizeName(name);
        this.timestamp = timestamp;
    }

    protected GameEvent(String name) {
        this(name, nowFromEpoch());
    }

    public GameEvent withTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    GameEvent withPredefinedFeature(String key, String value) {
        nominals.put(key, sanitizeNominalValue(value));
        return this;
    }

    GameEvent withPredefinedFeature(String key, int value) {
        integrals.put(key, value);
        return this;
    }

    GameEvent withPredefinedFeature(String key, double value) {
        fractionals.put(key, value);
        return this;
    }

    private boolean checkFeaturesNumber() {
        int total = nominals.size() + integrals.size() + fractionals.size();
        return total < MAX_FEATURES;
    }

    public GameEvent withFeature(String key, String value) {
        if (!checkFeaturesNumber())
            return this;

        nominals.put(sanitizeKey(key), sanitizeNominalValue(value));
        return this;
    }

    public GameEvent withFeature(String key, int value) {
        if (!checkFeaturesNumber())
            return this;

        integrals.put(sanitizeKey(key), value);
        return this;
    }

    public GameEvent withFeature(String key, double value) {
        if (!checkFeaturesNumber())
            return this;

        fractionals.put(sanitizeKey(key), value);
        return this;
    }

    public RawEvent toRaw() {
        RawEvent raw = new RawEvent();
        raw.name = this.name;
        raw.timestamp = toISO8601String(this.timestamp);
        raw.nominals = new HashMap<String, String>(this.nominals);
        raw.integrals = new HashMap<String, Integer>(this.integrals);
        raw.fractionals = new HashMap<String, Double>(this.fractionals);

        return raw;
    }

    @Override
    public String toString() {
        return toRaw().toString();
    }
}
