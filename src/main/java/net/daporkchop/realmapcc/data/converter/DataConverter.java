package net.daporkchop.realmapcc.data.converter;

import net.daporkchop.lib.binary.stream.DataIn;
import net.daporkchop.lib.binary.stream.DataOut;
import net.daporkchop.lib.common.function.throwing.EBiConsumer;
import net.daporkchop.lib.common.util.PorkUtil;
import net.daporkchop.lib.concurrent.cache.Cache;
import net.daporkchop.lib.concurrent.cache.ThreadCache;
import net.daporkchop.lib.graphics.PImage;
import net.daporkchop.lib.graphics.impl.image.DirectImage;
import net.daporkchop.lib.hash.util.Digest;
import net.daporkchop.lib.http.SimpleHTTP;
import net.daporkchop.lib.logging.Logging;
import net.daporkchop.lib.math.vector.d.Vec2d;
import net.daporkchop.lib.math.vector.i.Vec2i;
import net.daporkchop.realmapcc.Constants;
import net.daporkchop.realmapcc.data.DataConstants;
import net.daporkchop.realmapcc.data.Tile;
import net.daporkchop.realmapcc.data.converter.dataset.Dataset;
import net.daporkchop.realmapcc.data.converter.dataset.srtm.SRTMDataset;
import net.daporkchop.realmapcc.util.CoordUtils;
import net.daporkchop.realmapcc.util.TileWrapperImage;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.Imaging;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static net.daporkchop.lib.math.primitive.PMath.clamp;

/**
 * @author DaPorkchop_
 */
@SuppressWarnings("unchecked")
public class DataConverter implements Constants, Logging {
    //misc paths
    public static final File DATASET_VERSION_CACHE_FILE = new File("./data/versions.dat");
    public static final File OUTPUT_FILE_ROOT = new File("/home/daporkchop/192.168.1.119/Public/minecraft/mods/realworldcc/data/");

    //dataset paths
    public static final File SRTM_ROOT = new File("/home/daporkchop/192.168.1.119/Misc/HeightmapData/SRTMGL1");

    public static void main(String... args) throws IOException, InterruptedException {
        logger.add(new File("./converter.log"), true);
        new DataConverter().start();
    }

    protected Map<String, AtomicInteger> datasetVersionCache = new HashMap<>();
    protected List<Dataset> datasets = Arrays.asList(
            new SRTMDataset(SRTM_ROOT)
    );

    public void start() throws IOException, InterruptedException {
        if (false)   {
            System.out.println(CoordUtils.globalToBlock(new Vec2d(0.0d, 0.0d)));
            System.out.println(CoordUtils.globalToBlock(new Vec2d(8.0d, 47.0d)));
            System.out.println(CoordUtils.blockToGlobal(new Vec2i(0, 0)));
            System.out.println(CoordUtils.blockToGlobal(new Vec2i(1000, 1000)));
            return;
        }

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

        //generate the data in image form
        logger.info("Nuking output root (parallel!)...");
        if (OUTPUT_FILE_ROOT.exists()) {
            //parallel deletion of output dirs
            Stream.of(OUTPUT_FILE_ROOT.listFiles()).parallel().forEach(PorkUtil::rm);
        }
        PorkUtil.rm(OUTPUT_FILE_ROOT);
        this.ensureDirExists(OUTPUT_FILE_ROOT);
        logger.info("All files in output dir removed.");

        Vec2i[] positions = new Vec2i[DEGREE_SEGMENTS];
        {
            int i = 0;
            positions[i++] = new Vec2i(8, 47);
            for (int y = LATITUDE_MIN; y <= LATITUDE_MAX; y++) {
                for (int x = LONGITUDE_MIN; x <= LONGITUDE_MAX; x++) {
                    if (x == 8 && y == 47)    {
                        continue;
                    }
                    positions[i++] = new Vec2i(x, y);
                }
            }
        }
        Cache<TileWrapperImage> tileCache = ThreadCache.of(TileWrapperImage::new);
        AtomicInteger counter = new AtomicInteger(0);
        EBiConsumer<Integer, Vec2i> consumer = (curr, pos) -> {
            logger.info(String.format("% 5.2f%%  % 5d/% 5d  Drawing tile at (% 4d, % 3d)", (double) curr / DEGREE_SEGMENTS * 100.0d, curr, DEGREE_SEGMENTS, pos.getX(), pos.getY()));
            TileWrapperImage img = tileCache.get();
            Tile tile = img.getTile().setDegLon(pos.getX()).setDegLat(pos.getY());

            boolean first = true;
            for (int tileX = STEPS_PER_DEGREE - 1; tileX >= 0; tileX--) {
                for (int tileY = STEPS_PER_DEGREE - 1; tileY >= 0; tileY--) {
                    tile.setTileLon(tileX).setTileLat(tileY);
                    for (Dataset dataset : this.datasets) {
                        dataset.applyTo(tile);
                    }
                    File file = new File(OUTPUT_FILE_ROOT, DataConstants.getSubpath(pos.getX(), pos.getY(), tileX, tileY));
                    if (first) {
                        this.ensureDirExists(file.getParentFile());
                        first = false;
                    }
                    //this.ensureFileExists(file);
                    Imaging.writeImage(img.getAsBufferedImage(), file, ImageFormats.PNG, null);
                }
            }
        };
        for (int i = CPU_COUNT - 1; i >= 0; i--) {
            new Thread(null, () -> {
                int next;
                while ((next = counter.getAndIncrement()) < DEGREE_SEGMENTS) {
                    consumer.accept(next, positions[next]);
                }
            }, String.format("RealWorldCC data converter #%d", i)).start();
        }

        //dirty tests
        if (false) {
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
            PorkUtil.simpleDisplayImage(INTERPOLATOR_LINEAR.interp(dst, 512, 512).getAsBufferedImage(), true);
        } else if (false) {
            //test to see if cloudflare mangles images
            int[] b = new int[225 * 225];
            for (int i = b.length - 1; i >= 0; i--) {
                b[i] = ThreadLocalRandom.current().nextInt() & 0xFFFFFF;
            }
            BufferedImage img = new BufferedImage(225, 225, BufferedImage.TYPE_INT_RGB);
            int i = 0;
            for (int x = 224; x >= 0; x--) {
                for (int y = 224; y >= 0; y--) {
                    img.setRGB(x, y, b[i++]);
                }
            }
            ImageIO.write(img, "png", new File("/home/daporkchop/192.168.1.119/Public/misc/test.png"));
            logger.info("Image written.");
            byte[] hash = Digest.SHA3_512.hash(new File("/home/daporkchop/192.168.1.119/Public/misc/test.png")).getHash();

            byte[] fetchedBytes = SimpleHTTP.get("https://cloud.daporkchop.net/misc/test.png");
            byte[] hash2 = Digest.SHA3_512.hash(fetchedBytes).getHash();
            if (!Arrays.equals(hash, hash2)) {
                throw new IllegalStateException("Bytes don't match!");
            }

            logger.info("Waiting 30 seconds...");
            Thread.sleep(30000L);

            fetchedBytes = SimpleHTTP.get("https://cloud.daporkchop.net/misc/test.png");
            hash2 = Digest.SHA3_512.hash(fetchedBytes).getHash();
            if (!Arrays.equals(hash, hash2)) {
                throw new IllegalStateException("Bytes don't match!");
            }
        }
    }
}
