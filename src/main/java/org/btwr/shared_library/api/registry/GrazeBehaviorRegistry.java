package org.btwr.shared_library.api.registry;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Public API for defining what happens when animals graze on a block, without needing a mixin on that block.
 * <p> Block#btwr$canBeGrazedOn, Block#btwr$onGrazed and Block#btwr$onVegetationAboveGrazed check this registry first.
 *
 * <p>When several mods register a behavior for the same block, the one with the highest priority wins.
 * That way a mod that adds e.g. sparse grass can override another mod's default grass behavior.
 *
 * <p>Example usage:
 * <pre>{@code
 * // tall grass is always grazeable and just gets eaten
 * GrazeBehaviorRegistry.register(Blocks.TALL_GRASS, (world, pos, state, animal) -> true);
 *
 * // override another mod's behavior
 * GrazeBehaviorRegistry.register(Blocks.GRASS_BLOCK, new MyGrassGrazeBehavior(), GrazeBehaviorRegistry.OVERRIDE_PRIORITY);
 * }</pre>
 */
public class GrazeBehaviorRegistry {
    public static final int DEFAULT_PRIORITY = 0;
    public static final int OVERRIDE_PRIORITY = 100;

    private static final Map<Block, Entry> BEHAVIORS = new ConcurrentHashMap<>();

    private GrazeBehaviorRegistry() {}

    public static void register(Block block, GrazeBehavior behavior) {
        register(block, behavior, DEFAULT_PRIORITY);
    }

    /** Registers a behavior for the block, unless one with a higher priority is already registered **/
    public static void register(Block block, GrazeBehavior behavior, int priority) {
        BEHAVIORS.merge(block, new Entry(behavior, priority), (old, added) -> added.priority >= old.priority ? added : old);
    }

    @Nullable
    public static GrazeBehavior get(Block block) {
        Entry entry = BEHAVIORS.get(block);
        return entry != null ? entry.behavior : null;
    }

    private record Entry(GrazeBehavior behavior, int priority) {}

    public interface GrazeBehavior {
        boolean canBeGrazedOn(WorldAccess world, BlockPos pos, BlockState state, AnimalEntity animal);

        /** By default the block gets eaten (removed) and the block below is notified, like vegetation **/
        default void onGrazed(World world, BlockPos pos, BlockState state, AnimalEntity animal) {
            removeAndNotifyBelow(world, pos, animal);
        }

        default void onVegetationAboveGrazed(World world, BlockPos pos, BlockState state, AnimalEntity animal) {}

        static void removeAndNotifyBelow(World world, BlockPos pos, AnimalEntity animal) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState());

            BlockPos below = pos.down();
            world.getBlockState(below).getBlock().btwr$onVegetationAboveGrazed(world, below, animal);
        }
    }
}
