package net.daporkchop.realmapcc.data.converter.dataset.srtm;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.common.util.PorkUtil;
import net.daporkchop.lib.math.vector.i.Vec2i;
import net.daporkchop.realmapcc.data.Tile;
import net.daporkchop.realmapcc.data.converter.dataset.Dataset;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.abs;
import static net.daporkchop.lib.math.primitive.PMath.floorI;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
@Getter
public class SRTMDataset implements Dataset {
    @NonNull
    protected final File root;
    @NonNull
    protected final SRTMEngine engine;

    protected final LoadingCache<Vec2i, ByteBuffer> regionCache = CacheBuilder.newBuilder()
            .expireAfterAccess(1L, TimeUnit.MINUTES)
            .maximumSize(CPU_COUNT * 3L)
            .removalListener((RemovalListener<Vec2i, ByteBuffer>) notification -> PorkUtil.release(notification.getValue()))
            .build(new CacheLoader<Vec2i, ByteBuffer>() {
                @Override
                public ByteBuffer load(@NonNull Vec2i key) throws Exception {
                    return SRTMDataset.this.engine.getTile(key.getX(), key.getY(), SRTMDataset.this.root);
                }
            });

    @Override
    public String getName() {
        return "SRTM";
    }

    @Override
    public void applyTo(@NonNull Tile tile) {
        tile.validatePos();
        ByteBuffer buf = this.regionCache.getUnchecked(new Vec2i(tile.getDegLon(), tile.getDegLat()));
        if (buf.capacity() == 0)    {
            for (int x = TILE_SIZE - 1; x >= 0; x--) {
                for (int y = TILE_SIZE - 1; y >= 0; y--) {
                    tile.setRawHeight(x, y, 0);
                }
            }
        } else {
            int xOff = tile.getTileLon() * TILE_SIZE;
            int yOff = tile.getTileLat() * TILE_SIZE;
            for (int x = TILE_SIZE - 1; x >= 0; x--) {
                for (int y = TILE_SIZE - 1; y >= 0; y--) {
                    tile.setRawHeight(x, y, this.engine.getHeight(buf, x + xOff, y + yOff));
                }
            }
        }
    }
}
