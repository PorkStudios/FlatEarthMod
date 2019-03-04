package net.daporkchop.realmapcc.data;

/**
 * @author DaPorkchop_
 */
public interface DataConstants {
    static String getSubpath(int degLon, int degLat, int tileLon, int tileLat)    {
        return String.format("%04d/%03d/%02d.%02d.png", degLon, degLat, tileLon, tileLat);
    }
}
