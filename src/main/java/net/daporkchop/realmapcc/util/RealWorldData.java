package net.daporkchop.realmapcc.util;

import net.daporkchop.lib.binary.PorkBuf;

/**
 * This contains height + biome data from the world map used to calculate everything with
 *
 * @author DaPorkchop_
 */
public class RealWorldData {
    private final short[] heights;

    public RealWorldData(short[] heights) {
        this.heights = heights;
    }

    public static RealWorldData read(PorkBuf in) {
        short[] heights = new short[16 * 16];
        for (int i = 0; i < heights.length; i++) {
            heights[i] = in.getShort();
        }
        return new RealWorldData(heights);
    }

    public int getHeight(int x, int z) {
        assert (x & 0xF) == x;
        assert (z & 0xF) == z;

        return this.heights[(x << 4) | z];
    }

    public void write(PorkBuf out) {
        for (short s : this.heights) {
            out.putShort(s);
        }
    }
}
