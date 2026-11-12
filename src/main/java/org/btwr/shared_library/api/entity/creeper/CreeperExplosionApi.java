package org.btwr.shared_library.api.entity.creeper;

import net.minecraft.entity.mob.CreeperEntity;

public class CreeperExplosionApi {

    private CreeperExplosionApi() {}

    public static boolean canExplode(CreeperEntity creeper) {
        return CreeperExplosionCallback.EVENT.invoker().canExplode(creeper);
    }
}