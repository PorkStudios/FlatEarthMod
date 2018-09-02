package net.daporkchop.realmapcc.generator.dataset.srtm;

import net.daporkchop.lib.db.PorkDB;
import net.daporkchop.realmapcc.data.CompactedHeightData;
import net.minecraft.util.math.ChunkPos;

@Deprecated
public class SrtmElevationDB {

    private final SrtmHelperDB osmSrtm;

    public SrtmElevationDB(PorkDB<ChunkPos, CompactedHeightData> db) {
        this.osmSrtm = new SrtmHelperDB(db);
    }

    public int getElevation(double lat, double lon) {
        return this.osmSrtm.getElevation(lat, lon);
    }

    public SrtmHelperDB getHelper() {
        return this.osmSrtm;
    }
}
