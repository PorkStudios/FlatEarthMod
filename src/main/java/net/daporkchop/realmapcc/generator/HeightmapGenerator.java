package net.daporkchop.realmapcc.generator;

import com.mortennobel.imagescaling.AdvancedResizeOp;
import com.mortennobel.imagescaling.ResampleFilter;
import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.daporkchop.lib.binary.NBitArray;
import net.daporkchop.lib.db.DBBuilder;
import net.daporkchop.lib.db.DatabaseFormat;
import net.daporkchop.lib.db.PorkDB;
import net.daporkchop.realmapcc.Constants;
import net.daporkchop.realmapcc.data.CompactedHeightData;
import net.daporkchop.realmapcc.generator.dataset.srtm.SrtmElevationAPI;
import net.daporkchop.realmapcc.generator.dataset.srtm.SrtmElevationDB;
import net.daporkchop.realmapcc.generator.dataset.surface.cover.BiomeColor;
import net.daporkchop.realmapcc.generator.dataset.surface.cover.GlobCover;
import net.daporkchop.realmapcc.util.ImageUtil;
import net.daporkchop.realmapcc.util.KeyHasherChunkPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import smile.interpolation.BilinearInterpolation;
import smile.interpolation.Interpolation2D;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
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
public class HeightmapGenerator implements Constants {

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

