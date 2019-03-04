package net.daporkchop.realmapcc.data.client.lookup;

import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.daporkchop.realmapcc.data.Tile;
import net.daporkchop.realmapcc.data.TilePos;

/**
 * @author DaPorkchop_
 */
public abstract class AbstractTileLookup<Impl extends AbstractTileLookup> implements TileLookup {
    protected TileLookup delegate;

    @Override
    public Tile getTile(@NonNull TilePos pos) {
        if (this.delegate == null)  {
            throw new NullPointerException("Delegate is not set!");
        } else {
            return this.delegate.getTile(pos);
        }
    }

    @SuppressWarnings("unchecked")
    public Impl setDelegate(TileLookup delegate)    {
        this.delegate = delegate;
        return (Impl) this;
    }
}
