package net.daporkchop.realmapcc;

import org.apache.commons.imaging.ImageParser;
import org.apache.commons.imaging.formats.tiff.TiffImageParser;
import org.apache.commons.math3.analysis.interpolation.PiecewiseBicubicSplineInterpolator;

import java.io.File;

/**
 * @author DaPorkchop_
 */
public interface Constants {
    File rootDir = new File(".", "../mapData/");

    ImageParser tiffParser = new TiffImageParser();

    int minLongitude = -180;
    int maxLongitude = 180;
    int minLatitude = -56;
    int maxLatitude = 60;

    int SRTM_valuesPerDegree = 3600;
    int SRTM_subDegreeCount = 16;

    int GLOBCOVER_valuesPerDegree = 360;
    int GLOBCOVER_minLatitude = -65;
    int GLOBCOVER_maxLatitude = 90;

    int cpuCount = Runtime.getRuntime().availableProcessors();

    double spaceBetweenBlocks = 1.0d / 60.0d / 60.0d / 30.0d;
    double spaceBetweenChunks = 16.0d / 60.0d / 60.0d / 30.0d;

    PiecewiseBicubicSplineInterpolator interpolatorBicubic = new PiecewiseBicubicSplineInterpolator();
}
