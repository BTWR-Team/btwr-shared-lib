package org.btwr.shared_library.mixin.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.btwr.shared_library.api.entity.interfaces.Startleable;
import org.btwr.shared_library.api.event.AnimalStartleEvent;
import org.btwr.shared_library.api.event.BTWREvents;
import org.btwr.shared_library.api.util.SuffocationUtil;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(AnimalEntity.class)
public abstract class AnimalStartleMixin extends PassiveEntity implements Startleable {

    @Shadow public abstract boolean isInLove();

    @Unique private UUID btwr$startledBy;
    @Unique private int btwr$startleTime;
    @Unique private int btwr$uncomfortableTick;

    protected AnimalStartleMixin(EntityType<? extends PassiveEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override public int btwr$getUncomfortableTick() { return btwr$uncomfortableTick; }
    @Override public void btwr$setUncomfortableTick(int value) { btwr$uncomfortableTick = value; }
    @Override public int btwr$getStartleTime() {
        return btwr$startleTime;
    }
    @Override public void btwr$setStartleTime(int ticks) {
        btwr$startleTime = ticks;
    }
    @Override public @Nullable UUID btwr$getStartledBy() {
        return btwr$startleTime > 0 ? btwr$startledBy : null;
    }
    @Override public void btwr$setStartledBy(@Nullable UUID uuid) {
        btwr$startledBy = uuid;
    }
    @Override public boolean btwr$ticksStartleTimer() {
        return !((Object) this instanceof Angerable);
    }
    @Override public boolean btwr$isPanicked() {
        return this.isOnFire() || this.btwr$getStartledBy() != null;
    }
    @Override public void btwr$onNearbyFireStartAttempt(PlayerEntity player) {
        btwr$onNearbyPlayerStartles(player);
    }
    @Override public void btwr$onNearbyPlayerBlockAddOrRemove(PlayerEntity player) {
        btwr$onNearbyPlayerStartles(player);
    }
    @Override
    public void btwr$onNearbyAnimalAttacked(AnimalEntity attacked, LivingEntity source) {
        btwr$startle(source.getUuid(), 150);
        this.detachLeash();
    }
    @Override
    public void btwr$onNearbyPlayerStartles(PlayerEntity player) {
        btwr$startle(player.getUuid(), 150);
        this.detachLeash(true, true);
    }

    @Inject(method = "tickMovement", at = @At("TAIL"))
    private void btwr$tickAlarm(CallbackInfo ci) {
        if (this.getWorld().isClient() || !this.isAlive()) return;

        if (btwr$ticksStartleTimer() && btwr$startleTime > 0 && --btwr$startleTime == 0) {
            btwr$startledBy = null;
        }

        AnimalStartleEvent startle = new AnimalStartleEvent((AnimalEntity) (Object) this);
        BTWREvents.ANIMAL_STARTLE_TICK.createInvoker().accept(startle);

        if (startle.shouldJump()) this.jump();

        if (startle.getInitiator() instanceof PlayerEntity player) {
            this.btwr$onNearbyPlayerStartles(player);
        }
    }

    @Inject(method = "applyDamage", at = @At("HEAD"))
    private void btwr$panicNearbyAnimals(DamageSource source, float amount, CallbackInfo ci) {
        if (this.getWorld().isClient) return;
        if (!(source.getAttacker() instanceof LivingEntity attacker)) return;

        this.detachLeash();

        AnimalEntity self = (AnimalEntity) (Object) this;
        Box box = this.getBoundingBox().expand(16.0, 8.0, 16.0);

        for (AnimalEntity a : this.getWorld().getEntitiesByClass(AnimalEntity.class, box,
                a -> a != self && a != attacker && a.isAlive()
        )) {
            a.btwr$onNearbyAnimalAttacked(self, attacker);
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putInt("UncomfortableTick", this.btwr$uncomfortableTick);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readFromNbt(NbtCompound nbt, CallbackInfo ci) {
        this.btwr$uncomfortableTick = nbt.getInt("UncomfortableTick");
    }

}