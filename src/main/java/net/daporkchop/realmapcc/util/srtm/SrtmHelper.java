package net.daporkchop.realmapcc.util.srtm;

import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.daporkchop.lib.primitive.lambda.function.ObjectToBooleanFunction;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class SrtmHelper {

    private final File localDir;
    private final Map<File, RandomAccessFile> srtmMap;
    private final Int2BooleanOpenHashMap exists = new Int2BooleanOpenHashMap();
    private final Int2BooleanOpenHashMap zip_exists = new Int2BooleanOpenHashMap();
    private final Long2DoubleOpenHashMap cachedVals = new Long2DoubleOpenHashMap();
    /**
     * SRTM1: 3600
     * SRTM3: 1200
     */
    private final int samplesPerFile;
    private final boolean interpolate;
    private final ObjectToBooleanFunction<File> existsFunc = File::exists;
    private final ObjectToBooleanFunction<File> zip_existsFunc;

    public SrtmHelper(File localDir, int samplesPerFile, boolean interpolate) {
        this.localDir = localDir;
        this.srtmMap = new Object2ObjectOpenHashMap<>();
        this.samplesPerFile = samplesPerFile;
        this.interpolate = interpolate;
        this.zip_existsFunc = f -> new File(this.localDir, f.getName() + ".zip").exists();

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

    private double getILat(double lat) {
        double dlat = lat - Math.floor(lat);
        double ilat = dlat * this.samplesPerFile;
        return ilat;
    }

    private double getILon(double lon) {
        double dlon = lon - Math.floor(lon);
        double ilon = dlon * this.samplesPerFile;
        return ilon;
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
        if (this.interpolate) {
            File file = SrtmUtil.getSrtmFileName(lat, lon, this.localDir);

            double ilat = this.getILat(lat);
            double ilon = this.getILon(lon);
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

            return (val1 * coefcolmin) + (val2 * (1 - coefcolmin));
        } else {
            File file = SrtmUtil.getSrtmFileName(lat, lon, this.localDir);

            double ilat = this.getILat(lat);
            double ilon = this.getILon(lon);
            int rowmin = (int) Math.floor(ilon);
            int colmin = (int) Math.floor(ilat);

            return this.getValues(file, rowmin, colmin);
        }
    }

    public void flushCache() {
        this.cachedVals.clear();
    }

    private short readShort(BufferedInputStream in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        return (short) ((ch1 << 8) + (ch2));
    }

    private double getValues(File fin, int rowmin, int colmin) throws IOException {
        if (this.interpolate) {
            return this.cachedVals.computeIfAbsent(((rowmin & 0xFFFFFFFFL) << 32L) | (colmin & 0xFFFFFFFFL),
                    x -> {
                        try {
                            return this.getValuesReal(fin, rowmin, colmin);
                        } catch (IOException e) {
                            e.printStackTrace();
                            return -1.0d;
                        }
                    });
        } else {
            return this.getValuesReal(fin, rowmin, colmin);
        }
    }

    private double getValuesReal(File file, int rowmin, int colmin) throws IOException {
        int fH = file.hashCode();
        if (!this.computeIfAbsent(this.exists, fH, file, this.existsFunc)) {
            if (!this.computeIfAbsent(this.zip_exists, fH, file, this.zip_existsFunc)) {
                return Short.MIN_VALUE;
            }

            File zipped = new File(this.localDir, file.getName() + ".zip");

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
            this.exists.put(fH, true);
        }

        RandomAccessFile raf = this.srtmMap.get(file);
        if (raf == null) {
            this.srtmMap.put(file, raf = new RandomAccessFile(file, "r"));
        }

        raf.seek(((this.samplesPerFile - colmin) * (this.samplesPerFile * 2 + 2L)) + (rowmin * 2L));
        return raf.readShort();
    }

    private boolean computeIfAbsent(Int2BooleanMap map, int key, File file, ObjectToBooleanFunction<File> func) {
        if (map.containsKey(key)) {
            return map.get(key);
        } else {
            boolean val = func.apply(file);
            map.put(key, val);
            return val;
        }
    }
}
