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
    protected static File getSrtmFileName(int lat, int lon, @NonNull File localDir) {
        //int lonFloor = abs(floorI(lon));
        //int latFloor = abs(floorI(lat));

        char NS = lat > 0 ? 'N' : 'S';
        char EW = lon > 0 ? 'E' : 'W';

        //NumberFormat nf = NumberFormat.getInstance();
        //nf.setMinimumIntegerDigits(2);
        //String f_nlat = nf.format(latFloor);
        //nf.setMinimumIntegerDigits(3);
        //String f_nlon = nf.format(lonFloor);

        return new File(localDir, String.format("%c%02d%c%03d.hgt", NS, abs(floorI(lat)), EW, abs(floorI(lon))));
    }

    @NonNull
    protected final File root;
    protected final LoadingCache<Vec2i, ByteBuffer> regionCache = CacheBuilder.newBuilder()
            .expireAfterAccess(1L, TimeUnit.MINUTES)
            .removalListener((RemovalListener<Vec2i, ByteBuffer>) notification -> PorkUtil.release(notification.getValue()))
            .build(new CacheLoader<Vec2i, ByteBuffer>() {
                @Override
                public ByteBuffer load(@NonNull Vec2i key) throws Exception {
                    File file = getSrtmFileName(key.getY(), key.getX(), SRTMDataset.this.root); //yes, these are swapped
                    if (file.exists()) {
                        try (FileChannel channel = FileChannel.open(file.toPath(), Collections.singleton(StandardOpenOption.READ))) {
                            ByteBuffer toReturn = ByteBuffer.allocateDirect((ARCSECONDS_PER_DEGREE + 1) * (ARCSECONDS_PER_DEGREE + 1) * 2);
                            if (channel.read(toReturn, 0L) != (ARCSECONDS_PER_DEGREE + 1) * (ARCSECONDS_PER_DEGREE + 1) * 2) {
                                throw new IllegalStateException("File not read completely!");
                            }
                            return toReturn;
                        }
                    } else {
                        return ByteBuffer.allocateDirect(0);
                    }
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
                    //tile.setRawHeight(x, y, buf.getShort(((ARCSECONDS_PER_DEGREE - y - yOff) * (ARCSECONDS_PER_DEGREE + 1) + x + xOff) << 1));
                    tile.setRawHeight(x, y, buf.getShort(((y + yOff) * (ARCSECONDS_PER_DEGREE + 1) + x + xOff) << 1));
                }
            }
        }
    }
}
