package net.daporkchop.realmapcc.data.client;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.math.arrays.grid.Grid2d;
import net.daporkchop.realmapcc.Constants;
import net.daporkchop.realmapcc.data.Tile;
import net.daporkchop.realmapcc.data.client.lookup.TileLookup;
import net.daporkchop.realmapcc.util.LookupCalculator;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
@Getter
public class LookupGrid2d implements Grid2d, Constants {
    @NonNull
    protected final TileLookup lookup;
    @NonNull
    protected final LookupCalculator calculator;

    @Override
    public int startX() {
        return LATITUDE_MIN * ARCSECONDS_PER_DEGREE;
    }

    @Override
    public int endX() {
        return (LATITUDE_MAX + 1) * ARCSECONDS_PER_DEGREE - 1;
    }

    @Override
    public int startY() {
        return LATITUDE_MIN * ARCSECONDS_PER_DEGREE;
    }

    @Override
    public int endY() {
        return (LATITUDE_MAX + 1) * ARCSECONDS_PER_DEGREE - 1;
    }

    @Override
    public int getI(int x, int y) {
        Tile tile = this.lookup.getTile(
                x / ARCSECONDS_PER_DEGREE,
                y / ARCSECONDS_PER_DEGREE,
                Constants.mod(x / TILE_SIZE, STEPS_PER_DEGREE),
                Constants.mod(y / TILE_SIZE, STEPS_PER_DEGREE)
        );
        return this.calculator.apply(tile, Constants.mod(x, TILE_SIZE), Constants.mod(y, TILE_SIZE));
    }

    @Override
    public double getD(int x, int y) {
        return this.getI(x, y);
    }

    @Override
    public void setD(int x, int y, double val) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setI(int x, int y, int val) {
        throw new UnsupportedOperationException();
    }
}
