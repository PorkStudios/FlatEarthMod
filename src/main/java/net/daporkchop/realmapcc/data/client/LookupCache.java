package net.daporkchop.realmapcc.data.client;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.daporkchop.lib.common.util.PUnsafe;
import net.daporkchop.realmapcc.Constants;
import net.daporkchop.realmapcc.data.Tile;
import net.daporkchop.realmapcc.data.client.lookup.TileLookup;

import static net.daporkchop.lib.math.primitive.PMath.clamp;

/**
 * @author DaPorkchop_
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class LookupCache implements Constants {
    protected int lon;
    protected int lat;
    protected final int w;
    protected final int h;

    protected final long addr;

    public LookupCache(int lon, int lat, int w, int h) {
        this.lon = lon;
        this.lat = lat;
        this.w = w;
        this.h = h;

        this.addr = PUnsafe.allocateMemory(this, ((long) w * (long) h) << 2L);
        PUnsafe.setMemory(this.addr, ((long) w * (long) h) << 2L, (byte) 0);
    }

    public LookupCache load(@NonNull TileLookup lookup) {
        Tile tile = null;

        //TODO: this can be optimized even further by loading in an entire tile at a time rather than iterating over the entire area one line at a time
        for (int x = this.w - 1; x >= 0; x--) {
            for (int y = this.h - 1; y >= 0; y--) {
                int subLon = Constants.mod(x, TILE_SIZE);
                int subLat = Constants.mod(y, TILE_SIZE);
                int tileLon = Constants.mod((x - subLon + this.lon) / TILE_SIZE, STEPS_PER_DEGREE);
                int tileLat = Constants.mod((y - subLat + this.lat) / TILE_SIZE, STEPS_PER_DEGREE);
                int degLon = (x - subLon - tileLon * STEPS_PER_DEGREE + this.lon) / ARCSECONDS_PER_DEGREE;
                int degLat = (y - subLat - tileLat * STEPS_PER_DEGREE + this.lat) / ARCSECONDS_PER_DEGREE;

                if (tile == null
                        || tile.getDegLon() != degLon
                        || tile.getDegLat() != degLat
                        || tile.getTileLon() != tileLon
                        || tile.getTileLat() != tileLat)    {
                    tile = lookup.getTile(degLon, degLat, tileLon, tileLat);
                }

                PUnsafe.putInt(this.addr + ((((long) x * this.h) + y) << 2L), tile.getRawVal(Constants.mod(x, TILE_SIZE), Constants.mod(y, TILE_SIZE)));
            }
        }

        return this;
    }

    public int getVal(int x, int y){
        x -= this.lon;
        y -= this.lat;
        return PUnsafe.getInt(this.addr + ((((long) clamp(x, 0, this.lon - 1) * this.h) + clamp(y, 0, this.lat - 1)) << 2L));
    }

    public int getHeight(int x, int y)  {
        int val = this.getVal(x, y);
        return ((val & Tile.HEIGHT_MASK) >>> Tile.HEIGHT_SHIFT) * ((val & 1) != 0 ? -1 : 1);
    }

    public int getWaterPresence(int x, int y)  {
        return (this.getVal(x, y) & Tile.WATER_MASK) >>> Tile.WATER_SHIFT;
    }
}
