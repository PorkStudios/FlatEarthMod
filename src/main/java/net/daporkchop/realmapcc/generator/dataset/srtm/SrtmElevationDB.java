package net.daporkchop.realmapcc.generator.dataset.srtm;

import net.daporkchop.lib.db.PorkDB;
import net.daporkchop.realmapcc.data.CompactedHeightData;
import net.minecraft.util.math.ChunkPos;

public class SrtmElevationDB {

    private final SrtmHelperDB osmSrtm;

    public SrtmElevationDB(PorkDB<ChunkPos, CompactedHeightData> db, int samplesPerFile, boolean interpolate) {
        this.osmSrtm = new SrtmHelperDB(db, samplesPerFile, interpolate);
    }

    public int getElevation(double lat, double lon) {
        return this.osmSrtm.srtmHeight(lat, lon);
    }

    public SrtmHelperDB getHelper() {
        return this.osmSrtm;
    }
}
