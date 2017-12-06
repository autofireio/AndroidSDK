package io.autofire.client.japi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.autofire.client.japi.event.GameEvent;

public final class Initializer {
    private static final int MAX_HEADERS = 0;
    private static final int MAX_TAGS = 4;

    String gameId;
    String playerId = "";
    long timestamp = 0L;
    Map<String, String> headers = new HashMap<String, String>();
    Set<String> tags = new HashSet<String>();

    public Initializer(String gameId) {
        this.gameId = gameId;
        this.timestamp = GameEvent.nowFromEpoch();
    }

    public Initializer withPlayerId(String playerId) {
        this.playerId = playerId;
        return this;
    }

    public Initializer withTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public Initializer withPlatform(String platform) {
        headers.put(Header.PLATFORM_KEY, GameEvent.sanitizeNominalValue(platform));
        return this;
    }

    public Initializer withOs(String os) {
        headers.put(Header.OS_KEY, GameEvent.sanitizeNominalValue(os));
        return this;
    }

    public Initializer withModel(String model) {
        headers.put(Header.MODEL_KEY, GameEvent.sanitizeNominalValue(model));
        return this;
    }

    public Initializer withLocale(String locale) {
        headers.put(Header.LOCALE_KEY, GameEvent.sanitizeNominalValue(locale));
        return this;
    }

    public Initializer withVersion(String version) {
        headers.put(Header.VERSION_KEY, GameEvent.sanitizeNominalValue(version));
        return this;
    }

    public Initializer withHeader(String key, String value) {
        if (headers.size() >= MAX_HEADERS)
            return this;

        headers.put(GameEvent.sanitizeKey(key), GameEvent.sanitizeNominalValue(value));
        return this;
    }

    public Initializer withTag(String tag) {
        if (tags.size() >= MAX_TAGS)
            return this;

        String t = GameEvent.sanitizeName(tag);
        if (!tags.contains(t))
            tags.add(t);

        return this;
    }
}
