package net.daporkchop.realmapcc.generator.dataset.surface.cover;

import net.daporkchop.lib.binary.NBitArray;
import net.daporkchop.realmapcc.generator.dataset.Dataset;

import java.io.File;

import static net.daporkchop.realmapcc.generator.dataset.surface.cover.BiomeColor.numBiomes_bits;

/**
 * @author DaPorkchop_
 */
public class GlobCover implements Dataset<NBitArray> {
    public static final File globCoverPath = new File(rootDir, "GlobCover/globcover.tif");
    public static ThreadLocal<NBitArray> arrayCache = ThreadLocal.withInitial(
            () -> new NBitArray(GLOBCOVER_valuesPerDegree * GLOBCOVER_valuesPerDegree, numBiomes_bits)
    );

    @Override
    public int getDataAtPos(double lon, double lat) {

        return 0;
    }

    @Override
    public NBitArray getDataAtDegree(int lon, int lat) {
        NBitArray array = arrayCache.get();
        //TODO
        return array;
    }

    @Override
    public int getValuesPerDegree() {
        return GLOBCOVER_valuesPerDegree;
    }
}
