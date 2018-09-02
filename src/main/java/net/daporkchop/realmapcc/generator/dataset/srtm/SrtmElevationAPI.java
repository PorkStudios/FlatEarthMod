package net.daporkchop.realmapcc.generator.dataset.srtm;

import java.io.File;
import java.io.IOException;

@Deprecated
public class SrtmElevationAPI {

    private final SrtmHelper osmSrtm;

    /**
     * Init the SRTM based ElevationApi
     *
     * @param localDir The local folder that contains the .hgt or .zip srtm files
     */
    public SrtmElevationAPI(File localDir, int samplesPerFile, boolean interpolate) {
        this.osmSrtm = new SrtmHelper(localDir, samplesPerFile, interpolate);
    }

    public double getElevation(double lat, double lon) throws IOException {
        return this.osmSrtm.getElevation(lat, lon);
    }

    public short[] getElevations(int lat, int lon) throws IOException {
        return this.osmSrtm.getAllAt(lat, lon);
    }

    public SrtmHelper getHelper() {
        return this.osmSrtm;
    }
}
