package me.winflix.vitalcore.utils;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.block.Block;

public class BlocksUtils {

    public static Set<Block> getNearbyBlocks(Block block, int radius) {
        int minX = block.getX() - radius;
        int minY = block.getY() - radius;
        int minZ = block.getZ() - radius;

        int maxX = block.getX() + radius;
        int maxY = block.getY() + radius;
        int maxZ = block.getZ() + radius;

        return IntStream.rangeClosed(minX, maxX)
                .boxed()
                .flatMap(x -> IntStream.rangeClosed(minY, maxY)
                        .boxed()
                        .flatMap(y -> IntStream.rangeClosed(minZ, maxZ)
                                .mapToObj(z -> block.getWorld().getBlockAt(x, y, z))))
                .collect(Collectors.toSet());
    }
}
