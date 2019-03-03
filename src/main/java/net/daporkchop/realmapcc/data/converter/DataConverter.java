package net.daporkchop.realmapcc.data.converter;

import net.daporkchop.lib.binary.stream.DataIn;
import net.daporkchop.lib.binary.stream.DataOut;
import net.daporkchop.lib.common.util.PorkUtil;
import net.daporkchop.lib.logging.Logging;
import net.daporkchop.realmapcc.Constants;
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

/**
 * @author DaPorkchop_
 */
@SuppressWarnings("unchecked")
public class DataConverter implements Constants, Logging {
    public static final File DATASET_VERSION_CACHE_FILE = new File("data/versions.dat");

    public static void main(String... args) throws IOException {
        logger.add(new File("converter.log"), true);
        new DataConverter().start();
    }

    protected Map<String, AtomicInteger> datasetVersionCache = new HashMap<>();
    protected List<Dataset> datasets = Arrays.asList(
            new SRTM()
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
                File dir = new File(String.format("data/%s", dataset.getName()));
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
            if (dirty)  {
                logger.debug("Saving dataset version cache...");
                try (ObjectOutputStream out = new ObjectOutputStream(DataOut.wrap(DATASET_VERSION_CACHE_FILE))) {
                    out.writeObject(this.datasetVersionCache);
                }
                logger.debug("Dataset version cache saved.");
            }
        }
    }
}
