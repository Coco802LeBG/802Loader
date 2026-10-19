package com.coco802.client.gui;

import com.coco802.client.Client802;
import com.coco802.client.module.BoolSetting;
import com.coco802.client.module.Module802;
import com.coco802.client.module.NumberSetting;
import com.coco802.client.module.Setting;
import com.deathmotion.marlowcrystal.MarlowCrystal;
import com.deathmotion.marlowcrystal.config.Settings;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleConsumer;

/** Menu 802 Client dessiné à la main : sidebar, modules avec interrupteurs, sliders. */
public final class MenuScreen extends Screen {
    private static final int ACCENT = 0xFF2E9BFF;
    private static final String[] CATS = {"Modules", "GUI Scale", "Crystal Reflex", "Infos"};
    private static final int[] SCALES = {0, 1, 2, 3, 4, 5, 6};
    private static int category = 0;
    private static String expanded = null;

    private record Hit(int x1, int y1, int x2, int y2, DoubleConsumer press, DoubleConsumer drag) {
        boolean contains(int x, int y) { return x >= x1 && x < x2 && y >= y1 && y < y2; }
    }

    private final Screen parent;
    private final long openedAt = System.currentTimeMillis();
    private final List<Hit> hits = new ArrayList<>();
    private Hit dragging;
    private boolean wasDown;
    private float alpha = 1f;

    public MenuScreen(Screen parent) {
        super(Text.literal("802 Client"));
        this.parent = parent;
    }

    private static int col(int argb, float a) {
        int al = (int) (((argb >>> 24) & 0xFF) * a);
        return (al << 24) | (argb & 0xFFFFFF);
    }

    private void rect(DrawContext c, int x1, int y1, int x2, int y2, int color) {
        c.fill(x1, y1, x2, y2, col(color, alpha));
    }

    private void text(DrawContext c, String s, int x, int y, int color) {
        c.drawTextWithShadow(this.textRenderer, s, x, y, col(color, alpha));
    }

