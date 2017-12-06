package io.autofire.client.japi.event;

public class Action extends GameEvent {
    public static final String ACTION_NAME = "ACTION";

    private String what;

    public Action(String what) {
        super(ACTION_NAME);
        this.what = what;
        withPredefinedFeature("what", what);
    }

    public String getWhat() {
        return what;
    }
}
