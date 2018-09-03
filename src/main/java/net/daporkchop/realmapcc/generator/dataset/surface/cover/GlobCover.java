package net.daporkchop.realmapcc.generator.dataset.surface.cover;

import net.daporkchop.lib.binary.NBitArray;
import net.daporkchop.realmapcc.generator.dataset.Dataset;
import net.daporkchop.realmapcc.util.ImageUtil;
import net.minecraft.util.math.ChunkPos;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static java.lang.Math.abs;
import static net.daporkchop.realmapcc.generator.dataset.surface.cover.BiomeColor.numBiomes_bits;

/**
 * @author DaPorkchop_
 */
public class GlobCover implements Dataset<NBitArray, BiomeColor[]> {
    public static final GlobCover INSTANCE = new GlobCover();
    public static final File globCoverPath = new File(rootDir, "GlobCover/globcover_colored_shrunk.tif");
    public static ThreadLocal<NBitArray> arrayCache = ThreadLocal.withInitial(
            () -> new NBitArray(GLOBCOVER_valuesPerDegree * GLOBCOVER_valuesPerDegree, numBiomes_bits)
    );

    private GlobCover() {
        super();
    }

    @Override
    public int getDataAtPos(double lonD, double latD) {
        return 0;
    }

    @Override
    public NBitArray getDataAtDegree(int lon, int lat) throws IOException {
        if (lat < 0) {
            lat = abs(lat) + maxLatitude;
        } else {
            lat = maxLatitude - lat;
        }
        NBitArray array = arrayCache.get();
        BufferedImage image = ImageUtil.getImageSection(
                globCoverPath,
                (lon - minLongitude) * GLOBCOVER_valuesPerDegree,
                lat * GLOBCOVER_valuesPerDegree,
                GLOBCOVER_valuesPerDegree, GLOBCOVER_valuesPerDegree
        );
        for (int x = GLOBCOVER_valuesPerDegree - 1; x >= 0; x--) {
            for (int y = GLOBCOVER_valuesPerDegree - 1; y >= 0; y--) {
                array.set(x * GLOBCOVER_valuesPerDegree + y, BiomeColor.getBiomeFromColor(image.getRGB(x, y)).ordinal());
            }
        }
        return array;
    }

    @Override
    public BiomeColor[] getDataForChunk(ChunkPos pos) {
        //TODO
        return null;
    }

    @Override
    public int getValuesPerDegree() {
        return GLOBCOVER_valuesPerDegree;
    }
}
