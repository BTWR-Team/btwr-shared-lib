package org.btwr.shared_library.gui.config;

import net.minecraft.text.Text;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.btwr.shared_library.api.config.ConfigGroup;
import org.btwr.shared_library.api.config.ConfigSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * One setting shown as a card. The screen picks the control from the type: a switch for Boolean, a
 * stepper for Integer, Double and Float, a text box for String, and a cycling picker for enums. Min and max can
 * be null for no limit.
 */
public final class UiSetting<T> {
    private static final Text DEFAULT_LOCK_REASON = Text.translatable("screen.config_ui.locked");

    private record Lock(BooleanSupplier condition, Text reason) {}

    private final Class<T> type;
    private final Text name;
    private final Text description;
    private final T defaultValue;
    private final T min;
    private final T max;
    private final Supplier<T> getter;
    private final Consumer<T> setter;
    private Function<T, Text> valueName = value -> Text.literal(String.valueOf(value));
    private ItemStack icon = new ItemStack(Items.PAPER);
    private int step = 1;
    private final List<Lock> locks = new ArrayList<>();
    private ConfigGroup group;

    public UiSetting(Class<T> type, Text name, Text description, T defaultValue, T min, T max, Supplier<T> getter, Consumer<T> setter) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
        this.min = min;
        this.max = max;
        this.getter = getter;
        this.setter = setter;
    }

    public static UiSetting<Boolean> toggle(Text name, Text description, boolean defaultValue, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return new UiSetting<>(Boolean.class, name, description, defaultValue, null, null, getter, setter);
    }

    /**
     * A card for a setting made with {@code ConfigBuilder}. The type, default, min and max come from the
     * setting, changes go straight into it, and the screen saves its config file when it closes.
     */
    public static <T> UiSetting<T> of(ConfigSetting<T> setting, Text name, Text description) {
        UiSetting<T> ui = new UiSetting<>(setting.getType(), name, description, setting.getDefaultValue(),
                setting.getMin(), setting.getMax(), setting::get, value -> setting.set(value, false));
        ui.group = setting.getGroup();
        if (setting.getType().isEnum()) {
            ui.valueName = value -> Text.literal(readableName(((Enum<?>) value).name().toLowerCase()));
        }
        return ui;
    }

    /**
     * Same as {@link #of(ConfigSetting, Text, Text)}, named from translation keys: {@code <prefix><key>} for
     * the name and {@code <prefix>tooltip.<key>} for the description. Missing translations fall back to the
     * setting's key, made readable, and its comment.
     */
    public static <T> UiSetting<T> of(ConfigSetting<T> setting, String translationPrefix) {
        String key = setting.getKey();
        String comment = setting.getComment() != null ? setting.getComment() : "";
        return of(setting,
                Text.translatableWithFallback(translationPrefix + key, readableName(key)),
                Text.translatableWithFallback(translationPrefix + "tooltip." + key, comment));
    }

    /** How the values show on the card, e.g. a translated name for each enum constant. */
    public UiSetting<T> valueName(Function<T, Text> valueName) {
        this.valueName = valueName;
        return this;
    }

    /** A picker that cycles through the enum's constants, shown with {@code valueName}. */
    public static <E extends Enum<E>> UiSetting<E> choice(Class<E> type, Text name, Text description, E defaultValue,
                                                          Function<E, Text> valueName, Supplier<E> getter, Consumer<E> setter) {
        UiSetting<E> setting = new UiSetting<>(type, name, description, defaultValue, null, null, getter, setter);
        setting.valueName = valueName;
        return setting;
    }

    public UiSetting<T> icon(ItemStack icon) {
        this.icon = icon;
        return this;
    }

    /** How much one click changes an Integer stepper. Shift still moves ten times as far. */
    public UiSetting<T> step(int step) {
        this.step = step;
        return this;
    }

    /** Stops the setting being changed while the condition is true, which is re-checked every frame. */
    public UiSetting<T> locked(BooleanSupplier locked) {
        return this.locked(locked, DEFAULT_LOCK_REASON);
    }

    /**
     * Same as {@link #locked(BooleanSupplier)}, but explains why with a message of its own. A setting
     * can have several locks. It's locked while any of them is active, and shows the first active reason.
     */
    public UiSetting<T> locked(BooleanSupplier locked, Text reason) {
        this.locks.add(new Lock(locked, reason));
        return this;
    }

    /**
     * Turns a config key into a display name, so settings without a translation still read well:
     * {@code alwaysSpawnTogether} becomes "Always Spawn Together".
     */
    public static String readableName(String key) {
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (c == '_' || c == '-' || c == '.') {
                name.append(' ');
            } else if (i == 0) {
                name.append(Character.toUpperCase(c));
            } else if (Character.isUpperCase(c) && !Character.isUpperCase(key.charAt(i - 1))) {
                name.append(' ').append(c);
            } else if (name.charAt(name.length() - 1) == ' ') {
                name.append(Character.toUpperCase(c));
            } else {
                name.append(c);
            }
        }
        return name.toString();
    }

    /** The config group the setting saves into, or null when the mod saves it itself. */
    ConfigGroup group() {
        return this.group;
    }

    Class<T> type() {
        return this.type;
    }

    Text name() {
        return this.name;
    }

    Text description() {
        return this.description;
    }

    ItemStack icon() {
        return this.icon;
    }

    /** How a value shows on the card and in the tooltip. */
    Text valueName(T value) {
        return this.valueName.apply(value);
    }

    int step() {
        return this.step;
    }

    boolean isLocked() {
        return this.lockReason() != null;
    }

    /** Why the setting is locked right now, or null if it isn't. */
    Text lockReason() {
        for (Lock lock : this.locks) {
            if (lock.condition().getAsBoolean()) {
                return lock.reason();
            }
        }
        return null;
    }

    T defaultValue() {
        return this.defaultValue;
    }

    T min() {
        return this.min;
    }

    T max() {
        return this.max;
    }

    T get() {
        return this.getter.get();
    }

    void set(T value) {
        this.setter.accept(value);
    }

    void reset() {
        this.set(this.defaultValue);
    }
}
