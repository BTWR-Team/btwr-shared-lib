package org.btwr.shared_library.gui.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;

/**
 * A plain flat button. Selected buttons get the accent colour, which is how section pills show the
 * current section.
 */
class FlatButton extends PressableWidget {
    private final Runnable onPress;
    private final boolean selected;

    FlatButton(int x, int y, int width, int height, Text message, boolean selected, Runnable onPress) {
        super(x, y, width, height, message);
        this.selected = selected;
        this.onPress = onPress;
    }

    @Override
    public void onPress() {
        this.onPress.run();
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float partialTick) {
        boolean lit = this.isHovered() || ConfigUiStyle.showsFocus(this);
        int fill = this.selected ? ConfigUiStyle.CARD_ON : lit ? ConfigUiStyle.CARD_HIGHLIGHT : ConfigUiStyle.CARD;
        int border = this.selected || ConfigUiStyle.showsFocus(this) ? ConfigUiStyle.ACCENT : ConfigUiStyle.LINE;
        context.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), fill);
        ConfigUiStyle.outline(context, this.getX(), this.getY(), this.width, this.height, border);

        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        String text = ConfigUiStyle.fit(font, this.getMessage().getString(), this.width - 6);
        int color = this.selected || lit ? ConfigUiStyle.TEXT : ConfigUiStyle.TEXT_FAINT;
        ConfigUiStyle.label(context, font, text, this.getX() + (this.width - font.getWidth(text)) / 2, this.getY() + (this.height - 8) / 2, color);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }
}