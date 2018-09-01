package net.daporkchop.realmapcc.generator.dataset.surface.water;

import net.daporkchop.realmapcc.generator.dataset.Dataset;

/**
 * @author DaPorkchop_
 */
public class SurfaceWaterDataset implements Dataset<boolean[]> {
    //TODO: implement

    @Override
    public int getDataAtPos(double lon, double lat) {
        return 0;
    }

    @Override
    public boolean[] getDataAtDegree(int lon, int lat) {
        return new boolean[0];
    }

    @Override
    public int getValuesPerDegree() {
        return 4000;
    }
}
