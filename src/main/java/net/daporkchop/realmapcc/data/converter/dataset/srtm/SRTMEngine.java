package net.daporkchop.realmapcc.data.converter.dataset.srtm;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.daporkchop.realmapcc.Constants;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

import static java.lang.Math.abs;
import static net.daporkchop.lib.math.primitive.PMath.floorI;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
@Getter
@ToString
public class SRTMEngine implements Constants {
    /**
     * The number of samples per tile.
     *
     * For example, on SRTMGL1, which is sampled at 1 arc-second, this value should be 3600
     */
    protected final int samples;

    public File getSrtmFile(int lat, int lon, @NonNull File parent) {
        return new File(parent, String.format("%c%02d%c%03d.hgt", lat > 0 ? 'N' : 'S', abs(floorI(lat)), lon > 0 ? 'E' : 'W', abs(floorI(lon))));
    }

    /**
     * Gets the data from the SRTM tile at the given position
     * @param lon the longitude of the tile to get
     * @param lat the latitude of the tile to get
     * @param root the root directory where the SRTM tiles are stored
     * @return the raw contents of the SRTM tile at the given coordinates. If not present, a dummy buffer with a capacity of 0 will be returned.
     * @throws IOException if an IO exception occurs you dummy
     */
    public ByteBuffer getTile(int lon, int lat, @NonNull File root) throws IOException {
        File file = getSrtmFile(lat, lon, root); //yes, these are swapped
        if (file.exists()) {
            try (FileChannel channel = FileChannel.open(file.toPath(), Collections.singleton(StandardOpenOption.READ))) {
                ByteBuffer toReturn = ByteBuffer.allocateDirect((ARCSECONDS_PER_DEGREE + 1) * (ARCSECONDS_PER_DEGREE + 1) * 2);
                if (channel.read(toReturn, 0L) != (ARCSECONDS_PER_DEGREE + 1) * (ARCSECONDS_PER_DEGREE + 1) * 2) {
                    throw new IllegalStateException("File not read completely!");
                }
                return toReturn;
            }
        } else {
            return ByteBuffer.allocateDirect(0);
        }
    }

    public int getHeight(@NonNull ByteBuffer tile, int subLon, int subLat)  {
        return tile.getShort(((this.samples - subLat) * (this.samples + 1) + subLon) << 1);
    }
}
