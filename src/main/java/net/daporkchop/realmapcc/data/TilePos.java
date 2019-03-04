package net.daporkchop.realmapcc.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.daporkchop.realmapcc.Constants;

/**
 * @author DaPorkchop_
 */
@Getter
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
    protected String subpath = null;

    public TilePos(int degLon, int degLat, int tileLon, int tileLat) {
        validatePos(degLon, degLat, tileLon, tileLat);

        this.degLon = degLon;
        this.degLat = degLat;
        this.tileLon = tileLon;
        this.tileLat = tileLat;
    }

    public synchronized String getSubpath() {
        String subpath = this.subpath;
        if (subpath == null)    {
            subpath = this.subpath = DataConstants.getSubpath(this.degLon, this.degLat, this.tileLon, this.tileLat);
        }
        return subpath;
    }

    @Override
    public String toString() {
        return String.format("%03d°%02d, %03d°%02d", this.degLon, this.tileLon, this.degLat, this.tileLat);
    }

    @Override
    public int hashCode() {
        return ((this.degLon * 1728083789 + this.tileLon) * 735562153 + this.degLat) * 1866811913 + this.tileLat;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)    {
            return true;
        } else if (obj instanceof TilePos)  {
            TilePos other = (TilePos) obj;
            return this.degLon == other.degLon && this.degLat == other.degLat && this.tileLon == other.tileLon && this.tileLat == other.tileLat;
        } else {
            return false;
        }
    }
}
