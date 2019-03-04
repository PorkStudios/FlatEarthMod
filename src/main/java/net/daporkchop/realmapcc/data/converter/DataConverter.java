package net.daporkchop.realmapcc.data.converter;

import net.daporkchop.lib.binary.stream.DataIn;
import net.daporkchop.lib.binary.stream.DataOut;
import net.daporkchop.lib.common.util.PorkUtil;
import net.daporkchop.lib.graphics.PImage;
import net.daporkchop.lib.graphics.impl.image.DirectImage;
import net.daporkchop.lib.graphics.util.ImageInterpolator;
import net.daporkchop.lib.logging.Logging;
import net.daporkchop.lib.math.interpolation.LinearInterpolationEngine;
import net.daporkchop.realmapcc.Constants;
import net.daporkchop.realmapcc.data.Tile;
import net.daporkchop.realmapcc.data.converter.dataset.Dataset;
import net.daporkchop.realmapcc.data.converter.dataset.srtm.SRTM;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static net.daporkchop.lib.math.primitive.PMath.clamp;

/**
 * @author DaPorkchop_
 */
@SuppressWarnings("unchecked")
public class DataConverter implements Constants, Logging {
    public static final File DATASET_VERSION_CACHE_FILE = new File("./data/versions.dat");

    public static void main(String... args) throws IOException {
        logger.add(new File("./converter.log"), true);
        new DataConverter().start();
    }

    protected Map<String, AtomicInteger> datasetVersionCache = new HashMap<>();
    protected List<Dataset> datasets = Arrays.asList(
            new SRTM(new File("/home/daporkchop/192.168.1.119/Misc/HeightmapData/SRTMGL1"))
    );

    public void start() throws IOException {
        if (DATASET_VERSION_CACHE_FILE.exists()) {
            logger.debug("Loading dataset version cache...");
            try (ObjectInputStream in = new ObjectInputStream(DataIn.wrap(DATASET_VERSION_CACHE_FILE))) {
                this.datasetVersionCache = (Map<String, AtomicInteger>) in.readObject();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            logger.debug("Dataset version cache loaded.");
        }
        {
            boolean dirty = false;
            for (Dataset dataset : this.datasets) {
                logger.debug("Checking if ${0} needs to be updated...", dataset.getName());
                AtomicInteger v = this.datasetVersionCache.computeIfAbsent(dataset.getName(), s -> new AtomicInteger(0));
                logger.debug("Disk version: ${0} Newest: ${1}", v.get(), dataset.getName());
                File dir = new File(String.format("./data/%s", dataset.getName()));
                if (dataset.getTempStorageVersion() <= 0) {
                    throw new IllegalStateException("Dataset storage version must be at least 1!");
                } else if (!dir.exists() || v.get() < dataset.getTempStorageVersion()) {
                    logger.info("Updating cache for ${0}...", dataset.getName());
                    PorkUtil.rm(dir);
                    this.ensureDirExists(dir);
                    dataset.handleConversion(dir);
                    v.set(dataset.getTempStorageVersion());
                    dirty = true;
                    logger.info("Cache for ${0} was updated!", dataset.getName());
                }
            }
            if (dirty) {
                logger.debug("Saving dataset version cache...");
                try (ObjectOutputStream out = new ObjectOutputStream(DataOut.wrap(DATASET_VERSION_CACHE_FILE))) {
                    out.writeObject(this.datasetVersionCache);
                }
                logger.debug("Dataset version cache saved.");
            }
        }

        //dirty test
        if (true) {
            //this simply displays the center of switzerland in bad resolution
            PImage dst = new DirectImage(TILE_SIZE * STEPS_PER_DEGREE, TILE_SIZE * STEPS_PER_DEGREE, true);
            PImage tmp = new DirectImage(TILE_SIZE, TILE_SIZE, true);
            Tile tile = new Tile();
            //tile.setDegLat(37).setDegLon(-123); //golden gate bay
            tile.setDegLat(47).setDegLon(8); //most of switzerland
            for (int lon = STEPS_PER_DEGREE - 1; lon >= 0; lon--) {
                for (int lat = STEPS_PER_DEGREE - 1; lat >= 0; lat--) {
                    tile.setTileLon(lon).setTileLat(lat);
                    this.datasets.get(0).applyTo(tile);
                    for (int x = TILE_SIZE - 1; x >= 0; x--) {
                        for (int y = TILE_SIZE - 1; y >= 0; y--) {
                            tmp.setARGB(x, y, clamp(tile.getRawHeight(x, y) / 2, 0, 0xFF));
                        }
                    }
                    dst.copy(tmp, 0, 0, lon * TILE_SIZE, lat * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
            ImageInterpolator interpolator = new ImageInterpolator(new LinearInterpolationEngine());
            PorkUtil.simpleDisplayImage(interpolator.interp(dst, 512, 512).getAsBufferedImage(), true);
        }
    }
}
