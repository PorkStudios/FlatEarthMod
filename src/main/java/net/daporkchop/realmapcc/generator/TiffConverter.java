package net.daporkchop.realmapcc.generator;


import net.daporkchop.lib.binary.NBitArray;
import net.daporkchop.realmapcc.Constants;
import net.daporkchop.realmapcc.generator.dataset.surface.cover.BiomeColor;
import net.daporkchop.realmapcc.generator.dataset.surface.cover.GlobCover;
import net.daporkchop.realmapcc.util.ImageUtil;
import net.daporkchop.realmapcc.util.MassiveBufferedImage;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffImageParser;
import org.apache.commons.imaging.formats.tiff.constants.TiffConstants;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import static java.lang.Math.abs;

/**
 * @author DaPorkchop_
 */
public class TiffConverter implements Constants {
    public static final File conversionRoot = new File(rootDir, "tifConversion");
    public static final File tiffRoot = new File(conversionRoot, "tif");
    public static final File pngRoot = new File(conversionRoot, "png");

    public static void main(String... args) throws Exception {
        if (false) { //debug: print all tiff resolutions (doesn't seem to work)
            for (File file : tiffRoot.listFiles()) {
                TiffImageParser parser = ImageUtil.parserCache.get();
                Dimension dimension = parser.getImageSize(file);
                System.out.printf("Image %s: Res %dx%d\n", file.getName(), dimension.width, dimension.height);
            }
        }
        if (false) { //debug: split a tiff into a lot of png tiles
            for (File file : pngRoot.listFiles()) {
                if (!file.delete()) {
                    throw new RuntimeException(String.format("Unable to delete %s", file.getAbsolutePath()));
                }
            }

            File file = new File(tiffRoot, "occurrence_90W_50N.tif");
            //File file = new File(rootDir, "GlobCover/globcover_colored.tif");

            for (int cpu = cpuCount - 1; cpu >= 0; cpu--) {
                int j = cpu;
                new Thread(() -> {
                    try {
                        for (int x = 0; x < 10; x++) {
                            for (int y = 0; y < 40; y++) {
                                int X = x + j * 10;
                                System.out.printf("Reading tile at %d,%d\n", X, y);
                                BufferedImage image = ImageUtil.getImageSection(file, X * 1000, y * 1000, 1000, 1000);
                                ImageIO.write(image, "png", new File(pngRoot, String.format("%d_%d.png", X, y))); //hehe i did this wrong
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }
        }
        if (false) {
            File in = new File(tiffRoot, "occurrence_90W_50N.tif");
            BufferedImage image = ImageUtil.getImageSection(in, 0, 0, 1000, 1000);
            {
                BufferedImage i2 = new MassiveBufferedImage(image.getWidth(), image.getHeight());
                for (int x = image.getWidth() - 1; x >= 0; x--) {
                    for (int y = image.getHeight() - 1; y >= 0; y--) {
                        //int col = image.getRGB(x, y);
                        //Color color = new Color(image.getRGB(x, y));
                        //i2.setRGB(x, y, (color.getRed() >> 16) | (color.getGreen() >> 8) | color.getBlue());
                        i2.setRGB(x, y, image.getRGB(x, y));
                    }
                }
                System.out.println(i2.getRGB(430, 140));
                image = i2;
            }

            //new File(rootDir, "tmp.tif").delete();
            new File(rootDir, "tmp.png").delete();
            //writeImage(image, new File(rootDir, "tmp.tif"), TiffConstants.TIFF_COMPRESSION_UNCOMPRESSED);
            ImageUtil.writeImage(image, new File(rootDir, "tmp.png"));
        }
        if (false) { //debug: trim globcover tiff to only contain SRTM ranges
            File in = new File(rootDir, "GlobCover/globcover_colored.tif");

            if (false) { //debug.debug: get compression of globcover image so we can use it again for re-encoding
                TiffImageMetadata metadata = (TiffImageMetadata) ImageUtil.parserCache.get().getMetadata(in);
                System.out.println(metadata);
                return;
            }

            File out = new File(rootDir, "GlobCover/globcover_colored_shrunk.tif");

            if (out.exists() && !out.delete()) {
                throw new RuntimeException(String.format("Unable to delete %s", out.getAbsolutePath()));
            }

            int yOffset = (GLOBCOVER_maxLatitude - maxLatitude) * GLOBCOVER_valuesPerDegree;
            int width = 129_600;
            int height = (abs(maxLatitude) + abs(minLatitude)) * GLOBCOVER_valuesPerDegree;
            System.out.printf("Y offset: %d\nSize: %dx%d\n", yOffset, width, height);

            System.out.println("Waiting 3 secs before malloc...");
            try {
                Thread.sleep(3000L);
            } catch (InterruptedException e) {
            }
            System.out.println("Allocating buffer...");
            BufferedImage image = new MassiveBufferedImage(width, height);

            System.out.println("Reading image...");
            {
                for (int y = height - 1; y >= 0; y--) {
                    BufferedImage read = ImageUtil.getImageSection(in, 0, y + yOffset, width, 1);
                    for (int x = width - 1; x >= 0; x--) {
                        image.setRGB(x, y, read.getRGB(x, 0));
                    }
                    System.out.printf("Row %d/%d\n", height - y, height);
                }
            }

            System.out.println("Writing image...");
            ImageUtil.writeImage(image, out, TiffConstants.TIFF_COMPRESSION_LZW);

            System.out.println("Done!");
        }
        if (false) { //debug: test viewing image segment
            File file = GlobCover.globCoverPath;
            int lat = 50;
            int lon = -90;
            int degSize = 10 * GLOBCOVER_valuesPerDegree;
            BufferedImage segment = ImageUtil.getImageSection(
                    file,
                    (lon - minLongitude) * GLOBCOVER_valuesPerDegree,
                    (maxLatitude - lat) * GLOBCOVER_valuesPerDegree,
                    degSize, degSize
            );

            if (false) {
                JFrame frame = new JFrame();
                frame.getContentPane().setLayout(new FlowLayout());
                frame.getContentPane().add(new JLabel(new ImageIcon(segment)));
                frame.pack();
                frame.setVisible(true);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
            File out = new File(rootDir, "tmp.tif");
            if (out.exists() && !out.delete()) {
                throw new RuntimeException();
            }
            ImageUtil.writeImage(segment, out);
        }
        if (true) { //debug: test viewing single degree
            NBitArray array = GlobCover.INSTANCE.getDataAtDegree(-90, 50);
            BufferedImage image = new BufferedImage(GLOBCOVER_valuesPerDegree, GLOBCOVER_valuesPerDegree, BufferedImage.TYPE_INT_ARGB);
            for (int x = GLOBCOVER_valuesPerDegree - 1; x >= 0; x--) {
                for (int y = GLOBCOVER_valuesPerDegree - 1; y >= 0; y--) {
                    image.setRGB(x, y, BiomeColor.values()[array.get(x * GLOBCOVER_valuesPerDegree + y)].color | 0xFF000000);
                }
            }
            if (true) {
                JFrame frame = new JFrame();
                frame.getContentPane().setLayout(new FlowLayout());
                frame.getContentPane().add(new JLabel(new ImageIcon(image)));
                frame.pack();
                frame.setVisible(true);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
        }
    }
}
