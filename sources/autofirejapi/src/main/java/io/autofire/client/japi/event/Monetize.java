package io.autofire.client.japi.event;

public class Monetize extends GameEvent {
    public static final String MONETIZE_NAME = "MONETIZE";

    private String item;
    private int ac;
    private int qty;

    public Monetize(String item, int ac, int qty) {
        super(MONETIZE_NAME);
        this.item = item;
        this.ac = ac;
        this.qty = qty;
        withPredefinedFeature("name", item);
        withPredefinedFeature("ac", ac);
        withPredefinedFeature("qty", qty);
    }

    public String getItem() {
        return item;
    }

    public int getAc() {
        return ac;
    }

    public int getQty() {
        return qty;
    }
}
