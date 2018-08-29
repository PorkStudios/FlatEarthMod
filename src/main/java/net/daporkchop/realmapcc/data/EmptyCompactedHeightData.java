package net.daporkchop.realmapcc.data;

/**
 * @author DaPorkchop_
 */
public class EmptyCompactedHeightData extends CompactedHeightData {
    public EmptyCompactedHeightData() {
        super((short) -1, null, -1);
    }

    @Override
    public int getHeight(int x, int z) {
        return 0;
    }
}
