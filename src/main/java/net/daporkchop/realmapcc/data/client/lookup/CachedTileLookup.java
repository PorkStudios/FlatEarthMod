package net.daporkchop.realmapcc.data.client.lookup;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.Getter;
import lombok.NonNull;
import net.daporkchop.realmapcc.data.Tile;
import net.daporkchop.realmapcc.data.TilePos;

import java.util.concurrent.TimeUnit;

/**
 * @author DaPorkchop_
 */
@Getter
public class CachedTileLookup extends AbstractTileLookup<CachedTileLookup> {
    protected final LoadingCache<TilePos, Tile> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(1L, TimeUnit.MINUTES)
            .maximumSize(256L)
            .build(new CacheLoader<TilePos, Tile>() {
                @Override
                public Tile load(@NonNull TilePos key) throws Exception {
                    return CachedTileLookup.this.delegate.getTile(key);
                }
            });

    @Override
    public Tile getTile(@NonNull TilePos pos) {
        return this.cache.getUnchecked(pos);
    }
}
