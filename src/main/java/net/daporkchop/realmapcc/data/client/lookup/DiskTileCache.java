package net.daporkchop.realmapcc.data.client.lookup;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.realmapcc.data.Tile;
import net.daporkchop.realmapcc.data.TilePos;

import java.io.File;

/**
 * Stores tiles on the disk somewhere!
 *
 * @author DaPorkchop_
 */
@Getter
public class DiskTileCache implements TileLookup {
    protected final File root;

    public DiskTileCache(@NonNull File root)    {
        this.root = root;
    }

    @Override
    public Tile getTile(TilePos pos) {
        return null;
    }
}
