package org.btwr.shared_library.gui.config;

import org.lwjgl.glfw.GLFW;
import net.minecraft.util.Formatting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.btwr.shared_library.api.config.TomlConfigManager;

import java.awt.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Draws {@link UiSetting}s as cards, choosing the control from the setting's type.
 */
class SettingCards implements Cards<UiSetting<?>> {
    private static final int STEPPER_WIDTH = 64;
    private static final int CHOICE_WIDTH = 96;
    private static final int STEPPER_HEIGHT = 16;
    private static final int BUTTON_ZONE = 16;
    private static final int SWITCH_WIDTH = 30;
    private static final int SWITCH_HEIGHT = 12;
    private static final Text DISABLED = Text.translatable("screen.config_ui.disabled");
    private static final int CARD_HEIGHT = 26;
    private static final int LINE_HEIGHT = 10;
    private static final int SWATCH_SIZE = STEPPER_HEIGHT;
    /** The colors a swatch steps through: pure white, then the dye colors in creative-tab order. */
    private static final int[] PALETTE = {
            0xFFFFFF, 0x9D9D97, 0x474F52, 0x1D1D21, 0x835432, 0xB02E26, 0xF9801D,
            0xFED83D, 0x80C71F, 0x5E7C16, 0x169C9C, 0x3AB3DA, 0x3C44AA, 0x8932B8, 0xC74EBD, 0xF38BAA
    };

    private final ConfigUiScreen screen;
    private final List<UiSetting<?>> settings;

    SettingCards(ConfigUiScreen screen, List<UiSetting<?>> settings) {
        this.screen = screen;
        this.settings = settings;
    }

    @Override
    public int minWidth() {
        return 170;
    }

    @Override
    public int maxColumns() {
        return 3;
    }

    /**
     * Cards only show the name and the control; the description is in the tooltip. A long name wraps onto
     * more lines, and every card is as tall as the longest name needs, so the rows stay even.
     */
    @Override
    public int height(int cardWidth) {
        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        int lines = 1;
        for (UiSetting<?> setting : this.settings) {
            lines = Math.max(lines, nameLines(font, setting, textWidth(setting, cardWidth)).size());
        }
        return Math.max(CARD_HEIGHT, lines * LINE_HEIGHT + 8);
    }

    /** The name wrapped to the text column. */
    private static List<String> nameLines(TextRenderer font, UiSetting<?> setting, int width) {
        List<String> lines = new ArrayList<>();
        for (StringVisitable line : font.getTextHandler().wrapLines(setting.name().getString(), Math.max(1, width), Style.EMPTY)) {
            lines.add(line.getString());
        }
        if (lines.isEmpty()) {
            lines.add("");
        }
        return lines;
    }

    @Override
    public int gap() {
        return 4;
    }

    private static boolean isOn(UiSetting<?> setting) {
        return setting.type() == Boolean.class && Boolean.TRUE.equals(setting.get());
    }

    private static int textWidth(UiSetting<?> setting, int cardWidth) {
        return cardWidth - 28 - controlWidth(setting) - 8;
    }

    private static int controlWidth(UiSetting<?> setting) {
        if (setting.type() == Boolean.class) {
            return SWITCH_WIDTH;
        }
        return setting.type().isEnum() || setting.type() == Color.class ? CHOICE_WIDTH : STEPPER_WIDTH;
    }

