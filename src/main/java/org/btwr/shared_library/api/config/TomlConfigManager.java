package org.btwr.shared_library.api.config;

import net.fabricmc.loader.api.FabricLoader;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class TomlConfigManager {
    private static final List<ConfigGroup> GROUPS = new CopyOnWriteArrayList<>();

    private TomlConfigManager() {}

    public static void registerGroup(ConfigGroup group) {
        if (!GROUPS.contains(group)) GROUPS.add(group);
        group.init(); // ensures file is loaded/created
    }

    public static File getConfigDir() {
        File dir = FabricLoader.getInstance().getConfigDir().toFile();
        if (!dir.exists() && !dir.mkdirs()) {
            System.err.println("[ConfigLib] Failed to create config directory: " + dir.getAbsolutePath());
        }
        return dir;
    }

    public static void update() {
        for (ConfigGroup group : GROUPS) group.save();
    }

    /** Writes TOML file with comments and default values */
    public static void writeConfigFile(ConfigGroup group) throws IOException {
        try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(group.getFile()), StandardCharsets.UTF_8))) {
            for (ConfigSetting<?> setting : group.getAll().values()) {
                String comment = setting.getComment();
                if (comment != null && !comment.isEmpty()) {
                    for (String line : comment.split("\n")) {
                        w.write("# " + line);
                        w.newLine();
                    }
                }

                w.write("# Default: " + setting.getDefaultValue());
                if (setting.getMin() != null || setting.getMax() != null) {
                    w.write(" | Min: " + setting.getMin() + " Max: " + setting.getMax());
                }
                w.newLine();
                w.write(setting.getKey() + " = " + serializeValue(setting));
                w.newLine();
                w.newLine();
            }
        }
    }

    private static String serializeValue(ConfigSetting<?> setting) {
        Object v = setting.get();
        if (v instanceof Enum<?> e) return "\"" + e.name() + "\"";
        if (v instanceof Color c) return "\"" + toHex(c) + "\"";
        if (v instanceof String || v instanceof Character) return "\"" + v.toString().replace("\"", "\\\"") + "\"";
        if (v instanceof Boolean) return v.toString();

        return v.toString();
    }

    public static void readConfigFile(ConfigGroup group) throws IOException {
        File file = group.getFile();
        if (!file.exists()) return;

        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                int eq = line.indexOf('=');
                if (eq <= 0) continue;

                String key = line.substring(0, eq).trim();
                String val = line.substring(eq + 1).trim();

                ConfigSetting<?> s = group.get(key);
                if (s != null) applyValue(s, val);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void applyValue(ConfigSetting<T> setting, String raw) {
        Class<T> type = setting.getType();
        try {
            if (type.isEnum()) {
                setting.set(parseEnum(type, raw));
                return;
            }
            Object v = switch (type.getSimpleName()) {
                case "Integer" -> Integer.parseInt(raw);
                case "Double" -> Double.parseDouble(raw);
                case "Float" -> Float.parseFloat(raw);
                case "Boolean" -> Boolean.parseBoolean(raw);
                case "String" -> {
                    if (raw.startsWith("\"") && raw.endsWith("\"")) raw = raw.substring(1, raw.length() - 1);
                    yield raw.replace("\\\"", "\"");
                }
                case "Color" -> {
                    Color color = parseColor(raw);
                    if (color == null) throw new IllegalArgumentException("Not a hex color");
                    yield color;
                }
                default -> raw;
            };
            setting.set((T) v);
        } catch (Exception e) {
            System.err.println("[ConfigLib] Failed to parse value for " + setting.getKey() + ": " + raw);
        }
    }

    /** A color as "#RRGGBB", or "#AARRGGBB" when it isn't fully opaque */
    public static String toHex(Color color) {
        if (color.getAlpha() == 255) return String.format(Locale.ROOT, "#%06X", color.getRGB() & 0xFFFFFF);
        return String.format(Locale.ROOT, "#%08X", color.getRGB());
    }

    /** Reads "#RRGGBB" or "#AARRGGBB", with or without quotes and the "#". Returns null if it isn't one */
    public static Color parseColor(String raw) {
        String hex = raw.trim();
        if (hex.startsWith("\"") && hex.endsWith("\"") && hex.length() >= 2) hex = hex.substring(1, hex.length() - 1).trim();
        if (hex.startsWith("#")) hex = hex.substring(1);
        else if (hex.startsWith("0x") || hex.startsWith("0X")) hex = hex.substring(2);
        try {
            if (hex.length() == 6) return new Color(Integer.parseUnsignedInt(hex, 16));
            if (hex.length() == 8) return new Color((int) Long.parseLong(hex, 16), true);
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    /** Finds the enum constant by name, ignoring quotes and case */
    private static <T> T parseEnum(Class<T> type, String raw) {
        if (raw.startsWith("\"") && raw.endsWith("\"") && raw.length() >= 2) raw = raw.substring(1, raw.length() - 1);
        for (T constant : type.getEnumConstants()) {
            if (((Enum<?>) constant).name().equalsIgnoreCase(raw.trim())) return constant;
        }
        throw new IllegalArgumentException("No constant " + raw + " in " + type.getSimpleName());
    }

    public static List<ConfigGroup> getAllGroups() {
        return Collections.unmodifiableList(GROUPS);
    }
}