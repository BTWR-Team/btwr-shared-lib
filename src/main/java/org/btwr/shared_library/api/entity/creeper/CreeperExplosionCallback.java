package org.btwr.shared_library.api.entity.creeper;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.mob.CreeperEntity;

public interface CreeperExplosionCallback {
    Event<CreeperExplosionCallback> EVENT = EventFactory.createArrayBacked(
            CreeperExplosionCallback.class,
            callbacks -> creeper -> {
                for (CreeperExplosionCallback callback : callbacks) {
                    if (!callback.canExplode(creeper)) {
                        return false;
                    }
                }

                return true;
            }
    );

    /**
     * Called when a Creeper is about to be allowed to explode.
     *
     * @return true to allow the explosion
     */
    boolean canExplode(CreeperEntity creeper);
}