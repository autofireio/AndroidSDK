package io.autofire.client.japi.util;

import java.util.UUID;

import io.autofire.client.japi.iface.GUIDProvider;

public class DefaultGUIDImpl implements GUIDProvider {
    public String newGUID(Object platformContext) {
        return UUID.randomUUID().toString();
    }
}
