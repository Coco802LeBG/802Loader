package com.coco802.client.module;

import com.coco802.client.Client802;

public final class BoolSetting extends Setting {
    private final boolean def;

    public BoolSetting(Module802 module, String key, String label, boolean def) {
        super(module, key, label);
        this.def = def;
    }

    public boolean get() { return Client802.CONFIG.bool(id(), def); }
    public void set(boolean v) { Client802.CONFIG.bools.put(id(), v); }
}
