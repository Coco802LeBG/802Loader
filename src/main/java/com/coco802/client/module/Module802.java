package com.coco802.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public abstract class Module802 {
    public final String name;
    public final String description;
    public final List<Setting> settings = new ArrayList<>();
    private boolean enabled;

    protected Module802(String name, String description) {
        this.name = name;
        this.description = description;
    }

    protected BoolSetting addBool(String key, String label, boolean def) {
        BoolSetting s = new BoolSetting(this, key, label, def);
        settings.add(s);
        return s;
    }

    protected NumberSetting addNumber(String key, String label, double min, double max, double step, double def) {
        NumberSetting s = new NumberSetting(this, key, label, min, max, step, def);
        settings.add(s);
        return s;
    }

    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean value) {
        if (value == enabled) return;
        enabled = value;
        if (value) onEnable(); else onDisable();
    }

    public void toggle() { setEnabled(!enabled); }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick(MinecraftClient mc) {}
    public void onHud(DrawContext ctx, MinecraftClient mc) {}
}
