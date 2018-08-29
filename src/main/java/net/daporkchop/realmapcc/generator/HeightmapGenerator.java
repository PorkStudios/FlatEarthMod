package net.daporkchop.realmapcc.generator;

import net.daporkchop.lib.db.DBBuilder;
import net.daporkchop.lib.db.DatabaseFormat;
import net.daporkchop.lib.db.PorkDB;
import net.daporkchop.lib.encoding.compression.EnumCompression;
import net.daporkchop.realmapcc.Constants;
import net.daporkchop.realmapcc.data.CompactedHeightData;
import net.daporkchop.realmapcc.util.KeyHasherChunkPos;
import net.daporkchop.realmapcc.util.srtm.SrtmElevationAPI;
import net.daporkchop.realmapcc.util.srtm.SrtmElevationDB;
import net.minecraft.util.math.ChunkPos;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.daporkchop.lib.math.primitive.Floor.floorI;

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

        File root = new File(".", "../mapData/");

        int tiles = Constants.subtileCount;
        int samples = Constants.width;
        int tileSamples = samples / tiles;

        SrtmElevationAPI api = new SrtmElevationAPI(new File(root, "actualData"), samples, false);
        PorkDB<ChunkPos, CompactedHeightData> db = new DBBuilder<ChunkPos, CompactedHeightData>()
                .setForceOpen(true)
                .setMaxOpenFiles(256)
                .setFormat(DatabaseFormat.TREE)
                .setKeyHasher(KeyHasherChunkPos.instance)
                .setValueSerializer(CompactedHeightData.serializer)
                .setRootFolder(new File(root, "worldData"))
                .build();

        if (false) {
            int cpuCores = Runtime.getRuntime().availableProcessors();
            BlockingQueue<ChunkPos> queue = new LinkedBlockingQueue<>(cpuCores);
            {
                for (int i = cpuCores - 1; i >= 0; i--) {
                    int j = i;
                    new Thread(() -> {
                        short[] shrunk = new short[tileSamples * tileSamples];
                        try {
                            System.out.println("Starting thread #" + j);
                            while (cont.get()) {
                                ChunkPos pos = queue.poll(1000L, TimeUnit.MILLISECONDS);
                                if (pos != null) {
                                    short[] heights = api.getElevations(pos.x, pos.z);
                                    if (heights != null) {
                                        System.out.println("(" + pos.x + ',' + pos.z + ')');
                                        for (int x = 0; x < tiles; x++) {
                                            for (int z = 0; z < tiles; z++) {
                                                for (int X = 0; X < tileSamples; X++) {
                                                    for (int Z = 0; Z < tileSamples; Z++) {
                                                        shrunk[X * tileSamples + Z] = heights[(x * tileSamples + X) * samples + z * tileSamples + Z];
                                                    }
                                                }
                                                CompactedHeightData data = CompactedHeightData.getFrom(shrunk, tileSamples);
                                                if (data != null) {
                                                    db.put(new ChunkPos(pos.x * tiles + x, pos.z * tiles + z), data, EnumCompression.NONE);
                                                }
                                                //System.out.printf("Written %d°N, %d°E (tile %d,%d)\n", pos.x, pos.z, x, z);
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
                for (int tileX = -56; cont.get() && tileX < -48; tileX++) {
                    //for (int tileX = -56; cont.get() && tileX < 60; tileX++) {
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

        if (true) {
            int size = 512;
            BufferedImage image = new BufferedImage(size, size--, BufferedImage.TYPE_INT_ARGB);
            SrtmElevationDB apiDb = new SrtmElevationDB(db, samples, true);
            System.out.println("Generating map...");
            for (int x = size; x >= 0; x--) {
                for (int z = size; z >= 0; z--) {
                    //int X = x - 375;
                    //int Z = z - 375;
                    //X = ((X >> 2) << 2) | (Z & 3);
                    //Z = ((Z >> 2) << 2) | ((x - 375) & 3);
                    //image.setRGB(z, x ^ size, db.contains(new ChunkPos(x - 375, z - 375)) ? 0xFFFF5555 : 0xFF000000);
                    //image.setRGB(z, x ^ size, 0xFF000000 | apiDb.getElevation(x * 0.02d - 56.0d, z * 0.02d - 80.0d));
                    image.setRGB(z, x ^ size, 0xFF000000 | floorI(apiDb.getElevation(x * 0.03d - 56.0d, z * 0.03d - 76.0d) / 15.0d));
                    //image.setRGB(z, x ^ size, 0xFF000000 | apiDb.getElevation(-52.4006819,-70.6573522));
                }
            }

            JFrame frame = new JFrame();
            frame.getContentPane().setLayout(new FlowLayout());
            frame.getContentPane().add(new JLabel(new ImageIcon(image)));
            frame.pack();
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
    }
}
