package net.daporkchop.realmapcc.data;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.realmapcc.Constants;
import net.daporkchop.realmapcc.util.TileWrapperImage;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingConstants;
import org.apache.commons.imaging.common.BufferedImageFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;

/**
 * @author DaPorkchop_
 */
public interface DataConstants extends Constants {
    static String getSubpath(int degLon, int degLat, int tileLon, int tileLat)    {
        return String.format("%04d/%03d/%02d.%02d.png", degLon, degLat, tileLon, tileLat);
    }

    static void loadImage(@NonNull File file, @NonNull TileWrapperImage wrapper) throws IOException {
        try {
            BufferedImage img = Imaging.getBufferedImage(file, Collections.singletonMap(ImagingConstants.BUFFERED_IMAGE_FACTORY, new DelegatingBufferedImageFactory(wrapper)));
            if (img != wrapper.getAsBufferedImage())    {
                throw new IllegalStateException("Decoded image was not the same instance!");
            }
        } catch (ImageReadException e)  {
            throw new IOException(e);
        }
    }

    @RequiredArgsConstructor
    @Getter
    class DelegatingBufferedImageFactory implements BufferedImageFactory    {
        @NonNull
        protected final TileWrapperImage wrapper;

        @Override
        public BufferedImage getColorBufferedImage(int width, int height, boolean hasAlpha) {
            if (width != TILE_SIZE || height != TILE_SIZE || hasAlpha) {
                throw new IllegalArgumentException(String.format("w=%d,h=%d,alpha=%b", width, height, hasAlpha));
            } else {
                return this.wrapper.getAsBufferedImage();
            }
        }

        @Override
        public BufferedImage getGrayscaleBufferedImage(int width, int height, boolean hasAlpha) {
            throw new UnsupportedOperationException();
        }
    }
}
