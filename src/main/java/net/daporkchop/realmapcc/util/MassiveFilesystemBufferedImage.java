package net.daporkchop.realmapcc.util;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Allows storing a {@link BufferedImage} on disk with nearly no limit to maximum size
 *
 * @author DaPorkchop_
 */
//TODO: ram cache (is that even needed?)
//TODO: make this work
public class MassiveFilesystemBufferedImage extends BufferedImage {
    private static final int MASK = 0x00FFFFFF;

    private final int width;
    private final int height;

    private final RandomAccessFile raf;
    private final File file;

    public MassiveFilesystemBufferedImage(int width, int height, File file) throws IOException {
        super(1, 1, TYPE_INT_RGB);

        this.width = width;
        this.height = height;

        long size = (long) width * height * 4L;
        file.mkdirs();
        file = new File(file, String.valueOf(ThreadLocalRandom.current().nextLong()) + "img");
        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("Unable to create file!");
        }
        this.raf = new RandomAccessFile(file, "rw");
        this.raf.setLength(size);
        this.file = file;
        this.file.deleteOnExit();
    }

    @Override
    public int getRGB(int x, int y) {
        try {
            long offset = x * (long) this.width + y;
            synchronized (this.raf) {
                this.raf.seek(offset);
                return this.raf.readInt();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int[] getRGB(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {
        try {
            int yoff = offset;
            int off;

            synchronized (this.raf) {
                for (int y = startY; y < startY + h; y++, yoff += scansize) {
                    off = yoff;
                    for (int x = startX; x < startX + w; x++) {
                        this.raf.seek(x * (long) this.width + y);
                        rgbArray[off++] = this.raf.readInt();
                    }
                }
            }

            return rgbArray;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void setRGB(int x, int y, int rgb) {
        //rgb &= MASK;
        try {
            long offset = x * (long) this.width + y;
            synchronized (this.raf) {
                this.raf.seek(offset);
                this.raf.writeInt(rgb);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public WritableRaster getRaster() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void finalize() throws Throwable {
        this.raf.close(); //TODO: better way of doing this?
    }
}
