package it.unibo.elevation.srtm;

import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Hashtable;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class SrtmHelper {

    private final File localDir;
    private final Map<File, RandomAccessFile> srtmMap;
    private final Int2BooleanOpenHashMap exists = new Int2BooleanOpenHashMap();
    private final Int2BooleanOpenHashMap zip_exists = new Int2BooleanOpenHashMap();
    /**
     * SRTM1: 3600
     * SRTM3: 1200
     */
    private final int samplesPerFile;

    public SrtmHelper(File localDir, int samplesPerFile) {
        this.localDir = localDir;
        this.srtmMap = new Hashtable<>();
        this.samplesPerFile = samplesPerFile;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Closing data files...");
            SrtmHelper.this.srtmMap.forEach((file, raf) -> {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            System.out.println("Done!");
        }));
    }

    private static double getILat(double lat) {
        double dlat = lat - Math.floor(lat);
        double ilat = dlat * 1200;
        return ilat;
    }

    private static double getILon(double lon) {
        double dlon = lon - Math.floor(lon);
        double ilon = dlon * 1200;
        return ilon;
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

    /**
     * Determine the filename of the srtm file corresponding to the lat and lon coordinates of the
     * actual node
     *
     * @param lat latitue
     * @param lon longitude
     * @return srtm height or Double.NaN if something is wrong
     * @throws IOException
     */
    public double srtmHeight(double lat, double lon) throws IOException {
        double val;
        String srtmFileName = SrtmUtil.getSrtmFileName(lat, lon);

        File file = new File(srtmFileName + ".hgt");

        double ilat = getILat(lat);
        double ilon = getILon(lon);
        int rowmin = (int) Math.floor(ilon);
        int colmin = (int) Math.floor(ilat);
        double[] values = new double[4];
        values[0] = this.getValues(file, rowmin, colmin);
        values[1] = this.getValues(file, rowmin + 1, colmin);
        values[2] = this.getValues(file, rowmin, colmin + 1);
        values[3] = this.getValues(file, rowmin + 1, colmin + 1);
        double coefrowmin = (rowmin + 1) - ilon;
        double coefcolmin = (colmin + 1) - ilat;
        double val1 = (values[0] * coefrowmin) + (values[1] * (1 - coefrowmin));
        double val2 = (values[2] * coefrowmin) + (values[3] * (1 - coefrowmin));
        val = (val1 * coefcolmin) + (val2 * (1 - coefcolmin));

        return val;
    }

    private short readShort(BufferedInputStream in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        return (short) ((ch1 << 8) + (ch2));
    }

    private double getValues(File fin, int rowmin, int colmin) throws IOException {
        File file = new File(this.localDir, fin.getName());
        if (!this.exists.computeIfAbsent(file.getName().hashCode(), i -> file.exists())) {
            File zipped = new File(this.localDir, file.getName() + ".zip");

            if (!this.zip_exists.computeIfAbsent(file.getName().hashCode(), i -> zipped.exists())) {
                //System.out.println(zipped.getAbsolutePath() + " not found!");
                return 0.0d;
                //throw new FileNotFoundException(zipped.getAbsolutePath() + " not found!");
            }

            ZipFile zipfile = new ZipFile(zipped, ZipFile.OPEN_READ);
            System.out.println(file.getName());
            ZipEntry entry = zipfile.getEntry(file.getName());
            if (entry == null) {
                entry = zipfile.getEntry(file.getName().toLowerCase());
            }
            InputStream inp = zipfile.getInputStream(entry);
            BufferedOutputStream outp = new BufferedOutputStream(new FileOutputStream(file), 1024);

            copyInputStream(inp, outp);
            outp.flush();
            zipfile.close();
            zipped.deleteOnExit();
            this.exists.put(file.getName().hashCode(), true);
        }

        RandomAccessFile raf = this.srtmMap.computeIfAbsent(file, a -> {
            try {
                return new RandomAccessFile(file, "r");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Runtime.getRuntime().exit(1);
                return null;
            }
        });

        raf.seek(((this.samplesPerFile - colmin) * (this.samplesPerFile * 2 + 2L)) + (rowmin * 2L));
        return raf.readShort();
    }
}
