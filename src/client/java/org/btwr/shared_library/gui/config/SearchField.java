package org.btwr.shared_library.gui.config;

import org.lwjgl.glfw.GLFW;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A search box with gray ghost text that Tab accepts, like vanilla command suggestions. With nothing
 * typed the ghost text shows the hint, because TextFieldWidget hides its own hint while focused.
 */
class SearchField extends TextFieldWidget {
    private static final int PADDING_X = 4;
    private static final int PADDING_Y = 3;

    private final String hint;
    private Function<String, String> completer = typed -> null;
    private String completion;

    SearchField(TextRenderer font, int x, int y, int width, int height, Text hint, String value) {
        super(font, x + PADDING_X, y + PADDING_Y, width - PADDING_X * 2, height - PADDING_Y * 2, hint);
        this.hint = hint.getString();
        this.setDrawsBackground(false);
        this.setMaxLength(256);
        this.setText(value);
    }

    /** Sets what finds a full completion for the typed text, returning null when there isn't one. */
    void setCompleter(Function<String, String> completer) {
        this.completer = completer;
        this.refreshSuggestion();
    }

    void onChange(Consumer<String> listener) {
        this.setChangedListener(value -> {
            listener.accept(value);
            this.refreshSuggestion();
        });
    }

    private void refreshSuggestion() {
        String typed = this.getText();
        if (typed.isEmpty()) {
            this.completion = null;
            this.setSuggestion(this.hint);
            return;
        }
        this.completion = this.completer.apply(typed);
        this.setSuggestion(this.completion == null ? null : this.completion.substring(typed.length()));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_TAB && this.isFocused() && this.completion != null) {
            this.setText(this.completion);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float partialTick) {
        int x = this.getX() - PADDING_X;
        int y = this.getY() - PADDING_Y;
        int width = this.width + PADDING_X * 2;
        int height = this.height + PADDING_Y * 2;
        context.fill(x, y, x + width, y + height, ConfigUiStyle.FIELD);
        ConfigUiStyle.outline(context, x, y, width, height, this.isFocused() ? ConfigUiStyle.ACCENT : ConfigUiStyle.LINE);
        super.renderWidget(context, mouseX, mouseY, partialTick);
    }
}