package org.btwr.shared_library.gui.config;

import org.lwjgl.glfw.GLFW;
import net.minecraft.util.Formatting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Draws a {@link Checklist}'s items as compact cards with a tick box. Right-clicking copies the ID,
 * for pasting into tag files.
 */
class ChecklistCards implements Cards<Checklist.Entry> {
    private static final int BOX = 9;

    private final ConfigUiScreen screen;
    private final Checklist checklist;

    ChecklistCards(ConfigUiScreen screen, Checklist checklist) {
        this.screen = screen;
        this.checklist = checklist;
    }

    @Override
    public int minWidth() {
        return 136;
    }

    @Override
    public int maxColumns() {
        return 6;
    }

    @Override
    public int height(int cardWidth) {
        return 21;
    }

    @Override
    public int gap() {
        return 3;
    }

    @Override
    public void render(DrawContext context, Checklist.Entry item, int x, int y, int width, int height, boolean hovered, boolean focused, int mouseX, int mouseY) {
        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        boolean checked = this.checklist.isChecked(item.id());
        boolean locked = this.checklist.lockReason(item.id()) != null;

        int fill = checked ? (hovered ? ConfigUiStyle.CARD_ON_HOVER : ConfigUiStyle.CARD_ON) : (hovered ? ConfigUiStyle.CARD_HOVER : ConfigUiStyle.CARD);
        context.fill(x, y, x + width, y + height, fill);
        if (focused || (checked && !locked)) {
            ConfigUiStyle.outline(context, x, y, width, height, ConfigUiStyle.ACCENT);
        }

        context.drawItem(item.icon(), x + 3, y + 3);

        int textWidth = width - 22 - BOX - 6;
        ConfigUiStyle.label(context, font, ConfigUiStyle.fit(font, item.name(), textWidth), x + 22, y + 2, ConfigUiStyle.TEXT);
        ConfigUiStyle.label(context, font, ConfigUiStyle.fit(font, item.id(), textWidth), x + 22, y + 11, ConfigUiStyle.TEXT_FAINT);

        int boxX = x + width - BOX - 3;
        int boxY = y + (height - BOX) / 2;
        if (locked) {
            context.drawGuiTexture(ConfigUiStyle.LOCK, boxX + 1, y + (height - 9) / 2, 7, 9);
            return;
        }

        context.fill(boxX, boxY, boxX + BOX, boxY + BOX, ConfigUiStyle.FIELD);
        ConfigUiStyle.outline(context, boxX, boxY, BOX, BOX, checked ? ConfigUiStyle.ACCENT : ConfigUiStyle.LINE);

        if (checked) {
            context.fill(boxX + 2, boxY + 2, boxX + BOX - 2, boxY + BOX - 2, ConfigUiStyle.ACCENT);
        }
    }

    @Override
    public boolean click(Checklist.Entry item, int x, int y, int width, int height, int mouseX, int mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            MinecraftClient.getInstance().keyboard.setClipboard(item.id());
            this.screen.flash(Text.translatable("screen.config_ui.copied", item.id()));
            return true;
        }

        return this.toggle(item);
    }

    private boolean toggle(Checklist.Entry item) {
        if (this.checklist.lockReason(item.id()) != null) {
            return false;
        }

        this.checklist.setChecked(item.id(), !this.checklist.isChecked(item.id()));

        return true;
    }

    @Override
    public void activate(Checklist.Entry item, boolean shift) {
        this.toggle(item);
    }

    @Override
    public Text name(Checklist.Entry item) {
        return Text.literal(item.name());
    }

    @Override
    public List<Text> tooltip(Checklist.Entry item) {
        List<Text> lines = new ArrayList<>();
        lines.add(Text.literal(item.name()));
        lines.add(Text.literal(item.id()).formatted(Formatting.DARK_GRAY));

        Text reason = this.checklist.lockReason(item.id());
        if (reason != null) {
            lines.add(reason.copy().formatted(Formatting.GOLD));
        }

        return lines;
    }

    @Override
    public Text hint(Checklist.Entry item) {
        Text reason = this.checklist.lockReason(item.id());
        return reason != null ? reason : Text.translatable("screen.config_ui.copy_hint");
    }

    @Override
    public boolean matches(Checklist.Entry item, String search) {
        return search.isEmpty() || item.name().toLowerCase(Locale.ROOT).contains(search) || item.id().contains(search);
    }

    @Override
    public String completionFor(Checklist.Entry item, String typed) {
        for (String candidate : List.of(item.name(), item.id())) {
            if (candidate.length() > typed.length() && candidate.regionMatches(true, 0, typed, 0, typed.length())) {
                return candidate;
            }
        }

        return null;
    }

    @Override
    public String status(List<Checklist.Entry> items) {
        int checked = 0;

        for (Checklist.Entry item : items) {
            if (this.checklist.isChecked(item.id())) {
                checked++;
            }
        }

        return Text.translatable("screen.config_ui.checked", checked, items.size()).getString();
    }

    @Override
    public void reset(List<Checklist.Entry> items) {
        this.checklist.clear();
    }
}