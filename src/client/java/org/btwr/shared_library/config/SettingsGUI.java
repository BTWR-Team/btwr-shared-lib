package org.btwr.shared_library.config;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.btwr.shared_library.BTWRSLModClient;
import org.btwr.shared_library.gui.config.ConfigUiScreen;
import org.btwr.shared_library.gui.config.ConfigUiSpec;
import org.btwr.shared_library.gui.config.UiSetting;
import org.btwr.shared_library.gui.hud.PenaltyDrawMode;

import java.util.List;

public class SettingsGUI {

    private static final String PREFIX = "config.btwrsl.draw_mode.anchor.";

    public static Screen createConfigScreen(Screen parent) {
        BTWRSLSettings settings = BTWRSLModClient.getSettings();

        List<UiSetting<?>> penalties = List.of(
                UiSetting.toggle(
                        Text.translatable("config.btwrsl.hunger_override"),
                        Text.translatable("config.btwrsl.tooltip.hunger_override"),
                        true,
                        () -> settings.doHungerOffsetOverride,
                        value -> settings.doHungerOffsetOverride = value
                ).icon(new ItemStack(Items.COOKED_BEEF)),
                new UiSetting<>(Integer.class,
                        Text.translatable("config.btwrsl.render_x"),
                        Text.translatable("config.btwrsl.tooltip.render_x"),
                        0, -100, 100,
                        () -> settings.renderXOffset,
                        value -> settings.renderXOffset = value
                ).icon(new ItemStack(Items.RAIL)),
                new UiSetting<>(Integer.class,
                        Text.translatable("config.btwrsl.render_y"),
                        Text.translatable("config.btwrsl.tooltip.render_y"),
                        0, -100, 100,
                        () -> settings.renderYOffset,
                        value -> settings.renderYOffset = value
                ).icon(new ItemStack(Items.LADDER)),
                UiSetting.choice(PenaltyDrawMode.class,
                        Text.translatable("config.btwrsl.draw_mode"),
                        Text.translatable("config.btwrsl.tooltip.draw_mode"),
                        PenaltyDrawMode.BTW,
                        mode -> Text.translatable(PREFIX + mode.name()),
                        () -> settings.drawMode,
                        value -> settings.drawMode = value
                ).icon(new ItemStack(Items.PAINTING)),
                new UiSetting<>(Integer.class,
                        Text.translatable("config.btwrsl.draw_margin"),
                        Text.translatable("config.btwrsl.tooltip.draw_margin"),
                        4, 0, 32,
                        () -> settings.renderMargin,
                        value -> settings.renderMargin = value
                ).icon(new ItemStack(Items.ITEM_FRAME))
                        .locked(() -> !settings.drawMode.isAnchor(), Text.translatable("config.btwrsl.locked.draw_margin"))
        );

        ConfigUiSpec spec = new ConfigUiSpec(
                Text.translatable("title.btwrsl.config"),
                () -> BTWRSLModClient.getInstance().saveSettings(),
                List.of(new ConfigUiSpec.Category(
                        Text.translatable("title.btwrsl.category.penalties"),
                        new ItemStack(Items.ROTTEN_FLESH),
                        List.of(ConfigUiSpec.Section.settings(
                                Text.translatable("title.btwrsl.category.penalties"),
                                Text.translatable("config.btwrsl.section.penalties"),
                                penalties
                        ))
                ))
        );

        return new ConfigUiScreen(parent, spec);
    }

}
