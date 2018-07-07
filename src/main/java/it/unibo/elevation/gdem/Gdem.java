package it.unibo.elevation.gdem;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

/**
 * @deprecated Experimental, currently there are no further developments.
 * Use instead Google or SRTM implementation
 *
 * @author Simone Rondelli - simone.rondelli2@studio.it.unibo.it
 */
@Deprecated
public class Gdem {

	private static final Logger log = Logger.getLogger(Gdem.class.getName());
    private final File localDir;
    private final Map<File, SoftReference<BufferedInputStream>> srtmMap;

	/**
	 * @param localOnly should only local available files be used? true/false
	 */
	public Gdem(boolean localOnly, File localDir) {
		this.localDir = localDir;
        this.srtmMap = new HashMap<File, SoftReference<BufferedInputStream>>();

	}

	private static double getILat(double lat) {
		double dlat = lat - Math.floor(lat);
		double ilat = dlat * 3600;
		return ilat;
	}

	private static double getILon(double lon) {
		double dlon = lon - Math.floor(lon);
		double ilon = dlon * 3600;
		return ilon;
	}

	private short readShort(BufferedInputStream in) throws IOException {
		int ch1 = in.read();
		int ch2 = in.read();
		return (short) ((ch1 << 8) + (ch2));
		// return (short) (ch1 + (ch2 << 8));
	}

    public static void main(String[] args) {
//		double[][] arr = new double[4][30];
//
//		System.out.println(arr.length);
//		System.out.println(arr[0].length);
//		System.out.println(arr[1].length);
//
//		System.exit(0);
//		double[] googleRes = { 1042.387207d, 1198.0783691d, 422.9329834d, 400.6455688d, 173.3656464d, 33.8222237d, 11.0d, 0.9395293d, 0.7247454d, -1.135463d };
//		GeoPoint p1;
//
//		GoogleElevationAPI google = new GoogleElevationAPI();
//
//		File gdemDir = new File("/home/mone/Documenti/Università/Elevation/paparazzi/gdem");
//		File srtmDir = new File("/home/mone/Documenti/Università/Elevation/paparazzi/srtm");
//		Gdem gdem = new Gdem(false, gdemDir);
//		OsmSrtm srtm = new OsmSrtm(false, srtmDir);
//		for (int i = 0; i < 10; i++) {
//			p1 = new GeoPoint(44f + ((float) i / 10), 11f + ((float) i / 10));
//			System.out.println("--------" + p1);
//			double gdemRes = gdem.srtmHeight(p1.getLatitude(), p1.getLongitude());
//			double srtmRes = srtm.srtmHeight(p1.getLatitude(), p1.getLongitude());
//
//			System.out.println("Gdem:       " + gdemRes);
//			System.out.println("Google:     " + googleRes[i]);
//			System.out.println("Srtm:       " + srtmRes);
//			System.out.println("DiffGdem:   " + Math.abs(gdemRes - googleRes[i]));
//			System.out.println("DiffSrtm:   " + Math.abs(srtmRes - googleRes[i]));
//			System.out.println();
//		}
//
//		p1 = new GeoPoint(44.89492, 11.8271);
//		double gdemRes = gdem.srtmHeight(p1.getLatitude(), p1.getLongitude());
//		double srtmRes = srtm.srtmHeight(p1.getLatitude(), p1.getLongitude());
//		double googleResSing = google.setElevation(p1).getElevation();
//
//		System.out.println("Gdem:       " + gdemRes);
//		System.out.println("Google:     " + googleResSing);
//		System.out.println("Srtm:       " + srtmRes);
//		System.out.println("DiffGdem:   " + Math.abs(gdemRes - googleResSing));
//		System.out.println("DiffSrtm:   " + Math.abs(srtmRes - googleResSing));
//		System.out.println();

    }

    private static void copyInputStream(InputStream in, BufferedOutputStream out) throws IOException {
        byte[] buffer = new byte[1024 * 1024];
        int len = in.read(buffer);
        while (len >= 0) {
            out.write(buffer, 0, len);
            len = in.read(buffer);
        }
        in.close();
        out.close();
    }

    private double getValues(File file, int rowmin, int colmin) throws IOException {
        file = new File(this.localDir, file.getName());
        if (!file.exists()) {
            File zipped = new File(this.localDir, file.getName() + ".zip");

            String demFolder = "ASTGTM2_" + file.getName();
            String demFile = "ASTGTM2_" + file.getName() + "_dem.tif";

            ZipFile zipfile = new ZipFile(zipped, ZipFile.OPEN_READ);
            InputStream inp = zipfile.getInputStream(zipfile.getEntry(demFolder + File.separator + demFile));
            BufferedOutputStream outp = new BufferedOutputStream(new FileOutputStream(file), 1024);

            copyInputStream(inp, outp);
            outp.flush();
            zipfile.close();

            log.log(Level.FINE, "Uncompressed zipped SRTM file ''{0}.zip'' to ''{1}''.", new Object[]{zipped.getName(), file.getName()});

        }

        SoftReference<BufferedInputStream> inRef = this.srtmMap.get(file);
        BufferedInputStream in = (inRef != null) ? inRef.get() : null;
        if (in == null) {
            int srtmbuffer = 2 * 3601 * 3601;
            in = new BufferedInputStream(new FileInputStream(file), srtmbuffer);
            this.srtmMap.put(file, new SoftReference<BufferedInputStream>(in));
            in.mark(srtmbuffer);
        }
        in.reset();

        long starti = ((3600 - colmin) * 7202) + (rowmin * 2);
        in.skip(starti);
        short readShort = this.readShort(in);
        return readShort;
    }
}
