package net.daporkchop.realmapcc.generator.dataset.srtm;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.daporkchop.lib.db.PorkDB;
import net.daporkchop.realmapcc.Constants;
import net.daporkchop.realmapcc.RealmapCC;
import net.daporkchop.realmapcc.data.CompactedHeightData;
import net.daporkchop.realmapcc.data.EmptyCompactedHeightData;
import net.daporkchop.realmapcc.generator.HeightmapGenerator;
import net.daporkchop.realmapcc.generator.dataset.Dataset;
import net.minecraft.util.math.ChunkPos;
import org.apache.commons.math3.analysis.BivariateFunction;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.floor;
import static net.daporkchop.lib.math.primitive.Floor.floorI;

public class SrtmHelperDB implements Dataset<short[], short[]> {
    private final PorkDB<ChunkPos, CompactedHeightData> db;
    private final LoadingCache<ChunkPos, CompactedHeightData> cachedHeights = CacheBuilder.newBuilder()
            .expireAfterAccess(1L, TimeUnit.MINUTES)
            .concurrencyLevel(4)
            .maximumSize(5L)
            .build(new CacheLoader<ChunkPos, CompactedHeightData>() {
                @Override
                public CompactedHeightData load(ChunkPos key) {
                    try {
                        CompactedHeightData data = SrtmHelperDB.this.db.get(key);
                        if (data == null) {
                            HeightmapGenerator.tryConvert(new ChunkPos(key.x >> 4, key.z >> 4), SrtmHelperDB.this.db);
                            data = SrtmHelperDB.this.db.get(key);
                        }
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
    public BivariateFunction functionCache;

    public SrtmHelperDB(PorkDB<ChunkPos, CompactedHeightData> db) {
        this.db = db;
    }

    @Override
    public int getDataAtPos(double lat, double lon) {
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
        //this.cachedHeights.invalidateAll();
        CompactedHeightData data = this.cachedHeights.getUnchecked(new ChunkPos(tileX, tileZ));
        return data instanceof EmptyCompactedHeightData ? 0 : data.getHeight(relZ, relX);
        //return data instanceof EmptyCompactedHeightData ? 0xFF5555 : data.getHeight(relZ, relX) / 15;
    }

    @Override
    public short[] getDataAtDegree(int lon, int lat) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BivariateFunction getInterpolatedData(double lon, double lat, double width, double height) {
        int offset = 5;
        int valuesX = offset + floorI(width * SRTM_valuesPerDegree) + offset + 1;
        int valuesZ = offset + floorI(height * SRTM_valuesPerDegree) + offset + 1;
        double[] xVals = new double[valuesX];
        double[] zVals = new double[valuesZ];
        for (int i = valuesX - 1; i >= 0; i--) {
            xVals[i] = (floor(lat * SRTM_valuesPerDegree) + i - offset) / (double) SRTM_valuesPerDegree;
        }
        for (int i = valuesZ - 1; i >= 0; i--) {
            zVals[i] = (floor(lon * SRTM_valuesPerDegree) + i - offset) / (double) SRTM_valuesPerDegree;
        }
        double[][] vals = new double[valuesX][valuesZ];
        for (int x = valuesX - 1; x >= 0; x--) {
            for (int z = valuesZ - 1; z >= 0; z--) {
                vals[x][z] = this.getDataAtPos(xVals[x], zVals[z]);
            }
        }
        return interpolatorBicubic.interpolate(xVals, zVals, vals);
    }

    @Override
    public short[] getDataForChunk(ChunkPos pos) {
        short[] s = new short[16 * 16];
        try {
            BivariateFunction function = this.getInterpolatedData(
                    (pos.z << 4) * spaceBetweenBlocks / RealmapCC.Conf.scaleHoriz,
                    (pos.x << 4) * spaceBetweenBlocks / RealmapCC.Conf.scaleHoriz,
                    16 * spaceBetweenBlocks / RealmapCC.Conf.scaleHoriz,
                    16 * spaceBetweenBlocks / RealmapCC.Conf.scaleHoriz
            );
            if (function == null) {
                return s;
            }
            for (int x = 15; x >= 0; x--) {
                for (int z = 15; z >= 0; z--) {
                    s[(x << 4) | z] = (short) (
                            function.value(
                                    ((pos.x << 4) | x) * spaceBetweenBlocks / RealmapCC.Conf.scaleHoriz,
                                    ((pos.z << 4) | z) * spaceBetweenBlocks / RealmapCC.Conf.scaleHoriz
                            ) * RealmapCC.Conf.scaleVert
                    );
                }
            }
        } catch (Throwable t) {
            if (false) {
                t.printStackTrace();
            }
            Arrays.fill(s, (short) 0);
        }
        return s;
    }

    @Override
    public int getValuesPerDegree() {
        return SRTM_valuesPerDegree;
    }
}
