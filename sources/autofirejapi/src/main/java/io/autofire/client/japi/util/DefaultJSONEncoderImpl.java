package io.autofire.client.japi.util;

import java.util.Map;
import java.util.Set;

import io.autofire.client.japi.event.RawEvent;
import io.autofire.client.japi.iface.BatchEncoderProvider;

public class DefaultJSONEncoderImpl implements BatchEncoderProvider {
    public static final String CONTENT_TYPE = "application/json";
    public static final String BEGIN_OBJECT = "{";
    public static final String END_OBJECT = "}";
    public static final String EMPTY_OBJECT = BEGIN_OBJECT + END_OBJECT;
    public static final String BEGIN_ARRAY = "[";
    public static final String END_ARRAY = "]";
    public static final String EMPTY_ARRAY = BEGIN_ARRAY + END_ARRAY;
    public static final String SEPARATOR = ",";
    public static final String ASSIGNMENT = ":";

    private String headerLabel = stringifyStringValue("header");
    private String tagsLabel = stringifyStringValue("tags");
    private String eventsLabel = stringifyStringValue("events");

    public String getContentType(Object platformContext) {
        return CONTENT_TYPE;
    }

    private static String stringifyStringValue(String value) {
        return "\"" + value + "\"";
    }

    private <K, V> String jsonifyDict(Map<K, V> dictionary, boolean isStringValue) {
        if (dictionary == null)
            return EMPTY_OBJECT;

        int count = dictionary.size();
        if (count == 0)
            return EMPTY_OBJECT;

        StringBuilder result = new StringBuilder(BEGIN_OBJECT);

        int i = 0;
        for (Map.Entry<K, V> entry : dictionary.entrySet()) {
            String k = entry.getKey().toString();
            String v = entry.getValue().toString();
            if (i > 0)
                result.append(SEPARATOR);
            if (isStringValue)
                v = stringifyStringValue(v);
            result.append(stringifyStringValue(k)).append(ASSIGNMENT).append(v);
            i++;
        }

        result.append(END_OBJECT);

        return result.toString();
    }

    private <T> String jsonifyList(Set<T> list, boolean isStringValue) {
        if (list == null)
            return EMPTY_ARRAY;

        int count = list.size();
        if (count == 0)
            return EMPTY_ARRAY;

        StringBuilder result = new StringBuilder(BEGIN_ARRAY);

        int i = 0;
        for (T x : list) {
            String v = x.toString();
            if (i > 0)
                result.append(SEPARATOR);
            if (isStringValue)
                v = stringifyStringValue(v);
            result.append(v);
            i++;
        }

        result.append(END_ARRAY);

        return result.toString();
    }

    public String encodeHeader(Object platformContext, Map<String, String> header) {
        return headerLabel + ASSIGNMENT + jsonifyDict(header, true);
    }

    public String encodeTags(Object platformContext, Set<String> tags) {
        return tagsLabel + ASSIGNMENT + jsonifyList(tags, true);
    }

    public String encodeEvent(Object platformContext, RawEvent rawEvent) {
        if (rawEvent == null)
            return "";

        String required = stringifyStringValue(rawEvent.name) +
                SEPARATOR +
                stringifyStringValue(rawEvent.timestamp);

        String optional = "";
        if ((rawEvent.nominals != null && rawEvent.nominals.size() != 0) ||
                (rawEvent.integrals != null && rawEvent.integrals.size() != 0) ||
                (rawEvent.fractionals != null && rawEvent.fractionals.size() != 0)) {

            optional = SEPARATOR +
                    jsonifyDict(rawEvent.nominals, true) + SEPARATOR +
                    jsonifyDict(rawEvent.integrals, false) + SEPARATOR +
                    jsonifyDict(rawEvent.fractionals, false);
        }

        return BEGIN_ARRAY + required + optional + END_ARRAY;
    }

    public String getSeparator(Object platformContext) {
        return SEPARATOR;
    }

    public String getBatchBegin(Object platformContext) {
        return BEGIN_OBJECT;
    }

    public String getEventsBegin(Object platformContext) {
        return eventsLabel + ASSIGNMENT + BEGIN_ARRAY;
    }

    public String getEventsEnd(Object platformContext) {
        return END_ARRAY;
    }

    public String getBatchEnd(Object platformContext) {
        return END_OBJECT;
    }
}
