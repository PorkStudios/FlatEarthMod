package net.daporkchop.realmapcc.util.exception;

import lombok.NonNull;
import net.daporkchop.realmapcc.data.TilePos;

/**
 * @author DaPorkchop_
 */
public class TileNotFoundException extends RuntimeException {
    public TileNotFoundException(String message) {
        super(message);
    }

    public TileNotFoundException(@NonNull TilePos pos)  {
        super(pos.toString());
    }
}
