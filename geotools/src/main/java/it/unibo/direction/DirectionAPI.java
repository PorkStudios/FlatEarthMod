package it.unibo.direction;

import it.unibo.entity.GeoPoint;
import it.unibo.entity.Route;

import java.util.List;

/**
 * 
 * @author Simone Rondelli - simone.rondelli2@studio.unibo.it
 *
 */
public interface DirectionAPI {

	public List<Route> getDirectionsBetween(String from, String to) throws Exception;

	public List<Route> getDirectionsBetween(GeoPoint from, GeoPoint to) throws Exception;

	public List<Route> getDirectionsBetween(String from, String to, List<String> waypoints) throws Exception;

	public List<Route> getDirectionsBetween(GeoPoint from, GeoPoint to, List<GeoPoint> waypoints) throws Exception;

}
