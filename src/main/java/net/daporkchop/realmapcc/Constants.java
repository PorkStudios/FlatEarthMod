package net.daporkchop.realmapcc;

import static java.lang.Math.abs;

/**
 * Some random constant values that are used throughout the program for easy access
 *
 * @author DaPorkchop_
 */
public interface Constants {
    //ImageParser tiffParser = new TiffImageParser();

    //the limits of the data that exists
    //only data inside of this region will be used
    int LONGITUDE_MAX = 180;
    int LATITUDE_MAX = 60;
    int LONGITUDE_MIN = -180;
    int LATITUDE_MIN = -56;

    //the total size of the usable data region (in °)
    int LON_DEGREES_TOTAL = abs(LONGITUDE_MIN - LONGITUDE_MAX);
    int LAT_DEGREES_TOTAL = abs(LATITUDE_MIN - LATITUDE_MAX);

    //the number of arc-seconds per degree of arc
    int ARCSECONDS_PER_DEGREE = 3600;

    //the total size of the usable data region (in arc-seconds)
    int LON_ARCSECONDS_TOTAL = LON_DEGREES_TOTAL * ARCSECONDS_PER_DEGREE;
    int LAT_ARCSECONDS_TOTAL = LAT_DEGREES_TOTAL * ARCSECONDS_PER_DEGREE;

    //the number of steps per degree of arc, a step being an ambiguously defined number
    // which represents the number of segments that each degree of arc will be split into
    // along both axes (i.e. each degree of arc will be split into 16² segments)
    int STEPS_PER_DEGREE = 16;

    //the total size of the usable data region (in steps)
    int LON_STEPS_TOTAL = LON_DEGREES_TOTAL * STEPS_PER_DEGREE;
    int LAT_STEPS_TOTAL = LAT_DEGREES_TOTAL * STEPS_PER_DEGREE;

    //the number of arc-seconds per step
    int ARCSECONDS_PER_STEP = ARCSECONDS_PER_DEGREE / STEPS_PER_DEGREE;

    int GLOBCOVER_valuesPerDegree = 360;
    int GLOBCOVER_minLatitude = -65;
    int GLOBCOVER_maxLatitude = 90;

    int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    double spaceBetweenBlocks = 1.0d / 60.0d / 60.0d / 30.0d;
    double spaceBetweenChunks = 16.0d / 60.0d / 60.0d / 30.0d;
}
