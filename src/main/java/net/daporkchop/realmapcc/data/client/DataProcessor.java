package net.daporkchop.realmapcc.data.client;

import lombok.Getter;
import lombok.NonNull;
import net.daporkchop.lib.math.arrays.grid.Grid2d;
import net.daporkchop.lib.math.interpolation.CubicInterpolationEngine;
import net.daporkchop.lib.math.interpolation.InterpolationEngine;
import net.daporkchop.realmapcc.Constants;
import net.daporkchop.realmapcc.RealmapCC;
import net.daporkchop.realmapcc.data.client.lookup.CachedTileLookup;
import net.daporkchop.realmapcc.data.client.lookup.DiskTileCache;
import net.daporkchop.realmapcc.data.client.lookup.RepoTileLookup;
import net.daporkchop.realmapcc.data.client.lookup.TileLookup;
import net.minecraft.world.chunk.Chunk;

import java.io.File;

import static net.daporkchop.lib.math.primitive.PMath.floorI;

/**
 * @author DaPorkchop_
 */
@Getter
public class DataProcessor implements Constants {
    protected final TileLookup tileLookup = new CachedTileLookup().setDelegate(new DiskTileCache(new File(RealmapCC.Conf.dataCacheDir)).setDelegate(new RepoTileLookup()));

    protected final InterpolationEngine engine = new CubicInterpolationEngine();
    protected final LookupGrid2d grid = new LookupGrid2d(this.tileLookup);

    public void prepare(int chunkX, int chunkZ, @NonNull short[] heights)    {
        int colX = chunkX << 4;
        int colZ = chunkZ << 4;
        double lon = (colX / METERS_PER_ARCSECOND) * RealmapCC.Conf.scaleHoriz;
        double lat = (colZ / METERS_PER_ARCSECOND) * RealmapCC.Conf.scaleHoriz;

        /*int tileLon = floorI(lon / TILE_SIZE);
        int tileLat = floorI(lat / TILE_SIZE);
        int degLon = tileLon / STEPS_PER_DEGREE;
        int degLat = tileLat / STEPS_PER_DEGREE;
        tileLon %= STEPS_PER_DEGREE;
        tileLat %= STEPS_PER_DEGREE;
        int subLon = floorI(lon) % TILE_SIZE;
        int subLat = floorI(lat) % TILE_SIZE;

        Grid2d grid = Grid2d.of(
                degLon * ARCSECONDS_PER_DEGREE + tileLon * STEPS_PER_DEGREE + subLon,
                degLat * ARCSECONDS_PER_DEGREE + tileLat * STEPS_PER_DEGREE + subLat,
                6, 6
        );*/

        for (int x = 15; x >= 0; x--)   {
            for (int z = 15; z >= 0; z--)   {
                heights[(x << 4) | z] = (short) floorI(this.engine.getInterpolated(lon + x * ARCSECONDS_PER_METER, lat + z * ARCSECONDS_PER_METER, this.grid));
            }
        }
    }
}
