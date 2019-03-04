package net.daporkchop.realmapcc.data.converter.dataset.srtm;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.common.function.io.IOFunction;
import net.daporkchop.lib.math.vector.i.Vec2i;
import net.daporkchop.realmapcc.Constants;
import net.daporkchop.realmapcc.data.Tile;
import net.daporkchop.realmapcc.data.converter.dataset.Dataset;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Map;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
@Getter
public class SRTM implements Dataset {
    protected static File getSrtmFileName(int lat, int lon, @NonNull File localDir) {
        int nlat = Math.abs((int) Math.floor(lat));
        int nlon = Math.abs((int) Math.floor(lon));

        NumberFormat nf = NumberFormat.getInstance();
        String NS, WE;
        String f_nlat, f_nlon;

        if (lat > 0) {
            NS = "N";
        } else {
            NS = "S";
        }
        if (lon > 0) {
            WE = "E";
        } else {
            WE = "W";
        }

        nf.setMinimumIntegerDigits(2);
        f_nlat = nf.format(nlat);
        nf.setMinimumIntegerDigits(3);
        f_nlon = nf.format(nlon);

        return new File(localDir, NS + f_nlat + WE + f_nlon + ".hgt");
    }

    @NonNull
    protected final File root;
    protected final Map<Vec2i, ByteBuffer> regionCache = Constants.newSoftCache();

    @Override
    public String getName() {
        return "SRTM";
    }

    @Override
    public void applyTo(@NonNull Tile tile) {
        ByteBuffer buf = this.regionCache.computeIfAbsent(new Vec2i(tile.getDegLon(), tile.getDegLat()), (IOFunction<Vec2i, ByteBuffer>) pos -> {
            File file = getSrtmFileName(pos.getY(), pos.getX(), this.root); //yes, these are swapped
            if (file.exists()) {
                try (FileChannel channel = FileChannel.open(file.toPath(), Collections.singleton(StandardOpenOption.READ))) {
                    ByteBuffer toReturn = ByteBuffer.allocateDirect((ARCSECONDS_PER_DEGREE + 1) * (ARCSECONDS_PER_DEGREE + 1) * 2);
                    if (channel.read(toReturn, 0L) != (ARCSECONDS_PER_DEGREE + 1) * (ARCSECONDS_PER_DEGREE + 1) * 2) {
                        throw new IllegalStateException("File not read completely!");
                    }
                    return toReturn;
                }
            } else {
                return ByteBuffer.allocate(0);
            }
        });
        if (buf.capacity() == 0)    {
            for (int x = TILE_SIZE - 1; x >= 0; x--) {
                for (int y = TILE_SIZE - 1; y >= 0; y--) {
                    tile.setRawHeight(x, y, 0);
                }
            }
        } else {
            int xOff = tile.getTileLon() * TILE_SIZE;
            int yOff = tile.getTileLat() * TILE_SIZE;
            for (int x = TILE_SIZE - 1; x >= 0; x--) {
                for (int y = TILE_SIZE - 1; y >= 0; y--) {
                    //tile.setRawHeight(x, y, buf.getShort(((ARCSECONDS_PER_DEGREE - y - yOff) * (ARCSECONDS_PER_DEGREE + 1) + x + xOff) << 1));
                    tile.setRawHeight(x, y, buf.getShort(((y + yOff) * (ARCSECONDS_PER_DEGREE + 1) + x + xOff) << 1));
                }
            }
        }
    }
}
