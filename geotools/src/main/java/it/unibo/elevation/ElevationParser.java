package it.unibo.elevation;

import it.unibo.entity.GeoPoint;

import java.io.InputStream;
import java.util.List;

/**
 * Interface that defines how the parser have to work.
 * 
 * @author Simone Rondelli - simone.rondelli2@studio.unibo.it
 */
public interface ElevationParser {

	public double getElevation(InputStream is) throws Exception;

	public double[] getElevations(InputStream is) throws Exception;

	public GeoPoint getPoint(InputStream is) throws Exception;

	public List<GeoPoint> getPoints(InputStream is) throws Exception;

}