    private void sw(DrawContext c, int x, int y, boolean on) {
        rect(c, x, y, x + 22, y + 10, on ? ACCENT : 0xFF3A4452);
        int kx = on ? x + 13 : x + 1;
        rect(c, kx, y + 1, kx + 8, y + 9, 0xFFFFFFFF);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        hits.clear();
        super.render(ctx, mx, my, delta);
        alpha = Math.max(0.05f, Math.min(1f, (System.currentTimeMillis() - openedAt) / 180f));

        int pw = 460;
        int ph = Math.min(290, this.height - 16);
        int x0 = (this.width - pw) / 2;
        int y0 = (this.height - ph) / 2;

        rect(ctx, x0 - 1, y0 - 1, x0 + pw + 1, y0 + ph + 1, 0x662E9BFF);
        rect(ctx, x0, y0, x0 + pw, y0 + ph, 0xF00F151D);
        rect(ctx, x0, y0, x0 + 110, y0 + ph, 0xFF0A0F15);
        text(ctx, "802", x0 + 14, y0 + 14, ACCENT);
        text(ctx, "CLIENT", x0 + 36, y0 + 14, 0xFFFFFFFF);

        for (int i = 0; i < CATS.length; i++) {
            final int idx = i;
            int cy = y0 + 44 + i * 24;
            boolean sel = category == i;
            boolean hover = mx >= x0 && mx < x0 + 110 && my >= cy && my < cy + 22;
            if (sel || hover) rect(ctx, x0, cy, x0 + 110, cy + 22, sel ? 0xFF16202C : 0xFF111923);
            if (sel) rect(ctx, x0, cy, x0 + 3, cy + 22, ACCENT);
            text(ctx, CATS[i], x0 + 14, cy + 7, sel ? 0xFFFFFFFF : 0xFF8FA3BC);
            hits.add(new Hit(x0, cy, x0 + 110, cy + 22, v -> category = idx, null));
        }

        int x1 = x0 + 122;
        int x2 = x0 + pw - 12;
        int y = y0 + 14;
        text(ctx, CATS[category], x1, y, ACCENT);
        y += 18;

        if (category == 0) {
            for (Module802 m : Client802.MODULES) {
                y = moduleRow(ctx, m, x1, x2, y, mx, my);
                if (m.name.equals(expanded)) {
                    for (Setting s : m.settings) y = settingRow(ctx, s, x1, x2, y);
                }
            }
        } else if (category == 1) {
            for (int i = 0; i < GuiScaleManager.KINDS.length; i++) {
                final String kind = GuiScaleManager.KINDS[i];
                int cur = Client802.CONFIG.scaleFor(kind);
                y = cycleRow(ctx, GuiScaleManager.LABELS[i], x1, x2, y, cur == 0 ? "Auto" : String.valueOf(cur),
                    () -> Client802.CONFIG.guiScale.put(kind, step(Client802.CONFIG.scaleFor(kind), -1)),
                    () -> Client802.CONFIG.guiScale.put(kind, step(Client802.CONFIG.scaleFor(kind), 1)));
            }
            text(ctx, "Auto = le GUI scale de tes options Minecraft.", x1, y + 4, 0xFF6F7F94);
        } else if (category == 2) {
            Settings s = MarlowCrystal.get().settings();
            y = switchRow(ctx, "Keep Render", x1, x2, y, s.isKeepRender(), () -> s.setKeepRender(!s.isKeepRender()));
            y = switchRow(ctx, "Raw mouse input", x1, x2, y, s.isRawMouseInput(), () -> s.setRawMouseInput(!s.isRawMouseInput()));
            y = slider(ctx, "Délai libération (ms)", x1, x2, y, 250, 3000, 50, s.getKeepReleaseMs(), v -> s.setKeepReleaseMs((int) v));
            y = slider(ctx, "Crystals gardés max", x1, x2, y, 1, 16, 1, s.getMaxKeptCrystals(), v -> s.setMaxKeptCrystals((int) v));
            text(ctx, "F8 : activer/couper Reflex   F7 : resync du slot tenu", x1, y + 4, 0xFF6F7F94);
        } else {
            String[] lines = {
                "802 Client 1.0 - Minecraft 1.21.11 (Fabric)",
                "Menu : Shift droit (modifiable dans les contrôles)",
                "Config : .minecraft/config/802client.json",
                "Crystal Reflex : .minecraft/config/marlowcrystal.json",
                "Crédits : Marlow / Bram, Coco802 (MIT)"
            };
            for (String l : lines) {
                text(ctx, l, x1, y, 0xFFB0BAC8);
                y += 14;
            }
        }
        handleMouse(mx, my);
    }

    private static int step(int cur, int dir) {
        int idx = 0;
        for (int i = 0; i < SCALES.length; i++) if (SCALES[i] == cur) idx = i;
        return SCALES[(idx + dir + SCALES.length) % SCALES.length];
    }

    private int moduleRow(DrawContext c, Module802 m, int x1, int x2, int y, int mx, int my) {
        boolean hover = mx >= x1 && mx < x2 && my >= y && my < y + 26;
        rect(c, x1, y, x2, y + 26, hover ? 0xFF1C2735 : 0xFF151D28);
        rect(c, x1, y, x1 + 2, y + 26, m.isEnabled() ? ACCENT : 0xFF2A3442);
        text(c, m.name, x1 + 10, y + 4, m.isEnabled() ? 0xFFFFFFFF : 0xFFB0BAC8);
        text(c, m.description, x1 + 10, y + 15, 0xFF6F7F94);
        sw(c, x2 - 30, y + 8, m.isEnabled());
        if (!m.settings.isEmpty()) {
            final boolean open = m.name.equals(expanded);
            text(c, open ? "[-]" : "[+]", x2 - 62, y + 9, 0xFF8FA3BC);
            hits.add(new Hit(x2 - 68, y, x2 - 34, y + 26, v -> expanded = open ? null : m.name, null));
        }
        hits.add(new Hit(x1, y, x2, y + 26, v -> { m.toggle(); Client802.saveModules(); }, null));
        return y + 28;
    }

