package it.unibo.direction;

import it.unibo.entity.GeoPoint;
import it.unibo.entity.Leg;
import it.unibo.entity.Route;
import it.unibo.entity.Step;
import it.unibo.util.Polylines;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Simone Rondelli - simone.rondelli2@studio.unibo.it
 *
 */
public class DirectionUtil {
	
	public static List<GeoPoint> getAllRoutePoints(Route route) {
		List<GeoPoint> points = new ArrayList<GeoPoint>();
		for (Leg leg : route.getLegs()) {
			for(Step step : leg.getSteps()) {
				points.addAll(Polylines.decode(step.getPolyline()));
			}
		}
		return points;
	}

}
