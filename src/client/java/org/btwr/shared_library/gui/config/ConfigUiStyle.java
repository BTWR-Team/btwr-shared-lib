package org.btwr.shared_library.gui.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.util.Identifier;

/**
 * Colours and small drawing helpers for the config UI.
 */
final class ConfigUiStyle {
    /** The namespace holding the sprites. */
    private static final String NAMESPACE = "btwr_sl";

    static final Identifier SWITCH_ON = sprite("switch_on");
    static final Identifier SWITCH_OFF = sprite("switch_off");
    static final Identifier SWITCH_LOCKED = sprite("switch_locked");
    static final Identifier LOCK = sprite("lock");

    static final int BLACK = 0xFF000000;
    static final int BASE = 0xFF1B1C20;
    static final int RAIL = 0xFF141518;
    static final int CARD = 0xFF2A2B31;
    static final int CARD_HOVER = 0xFF34353C;
    static final int CARD_HIGHLIGHT = 0xFF3A3B42;
    static final int CARD_ON = 0xFF322D26;
    static final int CARD_ON_HOVER = 0xFF3D362C;
    static final int FIELD = 0xFF0C0C0E;
    static final int FIELD_DISABLED = 0xFF1F2024;
    static final int LINE = 0xFF46474E;
    static final int ACCENT = 0xFFE89624;
    static final int TEXT = 0xFFE8E8EC;
    static final int TEXT_FAINT = 0xFF9697A0;
    static final int TEXT_DISABLED = 0xFF6E6E76;
    static final int TEXT_LOCKED = 0xFFC8544C;
    static final int TITLE = 0xFFFFECC4;

    private ConfigUiStyle() {
    }

    private static Identifier sprite(String name) {
        return Identifier.of(NAMESPACE, "config_ui/" + name);
    }

    static void label(DrawContext context, TextRenderer font, String text, int x, int y, int color) {
        context.drawText(font, text, x, y, color, false);
    }

    static String fit(TextRenderer font, String text, int maxWidth) {
        if (font.getWidth(text) <= maxWidth) {
            return text;
        }
        return font.trimToWidth(text, Math.max(0, maxWidth - font.getWidth("..."))) + "...";
    }

    /**
     * Draws a one-pixel box border.
     */
    static void outline(DrawContext context, int x, int y, int width, int height, int color) {
        context.drawBorder(x, y, width, height, color);
    }

    /**
     * Mouse clicks leave a widget focused, so only show focus when navigating by keyboard.
     */
    static boolean showsFocus(ClickableWidget widget) {
        return widget.isFocused() && MinecraftClient.getInstance().getNavigationType().isKeyboard();
    }
}