    @Override
    public void render(
            DrawContext context, UiSetting<?> setting, int x, int y, int width, int height,
            boolean hovered, boolean focused, int mouseX, int mouseY
    ) {
        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        boolean locked = setting.isLocked();
        boolean on = !locked && isOn(setting);

        int fill = on
                ? (hovered ? ConfigUiStyle.CARD_ON_HOVER : ConfigUiStyle.CARD_ON)
                : (hovered ? ConfigUiStyle.CARD_HOVER : ConfigUiStyle.CARD);

        context.fill(x, y, x + width, y + height, fill);
        context.fill(x, y, x + width, y + 1, ConfigUiStyle.CARD_HIGHLIGHT);
        context.fill(x, y, x + 2, y + height, on ? ConfigUiStyle.ACCENT : ConfigUiStyle.LINE);

        if (focused) {
            ConfigUiStyle.outline(context, x, y, width, height, ConfigUiStyle.ACCENT);
        }

        context.drawItem(setting.icon(), x + 7, y + (height - 16) / 2);

        int textWidth = textWidth(setting, width);
        List<String> nameLines = nameLines(font, setting, textWidth);
        int lineY = y + (height - nameLines.size() * LINE_HEIGHT + 2) / 2;
        for (String line : nameLines) {
            ConfigUiStyle.label(context, font, line, x + 28, lineY, locked ? ConfigUiStyle.TEXT_DISABLED : ConfigUiStyle.TEXT);
            lineY += LINE_HEIGHT;
        }

        // A locked setting hides its value, since it isn't in effect
        if (locked && setting.type() != Boolean.class) {
            this.renderDisabled(context, font, x + width - controlWidth(setting) - 4, y + (height - STEPPER_HEIGHT) / 2, controlWidth(setting));
        } else if (locked) {
            context.drawGuiTexture(
                    ConfigUiStyle.SWITCH_LOCKED, x + width - SWITCH_WIDTH - 5,
                    y + (height - SWITCH_HEIGHT) / 2, SWITCH_WIDTH, SWITCH_HEIGHT
            );
        } else if (setting.type() == Boolean.class) {
            context.drawGuiTexture(
                    on ? ConfigUiStyle.SWITCH_ON : ConfigUiStyle.SWITCH_OFF,
                    x + width - SWITCH_WIDTH - 5, y + (height - SWITCH_HEIGHT) / 2, SWITCH_WIDTH, SWITCH_HEIGHT
            );
        } else if (setting.type() == Color.class) {
            this.renderColor(
                    context, font, (Color) setting.get(),
                    x + width - CHOICE_WIDTH - 4, y + (height - STEPPER_HEIGHT) / 2, hovered, mouseX
            );
        } else if (setting.type() == String.class) {
            this.renderTextValue(
                    context, font, setting,
                    x + width - STEPPER_WIDTH - 4, y + (height - STEPPER_HEIGHT) / 2
            );
        } else {
            int controlWidth = controlWidth(setting);
            this.renderStepper(
                    context, font, setting,
                    x + width - controlWidth - 4, y + (height - STEPPER_HEIGHT) / 2, controlWidth, hovered, mouseX
            );
        }
    }

    private void renderStepper(DrawContext context, TextRenderer font, UiSetting<?> setting, int x, int y, int stepperWidth, boolean hovered, int mouseX) {
        context.fill(x, y, x + stepperWidth, y + STEPPER_HEIGHT, ConfigUiStyle.FIELD);
        ConfigUiStyle.outline(context, x, y, stepperWidth, STEPPER_HEIGHT, ConfigUiStyle.LINE);

        boolean overMinus = hovered && mouseX >= x && mouseX < x + BUTTON_ZONE;
        boolean overPlus = hovered && mouseX >= x + stepperWidth - BUTTON_ZONE && mouseX < x + stepperWidth;
        ConfigUiStyle.label(
                context, font,
                setting.type().isEnum() ? "<" : "-", x + 6, y + 4, overMinus ? ConfigUiStyle.ACCENT : ConfigUiStyle.TEXT_FAINT
        );
        ConfigUiStyle.label(
                context, font,
                setting.type().isEnum() ? ">" : "+", x + stepperWidth - 10, y + 4, overPlus ? ConfigUiStyle.ACCENT : ConfigUiStyle.TEXT_FAINT
        );

        String value = ConfigUiStyle.fit(font, valueText(setting), stepperWidth - BUTTON_ZONE * 2 + 4);
        ConfigUiStyle.label(
                context, font, value,
                x + (stepperWidth - font.getWidth(value)) / 2, y + 4, ConfigUiStyle.TEXT
        );
    }

