package io.autofire.client.japi.event;

class Custom extends GameEvent {
    private String sanitizeCustom(String customName) {
        if (customName.equals(Action.ACTION_NAME) ||
                customName.equals(Deinit.DEINIT_NAME) ||
                customName.equals(Init.INIT_NAME) ||
                customName.equals(Monetize.MONETIZE_NAME) ||
                customName.equals(Progress.PROGRESS_NAME) ||
                customName.equals(Resource.RESOURCE_NAME))
            return "_" + customName;
        else
            return customName;
    }

    public Custom(String name) {
        super(GameEvent.sanitizeName(name));
        this.name = sanitizeCustom(this.name);
    }
}
