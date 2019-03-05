package net.daporkchop.realmapcc.util;

import net.daporkchop.realmapcc.data.Tile;

/**
 * @author DaPorkchop_
 */
@FunctionalInterface
public interface LookupCalculator {
    int apply(Tile tile, int a, int b);
}
