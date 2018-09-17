package io.autofire.client.japi.event;

public class Monetize extends GameEvent {
    public static final String MONETIZE_NAME = "MONETIZE";

    private String monetizeName;
    private int ac;
    private int qty;

    public Monetize(String name, int ac, int qty) {
        super(MONETIZE_NAME);
        this.monetizeName = name;
        this.ac = ac;
        this.qty = qty;
        withPredefinedFeature("name", name);
        withPredefinedFeature("ac", ac);
        withPredefinedFeature("qty", qty);
    }

    public String getName() {
        return monetizeName;
    }

    public int getAc() {
        return ac;
    }

    public int getQty() {
        return qty;
    }
}
