package org.btwr.shared_library.api.config;

import java.util.function.BiConsumer;

public class ConfigSetting<T> {
    private final String key;
    private final String comment;
    private final T defaultValue;
    private final T minValue;
    private final T maxValue;
    private T value;
    private final Class<T> type;
    private final BiConsumer<ConfigSetting<T>, T> onChange;

    private ConfigGroup parentGroup;

    ConfigSetting(String key, T defaultValue, T minValue, T maxValue, String comment,
                  Class<T> type, BiConsumer<ConfigSetting<T>, T> onChange) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.comment = comment;
        this.type = type;
        this.onChange = onChange;
    }

    public T get() {
        return value;
    }

    /** Sets the value and saves its group's file right away */
    public void set(T newValue) {
        this.set(newValue, true);
    }

    /**
     * Sets the value, saving its group's file only if {@code save} is true. Config screens use false and
     * save the whole group once when they close.
     */
    public void set(T newValue, boolean save) {
        if (newValue == null) return;
        if (minValue != null && compare(newValue, minValue) < 0) newValue = minValue;
        if (maxValue != null && compare(newValue, maxValue) > 0) newValue = maxValue;
        this.value = newValue;
        if (onChange != null) onChange.accept(this, newValue);
        if (save && parentGroup != null) parentGroup.save();
    }

    public String getKey() { return key; }
    public String getComment() { return comment; }
    public T getDefaultValue() { return defaultValue; }
    public Class<T> getType() { return type; }
    public T getMin() { return minValue; }
    public T getMax() { return maxValue; }
    /** The group this setting was added to, or null */
    public ConfigGroup getGroup() { return parentGroup; }

    void setParentGroup(ConfigGroup group) { this.parentGroup = group; }

    @SuppressWarnings("unchecked")
    private int compare(T a, T b) {
        if (a instanceof Comparable c) return c.compareTo(b);
        return 0;
    }
}