    private int settingRow(DrawContext c, Setting s, int x1, int x2, int y) {
        int ix = x1 + 14;
        if (s instanceof BoolSetting b) {
            return switchRow(c, b.label, ix, x2, y, b.get(), () -> b.set(!b.get()));
        } else if (s instanceof NumberSetting n) {
            return slider(c, n.label, ix, x2, y, n.min, n.max, n.step, n.get(), n::set);
        }
        return y;
    }

    private int switchRow(DrawContext c, String label, int x1, int x2, int y, boolean value, Runnable toggle) {
        rect(c, x1, y, x2, y + 20, 0xFF121922);
        text(c, label, x1 + 8, y + 6, 0xFFD5DCE6);
        sw(c, x2 - 30, y + 5, value);
        hits.add(new Hit(x1, y, x2, y + 20, v -> toggle.run(), null));
        return y + 22;
    }

    private int cycleRow(DrawContext c, String label, int x1, int x2, int y, String value, Runnable dec, Runnable inc) {
        rect(c, x1, y, x2, y + 22, 0xFF121922);
        text(c, label, x1 + 8, y + 7, 0xFFD5DCE6);
        text(c, "<", x2 - 124, y + 7, ACCENT);
        int vw = this.textRenderer.getWidth(value);
        text(c, value, x2 - 70 - vw / 2, y + 7, 0xFFFFFFFF);
        text(c, ">", x2 - 14, y + 7, ACCENT);
        final int split = x2 - 70;
        hits.add(new Hit(x2 - 130, y, x2, y + 22, v -> { if (v < split) dec.run(); else inc.run(); }, null));
        return y + 24;
    }

    private int slider(DrawContext c, String label, int x1, int x2, int y, double min, double max, double step,
                       double value, DoubleConsumer setter) {
        rect(c, x1, y, x2, y + 20, 0xFF121922);
        text(c, label, x1 + 8, y + 6, 0xFFD5DCE6);
        final int bx1 = x2 - 130;
        final int bx2 = x2 - 50;
        double t = Math.max(0, Math.min(1, (value - min) / (max - min)));
        int fx = bx1 + (int) ((bx2 - bx1) * t);
        rect(c, bx1, y + 8, bx2, y + 12, 0xFF2A3442);
        rect(c, bx1, y + 8, fx, y + 12, ACCENT);
        rect(c, fx - 2, y + 5, fx + 2, y + 15, 0xFFFFFFFF);
        String vs = step >= 1 ? String.valueOf((int) Math.round(value)) : String.format("%.2f", value);
        text(c, vs, bx2 + 8, y + 6, 0xFFFFFFFF);
        DoubleConsumer set = mxv -> {
            double tt = Math.max(0, Math.min(1, (mxv - bx1) / (double) (bx2 - bx1)));
            double v = min + tt * (max - min);
            v = Math.round(v / step) * step;
            setter.accept(Math.max(min, Math.min(max, v)));
        };
        hits.add(new Hit(bx1 - 4, y, bx2 + 4, y + 20, set, set));
        return y + 22;
    }

    /** Souris lue directement (GLFW) : indépendant des changements de signatures de Screen. */
    private void handleMouse(int mx, int my) {
        long handle = this.client.getWindow().getHandle();
        boolean down = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        if (down && !wasDown) {
            for (Hit h : hits) {
                if (h.contains(mx, my)) {
                    h.press().accept(mx);
                    dragging = h;
                    break;
                }
            }
        } else if (down && dragging != null && dragging.drag() != null) {
            dragging.drag().accept(mx);
        }
        if (!down && wasDown) {
            dragging = null;
            Client802.CONFIG.save();
            if (category == 2) MarlowCrystal.get().saveSettings();
        }
        wasDown = down;
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}
