package net.daporkchop.realmapcc.data.client;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.math.arrays.grid.Grid2d;
import net.daporkchop.lib.math.interpolation.InterpolationEngine;
import net.daporkchop.realmapcc.Constants;
import net.daporkchop.realmapcc.RealmapCC;
import net.daporkchop.realmapcc.data.Tile;
import net.daporkchop.realmapcc.data.client.grid.DelegatingTileCacheGrid;
import net.daporkchop.realmapcc.data.client.lookup.CachedTileLookup;
import net.daporkchop.realmapcc.data.client.lookup.DiskTileCache;
import net.daporkchop.realmapcc.data.client.lookup.RepoTileLookup;
import net.daporkchop.realmapcc.data.client.lookup.TileLookup;
import net.daporkchop.realmapcc.util.TerrainDataConsumer;

import java.io.File;

import static net.daporkchop.lib.math.primitive.PMath.floorI;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
@Getter
public class DataProcessor implements Constants {
    protected final TileLookup tileLookup = new CachedTileLookup().setDelegate(new DiskTileCache(new File(RealmapCC.Conf.dataCacheDir)).setDelegate(new RepoTileLookup()));

    @NonNull
    protected final InterpolationEngine engine;
    protected final LookupGrid2d heightGrid = new LookupGrid2d(this.tileLookup, Tile::getHeight);
    protected final LookupGrid2d waterGrid = new LookupGrid2d(this.tileLookup, Tile::getIsWater);
    protected final LookupGrid2d biomeGrid = new LookupGrid2d(this.tileLookup, Tile::getBiome);

    public DataProcessor()  {
        this(ENGINE_LINEAR);
    }

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
                heights[(x << 4) | z] = (short) floorI(this.engine.getInterpolated(lon + x * ARCSECONDS_PER_METER, lat + z * ARCSECONDS_PER_METER, this.heightGrid));
            }
        }
    }

    public LookupCache forEachValueInRange(double lon, double lat, int xSteps, int ySteps, double step, @NonNull TerrainDataConsumer consumer, LookupCache cache)    {
        {
            int arcSecondsLon = floorI(lon * ARCSECONDS_PER_DEGREE);
            int arcSecondsLat = floorI(lat * ARCSECONDS_PER_DEGREE);
            int w = floorI(xSteps * step * ARCSECONDS_PER_DEGREE);
            int h = floorI(ySteps * step * ARCSECONDS_PER_DEGREE);

            int pad = this.engine.requiredRadius();

            if (cache == null || cache.w != w + 2 * pad - 1 || cache.h != h + 2 * pad - 1) {
                cache = new LookupCache(
                        arcSecondsLon - pad - 1,
                        arcSecondsLat - pad - 1,
                        w + 2 * pad - 1,
                        h + 2 * pad - 1
                );
            } else {
                cache.setLon(arcSecondsLon).setLat(arcSecondsLat);
            }
        }

        cache.load(this.tileLookup);

        Grid2d height = new DelegatingTileCacheGrid(cache, LookupCache::getHeight);
        Grid2d water = new DelegatingTileCacheGrid(cache, LookupCache::getWaterPresence);

        for (int x = xSteps - 1; x >= 0; x--)   {
            for (int y = ySteps - 1; y >= 0; y--)   {
                double valLon = lon + x * step;
                double valLat = lat + y * step;

                consumer.accept(
                        x,
                        y,
                        this.engine.getInterpolatedI(valLon, valLat, height),
                        this.engine.getInterpolated(valLon, valLat, water) > 0.5d,
                        -1
                );
            }
        }

        return cache;
    }
}
