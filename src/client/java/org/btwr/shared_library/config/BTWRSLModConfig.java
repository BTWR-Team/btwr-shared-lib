package org.btwr.shared_library.config;

import org.btwr.shared_library.BTWRSLMod;
import org.btwr.shared_library.api.config.ConfigBuilder;
import org.btwr.shared_library.api.config.ConfigGroup;
import org.btwr.shared_library.api.config.ConfigSetting;
import org.btwr.shared_library.api.config.TomlConfigManager;
import org.btwr.shared_library.gui.hud.PenaltyDrawMode;

/** BTWR: Shared Library's client settings, saved in config/btwr_sl/btwr_sl_common.toml */
public class BTWRSLModConfig {
    private static final String MOD_ID = BTWRSLMod.MOD_ID;

    public static final ConfigGroup CONFIG;

    /** Call this method in the client initializer so the class can initialize **/
    public static void register() {}

    public static final ConfigSetting<Boolean> doHungerOffsetOverride =
            ConfigBuilder.booleanSetting("doHungerOffsetOverride")
                    .defaultValue(true)
                    .comment("Place penalties as if the hunger bar were shown, even when it's hidden")
                    .build();

    public static final ConfigSetting<Integer> renderXOffset =
            ConfigBuilder.intSetting("renderXOffset")
                    .defaultValue(0).min(-100).max(100)
                    .comment("Moves the penalty text left or right, in pixels")
                    .build();

    public static final ConfigSetting<Integer> renderYOffset =
            ConfigBuilder.intSetting("renderYOffset")
                    .defaultValue(0).min(-100).max(100)
                    .comment("Moves the penalty text up or down, in pixels")
                    .build();

    public static final ConfigSetting<PenaltyDrawMode> drawMode =
            ConfigBuilder.enumSetting("drawMode", PenaltyDrawMode.class)
                    .defaultValue(PenaltyDrawMode.BTW)
                    .comment("Where penalties are drawn. \"BTW\" copies Better Than Wolves")
                    .build();

    public static final ConfigSetting<Integer> renderMargin =
            ConfigBuilder.intSetting("renderMargin")
                    .defaultValue(4).min(0).max(32)
                    .comment("Space between the penalty text and the screen edge, when it's anchored to a corner")
                    .build();

    static {
        CONFIG = new ConfigGroup(String.format("%s/%s_common.toml", MOD_ID, MOD_ID));

        CONFIG.add(doHungerOffsetOverride);
        CONFIG.add(renderXOffset);
        CONFIG.add(renderYOffset);
        CONFIG.add(drawMode);
        CONFIG.add(renderMargin);

        TomlConfigManager.registerGroup(CONFIG); // auto init/load/save
    }
}