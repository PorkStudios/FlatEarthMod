package net.daporkchop.realmapcc.data.client.lookup;

import lombok.NonNull;
import net.daporkchop.realmapcc.Constants;
import net.daporkchop.realmapcc.data.Tile;
import net.daporkchop.realmapcc.data.TilePos;

import static net.daporkchop.lib.math.primitive.PMath.floorI;

/**
 * @author DaPorkchop_
 */
public interface TileLookup extends Constants {
    /**
     * Fetches the tile at the given location. If not present on the local system, the request will be passed on (either to a delegate
     * instance of {@link TileLookup}, or e.g. a remote service on the network).
     *
     * @param pos the position of the tile to fetch
     * @return the tile at that position
     */
    Tile getTile(@NonNull TilePos pos);

    /**
     * @see #getTile(TilePos)
     */
    default Tile getTile(int degLon, int degLat, int tileLon, int tileLat) {
        return this.getTile(new TilePos(degLon, degLat, tileLon, tileLat));
    }

    /**
     * @param lon the real-life longitude value
     * @param lat the real-life latitude value
     * @see #getTile(TilePos)
     */
    default Tile getTile(double lon, double lat) {
        int floorLon = floorI(lon);
        int floorLat = floorI(lat);
        int tileLon = floorI((lon - floorLon) * STEPS_PER_DEGREE);
        int tileLat = floorI((lat - floorLat) * STEPS_PER_DEGREE);
        return this.getTile(floorLon, floorLat, tileLon, tileLat);
    }
}