    /** An empty field that says "Disabled", in place of a stepper, picker or text box that's locked. */
    private void renderDisabled(DrawContext context, TextRenderer font, int x, int y, int fieldWidth) {
        context.fill(x, y, x + fieldWidth, y + STEPPER_HEIGHT, ConfigUiStyle.FIELD_DISABLED);
        ConfigUiStyle.outline(context, x, y, fieldWidth, STEPPER_HEIGHT, ConfigUiStyle.LINE);
        String label = ConfigUiStyle.fit(font, DISABLED.getString(), fieldWidth - 6);
        ConfigUiStyle.label(context, font, label, x + (fieldWidth - font.getWidth(label)) / 2, y + 4, ConfigUiStyle.TEXT_LOCKED);
    }

    private void renderTextValue(DrawContext context, TextRenderer font, UiSetting<?> setting, int x, int y) {
        context.fill(x, y, x + STEPPER_WIDTH, y + STEPPER_HEIGHT, ConfigUiStyle.FIELD);
        ConfigUiStyle.outline(context, x, y, STEPPER_WIDTH, STEPPER_HEIGHT, ConfigUiStyle.LINE);
        ConfigUiStyle.label(
                context, font, ConfigUiStyle.fit(font, String.valueOf(setting.get()), STEPPER_WIDTH - 6),
                x + 3, y + 4, ConfigUiStyle.TEXT
        );
    }

    /** A swatch of the color, then its hex code in a field. */
    private void renderColor(DrawContext context, TextRenderer font, Color color, int x, int y, boolean hovered, int mouseX) {
        boolean overSwatch = hovered && mouseX >= x && mouseX < x + SWATCH_SIZE;
        ConfigUiStyle.outline(context, x, y, SWATCH_SIZE, SWATCH_SIZE, overSwatch ? ConfigUiStyle.ACCENT : ConfigUiStyle.LINE);
        context.fill(x + 1, y + 1, x + SWATCH_SIZE - 1, y + SWATCH_SIZE - 1, ConfigUiStyle.BLACK);
        if (color != null) {
            context.fill(x + 2, y + 2, x + SWATCH_SIZE - 2, y + SWATCH_SIZE - 2, color.getRGB());
        }

        int fieldX = x + SWATCH_SIZE + 4;
        int fieldWidth = CHOICE_WIDTH - SWATCH_SIZE - 4;
        boolean overField = hovered && mouseX >= fieldX;
        context.fill(fieldX, y, fieldX + fieldWidth, y + STEPPER_HEIGHT, ConfigUiStyle.FIELD);
        ConfigUiStyle.outline(context, fieldX, y, fieldWidth, STEPPER_HEIGHT, overField ? ConfigUiStyle.ACCENT : ConfigUiStyle.LINE);
        String hex = color != null ? TomlConfigManager.toHex(color) : "-";
        ConfigUiStyle.label(context, font, hex, fieldX + (fieldWidth - font.getWidth(hex)) / 2, y + 4, ConfigUiStyle.TEXT);
    }

    /** Steps to the next palette color, or the one before. A color that isn't in the palette goes to its start. */
    private static void cyclePalette(UiSetting<Color> setting, int steps) {
        Color current = setting.get();
        int index = -1;
        for (int i = 0; i < PALETTE.length; i++) {
            if (current != null && (current.getRGB() & 0xFFFFFF) == PALETTE[i]) {
                index = i;
                break;
            }
        }
        int next = index < 0 ? 0 : Math.floorMod(index + Integer.signum(steps), PALETTE.length);
        setting.set(new Color(PALETTE[next]));
    }

    private static <T> String valueText(UiSetting<T> setting) {
        return setting.type().isEnum() ? setting.valueName(setting.get()).getString() : formatNumber(setting.get());
    }

