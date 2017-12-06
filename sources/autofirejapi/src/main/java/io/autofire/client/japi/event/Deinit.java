package io.autofire.client.japi.event;

public class Deinit extends GameEvent {
    public static final String DEINIT_NAME = "DEINIT";

    public Deinit() {
        super(DEINIT_NAME);
    }
}
