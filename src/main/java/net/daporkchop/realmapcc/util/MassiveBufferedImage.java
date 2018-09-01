package net.daporkchop.realmapcc.util;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * @author DaPorkchop_
 */
public class MassiveBufferedImage extends Image {
    private final IntBuffer buffer;

    public MassiveBufferedImage(int width, int height, int type) {
        this.buffer = ByteBuffer.allocateDirect((int) (2612736060L * 4L)).asIntBuffer();
    }

    @Override
    public int getWidth(ImageObserver observer) {
        return 0;
    }

    @Override
    public int getHeight(ImageObserver observer) {
        return 0;
    }

    @Override
    public ImageProducer getSource() {
        return null;
    }

    @Override
    public Graphics getGraphics() {
        return null;
    }

    @Override
    public Object getProperty(String name, ImageObserver observer) {
        return null;
    }
}
