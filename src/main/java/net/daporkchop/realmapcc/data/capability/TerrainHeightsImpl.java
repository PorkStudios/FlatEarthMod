package net.daporkchop.realmapcc.data.capability;

/**
 * @author DaPorkchop_
 */
public class TerrainHeightsImpl implements ITerrainHeightHolder {
    private short[] heights;

    @Override
    public short[] getHeights() {
        return this.heights;
    }

    @Override
    public void setHeights(short[] heights) {
        this.heights = heights;
    }
}
