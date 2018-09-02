package net.daporkchop.realmapcc.util;

import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingConstants;
import org.apache.commons.imaging.formats.tiff.TiffImageParser;
import org.apache.commons.imaging.formats.tiff.constants.TiffConstants;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author DaPorkchop_
 */
public class ImageUtil {
    public static final ThreadLocal<TiffImageParser> parserCache = ThreadLocal.withInitial(TiffImageParser::new);
    private static final ThreadLocal<Map<String, Object>> rangeParamsCache = ThreadLocal.withInitial(HashMap::new);

    public static BufferedImage getImageSection(File file, int x, int y, int width, int height) throws IOException {
        return getImageSection(file, x, y, width, height, parserCache.get(), rangeParamsCache.get());
    }

    public static BufferedImage getImageSection(File file, int x, int y, int width, int height, Map<String, Object> params) throws IOException {
        return getImageSection(file, x, y, width, height, parserCache.get(), params);
    }

    public static BufferedImage getImageSection(File file, int x, int y, int width, int height, TiffImageParser parser) throws IOException {
        return getImageSection(file, x, y, width, height, parser, rangeParamsCache.get());
    }

    public static BufferedImage getImageSection(File file, int x, int y, int width, int height, TiffImageParser parser, Map<String, Object> params) throws IOException {
        {
            params.clear();
            params.put(TiffConstants.PARAM_KEY_SUBIMAGE_X, x);
            params.put(TiffConstants.PARAM_KEY_SUBIMAGE_Y, y);
            params.put(TiffConstants.PARAM_KEY_SUBIMAGE_WIDTH, width);
            params.put(TiffConstants.PARAM_KEY_SUBIMAGE_HEIGHT, height);
        }
        try {
            return parser.getBufferedImage(file, params);
        } catch (ImageReadException e) {
            throw new IOException(e);
        }
    }

    public static void writeImage(BufferedImage image, File file) throws IOException {
        Map<String, Object> params = rangeParamsCache.get();
        params.clear();
        writeImage(image, file, params);
    }

    public static void writeImage(BufferedImage image, File file, int compression) throws IOException {
        Map<String, Object> params = rangeParamsCache.get();
        params.clear();
        writeImage(image, file, params, compression);
    }

    public static void writeImage(BufferedImage image, File file, Map<String, Object> params, int compression) throws IOException {
        params.clear();
        params.put(ImagingConstants.PARAM_KEY_COMPRESSION, compression);
        writeImage(image, file, params);
    }

    public static void writeImage(BufferedImage image, File file, Map<String, Object> params) throws IOException {
        try {
            Imaging.writeImage(image, file, ImageFormats.TIFF, params);
        } catch (ImageWriteException e) {
            throw new IOException(e);
        }
    }
/*
    public static BufferedImage expandImage(BufferedImage src, int mult)  {
        return expandImage(src, mult, mult);
    }

    public static BufferedImage expandImage(BufferedImage src, BufferedImage dst, int mult)  {
        return expandImage(src, dst, mult, mult);
    }

    public static BufferedImage expandImage(BufferedImage src, int multX, int multY)  {
        return expandImage(src, new BufferedImage(src.getWidth() * multX, src.getHeight() * multY, BufferedImage.TYPE_INT_ARGB), multX, multY);
    }


    public static BufferedImage expandImage(BufferedImage src, BufferedImage dst, int multX, int multY)  {
        int w = src.getWidth();
        int h = src.getHeight();

        int col, cXplus, cXmin, cYplus, cYmin;
        for (int x = w - 1; x >= 0; x--)    {
            for (int y = h - 1; y >= 0; y--)    {
                col = src.getRGB(x, y);
                if (x + 1 == w) {
                    cXplus = col;
                } else {
                    cXplus = src.getRGB(x + 1, y);
                }
                if (x == 0) {
                    cXmin = col;
                } else {
                    cXmin = src.getRGB(x - 1, y);
                }
                if (y + 1 == w) {
                    cYplus = col;
                } else {
                    cYplus = src.getRGB(x, y + 1);
                }
                if (y == 0) {
                    cYmin = col;
                } else {
                    cYmin = src.getRGB(x, y - 1);
                }
                int sameCount = (col == )
                if (col == cX)
                for (int xx = multX - 1; xx >= 0; xx--)  {
                    for (int yy = multY - 1; yy >= 0; yy--) {

                    }
                }
            }
        }
        return dst;
    }*/
}