    private static String formatNumber(Object value) {
        return value instanceof Double || value instanceof Float
                ? String.format(Locale.ROOT, "%.2f", ((Number) value).doubleValue())
                : String.valueOf(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean click(UiSetting<?> setting, int x, int y, int width, int height, int mouseX, int mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }

        if (setting.isLocked()) {
            this.screen.flash(setting.lockReason());
            return true;
        }

        if (setting.type() == Boolean.class) {
            toggle((UiSetting<Boolean>) setting);
            return true;
        }

        int controlWidth = controlWidth(setting);
        int controlX = x + width - controlWidth - 4;
        if (setting.type() == String.class) {
            this.screen.editText((UiSetting<String>) setting, x + 28, y + (height - STEPPER_HEIGHT) / 2, width - 32);
            return true;
        }

        if (setting.type() == Color.class) {
            UiSetting<Color> color = (UiSetting<Color>) setting;
            int controlY = y + (height - STEPPER_HEIGHT) / 2;
            if (mouseX >= controlX && mouseX < controlX + SWATCH_SIZE) {
                cyclePalette(color, Screen.hasShiftDown() ? -1 : 1);
            } else if (mouseX >= controlX + SWATCH_SIZE) {
                int fieldX = controlX + SWATCH_SIZE + 4;
                String hex = color.get() != null ? TomlConfigManager.toHex(color.get()) : "";
                this.screen.editText(color.name(), hex, 9, text -> {
                    Color parsed = TomlConfigManager.parseColor(text);
                    if (parsed != null) {
                        color.set(parsed);
                    } else if (!text.isBlank()) {
                        this.screen.flash(Text.translatable("screen.config_ui.invalid_color", text));
                    }
                }, fieldX, controlY, controlX + controlWidth - fieldX);
            } else {
                return false;
            }
            return true;
        }

        int amount = Screen.hasShiftDown() ? 10 : 1;
        if (mouseX >= controlX && mouseX < controlX + BUTTON_ZONE) {
            step(setting, -amount);
            return true;
        }

        if (mouseX >= controlX + controlWidth - BUTTON_ZONE && mouseX < controlX + controlWidth) {
            step(setting, amount);
            return true;
        }

        // A picker also moves forward when its value is clicked, not just its arrow.
        if (setting.type().isEnum() && mouseX >= controlX && mouseX < controlX + controlWidth) {
            step(setting, 1);
            return true;
        }

        return false;
    }

    private static void toggle(UiSetting<Boolean> setting) {
        setting.set(!Boolean.TRUE.equals(setting.get()));
    }

    @SuppressWarnings("unchecked")
    private static void step(UiSetting<?> setting, int steps) {
        if (setting.type() == Integer.class) {
            UiSetting<Integer> number = (UiSetting<Integer>) setting;
            int value = number.get() + steps * number.step();

            // Large steps land on round numbers, even when starting from something like -1 or a hand-edited value.
            if (number.step() > 1) {
                value = (int) Math.round((double) value / number.step()) * number.step();
            }

            if (number.min() != null) {
                value = Math.max(value, number.min());
            }

            if (number.max() != null) {
                value = Math.min(value, number.max());
            }

            number.set(value);
        } else if (setting.type() == Double.class) {
            UiSetting<Double> number = (UiSetting<Double>) setting;
            number.set(stepDecimal(number, steps));
        } else if (setting.type() == Float.class) {
            UiSetting<Float> number = (UiSetting<Float>) setting;
            number.set((float) stepDecimal(number, steps));
        } else if (setting.type().isEnum()) {
            cycle(setting, steps);
        }
    }

    /** Moves one constant along, wrapping at either end. Shift's tenfold step doesn't apply here. */
    private static <T> void cycle(UiSetting<T> setting, int steps) {
        T[] values = setting.type().getEnumConstants();
        int index = ((Enum<?>) setting.get()).ordinal() + Integer.signum(steps);
        setting.set(values[Math.floorMod(index, values.length)]);
    }

    /** The next value of a Double or Float stepper, kept within its min and max. */
    private static <T extends Number> double stepDecimal(UiSetting<T> number, int steps) {
        // Rounded so repeated steps don't build up values like 0.30000000000000004.
        double value = Math.round((number.get().doubleValue() + steps * decimalStep(number)) * 1000.0) / 1000.0;

        if (number.min() != null) {
            value = Math.max(value, number.min().doubleValue());
        }

        if (number.max() != null) {
            value = Math.min(value, number.max().doubleValue());
        }

        return value;
    }

    /** A twentieth of the range when there is one, so any range takes the same number of clicks. */
    private static <T extends Number> double decimalStep(UiSetting<T> setting) {
        return setting.min() != null && setting.max() != null
                ? (setting.max().doubleValue() - setting.min().doubleValue()) / 20.0
                : 0.1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void activate(UiSetting<?> setting, boolean shift) {
        if (setting.isLocked()) {
            this.screen.flash(setting.lockReason());
        } else if (setting.type() == Boolean.class) {
            toggle((UiSetting<Boolean>) setting);
        } else if (setting.type() == Color.class) {
            cyclePalette((UiSetting<Color>) setting, shift ? -1 : 1);
        } else if (setting.type() != String.class) {
            step(setting, shift ? -1 : 1);
        }
    }

    /** No tooltip while the mouse is on the control, so it doesn't cover what's being changed. */
    @Override
    public boolean showsTooltipAt(UiSetting<?> setting, int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX < x + width - controlWidth(setting) - 6;
    }

    @Override
    public Text name(UiSetting<?> setting) {
        return setting.name();
    }

    @Override
    public List<Text> tooltip(UiSetting<?> setting) {
        List<Text> lines = new ArrayList<>();
        lines.add(setting.name());
        if (!setting.description().getString().isEmpty()) {
            lines.add(setting.description().copy().formatted(Formatting.GRAY));
        }
        lines.add(
                Text.translatable(
                        "screen.config_ui.default",
                        formatDefault(setting)
                ).formatted(Formatting.DARK_GRAY)
        );

        if (setting.min() != null || setting.max() != null) {
            Text any = Text.translatable("screen.config_ui.any");
            lines.add(
                    Text.translatable(
                            "screen.config_ui.range",
                            setting.min() != null ? formatNumber(setting.min()) : any,
                            setting.max() != null ? formatNumber(setting.max()) : any
                    ).formatted(Formatting.DARK_GRAY)
            );
        }

        if (setting.type() == Color.class) {
            lines.add(Text.translatable("screen.config_ui.color_hint").formatted(Formatting.DARK_GRAY));
        }

        if (setting.isLocked()) {
            lines.add(setting.lockReason().copy().formatted(Formatting.GOLD));
        }

        return lines;
    }

    private static <T> Object formatDefault(UiSetting<T> setting) {
        T value = setting.defaultValue();
        if (setting.type().isEnum()) {
            return setting.valueName(value);
        }
        if (value instanceof Color color) {
            return TomlConfigManager.toHex(color);
        }
        return value instanceof Boolean on ? (on ? ScreenTexts.ON : ScreenTexts.OFF) : formatNumber(value);
    }

    @Override
    public Text hint(UiSetting<?> setting) {
        return setting.description();
    }

    @Override
    public boolean matches(UiSetting<?> setting, String search) {
        return search.isEmpty()
                || setting.name().getString().toLowerCase(Locale.ROOT).contains(search)
                || setting.description().getString().toLowerCase(Locale.ROOT).contains(search);
    }

    @Override
    public String completionFor(UiSetting<?> setting, String typed) {
        String name = setting.name().getString();
        return name.length() > typed.length() && name.regionMatches(true, 0, typed, 0, typed.length()) ? name : null;
    }

    @Override
    public String status(List<UiSetting<?>> settings) {
        return "";
    }

    @Override
    public void reset(List<UiSetting<?>> settings) {
        settings.stream().filter(setting -> !setting.isLocked()).forEach(UiSetting::reset);
    }
}