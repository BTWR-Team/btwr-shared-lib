package org.btwr.shared_library.mixin.added;

import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.btwr.shared_library.api.block.util.FireBlockUtils;
import org.btwr.shared_library.api.block.util.Flammability;
import org.btwr.shared_library.api.block.interfaces.added.BlockAdded;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.btwr.shared_library.api.crafting.FurnaceBurnTime;
import org.btwr.shared_library.api.registry.GrazeBehaviorRegistry;
import org.btwr.shared_library.mixin.accessors.FireBlockAccessor;
import org.btwr.shared_library.util.utils.WorldUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Block.class)
public abstract class BlockAddedMixin implements BlockAdded {

    //------------ Hard Point related functionality ----------//


    @Override
    public boolean btwr$hasSmallCenterHardPointToFacing(WorldAccess world, BlockPos pos, Direction facing, boolean ignoreTransparency) {
        return btwr$hasCenterHardPointToFacing(world, pos, facing, ignoreTransparency);
    }

    @Override
    public boolean btwr$hasSmallCenterHardPointToFacing(WorldAccess world, BlockPos pos, Direction facing) {
        return btwr$hasCenterHardPointToFacing(world, pos, facing, false);
    }

    @Override
    public boolean btwr$hasCenterHardPointToFacing(WorldAccess world, BlockPos pos, Direction facing, boolean ignoreTransparency) {
        return btwr$hasLargeCenterHardPointToFacing(world, pos, facing, ignoreTransparency);
    }

    @Override
    public boolean btwr$hasCenterHardPointToFacing(WorldAccess world, BlockPos pos, Direction facing) {
        return btwr$hasCenterHardPointToFacing(world, pos, facing, false);
    }

    @Override
    public boolean btwr$hasLargeCenterHardPointToFacing(WorldAccess world, BlockPos pos, Direction facing, boolean ignoreTransparency) {
        return world.getBlockState(pos).isFullCube(world, pos);
    }

    @Override
    public boolean btwr$hasLargeCenterHardPointToFacing(WorldAccess world, BlockPos pos, Direction facing) {
        return btwr$hasLargeCenterHardPointToFacing(world, pos, facing, false);
    }

