package net.daporkchop.realmapcc.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.daporkchop.realmapcc.Constants;

/**
 * @author DaPorkchop_
 */
@Getter
@EqualsAndHashCode
public class TilePos implements Constants {
    public static void validatePos(int degLon, int degLat, int tileLon, int tileLat) {
        if (degLon < LONGITUDE_MIN || degLon > LONGITUDE_MAX) {
            throw new IllegalArgumentException(String.format("Invalid longitude value %03d, must be in range %d - %d", degLon, LONGITUDE_MIN, LONGITUDE_MAX));
        } else if (degLat < LATITUDE_MIN || degLat > LATITUDE_MAX) {
            throw new IllegalArgumentException(String.format("Invalid latitude value %03d, must be in range %d - %d", degLat, LATITUDE_MIN, LATITUDE_MAX));
        } else if (tileLon < 0 || tileLon > STEPS_PER_DEGREE) {
            throw new IllegalArgumentException(String.format("Invalid longitude tile value %03d, must be in range 0 - %d", tileLon, STEPS_PER_DEGREE));
        } else if (tileLat < 0 || tileLat > STEPS_PER_DEGREE) {
            throw new IllegalArgumentException(String.format("Invalid latitude tile value %03d, must be in range 0 - %d", tileLat, STEPS_PER_DEGREE));
        }
    }

    protected final int degLon;
    protected final int degLat;
    protected final int tileLon;
    protected final int tileLat;

    public TilePos(int degLon, int degLat, int tileLon, int tileLat) {
        validatePos(degLon, degLat, tileLon, tileLat);

        this.degLon = degLon;
        this.degLat = degLat;
        this.tileLon = tileLon;
        this.tileLat = tileLat;
    }

    public String getSubpath() {
        return DataConstants.getSubpath(this.degLon, this.degLat, this.tileLon, this.tileLat);
    }

    @Override
    public String toString() {
        return String.format("%03d°%02d, %03d°%02d", this.degLon, this.tileLon, this.degLat, this.tileLat);
    }
}
