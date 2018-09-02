package net.daporkchop.realmapcc.generator.dataset;

import net.daporkchop.realmapcc.Constants;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
public interface Dataset<ArrayType> extends Constants {
    /**
     * Gets a single value at a given point.
     *
     * @param lon the longitude of the value to get
     * @param lat the latitude of the value to get
     * @return the value at the given coordinates
     */
    int getDataAtPos(double lon, double lat) throws IOException;

    /**
     * Gets all values within a single degree of arc.
     * <p>
     * Values start at the minimum longitude/latitude coordinate and end one value before the beginning of
     * the next degree.
     *
     * @param lon the minimum longitude coordinate to get
     * @param lat the minimum latitude coordinate to get
     * @return the values at the given 1°x1° segment, indexed as [x * {@link #getValuesPerDegree()} + z]
     */
    ArrayType getDataAtDegree(int lon, int lat) throws IOException;

    /**
     * Gets the number of values per degree of arc.
     *
     * @return the number of values per degree of arc
     */
    int getValuesPerDegree();
}
