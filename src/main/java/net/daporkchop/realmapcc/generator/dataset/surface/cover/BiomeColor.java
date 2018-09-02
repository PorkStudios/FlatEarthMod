package net.daporkchop.realmapcc.generator.dataset.surface.cover;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.daporkchop.lib.binary.util.RequiredBits;

/**
 * Used for converting colors from GlobCover to biome data
 *
 * @author DaPorkchop_
 */
//TODO: actually add biome mappings
public enum BiomeColor {
    IRRIGATED_CROPLANDS(170, 240, 240),
    RAINFED_CROPLANDS(255, 255, 100),
    MOSAIC_CROPLAND(220, 240, 100),
    MOSAIC_VEGETATION(205, 205, 102),
    MIDDLE_BROADLEAVED_FOREST(0, 100, 0),
    CLOSED_BROADLEAVED_FOREST(0, 160, 0),
    OPEN_BROADLEAVED_FOREST(170, 200, 0),
    CLOSED_NEEDLELEAVED_FOREST(0, 60, 0),
    OPEN_NEEDLELEAVED_FOREST(40, 100, 0),
    BROADLEAVED_NEEDLELEAVED_FOREST(120, 130, 0),
    MOSAIC_FOREST(140, 160, 0),
    MOSAIC_GRASSLAND(190, 150, 0),
    MIDDLE_SHRUBLAND(150, 100, 0),
    MIDDLE_HERBACEOUS_VEGETATION(155, 180, 50),
    SPARSE_VEGETATION(255, 235, 175),
    MIDDLE_BROADLEAVED_FOREST_FLOODED(0, 120, 90),
    CLOSED_SHRUBLAND_FLOODED(0, 150, 120),
    MIDDLE_GRASSLAND_FLOODED(0, 220, 130),
    ARTIFICIAL(195, 20, 0),
    BARREN(255, 245, 215),
    WATER(0, 70, 200),
    PERMANENT_SNOW_ICE(255, 255, 255),
    NO_DATA(0, 0, 0);

    public static final int numBiomes = values().length;
    public static final int numBiomes_bits = RequiredBits.getNumBitsNeededFor(numBiomes);
    private static final Int2ObjectMap<BiomeColor> colorToBiome = new Int2ObjectOpenHashMap<>();

    static {
        for (BiomeColor val : values()) {
            colorToBiome.put(val.color, val);
        }
    }

    public final int color;

    BiomeColor(int r, int g, int b) {
        this.color = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public static BiomeColor getBiomeFromColor(int color) {
        return colorToBiome.getOrDefault(color & 0x00FFFFFF, NO_DATA);
    }
}
