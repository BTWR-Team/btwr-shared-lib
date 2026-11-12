package org.btwr.shared_library.gui.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;
import net.minecraft.item.ItemStack;

/**
 * A category in the left-hand rail: an icon and a name, with an accent bar when selected.
 */
class RailButton extends PressableWidget {
    static final int HEIGHT = 22;

    private final ItemStack icon;
    private final boolean selected;
    private final Runnable onPress;

    RailButton(int x, int y, int width, Text name, ItemStack icon, boolean selected, Runnable onPress) {
        super(x, y, width, HEIGHT, name);
        this.icon = icon;
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
        if (this.selected || lit) {
            context.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), this.selected ? ConfigUiStyle.CARD : ConfigUiStyle.CARD_HOVER);
        }
        if (this.selected) {
            context.fill(this.getX(), this.getY(), this.getX() + 2, this.getBottom(), ConfigUiStyle.ACCENT);
        }

        context.drawItem(this.icon, this.getX() + 6, this.getY() + 3);

        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        String text = ConfigUiStyle.fit(font, this.getMessage().getString(), this.width - 30);
        ConfigUiStyle.label(context, font, text, this.getX() + 26, this.getY() + 7, this.selected || lit ? ConfigUiStyle.TEXT : ConfigUiStyle.TEXT_FAINT);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }
}