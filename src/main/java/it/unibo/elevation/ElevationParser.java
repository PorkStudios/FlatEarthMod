package it.unibo.elevation;

import it.unibo.entity.GeoPoint;

import java.io.InputStream;
import java.util.List;

/**
 * Interface that defines how the parser have to work.
 *
 * @author Simone Rondelli - simone.rondelli2@studio.it.unibo.it
 */
public interface ElevationParser {

    double getElevation(InputStream is) throws Exception;

    double[] getElevations(InputStream is) throws Exception;

    GeoPoint getPoint(InputStream is) throws Exception;

    List<GeoPoint> getPoints(InputStream is) throws Exception;

}
