package com.coco802.client.module;

import com.coco802.client.Client802;

public final class NumberSetting extends Setting {
    public final double min, max, step;
    private final double def;

    public NumberSetting(Module802 module, String key, String label, double min, double max, double step, double def) {
        super(module, key, label);
        this.min = min;
        this.max = max;
        this.step = step;
        this.def = def;
    }

    public double get() { return Client802.CONFIG.number(id(), def); }
    public int getInt() { return (int) Math.round(get()); }
    public void set(double v) { Client802.CONFIG.numbers.put(id(), v); }
}
