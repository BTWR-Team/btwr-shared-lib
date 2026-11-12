package org.btwr.shared_library.api.block.util;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.*;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.btwr.shared_library.mixin.accessors.FireBlockAccessor;

public class FireBlockUtils {

    /**
     * BTW-style: first try to destroy the block with fire (using burnChance),
     * delegating to the block's onDestroyedByFire hook; if that fails, fall
     * back to the spread logic for this single location.
     *
     * This is the modern equivalent of BlockFire.checkForFireSpreadAndDestructionToOneBlockLocation.
     */
    public static void checkForFireSpreadAndDestructionToOneBlockLocation(
            World world,
            BlockPos pos,
            Random rand,
            int sourceFireAge,
            int spreadTopBound
    ) {
        if (!world.getGameRules().getBoolean(GameRules.DO_FIRE_TICK)) {
            return;
        }

        // First: attempt destruction, similar to BTW's "abilityToCatchFire" pass.
        int burnChance = getBurnChance(world, pos);
        if (burnChance > 0) {
            boolean isHighHumidity = world.getBiome(pos).isIn(BiomeTags.INCREASED_FIRE_BURNOUT);

            int chanceToDestroy = 250;
            if (isHighHumidity) {
                chanceToDestroy -= 50;
            }

            if (rand.nextInt(chanceToDestroy) < burnChance) {
                onBlockDestroyedByFire(world, pos, sourceFireAge, true);
                return;
            }
        }

        // If not destroyed, use our spread helper to possibly ignite this position.
        boolean isHighHumidity = world.getBiome(pos).isIn(BiomeTags.INCREASED_FIRE_BURNOUT);
        checkForFireSpreadToOneBlockLocation(
                world,
                pos,
                rand,
                sourceFireAge,
                isHighHumidity,
                spreadTopBound
        );
    }

    /**
     * Simpler overload with BTW defaults: sourceFireAge = 0, spreadTopBound = 100.
     */
    public static void checkForFireSpreadAndDestructionToOneBlockLocation(World world, BlockPos pos) {
        checkForFireSpreadAndDestructionToOneBlockLocation(
                world,
                pos,
                world.random,
                0,
                100
        );
    }

    /**
     * BTW-style: destruction pass for a single position.
     * Uses the FireBlock's burnChance table and then delegates to the target block
     * via the onDestroyedByFire hook.
     */
    public static void tryToDestroyBlockWithFire(
            World world,
            BlockPos pos,
            int chanceToDestroy,
            Random random,
            int sourceAge
    ) {
        if (!world.getGameRules().getBoolean(GameRules.DO_FIRE_TICK)) {
            return;
        }

        int burnChance = getBurnChance(world, pos);
        if (burnChance <= 0) {
            return;
        }

        if (random.nextInt(chanceToDestroy) < burnChance) {
            onBlockDestroyedByFire(world, pos, sourceAge, false);
        }
    }

    /**
     * Central hook used by both destruction and spread+destruction helpers.
     * Mirrors BTW's BlockFire.onBlockDestroyedByFire, but adapted to modern APIs.
     */
    public static void onBlockDestroyedByFire(
            World world,
            BlockPos pos,
            int fireAge,
            boolean forcedFireSpread
    ) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (block == Blocks.AIR) {
            return;
        }

