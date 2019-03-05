package net.daporkchop.realmapcc.util;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.common.util.DirectMemoryHolder;
import net.daporkchop.lib.common.util.PUnsafe;
import net.daporkchop.lib.graphics.PImage;
import net.daporkchop.lib.reflection.PField;
import net.daporkchop.realmapcc.Constants;
import net.daporkchop.realmapcc.data.Tile;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;

/**
 * Enables faster and lighter serialization of {@link Tile}s by eliminating the need to copy the pixels to/from a separate
 * {@link PImage} every time before reading/writing.
 *
 * @author DaPorkchop_
 */
@Getter
public class TileWrapperImage implements PImage, Constants {
    @NonNull
    protected final Tile tile;
    protected final BufferedImage image;

    public TileWrapperImage()   {
        this(new Tile());
    }

    public TileWrapperImage(@NonNull Tile tile) {
        synchronized (tile) {
            if (tile.getWrapperDirect() != null)    {
                throw new IllegalStateException("Tile already has a wrapper!");
            }
            this.tile = tile.setWrapper(this);
            this.image = new BufferedImage(new FastRGBColorModel(), this.newRaster(), false, null);
        }
    }

    @Override
    public int getWidth() {
        return TILE_SIZE;
    }

    @Override
    public int getHeight() {
        return TILE_SIZE;
    }

    @Override
    public int getARGB(int x, int y) {
        return this.tile.getRawVal(x, y);
    }

    @Override
    public void setARGB(int x, int y, int argb) {
        this.tile.setRawVal(x, y, argb);
    }

    @Override
    public int getRGB(int x, int y) {
        return this.tile.getRawVal(x, y);
    }

    @Override
    public void setRGB(int x, int y, int rgb) {
        this.tile.setRawVal(x, y, rgb);
    }

    @Override
    public boolean isBW() {
        return false;
    }

    //everything below this comment is compatibility code to be able to work with java AWT's godawful api
    @Override
    public BufferedImage getAsBufferedImage() {
        return this.image;
    }

    @Override
    public Icon getAsSwingIcon() {
        return new ImageIcon(this.getAsBufferedImage());
    }

    protected DataBuffer newDataBuffer() {
        return new DirectDataBuffer(this);
    }

    protected WritableRaster newRaster() {
        return new DirectRaster(this);
    }

    protected static class FastRGBColorModel extends ColorModel {
        protected static PField field_numComponents = PField.of(ColorModel.class, "numComponents");
        protected static PField field_supportsAlpha = PField.of(ColorModel.class, "supportsAlpha");

        public FastRGBColorModel() {
            super(24);

            field_numComponents.setInt(this, 3);
            field_supportsAlpha.setBoolean(this, false);
        }

        @Override
        public int getRed(int pixel) {
            return (pixel >> 16) & 0xFF;
        }

        @Override
        public int getGreen(int pixel) {
            return (pixel >>> 8) & 0xFF;
        }

        @Override
        public int getBlue(int pixel) {
            return pixel & 0xFF;
        }

        @Override
        public int getAlpha(int pixel) {
            return 0xFF;
        }

        @Override
        public int getRGB(Object inData) {
            return ((int[]) inData)[0];
        }

        @Override
        public boolean isCompatibleRaster(Raster raster) {
            return raster instanceof DirectRaster;
        }

        @Override
        public boolean isCompatibleSampleModel(SampleModel sm) {
            return sm instanceof LargerSinglePixelPackedSampleModel;
        }

        @Override
        public Object getDataElements(int rgb, Object pixel) {
            int[] i;
            if (pixel instanceof int[]) {
                i = (int[]) pixel;
            } else {
                i = new int[1];
            }
            i[0] = rgb;
            return i;
        }
    }
    /*protected class FastRGBColorModel extends DirectColorModel   {
        public FastRGBColorModel()  {
            super(24,
                    0x00ff0000,   // Red
                    0x0000ff00,   // Green
                    0x000000ff,   // Blue
                    0x0           // Alpha
            );
        }

        @Override
        public boolean isCompatibleRaster(Raster raster) {
            return raster instanceof DirectRaster;
        }

        @Override
        public boolean isCompatibleSampleModel(SampleModel sm) {
            return sm instanceof LargerSinglePixelPackedSampleModel;
        }
    }*/

