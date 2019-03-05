package net.daporkchop.realmapcc.data.converter.dataset.water.eu;

import net.daporkchop.realmapcc.Constants;

/**
 * @author DaPorkchop_
 */
public interface EUWaterConstants extends Constants {
    int EUWATER_SAMPLES_PER_IMAGE = 40000;
    int EUWATER_DEGREES_PER_IMAGE = 10;
    int EUWATER_SAMPLES_PER_DEGREE = EUWATER_SAMPLES_PER_IMAGE / EUWATER_DEGREES_PER_IMAGE;
    int EUWATER_SAMPLES_PER_TILE = EUWATER_SAMPLES_PER_DEGREE / STEPS_PER_DEGREE;
}
