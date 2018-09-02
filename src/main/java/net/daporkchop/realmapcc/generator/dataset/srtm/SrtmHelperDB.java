package net.daporkchop.realmapcc.generator.dataset.srtm;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.daporkchop.lib.db.PorkDB;
import net.daporkchop.realmapcc.Constants;
import net.daporkchop.realmapcc.data.CompactedHeightData;
import net.daporkchop.realmapcc.data.EmptyCompactedHeightData;
import net.minecraft.util.math.ChunkPos;

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
                            return new EmptyCompactedHeightData();
                        }
                        return data;
                    } catch (Throwable t) {
                        t.printStackTrace();
                        return new EmptyCompactedHeightData();
                    }
                }
            });

    public SrtmHelperDB(PorkDB<ChunkPos, CompactedHeightData> db) {
        this.db = db;
    }

    public int getElevation(double lat, double lon) {
        return this.getValue(lat, lon);
    }

    private int getValue(double lat, double lon) {
        int sections = Constants.SRTM_valuesPerDegree / Constants.SRTM_subDegreeCount;
        int tileX = floorI(lat * Constants.SRTM_subDegreeCount);
        int relX = floorI((lat - floor(lat)) * Constants.SRTM_valuesPerDegree) % sections;
        int tileZ = floorI(lon * Constants.SRTM_subDegreeCount);
        int relZ = floorI((lon - floor(lon)) * Constants.SRTM_valuesPerDegree) % sections;
        int oldTileZ = tileZ;
        tileZ = ((tileZ >> 4) << 4) | ((tileX & 0xF));
        tileX = ((tileX >> 4) << 4) | ((oldTileZ & 0xF));

        //tileX = ((tileX >> 2) << 2) | (tileZ & 3);
        //tileZ = ((tileZ >> 2) << 2) | (xold & 3);

        //System.out.println("tile: (" + tileX + ',' + tileZ + ')');
        //System.out.println("tile: (" + relX + ',' + relZ + ')');
        CompactedHeightData data = this.cachedHeights.getUnchecked(new ChunkPos(tileX, tileZ));
        return data instanceof EmptyCompactedHeightData ? 0 : data.getHeight(relZ, relX);
        //return data instanceof EmptyCompactedHeightData ? 0xFF5555 : data.getHeight(relZ, relX) / 15;
    }
}
