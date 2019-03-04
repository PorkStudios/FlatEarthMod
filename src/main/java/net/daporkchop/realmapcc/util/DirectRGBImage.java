package net.daporkchop.realmapcc.util;

import lombok.Getter;
import lombok.NonNull;
import net.daporkchop.lib.common.util.DirectMemoryHolder;
import net.daporkchop.lib.common.util.PUnsafe;
import net.daporkchop.lib.graphics.PImage;

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
 * @author DaPorkchop_
 */
@Getter
public class DirectRGBImage implements PImage, DirectMemoryHolder {
    protected long pos;
    protected final long size;

    protected final int width;
    protected final int height;

    public DirectRGBImage(int width, int height) {
        this.size = ((long) width * (long) height) << 2L;
        this.pos = PUnsafe.allocateMemory(this, this.size);

        this.width = width;
        this.height = height;
    }

    @Override
    public int getARGB(int x, int y) {
        if (x < 0 || x >= this.width || y < 0 || y >= this.height) {
            throw new ArrayIndexOutOfBoundsException(String.format("(%d,%d) w=%d,h=%d", x, y, this.width, this.height));
        } else {
            return PUnsafe.getInt(this.pos + (((long) x * (long) this.height + (long) y) << 2L));
        }
    }

    @Override
    public void setARGB(int x, int y, int argb) {
        if (x < 0 || x >= this.width || y < 0 || y >= this.height) {
            throw new ArrayIndexOutOfBoundsException(String.format("(%d,%d) w=%d,h=%d", x, y, this.width, this.height));
        } else {
            PUnsafe.putInt(this.pos + (((long) x * (long) this.height + (long) y) << 2L), argb);
        }
    }

    @Override
    public int getRGB(int x, int y) {
        if (x < 0 || x >= this.width || y < 0 || y >= this.height) {
            throw new ArrayIndexOutOfBoundsException(String.format("(%d,%d) w=%d,h=%d", x, y, this.width, this.height));
        } else {
            return PUnsafe.getInt(this.pos + (((long) x * (long) this.height + (long) y) << 2L));
        }
    }

    @Override
    public void setRGB(int x, int y, int rgb) {
        if (x < 0 || x >= this.width || y < 0 || y >= this.height) {
            throw new ArrayIndexOutOfBoundsException(String.format("(%d,%d) w=%d,h=%d", x, y, this.width, this.height));
        } else {
            PUnsafe.putInt(this.pos + (((long) x * (long) this.height + (long) y) << 2L), rgb);
        }
    }

    @Override
    public void fill(int argb) {
        long pos = this.pos; //this should allow JIT to put the value in a register
        for (long l = this.size - 5L; l >= 0L; l -= 4L) {
            PUnsafe.putInt(pos + l, argb);
        }
    }

    @Override
    public boolean isBW() {
        return false;
    }

    @Override
    public synchronized long getMemoryAddress() {
        return this.pos;
    }

    @Override
    public synchronized void releaseMemory() {
        if (this.isMemoryReleased()) {
            throw new IllegalStateException("Memory already released!");
        } else {
            PUnsafe.freeMemory(this.pos);
            this.pos = -1L;
        }
    }

    //everything below this comment is compatibility code to be able to work with java AWT's godawful api
    @Override
    public BufferedImage getAsBufferedImage() {
        return new BufferedImage(new FastRGBColorModel(), this.newRaster(), false, null);
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

    /*protected static class FastRGBColorModel extends ColorModel {
        public FastRGBColorModel() {
            super(24);
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
        public boolean isCompatibleRaster(Raster raster) {
            return raster instanceof DirectRaster;
        }
    }*/
    protected class FastRGBColorModel extends DirectColorModel   {
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
    }

    protected class DirectRaster extends WritableRaster {
        protected DirectRaster(@NonNull DirectRGBImage image) {
            super(new LargerSinglePixelPackedSampleModel(
                    DataBuffer.TYPE_INT,
                    image.width, image.height,
                    new int[]{0x00FF0000, 0x0000FF00, 0x000000FF}
            ), image.newDataBuffer(), new Point(0, 0));
        }

        @Override
        public void setPixel(int x, int y, int[] iArray) {
            DirectRGBImage.this.setARGB(x, y, iArray[0]);
        }
    }

    protected class DirectDataBuffer extends DataBuffer {
        protected DirectDataBuffer(@NonNull DirectRGBImage image) {
            super(DataBuffer.TYPE_INT, image.height, image.width);
        }

        @Override
        public int getElem(int bank, int i) {
            return DirectRGBImage.this.getRGB(bank, i);
        }

        @Override
        public void setElem(int bank, int i, int val) {
            DirectRGBImage.this.setRGB(bank, i, val);
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
            return DirectRGBImage.this.newDataBuffer();
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
