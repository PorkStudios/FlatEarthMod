package net.daporkchop.realmapcc.generator;


import net.daporkchop.realmapcc.Constants;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.formats.tiff.TiffImageParser;
import org.apache.commons.imaging.formats.tiff.constants.TiffConstants;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author DaPorkchop_
 */
public class TiffConverter implements Constants {
    public static final File root = new File(HeightmapGenerator.root, "tifConversion");
    public static final File tif_root = new File(root, "tif");
    public static final File png_root = new File(root, "png");
    public static final ThreadLocal<TiffImageParser> parserCache = ThreadLocal.withInitial(TiffImageParser::new);
    private static final ThreadLocal<Map<String, Object>> rangeParamsCache = ThreadLocal.withInitial(HashMap::new);

    public static void main(String... args) throws Exception {
        if (false) {
            for (File file : tif_root.listFiles()) {
                TiffImageParser parser = parserCache.get();
                Dimension dimension = parser.getImageSize(file);
                System.out.printf("Image %s: Res %dx%d\n", file.getName(), dimension.width, dimension.height);
            }
        }
        if (false) {
            File file = new File(tif_root, "occurrence_90W_50N.tif");
            for (int cpu = cpuCount - 1; cpu >= 0; cpu--) {
                int j = cpu;
                new Thread(() -> {
                    try {
                        for (int x = 0; x < 10; x++) {
                            for (int y = 0; y < 40; y++) {
                                int X = (x + j * 10) * 1000;
                                int Y = y * 1000;
                                System.out.printf("Reading tile at %d,%d\n", X, Y);
                                BufferedImage image = getRange(file, X, Y, 1000, 1000);
                                ImageIO.write(image, "png", new File(png_root, String.format("%d_%d.png", X, Y))); //hehe i did this wrong
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }
        }
        if (true) {
            File file = new File(root, "output.tif");
            for (int x = 0; x < 40; x++) {
                for (int y = 0; y < 40; y++) {
                    File png = new File(png_root, String.format("%d_%d.png", x * 1000, y * 1000));
                    BufferedImage image = ImageIO.read(png);
                    writeRange(image, file, x * 1000, y * 1000);
                }
            }
        }
    }

    public static BufferedImage getRange(File file, int x, int y, int width, int height) throws Exception {
        return getRange(file, x, y, width, height, parserCache.get(), rangeParamsCache.get());
    }

    public static BufferedImage getRange(File file, int x, int y, int width, int height, Map<String, Object> params) throws Exception {
        return getRange(file, x, y, width, height, parserCache.get(), params);
    }

    public static BufferedImage getRange(File file, int x, int y, int width, int height, TiffImageParser parser) throws Exception {
        return getRange(file, x, y, width, height, parser, rangeParamsCache.get());
    }

    public static BufferedImage getRange(File file, int x, int y, int width, int height, TiffImageParser parser, Map<String, Object> params) throws Exception {
        {
            params.clear();
            params.put(TiffConstants.PARAM_KEY_SUBIMAGE_X, x);
            params.put(TiffConstants.PARAM_KEY_SUBIMAGE_Y, y);
            params.put(TiffConstants.PARAM_KEY_SUBIMAGE_WIDTH, width);
            params.put(TiffConstants.PARAM_KEY_SUBIMAGE_HEIGHT, height);
        }
        return parser.getBufferedImage(file, params);
    }

    public static void writeRange(BufferedImage image, File file, int x, int y) throws Exception {
        writeRange(image, file, x, y, rangeParamsCache.get());
    }

    public static void writeRange(BufferedImage image, File file, int x, int y, Map<String, Object> params) throws Exception {
        {
            params.clear();
            params.put(TiffConstants.PARAM_KEY_SUBIMAGE_X, x);
            params.put(TiffConstants.PARAM_KEY_SUBIMAGE_Y, y);
            params.put(TiffConstants.PARAM_KEY_SUBIMAGE_WIDTH, image.getWidth());
            params.put(TiffConstants.PARAM_KEY_SUBIMAGE_HEIGHT, image.getHeight());
        }
        Imaging.writeImage(image, file, ImageFormats.TIFF, params);
    }
}
