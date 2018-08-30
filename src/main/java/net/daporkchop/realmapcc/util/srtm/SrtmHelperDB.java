package net.daporkchop.realmapcc.util.srtm;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.daporkchop.lib.db.PorkDB;
import net.daporkchop.realmapcc.Constants;
import net.daporkchop.realmapcc.data.CompactedHeightData;
import net.daporkchop.realmapcc.data.EmptyCompactedHeightData;
import net.minecraft.util.math.ChunkPos;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.floor;
import static net.daporkchop.lib.math.primitive.Floor.floorI;

public class SrtmHelperDB {

    private final PorkDB<ChunkPos, CompactedHeightData> db;
    private final LoadingCache<ChunkPos, CompactedHeightData> cachedHeights = CacheBuilder.newBuilder()
            .expireAfterAccess(1L, TimeUnit.MINUTES)
            .concurrencyLevel(4)
            .maximumSize(100L)
            .build(new CacheLoader<ChunkPos, CompactedHeightData>() {
                @Override
                public CompactedHeightData load(ChunkPos key) {
                    try {
                        CompactedHeightData data = SrtmHelperDB.this.db.get(key);
                        if (data == null) {
                            throw new NullPointerException();
                        } else {
                            //System.out.println("found!");
                        }
                        return data;
                    } catch (NullPointerException t) {
                        return new EmptyCompactedHeightData();
                    } catch (ArrayIndexOutOfBoundsException e) {
                        return new EmptyCompactedHeightData();
                        //throw new RuntimeException("Unable to get ChunkPos " + key);
                    }
                }
            });
    /**
     * SRTM1: 3600
     * SRTM3: 1200
     */
    private final int samplesPerFile;
    private final boolean interpolate;

    public SrtmHelperDB(PorkDB<ChunkPos, CompactedHeightData> db, int samplesPerFile, boolean interpolate) {
        this.db = db;
        this.samplesPerFile = samplesPerFile >> 2;
        this.interpolate = interpolate;
    }

    private static void copyInputStream(InputStream in, BufferedOutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len = in.read(buffer);
        while (len >= 0) {
            out.write(buffer, 0, len);
            len = in.read(buffer);
        }
        in.close();
        out.close();
    }

    public int srtmHeight(double lat, double lon) {
        if (this.interpolate) {
            //double incr = 1.0d / (double) Constants.width;
            int v1 = this.getValue(lat, lon);
            //int v2 = this.getValue(lat + incr, lon);
            //int v3 = this.getValue(lat, lon + incr);
            //int v4 = this.getValue(lat + incr, lon + incr);
            return v1;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private int getValue(double lat, double lon) {
        int sections = Constants.width / Constants.subtileCount;
        int tileX = floorI(lat * Constants.subtileCount);
        int relX = floorI((lat - floor(lat)) * Constants.width) % sections;
        int tileZ = floorI(lon * Constants.subtileCount);
        int relZ = floorI((lon - floor(lon)) * Constants.width) % sections;
        int oldTileZ = tileZ;
        tileZ = ((tileZ >> 4) << 4) | ((tileX & 0xF));
        tileX = ((tileX >> 4) << 4) | ((oldTileZ & 0xF));

        //tileX = ((tileX >> 2) << 2) | (tileZ & 3);
        //tileZ = ((tileZ >> 2) << 2) | (xold & 3);

        //System.out.println("tile: (" + tileX + ',' + tileZ + ')');
        //System.out.println("tile: (" + relX + ',' + relZ + ')');
        CompactedHeightData data = this.cachedHeights.getUnchecked(new ChunkPos(tileX, tileZ));
        //return data instanceof EmptyCompactedHeightData ? 0xFF5555 : 0x55FF55;
        return data instanceof EmptyCompactedHeightData ? 0xFF5555 : data.getHeight(relZ, relX) / 15;
    }
}