        block.btwr$onDestroyedByFire(world, pos, fireAge, forcedFireSpread);
    }

    /**
     * Helper to read the "burn chance" (ability to be destroyed by fire) from
     * vanilla FireBlock's internal table, respecting WATERLOGGED like vanilla does.
     */
    public static int getBurnChance(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        if (state.contains(Properties.WATERLOGGED) && state.get(Properties.WATERLOGGED)) {
            return 0;
        }

        Block block = state.getBlock();
        if (Blocks.FIRE instanceof FireBlock fireBlock) {
            Object2IntMap<Block> map = ((FireBlockAccessor) fireBlock).btwr$getBurnChances();
            return map.getInt(block);
        }

        return 0;
    }

    /**
     * BTW-style fire spread from a burning source, in a 3×3 area from one block below it to four blocks above.
     * The chance gets lower for each block above the first one.
     */
    public static void checkForFireSpreadFromLocation(World world, BlockPos pos, Random rand, int sourceFireAge) {
        if (!world.getGameRules().getBoolean(GameRules.DO_FIRE_TICK)) {
            return;
        }

        boolean isHighHumidity = world.getBiome(pos).isIn(BiomeTags.INCREASED_FIRE_BURNOUT);

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        for (int i = x - 1; i <= x + 1; ++i) {
            for (int k = z - 1; k <= z + 1; ++k) {
                for (int j = y - 1; j <= y + 4; ++j) {
                    if (i != x || j != y || k != z) {
                        int spreadTopBound = 100;

                        if (j > y + 1) {
                            spreadTopBound += (j - (y + 1)) * 100;
                        }

                        checkForFireSpreadToOneBlockLocation(
                                world,
                                new BlockPos(i, j, k),
                                rand,
                                sourceFireAge,
                                isHighHumidity,
                                spreadTopBound
                        );
                    }
                }
            }
        }
    }

    /**
     * BTW-style "smouldering spread" in a 3×3×2 area around the source.
     * This is a trimmed-down version of BTW's checkForSmoulderingSpreadFromLocation.
     */
    public static void checkForSmoulderingSpreadFromLocation(World world, BlockPos pos) {
        if (!world.getGameRules().getBoolean(GameRules.DO_FIRE_TICK)) {
            return;
        }

        boolean isHighHumidity = world.getBiome(pos).isIn(BiomeTags.INCREASED_FIRE_BURNOUT);

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        for (int i = x - 1; i <= x + 1; ++i) {
            for (int k = z - 1; k <= z + 1; ++k) {
                for (int j = y; j <= y + 1; ++j) {
                    if (i != x || j != y || k != z) {
                        int spreadTopBound = 50; // increased chance in a smaller area

                        BlockPos targetPos = new BlockPos(i, j, k);
                        checkForFireSpreadToOneBlockLocation(
                                world,
                                targetPos,
                                world.random,
                                0, // sourceFireAge
                                isHighHumidity,
                                spreadTopBound
                        );
                    }
                }
            }
        }
    }

    /**
     * Core BTW-style spread helper for a single target position.
     * This mirrors checkForFireSpreadToOneBlockLocation from BTW.
     */
    private static void checkForFireSpreadToOneBlockLocation(
            World world,
            BlockPos pos,
            Random rand,
            int sourceFireAge,
            boolean isHighHumidity,
            int spreadTopBound
    ) {
        if (!world.getGameRules().getBoolean(GameRules.DO_FIRE_TICK)) {
            return;
        }

        int neighborChance = getChanceOfNeighborsEncouragingFireCustom(world, pos);
        if (neighborChance <= 0) {
            return;
        }

        int spreadChance = (neighborChance + 61) / (sourceFireAge + 30);
        if (isHighHumidity) {
            spreadChance /= 2;
        }
        if (spreadChance <= 0) {
            return;
        }

        if (rand.nextInt(spreadTopBound) <= spreadChance
                && (!world.isRaining() || !world.hasRain(pos))
                && !world.hasRain(pos.west())
                && !world.hasRain(pos.east())
                && !world.hasRain(pos.north())
                && !world.hasRain(pos.south())) {

            int startAge = sourceFireAge + rand.nextInt(5) / 4;
            if (startAge > 15) {
                startAge = 15;
            }

            if (!world.getGameRules().getBoolean(GameRules.DO_FIRE_TICK)) {
                return;
            }

            BlockState targetState = world.getBlockState(pos);
            Block targetBlock = targetState.getBlock();

            // Fire only gets here with a flammable neighbor, and it can burn on any side of one, so what's below doesn't matter
            if (canFireReplaceBlock(world, pos)) {
                world.setBlockState(
                        pos,
                        Blocks.FIRE.getDefaultState().with(FireBlock.AGE, startAge),
                        Block.NOTIFY_ALL
                );
            }
            else if (targetBlock.btwr$getCanBeSetOnFireDirectly(world, pos)) {
                targetBlock.btwr$setOnFireDirectly(world, pos);
            }
        }
    }

    /**
     * BTW-style: getChanceOfNeighborsEncouragingFireCustom, adapted to BlockPos.
     *
     * In BTW this calls into:
     *  - custom direct-fire hooks if the block is non-replaceable
     *  - otherwise looks at the six orthogonal neighbors for chanceToEncourageFire[]
     */
    protected static int getChanceOfNeighborsEncouragingFireCustom(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (!canFireReplaceBlock(world, pos)) {
            // Non-replaceable block can expose its own fire spread chance via hooks.
            if (block.btwr$getCanBeSetOnFireDirectly(world, pos)) {
                return block.btwr$getChanceOfFireSpreadingDirectlyTo(world, pos);
            }
            else {
                return 0;
            }
        }
        else {
            int chance = 0;
            chance = getChanceToEncourageFire(world, pos.east(), chance);
            chance = getChanceToEncourageFire(world, pos.west(), chance);
            chance = getChanceToEncourageFire(world, pos.down(), chance);
            chance = getChanceToEncourageFire(world, pos.up(), chance);
            chance = getChanceToEncourageFire(world, pos.north(), chance);
            chance = getChanceToEncourageFire(world, pos.south(), chance);
            return chance;
        }
    }

    /**
     * BTW-style: getChanceToEncourageFire(world, x, y, z, prevChance)
     *
     * In modern FireBlock this is essentially FireBlock#getSpreadChance, but that
     * method is instance-level and also cares about WATERLOGGED; we mimic the same.
     */
    public static int getChanceToEncourageFire(World world, BlockPos pos, int prevChance) {
        BlockState state = world.getBlockState(pos);

        int chance = 0;

        if (!(state.contains(Properties.WATERLOGGED) && state.get(Properties.WATERLOGGED))) {
            Block block = state.getBlock();
            if (Blocks.FIRE instanceof FireBlock fireBlock) {
                Object2IntMap<Block> map = ((FireBlockAccessor) fireBlock).btwr$getSpreadChances();
                chance = map.getInt(block);
            }
        }

        return Math.max(chance, prevChance);
    }

    /**
     * Simple “canFireReplaceBlock” for Fabric: fire may replace air or other
     * blocks that explicitly allow replacement via a BTW-style hook.
     */
    public static boolean canFireReplaceBlock(World world, BlockPos pos) {
        if (!world.getGameRules().getBoolean(GameRules.DO_FIRE_TICK)) {
            return false;
        }

        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        // Air is always fine
        if (state.isAir()) {
            return true;
        }

        return block.btwr$getCanBlockBeReplacedByFire(world, pos);
    }

    /**
     * BTW-style: canBlockBeDestroyedByFire.
     *
     * Semantics: does this block have any non-zero "burn chance" in the FireBlock
     * table (ability to be destroyed by fire)?
     *
     * This is the modern equivalent of abilityToCatchFire[blockID] > 0.
     */
    public static boolean canBlockBeDestroyedByFire(World world, BlockPos pos) {
        return getBurnChance(world, pos) > 0;
    }

    /**
     * If you ever need it by block instance (not world/pos), you can also add:
     */
    public static boolean canBlockBeDestroyedByFire(Block block) {
        if (!(Blocks.FIRE instanceof FireBlock fireBlock)) {
            return false;
        }
        Object2IntMap<Block> burnMap = ((FireBlockAccessor) fireBlock).btwr$getBurnChances();
        return burnMap.getInt(block) > 0;
    }

    /**
     * BTW-style "canBlockCatchFire" adapted to modern APIs.
     *
     * Semantics: is this block *flammable enough to help fire spread*?
     * Implementation: checks the FireBlock's spreadChances table for the
     * block at the given position (ignoring WATERLOGGED blocks).
     */
    public static boolean canBlockCatchFire(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        // BTW didn't have waterlogging; modern fire code ignores waterlogged blocks
        if (state.contains(Properties.WATERLOGGED) && state.get(Properties.WATERLOGGED)) {
            return false;
        }

        Block block = state.getBlock();

        // Hook / special cases can go here (hellfire-style, etc).
        // Example pattern (pseudocode):
        // if (block == MyBlocks.HELLFIRE_BLOCK) return true;

        if (Blocks.FIRE instanceof FireBlock fireBlock) {
            Object2IntMap<Block> spreadMap = ((FireBlockAccessor) fireBlock).btwr$getSpreadChances();
            return spreadMap.getInt(block) > 0;
        }

        return false;
    }

    public static boolean hasFlammableNeighborsWithinSmoulderRange(World world, BlockPos pos) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 0; dy <= 1; dy++) {

                    if (dx == 0 && dy == 0 && dz == 0) continue;

                    mutable.set(
                            pos.getX() + dx,
                            pos.getY() + dy,
                            pos.getZ() + dz
                    );

                    if (isFlammableOrHasFlammableNeighbors(world, mutable)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean isFlammableOrHasFlammableNeighbors(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        // Directly flammable
        if (state.isBurnable()) {
            return true;
        }

        // If this block can be replaced by fire (air, plants, etc.)
        if (state.isAir() || state.isReplaceable()) {

            for (Direction dir : Direction.values()) {
                BlockState adjacent = world.getBlockState(pos.offset(dir));
                if (adjacent.isBurnable()) {
                    return true;
                }
            }
        }

        return false;
    }






}