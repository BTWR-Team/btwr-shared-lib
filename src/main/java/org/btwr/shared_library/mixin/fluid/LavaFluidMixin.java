package org.btwr.shared_library.mixin.fluid;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LavaFluid.class)
public abstract class LavaFluidMixin {

    @Shadow
    private boolean canLightFire(WorldView world, BlockPos pos) {
        throw new AssertionError();
    }

    @Shadow
    private boolean hasBurnableBlock(WorldView world, BlockPos pos) {
        throw new AssertionError();
    }

    // Lava sets blocks on fire directly, like unlit ovens. Replaces vanilla's fire starting so it doesn't roll twice
    // This doesn't check for still lava because that's only source blocks here, while settled lava streams count too in CE
    @Inject(method = "onRandomTick", at = @At("HEAD"), cancellable = true)
    private void btwr$startFiresAndSetBlocksOnFireDirectly(World world, BlockPos pos, FluidState state, Random random, CallbackInfo ci) {
        if (!world.getGameRules().getBoolean(GameRules.DO_FIRE_TICK)) return;

        btwr$setNeighborsInContactOnFire(world, pos);
        btwr$startFiresAround(world, pos, random);

        ci.cancel();
    }

    @Unique
    private void btwr$setNeighborsInContactOnFire(World world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (direction != Direction.UP) {
                btwr$trySettingOnFireDirectly(world, pos.offset(direction));
            }
        }
    }

    // Vanilla's fire starting, with the added ability to set blocks on fire directly
    @Unique
    private void btwr$startFiresAround(World world, BlockPos pos, Random random) {
        int attempts = random.nextInt(3);

        if (attempts > 0) {
            BlockPos targetPos = pos;

            for (int i = 0; i < attempts; i++) {
                targetPos = targetPos.add(random.nextInt(3) - 1, 1, random.nextInt(3) - 1);

                if (!world.canSetBlock(targetPos)) return;

                BlockState targetState = world.getBlockState(targetPos);

                if (targetState.isAir()) {
                    if (this.canLightFire(world, targetPos)) {
                        world.setBlockState(targetPos, AbstractFireBlock.getState(world, targetPos));
                        return;
                    }
                }
                else if (btwr$trySettingOnFireDirectly(world, targetPos) || targetState.blocksMovement()) {
                    return;
                }
            }
        }
        else {
            for (int i = 0; i < 3; i++) {
                BlockPos targetPos = pos.add(random.nextInt(3) - 1, 0, random.nextInt(3) - 1);

                if (!world.canSetBlock(targetPos)) return;

                if (world.isAir(targetPos.up()) && this.hasBurnableBlock(world, targetPos)) {
                    world.setBlockState(targetPos.up(), AbstractFireBlock.getState(world, targetPos));
                }
                else {
                    btwr$trySettingOnFireDirectly(world, targetPos);
                }
            }
        }
    }

    /**
     * @return If the block could be set on fire directly
     */
    @Unique
    private boolean btwr$trySettingOnFireDirectly(World world, BlockPos pos) {
        if (!world.canSetBlock(pos)) return false;

        Block block = world.getBlockState(pos).getBlock();

        if (block.btwr$getCanBeSetOnFireDirectly(world, pos)) {
            block.btwr$setOnFireDirectly(world, pos);
            return true;
        }

        return false;
    }

}
