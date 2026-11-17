package org.btwr.shared_library.config;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.btwr.shared_library.gui.config.ConfigUiScreen;
import org.btwr.shared_library.gui.config.ConfigUiSpec;
import org.btwr.shared_library.gui.config.UiSetting;

import java.util.List;

public class SettingsGUI {

    private static final String PREFIX = "config.btwrsl.draw_mode.anchor.";

    public static Screen createConfigScreen(Screen parent) {
        List<UiSetting<?>> penalties = List.of(
                UiSetting.of(BTWRSLModConfig.doHungerOffsetOverride,
                        Text.translatable("config.btwrsl.hunger_override"),
                        Text.translatable("config.btwrsl.tooltip.hunger_override")
                ).icon(new ItemStack(Items.COOKED_BEEF)),
                UiSetting.of(BTWRSLModConfig.renderXOffset,
                        Text.translatable("config.btwrsl.render_x"),
                        Text.translatable("config.btwrsl.tooltip.render_x")
                ).icon(new ItemStack(Items.RAIL)),
                UiSetting.of(BTWRSLModConfig.renderYOffset,
                        Text.translatable("config.btwrsl.render_y"),
                        Text.translatable("config.btwrsl.tooltip.render_y")
                ).icon(new ItemStack(Items.LADDER)),
                UiSetting.of(BTWRSLModConfig.drawMode,
                        Text.translatable("config.btwrsl.draw_mode"),
                        Text.translatable("config.btwrsl.tooltip.draw_mode")
                ).valueName(mode -> Text.translatable(PREFIX + mode.name()))
                        .icon(new ItemStack(Items.PAINTING)),
                UiSetting.of(BTWRSLModConfig.renderMargin,
                        Text.translatable("config.btwrsl.draw_margin"),
                        Text.translatable("config.btwrsl.tooltip.draw_margin")
                ).icon(new ItemStack(Items.ITEM_FRAME))
                        .locked(() -> !BTWRSLModConfig.drawMode.get().isAnchor(), Text.translatable("config.btwrsl.locked.draw_margin"))
        );

        ConfigUiSpec spec = new ConfigUiSpec(
                Text.translatable("title.btwrsl.config"),
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