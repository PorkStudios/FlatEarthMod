package net.daporkchop.realmapcc.data.converter.dataset.water.eu;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.math.arrays.grid.Grid2d;
import net.daporkchop.realmapcc.Constants;
import net.daporkchop.realmapcc.data.Tile;
import net.daporkchop.realmapcc.data.converter.dataset.Dataset;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.formats.tiff.constants.TiffConstants;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.abs;
import static java.lang.Math.round;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
@Getter
public class EUWaterDataset implements Dataset, EUWaterConstants {
    @NonNull
    protected final File root;

    protected final ThreadLocal<Map> paramCache = ThreadLocal.withInitial(() -> {
        Map map = new HashMap();
        map.put(TiffConstants.PARAM_KEY_SUBIMAGE_WIDTH, EUWATER_SAMPLES_PER_TILE);
        map.put(TiffConstants.PARAM_KEY_SUBIMAGE_HEIGHT, EUWATER_SAMPLES_PER_TILE);
        return map;
    });
    protected final ThreadLocal<Grid2d> gridCache = ThreadLocal.withInitial(() -> Grid2d.of(EUWATER_SAMPLES_PER_TILE, EUWATER_SAMPLES_PER_TILE, true));

    @Override
    public String getName() {
        return "water_eu";
    }

    protected File getPath(int degLon, int degLat)  {
        return new File(this.root, String.format(
                "extent_%d%c_%d%c.tif",
                abs(degLon) / 10 * 10, degLon < 0 ? 'W' : 'E',
                abs(degLat) / 10 * 10, degLat < 0 ? 'S' : 'N'
        ));
    }

    @Override
    public void applyTo(@NonNull Tile tile) {
        BufferedImage img;
        try {
            File file = this.getPath(tile.getDegLon(), tile.getDegLat());
            if (file.exists()) {
                Map params = this.paramCache.get();
                params.put(TiffConstants.PARAM_KEY_SUBIMAGE_X, Constants.mod(tile.getDegLon(), 10) * EUWATER_SAMPLES_PER_DEGREE + tile.getTileLon() * EUWATER_SAMPLES_PER_TILE);
                params.put(TiffConstants.PARAM_KEY_SUBIMAGE_Y, Constants.mod(tile.getDegLat(), 10) * EUWATER_SAMPLES_PER_DEGREE + tile.getTileLat() * EUWATER_SAMPLES_PER_TILE);
                img = Imaging.getBufferedImage(file, params);
            } else {
                for (int x = TILE_SIZE - 1; x >= 0; x--)    {
                    for (int y = TILE_SIZE - 1; y >= 0; y--)    {
                        tile.setWater(x, y, true);
                    }
                }
                return;
            }
        } catch (IOException | ImageReadException e)    {
            throw new RuntimeException(tile.getPos().toString(), e);
        }
        Grid2d grid = this.gridCache.get();
        for (int x = EUWATER_SAMPLES_PER_TILE - 1; x >= 0; x--) {
            for (int y = EUWATER_SAMPLES_PER_TILE - 1; y >= 0; y--) {
                grid.setI(x, y, img.getRGB(x, y) == 1 ? 1 : 0);
            }
        }
        double scale = (double) EUWATER_SAMPLES_PER_TILE / (double) TILE_SIZE;
        for (int x = TILE_SIZE - 1; x >= 0; x--) {
            for (int y = TILE_SIZE - 1; y >= 0; y--) {
                tile.setWater(x, y, round(ENGINE_LINEAR.getInterpolated(x * scale, y * scale, grid)) != 0L);
            }
        }
    }
}
