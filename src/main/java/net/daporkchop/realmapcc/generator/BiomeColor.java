package net.daporkchop.realmapcc.generator;

/**
 * Used for converting colors from GlobCover to biome data
 *
 * @author DaPorkchop_
 */
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
    MOSAIC_GRASSLAND(190, 150, 0),;

    public final int color;

    BiomeColor(int r, int g, int b) {
        this.color = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }
}
