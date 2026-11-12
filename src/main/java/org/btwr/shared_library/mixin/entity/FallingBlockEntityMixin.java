package org.btwr.shared_library.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.world.World;
import org.btwr.shared_library.api.entity.interfaces.IFallingImpactData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin extends Entity implements IFallingImpactData {

    @Shadow public int timeFalling;

    @Unique
    private float btwr$impactFallDistance;

    // Required constructor for Entity subclasses in mixins
    private FallingBlockEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public float btwr$getImpactFallDistance() {
        return this.btwr$impactFallDistance;
    }

    @Override
    public void btwr$setImpactFallDistance(float distance) {
        this.btwr$impactFallDistance = distance;
    }

    /**
     * Track a "pseudo fall distance" while the block is falling.
     * We hook into the tick method to accumulate how far it has fallen.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void btwr$captureFallDistance(CallbackInfo ci) {
        // Only while in air (timeFalling > 0, but not yet landed/removed)
        if (this.timeFalling > 0 && !this.isOnGround()) {
            // Rough approximation: use timeFalling as proxy for fall distance
            // to emulate BTW's (fallDistance - 5.0F) logic.
            // Each tick of falling roughly equals 1 block of fall height.
            this.btwr$impactFallDistance = Math.max(
                    this.btwr$impactFallDistance,
                    this.timeFalling
            );
        }
    }
}