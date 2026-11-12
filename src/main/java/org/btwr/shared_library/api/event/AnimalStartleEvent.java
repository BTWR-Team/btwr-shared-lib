package org.btwr.shared_library.api.event;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import org.jetbrains.annotations.Nullable;

public class AnimalStartleEvent {
    private final AnimalEntity animal;
    @Nullable private LivingEntity initiator;
    private boolean shouldJump;

    public AnimalStartleEvent(AnimalEntity animal) {
        this.animal = animal;
    }

    public AnimalEntity getAnimal() {
        return animal;
    }

    @Nullable
    public LivingEntity getInitiator() {
        return initiator;
    }

    /** First handler to report an initiator wins **/
    public void startle(LivingEntity startledBy) {
        if (initiator == null) {
            initiator = startledBy;
        }
    }

    public boolean shouldJump() {
        return shouldJump;
    }

    public void requestJump() {
        shouldJump = true;
    }
}
