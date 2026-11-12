package org.btwr.shared_library.mixin.entity;

import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.btwr.shared_library.api.event.BTWREvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnimalEntity.class)
public abstract class AnimalEntityEatMixin {
    // Vanilla hand feeding, before the stack gets decremented
    @Inject(method = "eat", at = @At("HEAD"))
    private void btwr$onEat(PlayerEntity player, Hand hand, ItemStack stack, CallbackInfo ci) {
        AnimalEntity self = (AnimalEntity) (Object) this;

        if (!self.getWorld().isClient) {
            BTWREvents.ANIMAL_EAT_ITEM.createInvoker().accept(self, stack);
        }
    }
}