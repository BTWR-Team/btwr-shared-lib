package org.btwr.shared_library.mixin.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Angerable;
import org.btwr.shared_library.api.entity.interfaces.Startleable;
import org.btwr.shared_library.api.event.BTWREvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "tick", at = @At("RETURN"))
    private void onEntityTick(CallbackInfo ci) {
        BTWREvents.LIVING_TICK.createInvoker().accept((LivingEntity)(Object)this);
    }

    /** For startle behavior
     * <p> Also check out {@link org.btwr.shared_library.mixin.entity.AnimalStartleMixin}
     */
    @Inject(method = "setAttacker", at = @At("RETURN"))
    private void btwr$setStartled(LivingEntity attacker, CallbackInfo ci) {
        if (attacker != null && (Object) this instanceof Startleable s && !((Object) this instanceof Angerable)) {
            s.btwr$startle(attacker.getUuid(), 300);
        }
    }

}