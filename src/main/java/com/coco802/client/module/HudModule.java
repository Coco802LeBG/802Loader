package com.coco802.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class HudModule extends Module802 {
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");
    private final BoolSetting fps = addBool("fps", "FPS", true);
    private final BoolSetting coords = addBool("coords", "Coordonnées", true);
    private final BoolSetting clock = addBool("clock", "Heure", false);
    private final NumberSetting x = addNumber("x", "Position X", 0, 400, 1, 4);
    private final NumberSetting y = addNumber("y", "Position Y", 0, 300, 1, 4);

    public HudModule() { super("HUD", "FPS, coordonnées et heure"); }

    @Override
    public void onHud(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null) return;
        int px = x.getInt();
        int py = y.getInt();
        if (fps.get()) {
            ctx.drawTextWithShadow(mc.textRenderer, "FPS: " + mc.getCurrentFps(), px, py, 0xFFFFFFFF);
            py += 12;
        }
        if (coords.get()) {
            ctx.drawTextWithShadow(mc.textRenderer, String.format("XYZ: %.1f %.1f %.1f",
                mc.player.getX(), mc.player.getY(), mc.player.getZ()), px, py, 0xFFFFFFFF);
            py += 12;
        }
        if (clock.get()) {
            ctx.drawTextWithShadow(mc.textRenderer, LocalTime.now().format(CLOCK), px, py, 0xFFFFFFFF);
        }
    }
}
