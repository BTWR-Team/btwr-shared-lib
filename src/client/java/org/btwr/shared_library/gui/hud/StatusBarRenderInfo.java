package org.btwr.shared_library.gui.hud;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import org.btwr.shared_library.config.BTWRSLModConfig;

/**
 * Contains information on which vanilla status bar is currently being rendered
 */
public class StatusBarRenderInfo {
    static boolean renderingFood, renderingMountHealth, renderingArmor, renderingAir = false;

    private static final StatusBarRenderInfo INSTANCE = new StatusBarRenderInfo();

    private StatusBarRenderInfo() {}

    public static StatusBarRenderInfo getInstance() {
        return INSTANCE;
    }

    public boolean getRenderingFood() {
        return renderingFood;
    }
    public boolean getRenderingMountHealth(PlayerEntity player) {
        return renderingMountHealth && player.hasVehicle();
    }
    public boolean getRenderingArmor(PlayerEntity player) {
        return renderingArmor && player.getArmor() > 0;
    }
    public boolean getRenderingAir(PlayerEntity player) {
        return renderingAir && player.getAir() != player.getMaxAir();
    }

    public void setRenderingFood(boolean value) {
        renderingFood = value;
        if (
            // Explicit compat checks here
                FabricLoader.getInstance().isModLoaded("granular-hunger") ||
                        BTWRSLModConfig.doHungerOffsetOverride.get()
        )
            renderingFood = true;
    }
    public void setRenderingMountHealth(boolean value) {
        renderingMountHealth = value;
    }
    public void setRenderingArmor(boolean value) {
        renderingArmor = value;
    }
    public void setRenderingAir(boolean value) {
        renderingAir = value;
    }
}