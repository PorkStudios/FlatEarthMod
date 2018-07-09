package net.daporkchop.realmapcc.util.srtm;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.io.File;
import java.text.NumberFormat;

/**
 * @author Simone Rondelli - simone.rondelli2@studio.it.unibo.it
 */
public class SrtmUtil {
    public static final String EURASIA = "Eurasia";
    public static final String AFRICA = "Africa";
    public static final String ISLANDS = "Islands";
    public static final String NORTH_AMERICA = "North_America";
    public static final String SOUTH_AMERICA = "South_America";
    private static final Long2ObjectMap<File> files = new Long2ObjectOpenHashMap<>();
    public static String SRTM3_URL = "http://dds.cr.usgs.gov/srtm/version2_1/SRTM3/";

    /**
     * Return the SRTM file name without the extension
     *
     * @param lat Latitude
     * @param lon Longitude
     * @return SRTM filename
     */
    public static File getSrtmFileName(double lat, double lon, File localDir) {
        long l = ((((int) lat) & 0xFFFFFFFFL) << 32L) | (((int) lon) & 0xFFFFFFFFL);
        if (files.containsKey(l)) {
            return files.get(l);
        } else {
            File file;
            {
                int nlat = Math.abs((int) Math.floor(lat));
                int nlon = Math.abs((int) Math.floor(lon));

                NumberFormat nf = NumberFormat.getInstance();
                String NS, WE;
                String f_nlat, f_nlon;

                if (lat > 0) {
                    NS = "N";
                } else {
                    NS = "S";
                }
                if (lon > 0) {
                    WE = "E";
                } else {
                    WE = "W";
                }

                nf.setMinimumIntegerDigits(2);
                f_nlat = nf.format(nlat);
                nf.setMinimumIntegerDigits(3);
                f_nlon = nf.format(nlon);

                file = new File(localDir, NS + f_nlat + WE + f_nlon + ".hgt");
            }
            files.put(l, file);
            return file;
        }
    }

}
