package net.daporkchop.realmapcc.data.client.lookup;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.daporkchop.realmapcc.data.Tile;
import net.daporkchop.realmapcc.data.TilePos;

import java.io.File;

/**
 * Stores tiles on the disk somewhere!
 *
 * @author DaPorkchop_
 */
@Getter
@Setter
@Accessors(chain = true)
public class DiskTileCache extends AbstractTileLookup<DiskTileCache> {
    protected final File root;

    public DiskTileCache(@NonNull File root)    {
        this.root = root;
    }

    @Override
    public Tile getTile(@NonNull TilePos pos) {
        File file = new File(this.root, pos.getSubpath());
        //TODO: fast tile to image serialization without having to copy pixels
        if (file.exists())  {
        } else {
        }
        return null;
    }
}
