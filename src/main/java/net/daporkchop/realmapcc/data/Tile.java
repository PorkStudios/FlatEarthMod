package net.daporkchop.realmapcc.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.daporkchop.lib.common.util.PUnsafe;
import net.daporkchop.lib.common.util.PorkUtil;
import net.daporkchop.realmapcc.Constants;

/**
 * A single square section of the global data.
 *
 * @author DaPorkchop_
 */
@Setter
@Getter
@Accessors(chain = true)
public class Tile implements Constants {
    public static final int HEIGHT_SHIFT = 1;
    public static final int HEIGHT_MASK = 0x7FFE;

    protected int degLon = Integer.MIN_VALUE;
    protected int degLat = Integer.MIN_VALUE;
    protected int tileLon = Integer.MIN_VALUE;
    protected int tileLat = Integer.MIN_VALUE;

    protected final long addr = PUnsafe.allocateMemory(this, TILE_LIMIT); //4 times

    //getters and setters lol
    public int getRawVal(int x, int y)  {
        return PUnsafe.getInt(this.getAddrAndCheck(x, y) + ((x * TILE_SIZE + y) << 2));
    }

    public void setRawVal(int x, int y, int val) {
        PUnsafe.putInt(this.getAddrAndCheck(x, y) + ((x * TILE_SIZE + y) << 2), val);
    }

    public int getRawHeight(int x, int y)   {
        return (this.getRawVal(x, y) & HEIGHT_MASK) >>> HEIGHT_SHIFT;
    }

    public void setRawHeight(int x, int y, int val)   {
        this.setRawVal(x, y, (this.getRawVal(x, y) & ~HEIGHT_MASK) | ((val << HEIGHT_SHIFT) & HEIGHT_MASK));
    }

    public boolean getIsHeightSigned(int x, int y)   {
        return (this.getRawVal(x, y) & 1) != 0;
    }

    public void setIsHeightSigned(int x, int y, boolean signed)   {
        this.setRawVal(x, y, (this.getRawVal(x, y) & 0xFFFFFFFE) | (signed ? 1 : 0));
    }

    public int getHeight(int x, int y)  {
        return this.getRawHeight(x, y) * (this.getIsHeightSigned(x, y) ? -1 : 1); //TODO: optimize this a LOT by inlining after i've made sure other functions work
    }

    public void setHeight(int x, int y, int height) {
        this.setRawHeight(x, y, height);
        this.setIsHeightSigned(x, y, height < 0); //TODO: optimize this a LOT by inlining after i've made sure other functions work
    }

    //other stuff
    public long getAddrAndCheck(int x, int y)   {
        if (x < 0 || x >= TILE_SIZE || y < 0 || y >= TILE_SIZE) {
            throw new IllegalArgumentException(String.format("Invalid position: (%d,%d)", x, y));
        }
        return this.addr;
    }
}
