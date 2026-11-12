package org.btwr.shared_library.gui.config;

import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

/**
 * A slim scrollbar. The grid it belongs to reads {@link #progress()}, and tells it how many rows there
 * are so the knob can show how much is visible.
 */
class Scroller extends ClickableWidget {
    static final int WIDTH = 4;
    private static final int MIN_KNOB = 12;

    private float progress;
    private int scrollableRows;
    private int knobHeight = MIN_KNOB;

    Scroller(int x, int y, int height) {
        super(x, y, WIDTH, height, Text.empty());
    }

    float progress() {
        return this.progress;
    }

    void setProgress(float progress) {
        this.progress = MathHelper.clamp(progress, 0.0F, 1.0F);
    }

    void setRows(int totalRows, int visibleRows) {
        this.scrollableRows = Math.max(0, totalRows - visibleRows);
        this.knobHeight = totalRows > 0
                ? MathHelper.clamp(this.height * visibleRows / totalRows, MIN_KNOB, this.height)
                : this.height;
    }

    boolean wheel(double scrollY) {
        if (this.scrollableRows == 0) {
            return false;
        }
        this.setProgress(this.progress - (float) Math.signum(scrollY) / this.scrollableRows);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return this.wheel(scrollY);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.moveTo(mouseY);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        this.moveTo(mouseY);
    }

    private void moveTo(double mouseY) {
        int travel = this.height - this.knobHeight;
        if (this.scrollableRows > 0 && travel > 0) {
            this.setProgress((float) ((mouseY - this.getY() - this.knobHeight / 2.0) / travel));
        }
    }

    @Override
    public void playDownSound(SoundManager handler) {
    }

    @Override
    public GuiNavigationPath getNavigationPath(GuiNavigation event) {
        // The grid handles its own arrow keys, so this doesn't need keyboard focus.
        return null;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float partialTick) {
        if (this.scrollableRows == 0) {
            return;
        }
        context.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), ConfigUiStyle.FIELD);
        int knobY = this.getY() + Math.round(this.progress * (this.height - this.knobHeight));
        context.fill(this.getX(), knobY, this.getRight(), knobY + this.knobHeight, this.isHovered() ? ConfigUiStyle.TEXT_FAINT : ConfigUiStyle.LINE);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }
}