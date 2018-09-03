package net.daporkchop.realmapcc.generator;

import com.mortennobel.imagescaling.ResampleFilter;
import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;
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
import smile.interpolation.BilinearInterpolation;
import smile.interpolation.Interpolation2D;

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
            for (int x = origSize - 1; x >= 0; x--) {
                for (int y = origSize - 1; y >= 0; y--) {
                    if (false) {
                        reference.setRGB(x, y, BiomeColor.values()[biomes.get(x * GLOBCOVER_valuesPerDegree + y)].color);
                    } else {
                        if (false) {
                            int col = biomes.get(x * GLOBCOVER_valuesPerDegree + y);
                            reference.setRGB(x, y, (col << 16) | (col << 8) | col);
                        } else {
                            int col = biomes.get(x * GLOBCOVER_valuesPerDegree + y) * 10;
                            reference.setRGB(x, y, (col << 16) | (col << 8) | col);
                        }
                    }
                }
            }

            /*ResampleOp resampler = new ResampleOp(origSize, origSize * sizeMult);
            resampler.setFilter(ResampleFilters.getBSplineFilter());
            BufferedImage target = resampler.filter(reference, null);
            for (int x = origSize - 1; x >= 0; x--) {
                for (int y = origSize - 2; y >= 0; y--) {
                    int colA = reference.getRGB(x, y);
                    int colB = reference.getRGB(x, y + 1);
                    double diff = colA - colB;
                    for (int yy = sizeMult - 1; yy >= 0; yy--)   {
                        int col = target.getRGB(x, y * sizeMult + yy);
                        double factor = Math.round((double) (col - colB) / diff);
                        if (factor == 0.0d) {
                            target.setRGB(x, y * sizeMult + yy, colB);
                        } else if (factor == 1.0d)  {
                            target.setRGB(x, y * sizeMult + yy, colA);
                        } else {
                            throw new IllegalStateException(String.valueOf(factor));
                        }
                    }
                }
            }*/

            /*ResampleOp resampler = new ResampleOp(origSize, origSize * sizeMult);
            resampler.setFilter(ResampleFilters.getBSplineFilter());
            BufferedImage target = resampler.filter(reference, null);

            if (true) {
                for (int x = origSize - 1; x >= 0; x--) {
                    for (int y = origSize - 2; y >= 0; y--) {
                        int colA = reference.getRGB(x, y);
                        int colB = reference.getRGB(x, y + 1);
                        int rA = (colA >> 16) & 0xFF;
                        int gA = (colA >> 8) & 0xFF;
                        int bA = colA & 0xFF;
                        int rB = (colB >> 16) & 0xFF;
                        int gB = (colB >> 8) & 0xFF;
                        int bB = colB & 0xFF;
                        {
                            rA = floorI(rA * 0.1d) * 10;
                            gA = floorI(gA * 0.1d) * 10;
                            bA = floorI(bA * 0.1d) * 10;
                            rB = floorI(rB * 0.1d) * 10;
                            gB = floorI(gB * 0.1d) * 10;
                            bB = floorI(bB * 0.1d) * 10;
                        }
                        double diffR = rA - rB;
                        double diffG = gA - gB;
                        double diffB = bA - bB;
                        for (int yy = sizeMult - 1; yy >= 0; yy--) {
                            int col = target.getRGB(x, y * sizeMult + yy);
                            int r = (col >> 16) & 0xFF;
                            int g = (col >> 8) & 0xFF;
                            int b = col & 0xFF;
                            col = 0;
                            if (diffR == 0) {
                                col |= rA << 16;
                            } else {
                                double factorR = Math.abs((double) (r - rB) / diffR);
                                if (factorR < 0.5d) {
                                    col |= rB << 16;
                                } else {
                                    col |= rA << 16;
                                }
                            }
                            if (diffG == 0) {
                                col |= gA << 8;
                            } else {
                                double factorG = Math.abs((double) (g - gB) / diffG);
                                if (factorG < 0.5d) {
                                    col |= gB << 8;
                                } else {
                                    col |= gA << 8;
                                }
                            }
                            if (diffB == 0) {
                                col |= bA;
                            } else {
                                double factorB = Math.abs((double) (b - gB) / diffB);
                                if (factorB < 0.5d) {
                                    col |= bB;
                                    //col = 0xFF5555;
                                } else {
                                    col |= bA;
                                    //col = 0x55FF55;
                                }
                            }
                            target.setRGB(x, y * sizeMult + yy, col);
                        }
                    }
                }
            }*/

            ResampleOp resampler = new ResampleOp(origSize * sizeMult, origSize * sizeMult);
            resampler.setFilter(ResampleFilters.getBSplineFilter());
            BufferedImage target = resampler.filter(reference, null);
            for (int x = origSize - 2; x >= 0; x--) {
                for (int y = origSize - 2; y >= 0; y--) {
                    int colA = reference.getRGB(x, y);
                    int colB = reference.getRGB(x, y + 1);
                    int colC = reference.getRGB(x + 1, y);
                    int colD = reference.getRGB(x + 1, y + 1);
                    int rA = (colA >> 16) & 0xFF;
                    int gA = (colA >> 8) & 0xFF;
                    int bA = colA & 0xFF;
                    int rB = (colB >> 16) & 0xFF;
                    int gB = (colB >> 8) & 0xFF;
                    int bB = colB & 0xFF;
                    int rC = (colC >> 16) & 0xFF;
                    int gC = (colC >> 8) & 0xFF;
                    int bC = colC & 0xFF;
                    int rD = (colD >> 16) & 0xFF;
                    int gD = (colD >> 8) & 0xFF;
                    int bD = colD & 0xFF;
                    if (false) {
                        rA = floorI(rA * 0.1d) * 10;
                        gA = floorI(gA * 0.1d) * 10;
                        bA = floorI(bA * 0.1d) * 10;
                        rB = floorI(rB * 0.1d) * 10;
                        gB = floorI(gB * 0.1d) * 10;
                        bB = floorI(bB * 0.1d) * 10;
                        rC = floorI(rC * 0.1d) * 10;
                        gC = floorI(gC * 0.1d) * 10;
                        bC = floorI(bC * 0.1d) * 10;
                        rD = floorI(rD * 0.1d) * 10;
                        gD = floorI(gD * 0.1d) * 10;
                        bD = floorI(bD * 0.1d) * 10;
                    }
                    Interpolation2D interp = new BilinearInterpolation(
                            new double[]{0.0d, 1.0d},
                            new double[]{0.0d, 1.0d},
                            new double[][]{
                                    new double[]{0.0d, rB - rA},
                                    new double[]{rC - rA, rD - rA}
                            }
                    );
                    double diffR = rA - rB;
                    double diffG = gA - gB;
                    double diffB = bA - bB;
                    for (int xx = sizeMult - 1; xx >= 0; xx--) {
                        for (int yy = sizeMult - 1; yy >= 0; yy--) {
                            double variationFromA = interp.interpolate(xx / 10.0d, yy / 10.0d);
                            int col = target.getRGB(x, y * sizeMult + yy);
                            int r = (col >> 16) & 0xFF;
                            int g = (col >> 8) & 0xFF;
                            int b = col & 0xFF;
                            col = 0;
                            if (diffR == 0) {
                                col |= rA << 16;
                            } else {
                                double factorR = (double) (r - rB) / diffR;
                                if (factorR < 0.0d) {
                                    col |= rB << 16;
                                } else {
                                    col |= rA << 16;
                                }
                            }
                            if (diffG == 0) {
                                col |= gA << 8;
                            } else {
                                double factorG = (double) (g - gB) / diffG;
                                if (factorG < 0.0d) {
                                    col |= gB << 8;
                                } else {
                                    col |= gA << 8;
                                }
                            }
                            if (diffB == 0) {
                                col |= bA;
                            } else {
                                double factorB = (double) (b - gB) / diffB;
                                if (factorB < 0.0d) {
                                    col |= bB;
                                    //col = 0xFF5555;
                                } else {
                                    col |= bA;
                                    //col = 0x55FF55;
                                }
                            }
                            target.setRGB(x, y * sizeMult + yy, col);
                        }
                    }
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
