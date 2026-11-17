package org.btwr.shared_library.api.config;

import java.awt.*;
import java.util.function.BiConsumer;

public class ConfigBuilder<T> {
    private String key;
    private T defaultValue;
    private T minValue;
    private T maxValue;
    private String comment;
    private BiConsumer<ConfigSetting<T>, T> onChange;
    private final Class<T> type;

    private ConfigBuilder(Class<T> type) {
        this.type = type;
    }

    public static ConfigBuilder<Integer> intSetting(String key) {
        return new ConfigBuilder<>(Integer.class).key(key);
    }

    public static ConfigBuilder<Double> doubleSetting(String key) {
        return new ConfigBuilder<>(Double.class).key(key);
    }

    public static ConfigBuilder<Float> floatSetting(String key) {
        return new ConfigBuilder<>(Float.class).key(key);
    }

    public static ConfigBuilder<Boolean> booleanSetting(String key) {
        return new ConfigBuilder<>(Boolean.class).key(key);
    }

    public static <E extends Enum<E>> ConfigBuilder<E> enumSetting(String key, Class<E> enumClass) {
        return new ConfigBuilder<>(enumClass).key(key);
    }

    public static ConfigBuilder<String> stringSetting(String key) {
        return new ConfigBuilder<>(String.class).key(key);
    }

    public static ConfigBuilder<Color> colorSetting(String key) {
        return new ConfigBuilder<>(Color.class).key(key);
    }

    public ConfigBuilder<T> key(String key) {
        this.key = key;
        return this;
    }

    public ConfigBuilder<T> defaultValue(T val) {
        this.defaultValue = val;
        return this;
    }

    public ConfigBuilder<T> min(T val) {
        this.minValue = val;
        return this;
    }

    public ConfigBuilder<T> max(T val) {
        this.maxValue = val;
        return this;
    }

    public ConfigBuilder<T> comment(String comment) {
        this.comment = comment;
        return this;
    }

    public ConfigBuilder<T> onChange(BiConsumer<ConfigSetting<T>, T> onChange) {
        this.onChange = onChange;
        return this;
    }

    public ConfigSetting<T> build() {
        return new ConfigSetting<>(key, defaultValue, minValue, maxValue, comment, type, onChange);
    }
}