package net.daporkchop.realmapcc.util;

/**
 * @author DaPorkchop_
 */
@FunctionalInterface
public interface TerrainDataConsumer {
    void accept(int xStep, int yStep, int height, boolean waterPresence, int biome);
}
