package net.daporkchop.realmapcc.data.client.grid;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.math.arrays.grid.Grid2d;
import net.daporkchop.realmapcc.data.client.LookupCache;
import net.daporkchop.realmapcc.util.LookerUpper;
import net.daporkchop.realmapcc.util.LookupCalculator;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
@Getter
public class DelegatingTileCacheGrid implements Grid2d {
    @NonNull
    protected final LookupCache cache;
    @NonNull
    protected final LookerUpper delegate;

    @Override
    public int startX() {
        return this.cache.getLon();
    }

    @Override
    public int endX() {
        return this.cache.getLon() + this.cache.getW();
    }

    @Override
    public int startY() {
        return this.cache.getLat();
    }

    @Override
    public int endY() {
        return this.cache.getLat() + this.cache.getH();
    }

    @Override
    public double getD(int x, int y) {
        return this.getI(x, y);
    }

    @Override
    public int getI(int x, int y) {
        return this.delegate.apply(this.cache, x, y);
    }

    @Override
    public void setD(int x, int y, double val) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setI(int x, int y, int val) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOverflowing() {
        return true;
    }
}
