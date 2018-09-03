package net.daporkchop.realmapcc.generator.dataset.srtm;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.daporkchop.lib.db.PorkDB;
import net.daporkchop.realmapcc.Constants;
import net.daporkchop.realmapcc.data.CompactedHeightData;
import net.daporkchop.realmapcc.data.EmptyCompactedHeightData;
import net.daporkchop.realmapcc.generator.HeightmapGenerator;
import net.daporkchop.realmapcc.generator.dataset.Dataset;
import net.minecraft.util.math.ChunkPos;
import org.apache.commons.math3.analysis.BivariateFunction;

import java.util.concurrent.TimeUnit;

import static java.lang.Math.floor;
import static net.daporkchop.lib.math.primitive.Floor.floorI;

public class SrtmHelperDB implements Dataset<short[]> {
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
        /*int tileX = lon * Constants.SRTM_subDegreeCount;
        int tileZ = lat * Constants.SRTM_subDegreeCount;

        short[] heights = new short[SRTM_valuesPerDegree * SRTM_valuesPerDegree];
        for (int tOffsetX = SRTM_subDegreeCount - 1; tOffsetX >= 0; tOffsetX--) {
            for (int tOffsetZ = SRTM_subDegreeCount - 1; tOffsetZ >= 0; tOffsetZ--) {
                CompactedHeightData data = this.cachedHeights.getUnchecked(new ChunkPos(tileX + tOffsetX, tileZ + tOffsetZ));
                for (int x = data.width - 1; x >= 0; x--) {
                    for (int z = data.width - 1; z >= 0; z--) {
                        heights[(tOffsetX * SRTM_subDegreeCount + x) * SRTM_valuesPerDegree + (tOffsetZ * SRTM_subDegreeCount + z)] =
                                (short) data.getHeight(x, z);
                    }
                }
            }
        }
        return heights;*/
        throw new UnsupportedOperationException();
    }

    @Override
    public BivariateFunction getInterpolatedData(double lon, double lat, double width, double height) {
        /*if (true) {
            A:
            if (this.functionCache != null && !Keyboard.isKeyDown(Keyboard.KEY_Q)) {
                try {
                    this.functionCache.value(lat, lon);
                } catch (Throwable t) {
                    if (false)   {
                        return null;
                    }
                    System.out.println("Generating new bicubic thingy");
                    break A;
                }
                return this.functionCache;
            }
            int a = SRTM_valuesPerDegree * 2;
            double[] xS = new double[a];
            double[] zS = new double[a];
            double[][] vals = new double[a][a];
            for (int x = a - 1; x >= 0; x--) {
                xS[x] = floor(lat) + (double) x / SRTM_valuesPerDegree;
            }
            for (int z = a - 1; z >= 0; z--) {
                zS[z] = floor(lon) + (double) z / SRTM_valuesPerDegree;
            }
            for (int x = a - 1; x >= 0; x--) {
                for (int z = a - 1; z >= 0; z--) {
                    //vals[x][z] = heights[x * SRTM_valuesPerDegree + z];
                    vals[x][z] = this.getDataAtPos(xS[x], zS[z]);
                }
            }
            this.functionCache = interpolatorBicubic.interpolate(xS, zS, vals);
            return this.functionCache;
        }*/
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
        //return new BicubicInterpolator().interpolate(xVals, zVals, vals);
    }

    @Override
    public int getValuesPerDegree() {
        return SRTM_valuesPerDegree;
    }
}
