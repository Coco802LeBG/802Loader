package com.coco802.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public final class ArmorHudModule extends Module802 {
    private static final EquipmentSlot[] SLOTS = {
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private final NumberSetting x = addNumber("x", "Position X", 0, 400, 1, 4);
    private final NumberSetting y = addNumber("y", "Position Y", 0, 300, 1, 110);

    public ArmorHudModule() { super("ArmorHud", "Affiche ton armure à l'écran"); }

    @Override
    public void onHud(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null) return;
        int py = y.getInt();
        for (EquipmentSlot slot : SLOTS) {
            ItemStack stack = mc.player.getEquippedStack(slot);
            if (!stack.isEmpty()) {
                ctx.drawItem(stack, x.getInt(), py);
                py += 18;
            }
        }
    }
}
