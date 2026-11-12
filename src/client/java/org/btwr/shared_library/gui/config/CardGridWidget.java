package org.btwr.shared_library.gui.config;

import org.lwjgl.glfw.GLFW;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * A scrolling grid of cards that reflows to its size: a bigger window gets more columns and rows.
 */
class CardGridWidget<T> extends ClickableWidget {
    private final Screen screen;
    private final Cards<T> cards;
    private final Scroller scroller;
    private final int columns;
    private final int rows;
    private final int cardWidth;
    private final int cardHeight;

    private List<T> items = List.of();
    private int cursor = -1;
    private T hoveredItem;

    CardGridWidget(Screen screen, Cards<T> cards, int x, int y, int width, int height, Scroller scroller) {
        super(x, y, width, height, Text.empty());
        this.screen = screen;
        this.cards = cards;
        this.scroller = scroller;
        this.columns = MathHelper.clamp((width + cards.gap()) / (cards.minWidth() + cards.gap()), 1, cards.maxColumns());
        this.cardWidth = (width - (this.columns - 1) * cards.gap()) / this.columns;
        this.cardHeight = cards.height(this.cardWidth);
        this.rows = Math.max(1, (height + cards.gap()) / (this.cardHeight + cards.gap()));
    }

    /** Replaces the items and scrolls back to the top. */
    void setItems(List<T> items) {
        this.items = items;
        this.cursor = -1;
        this.scroller.setRows(this.totalRows(), this.rows);
        this.scroller.setProgress(0.0F);
    }

    int itemCount() {
        return this.items.size();
    }

    /** The footer line for the card under the mouse, or null. */
    Text hoverHint() {
        return this.hoveredItem == null ? null : this.cards.hint(this.hoveredItem);
    }

    private int totalRows() {
        return (this.items.size() + this.columns - 1) / this.columns;
    }

    private int maxFirstRow() {
        return Math.max(0, this.totalRows() - this.rows);
    }

    private int firstRow() {
        return Math.round(this.scroller.progress() * this.maxFirstRow());
    }

    private int cardX(int column) {
        return this.getX() + column * (this.cardWidth + this.cards.gap());
    }

    private int cardY(int row) {
        return this.getY() + row * (this.cardHeight + this.cards.gap());
    }

    /** Finds the card under the mouse, or -1. The gaps between cards don't count. */
    private int indexAt(double mouseX, double mouseY) {
        if (mouseX < this.getX() || mouseY < this.getY()) {
            return -1;
        }

        int column = (int) ((mouseX - this.getX()) / (this.cardWidth + this.cards.gap()));

        int row = (int) ((mouseY - this.getY()) / (this.cardHeight + this.cards.gap()));
        if (column >= this.columns || row >= this.rows
                || mouseX >= this.cardX(column) + this.cardWidth
                || mouseY >= this.cardY(row) + this.cardHeight) {
            return -1;
        }

        int index = (this.firstRow() + row) * this.columns + column;

        return index < this.items.size() ? index : -1;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float partialTick) {
        int hoveredIndex = this.isHovered() ? this.indexAt(mouseX, mouseY) : -1;
        this.hoveredItem = hoveredIndex >= 0 ? this.items.get(hoveredIndex) : null;

        int first = this.firstRow() * this.columns;
        int visible = Math.min(this.columns * this.rows, this.items.size() - first);
        boolean showCursor = ConfigUiStyle.showsFocus(this);

        for (int offset = 0; offset < visible; offset++) {
            int index = first + offset;
            this.cards.render(
                    context, this.items.get(index),
                    this.cardX(offset % this.columns), this.cardY(offset / this.columns),
                    this.cardWidth, this.cardHeight,
                    index == hoveredIndex, showCursor && index == this.cursor, mouseX, mouseY
            );
        }

        if (this.hoveredItem != null) {
            List<OrderedText> lines = new ArrayList<>();

            for (Text line : this.cards.tooltip(this.hoveredItem)) {
                lines.addAll(MinecraftClient.getInstance().textRenderer.wrapLines(line, 220));
            }

            if (!lines.isEmpty()) {
                this.screen.setTooltip(lines);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isMouseOver(mouseX, mouseY) || button > GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            return false;
        }

        int index = this.indexAt(mouseX, mouseY);
        if (index >= 0) {
            this.cursor = index;
            int offset = index - this.firstRow() * this.columns;
            boolean acted = this.cards.click(this.items.get(index),
                    this.cardX(offset % this.columns), this.cardY(offset / this.columns),
                    this.cardWidth, this.cardHeight, (int) mouseX, (int) mouseY, button);
            if (acted) {
                this.playClick();
            }
        }

        return true;
    }

    private void playClick() {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public void playDownSound(SoundManager handler) {
        // Clicks on empty space stay silent, so mouseClicked plays this itself.
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return this.scroller.wheel(scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.isFocused() || this.items.isEmpty()) {
            return false;
        }

        int target = switch (keyCode) {
            case GLFW.GLFW_KEY_UP -> this.cursor - this.columns;
            case GLFW.GLFW_KEY_DOWN -> this.cursor < 0 ? 0 : this.cursor + this.columns;
            case GLFW.GLFW_KEY_LEFT -> this.columns > 1 ? this.cursor - 1 : -1;
            case GLFW.GLFW_KEY_RIGHT -> this.columns > 1 ? this.cursor + 1 : -1;
            default -> -1;
        };

        if (target >= 0 && target < this.items.size()) {
            this.cursor = target;
            this.ensureVisible(target);
            return true;
        }

        if (KeyCodes.isToggle(keyCode) && this.cursor >= 0) {
            this.cards.activate(this.items.get(this.cursor), Screen.hasShiftDown());
            this.playClick();
            return true;
        }

        return keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN;
    }

    private void ensureVisible(int index) {
        int row = index / this.columns;
        int first = this.firstRow();
        int max = this.maxFirstRow();

        if (max == 0) {
            return;
        }

        if (row < first) {
            this.scroller.setProgress((float) row / max);
        } else if (row >= first + this.rows) {
            this.scroller.setProgress((float) (row - this.rows + 1) / max);
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        if (this.cursor >= 0 && this.cursor < this.items.size()) {
            builder.put(NarrationPart.TITLE, this.cards.name(this.items.get(this.cursor)));
        }
    }
}