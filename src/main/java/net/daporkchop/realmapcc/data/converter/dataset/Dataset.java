package net.daporkchop.realmapcc.data.converter.dataset;

import lombok.NonNull;
import net.daporkchop.realmapcc.Constants;

import java.io.File;
import java.io.IOException;

/**
 * @author DaPorkchop_
 */
public interface Dataset extends Constants {
    String getName();

    /**
     * Converts the dataset into some temporary format that may be used for rapid access during conversion to the target
     * format (an array of PNGs)
     *
     * @param target a folder to which the temporary files may be stored
     */
    default void handleConversion(@NonNull File target) throws IOException {
    }

    /**
     * Gets the version of this dataset's temporary storage format. If higher than the current version on disk, a full re-conversion
     * will be initiated.
     * @return the version of this dataset's temporary storage format
     */
    default int getTempStorageVersion() {
        return 1;
    }
}
