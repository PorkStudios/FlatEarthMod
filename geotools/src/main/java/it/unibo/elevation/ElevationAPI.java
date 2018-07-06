package it.unibo.elevation;

import it.unibo.entity.GeoPoint;

import java.util.List;

/**
 * Defines the base method of Elevation API
 * 
 * @author Simone Rondelli - simone.rondelli2@studio.unibo.it
 */
public interface ElevationAPI {

	/**
	 * Get the elevation of a given latitude and longitude
	 * 
	 * @param lat Latitude
	 * @param lon Longitude
	 * @return The elevation of given point
	 */
	public double getElevation(double lat, double lon) throws Exception;

	/**
	 * Get the elevation of a given GeoPoint
	 * 
	 * @param lat Latitude
	 * @param lon Longitude
	 * @return The elevation of given point
	 */
	public double getElevation(GeoPoint p) throws Exception;

	/**
	 * Sets the elevation of the given Point
	 * 
	 * @param point GeoPoint
	 * @throws Exception Exceptions thrown by fetching data
	 */
	public void setElevation(GeoPoint point) throws Exception;

	/**
	 * Gets the elevation of the given points.
	 * 
	 * @param points List of GeoPoints
	 * @return List of GeoPoint that contains the elevations of given points
	 * @throws Exception Exceptions thrown by fetching data
	 */
	public List<GeoPoint> getElevations(List<GeoPoint> points) throws Exception;

	/**
	 * Sets the elevation of the given points
	 * 
	 * @param points Points
	 * @throws Exception Exceptions thrown by fetching data
	 */
	public void setElevations(List<GeoPoint> points) throws Exception;

	/**
	 * Gets the elevation of the points represented by the given polyline
	 * 
	 * @param polyline Points encoded with Polyline Algorithm
	 * @return Points with elevation
	 * @throws Exception Exceptions thrown by fetching data
	 */
	public List<GeoPoint> getElevations(String polyline) throws Exception;

}
