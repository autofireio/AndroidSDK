package io.autofire.client.japi.iface;

import java.util.Map;
import java.util.Set;

import io.autofire.client.japi.event.RawEvent;

public interface BatchEncoderProvider {
    String getContentType(Object platformContext);

    String encodeHeader(Object platformContext, Map<String, String> header);

    String encodeTags(Object platformContext, Set<String> tags);

    String encodeEvent(Object platformContext, RawEvent rawEvent);

    String getSeparator(Object platformContext);

    String getBatchBegin(Object platformContext);

    String getEventsBegin(Object platformContext);

    String getEventsEnd(Object platformContext);

    String getBatchEnd(Object platformContext);
}
