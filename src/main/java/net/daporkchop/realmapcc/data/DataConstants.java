package net.daporkchop.realmapcc.data;

/**
 * @author DaPorkchop_
 */
public interface DataConstants {
    static String getSubpath(int lon, int lat, int tileX, int tileY)    {
        return String.format("%03d/%02d/%02d.%02d.png", lon, lat, tileX, tileY);
    }
}
