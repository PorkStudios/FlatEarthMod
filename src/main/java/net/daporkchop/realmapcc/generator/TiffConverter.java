package net.daporkchop.realmapcc.generator;


import net.daporkchop.realmapcc.Constants;
import net.daporkchop.realmapcc.util.MassiveBufferedImage;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingConstants;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffImageParser;
import org.apache.commons.imaging.formats.tiff.constants.TiffConstants;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.abs;

/**
 * @author DaPorkchop_
 */
public class TiffConverter implements Constants {
    public static final File conversionRoot = new File(rootDir, "tifConversion");
    public static final File tiffRoot = new File(conversionRoot, "tif");
    public static final File pngRoot = new File(conversionRoot, "png");
    public static final ThreadLocal<TiffImageParser> parserCache = ThreadLocal.withInitial(TiffImageParser::new);
    private static final ThreadLocal<Map<String, Object>> rangeParamsCache = ThreadLocal.withInitial(HashMap::new);

    public static void main(String... args) throws Exception {
        if (false) { //debug: print all tiff resolutions (doesn't seem to work)
            for (File file : tiffRoot.listFiles()) {
                TiffImageParser parser = parserCache.get();
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
                                BufferedImage image = getImageSection(file, X * 1000, y * 1000, 1000, 1000);
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
            BufferedImage image = getImageSection(in, 0, 0, 1000, 1000);
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
            writeImage(image, new File(rootDir, "tmp.png"));
        }
        if (false) { //debug: trim globcover tiff to only contain SRTM ranges
            File in = new File(rootDir, "GlobCover/globcover_colored.tif");

            if (false) { //debug.debug: get compression of globcover image so we can use it again for re-encoding
                TiffImageMetadata metadata = (TiffImageMetadata) parserCache.get().getMetadata(in);
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
                    BufferedImage read = getImageSection(in, 0, y + yOffset, width, 1);
                    for (int x = width - 1; x >= 0; x--) {
                        image.setRGB(x, y, read.getRGB(x, 0));
                    }
                    System.out.printf("Row %d/%d\n", height - y, height);
                }
            }

            System.out.println("Writing image...");
            writeImage(image, out, TiffConstants.TIFF_COMPRESSION_LZW);

            System.out.println("Done!");
        }
    }

    public static BufferedImage getImageSection(File file, int x, int y, int width, int height) throws Exception {
        return getImageSection(file, x, y, width, height, parserCache.get(), rangeParamsCache.get());
    }

    public static BufferedImage getImageSection(File file, int x, int y, int width, int height, Map<String, Object> params) throws Exception {
        return getImageSection(file, x, y, width, height, parserCache.get(), params);
    }

    public static BufferedImage getImageSection(File file, int x, int y, int width, int height, TiffImageParser parser) throws Exception {
        return getImageSection(file, x, y, width, height, parser, rangeParamsCache.get());
    }

    public static BufferedImage getImageSection(File file, int x, int y, int width, int height, TiffImageParser parser, Map<String, Object> params) throws Exception {
        {
            params.clear();
            params.put(TiffConstants.PARAM_KEY_SUBIMAGE_X, x);
            params.put(TiffConstants.PARAM_KEY_SUBIMAGE_Y, y);
            params.put(TiffConstants.PARAM_KEY_SUBIMAGE_WIDTH, width);
            params.put(TiffConstants.PARAM_KEY_SUBIMAGE_HEIGHT, height);
        }
        return parser.getBufferedImage(file, params);
    }

    public static void writeImage(BufferedImage image, File file) throws Exception {
        Map<String, Object> params = rangeParamsCache.get();
        params.clear();
        writeImage(image, file, params);
    }

    public static void writeImage(BufferedImage image, File file, int compression) throws Exception {
        Map<String, Object> params = rangeParamsCache.get();
        params.clear();
        writeImage(image, file, params, compression);
    }

    public static void writeImage(BufferedImage image, File file, Map<String, Object> params, int compression) throws Exception {
        params.clear();
        params.put(ImagingConstants.PARAM_KEY_COMPRESSION, compression);
        writeImage(image, file, params);
    }

    public static void writeImage(BufferedImage image, File file, Map<String, Object> params) throws Exception {
        /*OutputStream os1 = new FileOutputStream(file);
        OutputStream os2 = new BufferedOutputStream(os1);
        new TiffImageWriterLossless(new byte[0])   {
        }.writeImage(image, os2, params);
        os2.close();
        os1.close();*/
        Imaging.writeImage(image, file, ImageFormats.TIFF, params);
    }
}