    protected class DirectRaster extends WritableRaster {
        protected DirectRaster(@NonNull TileWrapperImage image) {
            super(new LargerSinglePixelPackedSampleModel(
                    DataBuffer.TYPE_INT,
                    TILE_SIZE, TILE_SIZE,
                    new int[]{0x00FF0000, 0x0000FF00, 0x000000FF}
            ), image.newDataBuffer(), new Point(0, 0));
        }

        @Override
        public void setPixel(int x, int y, int[] iArray) {
            TileWrapperImage.this.setARGB(x, y, iArray[0]);
        }
    }

    protected class DirectDataBuffer extends DataBuffer {
        protected DirectDataBuffer(@NonNull TileWrapperImage image) {
            super(DataBuffer.TYPE_INT, TILE_SIZE, TILE_SIZE);
        }

        @Override
        public int getElem(int bank, int i) {
            return TileWrapperImage.this.getRGB(bank, i);
        }

        @Override
        public void setElem(int bank, int i, int val) {
            TileWrapperImage.this.setRGB(bank, i, val);
        }
    }

    protected class LargerSinglePixelPackedSampleModel extends SinglePixelPackedSampleModel {
        public LargerSinglePixelPackedSampleModel(int dataType, int w, int h, int[] bitMasks) {
            super(dataType, w, h, bitMasks);
        }

        @Override
        public int getNumDataElements() {
            return this.width * this.height;
        }

        @Override
        public DataBuffer createDataBuffer() {
            return TileWrapperImage.this.newDataBuffer();
        }

        @Override
        public int getOffset(int x, int y) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getDataElements(int x, int y, Object obj, DataBuffer data) {
            if ((x < 0) || (y < 0) || (x >= this.width) || (y >= this.height)) {
                throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
            }

            int[] idata;
            if (obj == null) {
                idata = new int[1];
            } else {
                idata = (int[]) obj;
            }

            idata[0] = data.getElem(x, y);
            return idata;
        }

        @Override
        public int[] getPixel(int x, int y, int iArray[], DataBuffer data) {
            if ((x < 0) || (y < 0) || (x >= this.width) || (y >= this.height)) {
                throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
            }
            int pixels[];
            if (iArray == null) {
                pixels = new int[this.numBands];
            } else {
                pixels = iArray;
            }

            int value = data.getElem(x, y);

            pixels[0] = (value >>> 16) & 0xFF;
            pixels[1] = (value >>> 8) & 0xFF;
            pixels[2] = value & 0xFF;
            return pixels;
        }

        @Override
        public int[] getPixels(int x, int y, int w, int h, int iArray[], DataBuffer data) {
            int x1 = x + w;
            int y1 = y + h;

            if (x < 0 || x >= this.width || w > this.width || x1 < 0 || x1 > this.width || y < 0 || y >= this.height || h > this.height || y1 < 0 || y1 > this.height) {
                throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
            }
            int pixels[];
            if (iArray != null) {
                pixels = iArray;
            } else {
                pixels = new int[w * h * this.numBands];
            }
            int dstOffset = 0;

            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    int value = data.getElem(x + j, y + i);
                    pixels[dstOffset++] = (value >>> 16) & 0xFF;
                    pixels[dstOffset++] = (value >>> 8) & 0xFF;
                    pixels[dstOffset++] = value & 0xFF;
                }
            }
            return pixels;
        }

        @Override
        public void setDataElements(int x, int y, Object obj, DataBuffer data) {
            if ((x < 0) || (y < 0) || (x >= this.width) || (y >= this.height)) {
                throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
            }

            data.setElem(x, y, ((int[]) obj)[0]);
        }

        @Override
        public void setPixel(int x, int y, int iArray[], DataBuffer data) {
            if ((x < 0) || (y < 0) || (x >= this.width) || (y >= this.height)) {
                throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
            }

            data.setElem(x, y, (iArray[0] << 16) | (iArray[1] << 8) | iArray[2]);
        }

        @Override
        public void setPixels(int x, int y, int w, int h, int iArray[], DataBuffer data) {
            int x1 = x + w;
            int y1 = y + h;

            if (x < 0 || x >= this.width || w > this.width || x1 < 0 || x1 > this.width || y < 0 || y >= this.height || h > this.height || y1 < 0 || y1 > this.height) {
                throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
            }

            int srcOffset = 0;

            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    data.setElem(x + j, y + i, (iArray[srcOffset++] << 16) | (iArray[srcOffset++] << 8) | iArray[srcOffset++]);
                }
            }
        }
    }
}