    @Override
    public boolean btwr$isBlockRestingOnThatBelow(WorldAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean btwr$isBlockAttachedToFacing(WorldAccess world, BlockPos pos, Direction facing) {
        return false;
    }

    @Override
    public void btwr$attachToFacing(World world, BlockPos pos, Direction facing) {}

    @Override
    public boolean btwr$hasContactPointToFullFace(WorldAccess world, BlockPos pos, Direction facing) {
        return world.getBlockState(pos).isFullCube(world, pos);
    }

    @Override
    public boolean btwr$hasContactPointToSlabSideFace(WorldAccess world, BlockPos pos, Direction facing, boolean isSlabUpsideDown) {
        return btwr$hasContactPointToFullFace(world, pos, facing);
    }

    @Override
    public boolean btwr$hasContactPointToStairShapedFace(WorldAccess world, BlockPos pos, Direction facing) {
        return btwr$hasContactPointToFullFace(world, pos, facing);
    }

    @Override
    public boolean btwr$hasContactPointToStairNarrowVerticalFace(WorldAccess world, BlockPos pos, Direction facing, int stairFacing) {
        return btwr$hasContactPointToFullFace(world, pos, facing);
    }

    @Override
    public boolean btwr$onMortarApplied(World world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean btwr$hasMortar(WorldAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean btwr$hasNeighborWithMortarInContact(World world, BlockPos pos) {
        for (Direction facing : Direction.values()) {
            if (WorldUtils.hasNeighborWithMortarInFullFaceContactToFacing(world, pos, facing)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean btwr$isStickyToSnow(WorldAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean btwr$hasStickySnowNeighborInContact(World world, BlockPos pos) {
        for (Direction facing : Direction.values()) {
            if (WorldUtils.hasStickySnowNeighborInFullFaceContactToFacing(world, pos, facing)) {
                return true;
            }
        }

        return false;
    }

    //--------------- Fire related functionality -------------//

    @Unique
    private int defaultFurnaceBurnTime = 0;

    @Override
    public int btwr$getFurnaceBurnTime(int itemDamage)
    {
        return defaultFurnaceBurnTime;
    }

    @Override
    public void btwr$setFurnaceBurnTime(int burnTime)
    {
        defaultFurnaceBurnTime = burnTime;
    }

    @Override
    public void btwr$setFurnaceBurnTime(FurnaceBurnTime burnTime)
    {
        btwr$setFurnaceBurnTime(burnTime.burnTime);
    }

    @Override
    public boolean btwr$doesInfiniteBurnToFacing(WorldAccess world, BlockPos pos, Direction facing) {
        return false;
    }

    @Override
    public boolean btwr$doesExtinguishFireAbove(World world, BlockPos pos)
    {
        return false;
    }

    @Override
    public boolean btwr$hasCustomFireDestructionBehavior() {
        return false;
    }

    @Override
    public void btwr$onDestroyedByFire(World world, BlockPos pos, int fireAge, boolean forcedFireSpread) {
        if (forcedFireSpread || (world.getRandom().nextInt(fireAge + 10) < 5 && !world.hasRain(pos))) {
            int newFireAge = fireAge + world.getRandom().nextInt(5) / 4;

            if (newFireAge > 15) {
                newFireAge = 15;
            }

            world.setBlockState(pos, Blocks.FIRE.getDefaultState().with(FireBlock.AGE, newFireAge));
        }
        else {
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
        }
    }

    @Override
    public Block btwr$setFireProperties(int chanceToEncourageFire, int abilityToCatchFire) {
        if (Blocks.FIRE instanceof FireBlock fireBlock) {
            FireBlockAccessor accessor = (FireBlockAccessor) fireBlock;

            Block self = (Block)(Object)this;

            // BTW: chanceToEncourageFire == spreadChance
            accessor.btwr$getSpreadChances().put(self, chanceToEncourageFire);

            // BTW: abilityToCatchFire == burnChance
            accessor.btwr$getBurnChances().put(self, abilityToCatchFire);
        }

        return (Block)(Object)this;
    }

    @Override
    public Block btwr$setFireProperties(Flammability flammability) {
        return btwr$setFireProperties(flammability.chanceToEncourageFire, flammability.abilityToCatchFire);
    }

    /**
     * Whether the block itself can be set on fire, rather than a neighboring block being set to a fire block
     */
    @Override
    public boolean btwr$getCanBeSetOnFireDirectly(WorldAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean btwr$getCanBeSetOnFireDirectlyByItem(WorldAccess world, BlockPos pos) {
        return btwr$getCanBeSetOnFireDirectly(world, pos);
    }

    @Override
    public boolean btwr$setOnFireDirectly(World world, BlockPos pos) {
        return false;
    }

    @Override
    public int btwr$getChanceOfFireSpreadingDirectlyTo(WorldAccess world, BlockPos pos) {
        return 0;
    }

    @Override
    public boolean btwr$getCanBlockLightItemOnFire(WorldAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean btwr$getDoesFireDamageToEntities(World world, BlockPos pos, Entity entity) {
        return btwr$getDoesFireDamageToEntities(world, pos);
    }

    @Override
    public boolean btwr$getDoesFireDamageToEntities(World world, BlockPos pos) {
        return false;
    }

    /**
     * Used by Hibachi to determine if it can remove the block above it when lit
     */
    @Override
    public boolean btwr$getCanBlockBeIncinerated(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        // Modern replacement for "!blockMaterial.blocksMovement()":
        boolean doesNotBlockMovement = state.getCollisionShape(world, pos).isEmpty();

        return FireBlockUtils.canBlockCatchFire(world, pos) || doesNotBlockMovement;
    }

    /**
     * Whether a fire block can be directly placed over this one, without first burning or catching fire, as if it were air.
     */
    @Override
    public boolean btwr$getCanBlockBeReplacedByFire(World world, BlockPos pos) {
        return world.getBlockState(pos).isAir();
    }

    @Override
    public boolean btwr$isIncineratedInCrucible() {
        return FireBlockUtils.canBlockBeDestroyedByFire((Block)(Object)this);
    }

    //----------- Plant related functionality ----------//

    @Override
    public void btwr$notifyOfFullStagePlantGrowthOn(World world, BlockPos pos, Block plantBlock) {}

    @Override
    public float btwr$getPlantGrowthOnMultiplier(World world, BlockPos pos, Block plantBlock) { return 1F; }

    @Override
    public boolean btwr$isBlockHydratedForPlantGrowthOn(World world, BlockPos pos) {return false;}

    @Override
    public boolean btwr$getIsFertilizedForPlantGrowth(World world, BlockPos pos) {
        return false;
    }

    @Override
    public int btwr$getWeedsGrowthLevel(WorldAccess blockAccess, BlockPos pos)
    {
        return 0;
    }

    @Override
    public void btwr$removeWeeds(World world, BlockPos pos) {}

    //------------- Grazing Functionality -------------//

    @Override
    public boolean btwr$canBeGrazedOn(WorldAccess worldAccess, BlockPos pos, AnimalEntity byAnimal) {
        GrazeBehaviorRegistry.GrazeBehavior behavior = GrazeBehaviorRegistry.get((Block) (Object) this);
        return behavior != null && behavior.canBeGrazedOn(worldAccess, pos, worldAccess.getBlockState(pos), byAnimal);
    }

    @Override
    public void btwr$onGrazed(World world, BlockPos pos, AnimalEntity animal) {
        GrazeBehaviorRegistry.GrazeBehavior behavior = GrazeBehaviorRegistry.get((Block) (Object) this);

        if (behavior != null) {
            behavior.onGrazed(world, pos, world.getBlockState(pos), animal);
        } else {
            GrazeBehaviorRegistry.GrazeBehavior.removeAndNotifyBelow(world, pos, animal);
        }
    }

    @Override
    public void btwr$onVegetationAboveGrazed(World world, BlockPos pos, AnimalEntity animal) {
        GrazeBehaviorRegistry.GrazeBehavior behavior = GrazeBehaviorRegistry.get((Block) (Object) this);
        if (behavior != null) {
            behavior.onVegetationAboveGrazed(world, pos, world.getBlockState(pos), animal);
        }
    }

    @Override
    public void btwr$notifyNeighborsBlockDisrupted(World world, BlockPos pos) {
        BlockPos tempPos = new BlockPos(pos);

        for (int facingId = 0; facingId <= 5; facingId++) {
            Direction facing = Direction.byId(facingId);

            tempPos.offset(facing);

            Block tempBlock = world.getBlockState(tempPos).getBlock();

            if (tempBlock != null) {
                tempBlock.btwr$onNeighborDisrupted(world, tempPos, facing.getOpposite());
            }
        }
    }

    @Override
    public void btwr$onNeighborDisrupted(World world, BlockPos pos, Direction facing) {}

    //------------- Ground cover related functionality ------------//

    @Override
    public boolean btwr$canGroundCoverRestOnBlock(World world, BlockPos pos) {
        return world.btwr$doesBlockHaveSolidTopSurface(pos);
    }

    @Override
    public float btwr$groundCoverRestingOnVisualOffset(BlockView world, BlockPos pos)
    {
        return 0F;
    }

    @Override
    public boolean btwr$isGroundCover()
    {
        return false;
    }

    @Override
    public boolean btwr$getCanGrassSpreadToBlock(World world, BlockPos pos)
    {
        return false;
    }

    @Override
    public boolean btwr$spreadGrassToBlock(World world, BlockPos pos)
    {
        return false;
    }

    @Override
    public boolean btwr$getCanGrassGrowUnderBlock(World world, BlockPos pos, boolean grassOnHalfSlab) {
        if (!grassOnHalfSlab) {
            return !btwr$hasLargeCenterHardPointToFacing(world, pos, Direction.DOWN);
        }

        return true;
    }

    @Override
    public boolean btwr$getCanMyceliumSpreadToBlock(World world, BlockPos pos)
    {
        return false;
    }

    @Override
    public boolean btwr$spreadMyceliumToBlock(World world, BlockPos pos)
    {
        return false;
    }

    @Override
    public boolean btwr$getCanBlightSpreadToBlock(World world, BlockPos pos, int blightLevel)
    {
        return false;
    }

    /**
     * Used by blocks like grass and mycellium to determine if they should use a snow side
     * texture.  Note that this refers to the top visible surface, not just the top facing,
     * which means that stuff like half-slabs should only return true if they have ground cover
     * actually on the top surface halfway up the block vertically.
     */
    @Override
    public boolean btwr$isSnowCoveringTopSurface(WorldAccess world, BlockPos pos) {
        BlockState stateAbove = world.getBlockState(pos.up());

        if (!stateAbove.isAir()) {
            Block blockAbove = stateAbove.getBlock();

            if (stateAbove.isOf(Blocks.SNOW) || stateAbove.isOf(Blocks.SNOW_BLOCK) &&
                    blockAbove.btwr$hasCenterHardPointToFacing( world, pos.up(), Direction.DOWN))
            {
                return true;
            }
            else if (
                    blockAbove.btwr$groundCoverRestingOnVisualOffset(world, pos.up()) < -0.99F &&
                            world.getBlockState(pos.up(2)).isOf(Blocks.SNOW)
            )
            {
                // consider snow resting on tall grass and such

                return true;
            }
        }

        return false;
    }
}