package org.btwr.shared_library.api.entity.interfaces;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/** Implements common behavior for entities that can be startled.
 * <p>Entities like that are different from ones using the Angerable interface
 * as it's usually implemented onto passive entities.
 */
public interface Startleable {
    int btwr$getUncomfortableTick();
    void btwr$setUncomfortableTick(int value);

    int btwr$getStartleTime();
    void btwr$setStartleTime(int ticks);

    @Nullable UUID btwr$getStartledBy();
    void btwr$setStartledBy(@Nullable UUID uuid);

    void btwr$onNearbyAnimalAttacked(AnimalEntity attackedAnimal, LivingEntity attackSource);
    void btwr$onNearbyPlayerStartles(PlayerEntity player);
    void btwr$onNearbyPlayerBlockAddOrRemove(PlayerEntity player);
    void btwr$onNearbyFireStartAttempt(PlayerEntity player);

    default void btwr$startle(@Nullable UUID source, int ticks) {
        if (btwr$getStartledBy() == null) btwr$setStartledBy(source);
        if (btwr$getStartleTime() < ticks) btwr$setStartleTime(ticks);
    }

    /** Whether the base mixin should count the timer down. Mobs with their own anger timer return false. */
    default boolean btwr$ticksStartleTimer() {
        return true;
    }

    default boolean btwr$isPanicked() {
        // fire is checked by the mixin
        return btwr$getStartledBy() != null;
    }
}