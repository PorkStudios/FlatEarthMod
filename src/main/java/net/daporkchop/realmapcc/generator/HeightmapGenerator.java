package net.daporkchop.realmapcc.generator;

import net.daporkchop.lib.binary.stream.StreamUtil;
import net.daporkchop.lib.db.DBBuilder;
import net.daporkchop.lib.db.DatabaseFormat;
import net.daporkchop.lib.db.PorkDB;
import net.daporkchop.lib.encoding.compression.EnumCompression;
import net.daporkchop.realmapcc.Constants;
import net.daporkchop.realmapcc.data.CompactedHeightData;
import net.daporkchop.realmapcc.util.KeyHasherChunkPos;
import net.daporkchop.realmapcc.util.srtm.SrtmElevationAPI;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author DaPorkchop_
 */
public class HeightmapGenerator {
    public static void main(String... args) throws Exception {
        AtomicBoolean cont = new AtomicBoolean(true);

        {
            Thread t = Thread.currentThread();
            new Thread(() -> {
                Scanner s = new Scanner(System.in);
                s.nextLine();
                s.close();
                cont.set(false);
                t.interrupt();
            }).start();
        }

        File root = new File("/media/daporkchop/TooMuchStuff/PortableIDE/RealWorldCC/mapData/actualData/");

        if (false) {
            //debug: extract all zips

            AtomicLong totalSize = new AtomicLong(0L);
            AtomicInteger totalCount = new AtomicInteger(0);
            AtomicLong compressedSize = new AtomicLong(0L);
            File[] list = root.listFiles();
            long startTime = System.currentTimeMillis();
            for (int i = list.length - 1; cont.get() && i >= 0; i--) {
                File file = list[i];
                if (file.getName().endsWith(".zip")) {
                    File target = new File(root, file.getName().replace(".zip", ""));
                    if (target.exists()) {
                        System.out.println("Skipping " + file.getName());
                        continue;
                    }
                    ZipFile zipFile = new ZipFile(file, ZipFile.OPEN_READ);
                    ZipEntry entry = zipFile.getEntry(file.getName().replace(".zip", ""));
                    if (entry == null) {
                        entry = zipFile.getEntry(file.getName().toLowerCase());
                    }
                    if (entry == null) {
                        entry = zipFile.entries().nextElement();
                    }
                    InputStream zin = zipFile.getInputStream(entry);
                    System.out.println("Extracting " + file.getName() + " to " + target.getName() + "... (" + (list.length - i) + "/" + list.length + ")");

                    byte[] buf = new byte[(int) entry.getSize()];
                    StreamUtil.read(zin, buf, 0, buf.length);
                    zin.close();

                    target.createNewFile();
                    FileOutputStream fos = new FileOutputStream(target);
                    fos.write(buf);
                    fos.close();

                    totalSize.addAndGet(entry.getSize());
                    compressedSize.addAndGet(entry.getCompressedSize());
                    totalCount.incrementAndGet();

                    //file.deleteOnExit();
                }
            }
            startTime = System.currentTimeMillis() - startTime;
            System.out.println("Processed " + totalCount.get() + " files in " + startTime + "ms (" + (startTime / 1000d / 60d / 60d) + " hrs)");
            System.out.println("Written: " + totalSize.get());
            System.out.println("Read (compressed): " + compressedSize.get());
            return;
        }

        int tiles = Constants.subtileCount;
        int samples = Constants.width;
        int tileSamples = samples / tiles;
        double sampleStep = 1.0d / (double) samples;

        SrtmElevationAPI api = new SrtmElevationAPI(root, samples, false);
        PorkDB<ChunkPos, CompactedHeightData> db = new DBBuilder<ChunkPos, CompactedHeightData>()
                .setCompression(EnumCompression.XZIP)
                .setForceOpen(true)
                .setFormat(DatabaseFormat.TAR_TREE)
                .setKeyHasher(new KeyHasherChunkPos())
                .setValueSerializer(new CompactedHeightData.Serializer())
                .setRootFolder(new File("/media/daporkchop/TooMuchStuff/PortableIDE/RealWorldCC/mapData/worldData"))
                .build();

        {
            System.out.println("Wiping existing database...");
            Set<ChunkPos> toRemove = new HashSet<>();
            db.forEach((k, v) -> toRemove.add(k));
            toRemove.forEach(db::remove);
            System.out.println("Done!");
        }

        if (false) {
            short[] heights = new short[samples * samples];
            for (int tileX = -56; cont.get() && tileX < 60; tileX++) {
                for (int tileZ = -180; cont.get() && tileZ < 180; tileZ++) {
                    System.out.println("Processing tile at " + tileX + ',' + tileZ);
                    for (int x = 0; x < samples; x++) {
                        for (int z = 0; z < samples; z++) {
                            heights[x * samples + z] = (short) MathHelper.clamp(api.getElevation(
                                    tileX + x * sampleStep,
                                    tileZ + z * sampleStep), -1.0d, Short.MAX_VALUE);
                        }
                    }
                    api.getHelper().flushCache();

                    CompactedHeightData data = CompactedHeightData.getFrom(heights, samples);
                    if (data != null) {
                        db.put(new ChunkPos(tileX, tileZ), data);
                    }
                }
            }
        }

        BlockingQueue<ChunkPos> queue = new LinkedBlockingQueue<>(4);
        {
            for (int i = 0; i < 4; i++) {
                new Thread(() -> {
                    short[] shrunk = new short[tileSamples * tileSamples];
                    try {
                        while (cont.get()) {
                            ChunkPos pos = queue.poll(25L, TimeUnit.MILLISECONDS);
                            if (pos != null) {
                                short[] heights = api.getElevations(pos.x, pos.z);
                                if (heights != null) {
                                    System.out.println("(" + pos.x + ',' + pos.z + ')');
                                    for (int x = 0; x < tiles; x++) {
                                        for (int z = 0; z < tiles; z++) {
                                            for (int X = 0; X < tileSamples; X++) {
                                                for (int Z = 0; Z < tileSamples; Z++) {
                                                    shrunk[X * tileSamples + Z] = heights[(x * tileSamples + X) * samples + z * tileSamples + z];
                                                }
                                            }
                                            CompactedHeightData data = CompactedHeightData.getFrom(shrunk, tileSamples);
                                            if (data != null) {
                                                db.put(new ChunkPos(pos.x * tiles + x, pos.z * tiles + z), data);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
        try {
            //for (int tileX = -56; cont.get() && tileX < -48; tileX++) {
            for (int tileX = -56; cont.get() && tileX < 60; tileX++) {
                for (int tileZ = -180; cont.get() && tileZ < 180; tileZ++) {
                    queue.put(new ChunkPos(tileX, tileZ));
                }
            }
        } catch (InterruptedException e) {
        }

        cont.set(false);
        System.out.println("Complete!");
        Runtime.getRuntime().addShutdownHook(new Thread(db::shutdown));
    }
}
