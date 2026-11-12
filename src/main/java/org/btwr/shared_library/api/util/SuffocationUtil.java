package org.btwr.shared_library.api.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class SuffocationUtil {

    private SuffocationUtil() {}

    public static boolean isInsideSuffocatingBlock(Entity entity) {
        return isInsideSuffocatingBlock(entity, false);
    }

    public static boolean isInsideSuffocatingBlock(Entity entity, boolean checkFeet) {
        World world = entity.getWorld();
        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int count = 0; count < 8; ++count) {
            float i = (((count >> 0) % 2) - 0.5f) * entity.getWidth() * 0.8f;
            float j = (((count >> 1) % 2) - 0.5f) * 0.1f;
            float k = (((count >> 2) % 2) - 0.5f) * entity.getWidth() * 0.8f;

            int x = MathHelper.floor(entity.getX() + i);
            int feetY = MathHelper.floor(entity.getY() + 0.5 + j);
            int eyeY = MathHelper.floor(entity.getEyeY() + j);
            int z = MathHelper.floor(entity.getZ() + k);

            boolean suffocateAtFeet = checkFeet && canSuffocate(world, pos.set(x, feetY, z));

            if (canSuffocate(world, pos.set(x, eyeY, z)) || suffocateAtFeet) {
                return true;
            }
        }

        return false;
    }

    private static boolean canSuffocate(World world, BlockPos pos) {
        return world.getBlockState(pos).shouldSuffocate(world, pos);
    }
}