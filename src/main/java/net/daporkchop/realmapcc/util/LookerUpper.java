package net.daporkchop.realmapcc.util;

import lombok.NonNull;
import net.daporkchop.realmapcc.data.client.LookupCache;

/**
 * @author DaPorkchop_
 */
@FunctionalInterface
public interface LookerUpper {
    int apply(@NonNull LookupCache cache, int x, int y);
}
