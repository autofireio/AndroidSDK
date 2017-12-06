package io.autofire.client.japi.event;

public class Resource extends GameEvent {
    public static final String RESOURCE_NAME = "RESOURCE";

    private String resourceName;
    private int qty;

    public Resource(String name, int qty) {
        super(RESOURCE_NAME);
        this.resourceName = name;
        this.qty = qty;
        withPredefinedFeature("name", name);
        withPredefinedFeature("qty", qty);
    }

    public String getName() {
        return resourceName;
    }

    public int getQty() {
        return qty;
    }
}