        if (false) {
            int tiles = SRTM_subDegreeCount;
            int samples = SRTM_valuesPerDegree;
            int tileSamples = samples / tiles;

            int cpuCores = Runtime.getRuntime().availableProcessors();
            SrtmElevationAPI api = new SrtmElevationAPI(new File(rootDir, "SRTMGL1"), samples, false);
            PorkDB<ChunkPos, CompactedHeightData> db = new DBBuilder<ChunkPos, CompactedHeightData>()
                    .setForceOpen(true)
                    .setMaxOpenFiles(cpuCores << 5)
                    .setFormat(DatabaseFormat.TREE)
                    .setKeyHasher(KeyHasherChunkPos.instance)
                    .setValueSerializer(CompactedHeightData.serializer)
                    .setRootFolder(new File(rootDir, "worldData"))
                    .build();

            if (cont.get() && true) {
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
                                                        db.put(new ChunkPos(pos.x * tiles + x, pos.z * tiles + z), data);
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

            if (cont.get() && true) {
                int size = 512;
                BufferedImage image = new BufferedImage(size, size--, BufferedImage.TYPE_INT_ARGB);
                SrtmElevationDB apiDb = new SrtmElevationDB(db);
                System.out.println("Generating map...");
                for (int x = size; cont.get() && x >= 0; x--) {
                    for (int z = size; cont.get() && z >= 0; z--) {
                        //int X = x - 375;
                        //int Z = z - 375;
                        //X = ((X >> 2) << 2) | (Z & 3);
                        //Z = ((Z >> 2) << 2) | ((x - 375) & 3);
                        //image.setRGB(z, x ^ size, db.contains(new ChunkPos(x - 375, z - 375)) ? 0xFFFF5555 : 0xFF000000);
                        //image.setRGB(z, x ^ size, 0xFF000000 | apiDb.getElevation(x * 0.02d - 56.0d, z * 0.02d - 80.0d));
                        image.setRGB(z, x ^ size, 0xFF000000 | apiDb.getElevation(x * 0.03d - 56.0d, z * 0.03d - 76.0d));
                        //image.setRGB(z, x ^ size, 0xFF000000 | apiDb.getElevation(-52.4006819,-70.6573522));
                    }
                    System.out.printf("Row %d/%d\n", size - x, size + 1);
                }

                JFrame frame = new JFrame();
                frame.getContentPane().setLayout(new FlowLayout());
                frame.getContentPane().add(new JLabel(new ImageIcon(image)));
                frame.pack();
                frame.setVisible(true);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
        }
        if (false) {
            int origSize = 100;
            int sizeMult = 10;
            GlobCover globCover = GlobCover.INSTANCE;
            NBitArray biomes = globCover.getDataAtDegree(-90, 50);
            BufferedImage reference = new BufferedImage(origSize, origSize, BufferedImage.TYPE_INT_RGB);
            for (int x = origSize - 1; x >= 0; x--) {
                for (int y = origSize - 1; y >= 0; y--) {
                    reference.setRGB(x, y, BiomeColor.values()[biomes.get(x * GLOBCOVER_valuesPerDegree + y)].color);
                }
            }

            if (false) {
                JFrame frame = new JFrame();
                frame.getContentPane().setLayout(new FlowLayout());
                frame.getContentPane().add(new JLabel(new ImageIcon(reference)));
                frame.pack();
                frame.setVisible(true);
                frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                while (frame.isVisible()) {
                    Thread.sleep(500L);
                }
            }

            /*BufferedImage target = new BufferedImage(origSize * sizeMult, origSize * sizeMult, BufferedImage.TYPE_INT_RGB);
            double[] xS = new double[origSize];
            double[] yS = new double[origSize];
            for (int i = origSize - 1; i >= 0; i--) {
                xS[i] = yS[i] = i * sizeMult;
            }
            double[][] rIn = new double[origSize][origSize];
            double[][] gIn = new double[origSize][origSize];
            double[][] bIn = new double[origSize][origSize];
            for (int x = origSize - 1; x >= 0; x--) {
                for (int y = origSize - 1; y >= 0; y--) {
                    Color color = new Color(reference.getRGB(x, y));
                    rIn[x][y] = color.getRed();
                    gIn[x][y] = color.getGreen();
                    bIn[x][y] = color.getBlue();
                }
            }

            BivariateFunction functionR = interpolatorBicubic.interpolate(xS, yS, rIn);
            BivariateFunction functionG = interpolatorBicubic.interpolate(xS, yS, gIn);
            BivariateFunction functionB = interpolatorBicubic.interpolate(xS, yS, bIn);
            for (int x = target.getWidth() - 1 - sizeMult; x >= 0; x--) {
                for (int y = target.getHeight() - 1 - sizeMult; y >= 0; y--) {
                    target.setRGB(x, y, new Color(
                            MathHelper.clamp(floorI(functionR.value(x, y)), 0, 255),
                            MathHelper.clamp(floorI(functionG.value(x, y)), 0, 255),
                            MathHelper.clamp(floorI(functionB.value(x, y)), 0, 255)
                    ).getRGB());
                }
            }*/
            ResampleFilter[] filters = {
                    ResampleFilters.getBellFilter(),
                    ResampleFilters.getBiCubicFilter(),
                    ResampleFilters.getBiCubicHighFreqResponse(),
                    ResampleFilters.getBoxFilter(),
                    ResampleFilters.getBSplineFilter(),
                    ResampleFilters.getHermiteFilter(),
                    ResampleFilters.getLanczos3Filter(),
                    ResampleFilters.getMitchellFilter(),
                    ResampleFilters.getTriangleFilter()
            };
            filters = new ResampleFilter[]{
                    ResampleFilters.getBoxFilter()
            };
            BufferedImage done = new BufferedImage(origSize * sizeMult * filters.length, origSize * sizeMult, BufferedImage.TYPE_INT_RGB);
            ResampleOp resampler = new ResampleOp(origSize * sizeMult, origSize * sizeMult);
            for (int i = 0; i < filters.length; i++) {
                ResampleFilter filter = filters[i];
                System.out.println(filter.getName());
                resampler.setFilter(filter);
                BufferedImage target = resampler.filter(reference, null);

                for (int x = target.getWidth() - 1; x >= 0; x--) {
                    for (int y = target.getHeight() - 1; y >= 0; y--) {
                        int col = target.getRGB(x, y);
                        done.setRGB(origSize * sizeMult * i + x, y, col);
                    }
                }

                if (false) {
                    JFrame frame = new JFrame();
                    frame.getContentPane().setLayout(new FlowLayout());
                    frame.getContentPane().add(new JLabel(new ImageIcon(target)));
                    frame.pack();
                    frame.setVisible(true);
                    frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                    while (frame.isVisible()) {
                        Thread.sleep(500L);
                    }
                }
            }
            System.out.println("done");
            ImageUtil.writeImage(done, new File(rootDir, "tmp.png"));
        }
        if (true) {
            int origSize = 100;
            int sizeMult = 10;
            GlobCover globCover = GlobCover.INSTANCE;
            NBitArray biomes = globCover.getDataAtDegree(-90, 50);
            BufferedImage reference = new BufferedImage(origSize, origSize, BufferedImage.TYPE_INT_RGB);
            IntList occuringIDs = new IntArrayList();
            for (int x = origSize - 1; x >= 0; x--) {
                for (int y = origSize - 1; y >= 0; y--) {
                    if (false) {
                        reference.setRGB(x, y, BiomeColor.values()[biomes.get(x * GLOBCOVER_valuesPerDegree + y)].color);
                    } else {
                        if (true) {
                            int col = biomes.get(x * GLOBCOVER_valuesPerDegree + y);
                            if (!occuringIDs.contains(col)) {
                                occuringIDs.add(col);
                            }
                            reference.setRGB(x, y, (col << 16) | (col << 8) | col);
                        } else {
                            int col = biomes.get(x * GLOBCOVER_valuesPerDegree + y) * 10;
                            reference.setRGB(x, y, (col << 16) | (col << 8) | col);
                        }
                    }
                }
            }

            BufferedImage target;

            if (true) {
                {
                    ResampleOp resampler = new ResampleOp(origSize * sizeMult, origSize * sizeMult);
                    resampler.setFilter(ResampleFilters.getBSplineFilter());
                    resampler.setNumberOfThreads(Runtime.getRuntime().availableProcessors());
                    BufferedImage bufferIn = new BufferedImage(origSize, origSize, BufferedImage.TYPE_INT_RGB);
                    BufferedImage bufferOut = null;
                    target = new BufferedImage(origSize * sizeMult, origSize * sizeMult, BufferedImage.TYPE_INT_RGB);
                    for (int id : occuringIDs) {
                        for (int x = origSize - 1; x >= 0; x--) {
                            for (int y = origSize - 1; y >= 0; y--) {
                                int col = 0;
                                if ((reference.getRGB(x, y) & 0xFF) == id) {
                                    col = 0xFF;
                                }
                                bufferIn.setRGB(x, y, col);
                            }
                        }

                        bufferOut = resampler.filter(bufferIn, bufferOut);

                        int col = BiomeColor.values()[id].color;
                        for (int x = origSize * sizeMult - 1; x >= 0; x--) {
                            for (int y = origSize * sizeMult - 1; y >= 0; y--) {
                                if ((bufferOut.getRGB(x, y) & 0xFF) >= 128) {
                                    target.setRGB(x, y, col);
                                }
                            }
                        }
                    }
                }
                System.gc();
                {
                    BufferedImage newTarget = deepCopy(target);
                    for (int x = origSize * sizeMult - 1; x >= 0; x--) {
                        for (int y = origSize * sizeMult - 1; y >= 0; y--) {
                            int col = target.getRGB(x, y);
                            if ((col & 0xFFFFFF) == 0) {
                                int X = sizeMult << 1;
                                int Y = sizeMult << 1;
                                int xx = 0, yy = 0, dx = 0, dy = -1;
                                int t = Math.max(X, Y);
                                int maxI = t * t;

                                A:
                                for (int i = 0; i < maxI; i++) {
                                    if ((-X / 2 <= xx) && (xx <= X / 2) && (-Y / 2 <= yy) && (yy <= Y / 2)) {
                                        col = target.getRGB(
                                                MathHelper.clamp(x + xx, 0, origSize * sizeMult - 1),
                                                MathHelper.clamp(y + yy, 0, origSize * sizeMult - 1)
                                        );
                                        if ((col & 0xFFFFFF) != 0) {
                                            newTarget.setRGB(x, y, col);
                                            break A;
                                        }
                                    }

                                    if ((xx == yy) || ((xx < 0) && (xx == -yy)) || ((xx > 0) && (xx == 1 - yy))) {
                                        t = dx;
                                        dx = -dy;
                                        dy = t;
                                    }
                                    xx += dx;
                                    yy += dy;
                                }
                            }
                        }
                    }
                    target = newTarget;
                    System.gc();
                }
            }

            if (false) {
                JFrame frame = new JFrame();
                frame.getContentPane().setLayout(new FlowLayout());
                frame.getContentPane().add(new JLabel(new ImageIcon(target)));
                frame.pack();
                frame.setVisible(true);
                frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                while (frame.isVisible()) {
                    Thread.sleep(500L);
                }
            }
            if (true) {
                File file = new File(rootDir, "stage2-1.png");
                if (file.exists()) {
                    file.delete();
                }
                ImageUtil.writeImage(target, file);
            }
        }

        System.exit(0);
    }

    static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public static void tryConvert(ChunkPos pos, PorkDB<ChunkPos, CompactedHeightData> db) {
        int tiles = SRTM_subDegreeCount;
        int samples = SRTM_valuesPerDegree;
        int tileSamples = samples / tiles;

        SrtmElevationAPI api = new SrtmElevationAPI(new File(rootDir, "SRTMGL1"), samples, false);

        short[] shrunk = new short[tileSamples * tileSamples];
        try {
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
                                db.put(new ChunkPos(pos.x * tiles + x, pos.z * tiles + z), data);
                            }
                            //System.out.printf("Written %d°N, %d°E (tile %d,%d)\n", pos.x, pos.z, x, z);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException(t);
        }
    }
}
