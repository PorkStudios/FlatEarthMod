package it.unibo.util;

import it.unibo.entity.GeoPoint;

/**
 * Some useful function
 * 
 * @see http://www.movable-type.co.uk/scripts/latlong.html
 * 
 * @author Simone Rondelli - simone.rondelli2@studio.unibo.it
 */
public class GeoUtil {

	/**
	 * Mean radius defined by International Union of Geodesy and Geophysics in meters
	 */
	public static final double EARTH_RADIUS = 6371009;

	/**
	 * This uses the "haversine" formula to calculate the great-circle distance between two points
	 * that is, the shortest distance over the earth’s surface giving an ‘as-the-crow-flies’
	 * distance between the points (ignoring any hills, of course!).
	 * 
	 * The haversine formula ‘remains particularly well-conditioned for numerical computation even
	 * at small distances’ unlike calculations based on the spherical law of cosines. The ‘versed
	 * sine’ is 1-cosθ, and the ‘half-versed-sine’ (1-cosθ)/2 = sin²(θ/2) as used above. It was
	 * published by Roger Sinnott in Sky & Telescope magazine in 1984 (“Virtues of the Haversine”),
	 * though known about for much longer by navigators. (For the curious, c is the angular distance
	 * in radians, and a is the square of half the chord length between the points). A (surprisingly
	 * marginal) performance improvement can be obtained, of course, by factoring out the terms
	 * which get squared.
	 * 
	 * @return distance in meters
	 */
	public static double getHaversineDistance(GeoPoint g1, GeoPoint g2) {
		double dLat = toRad(g2.getLatitude() - g1.getLatitude());
		double dLon = toRad(g2.getLongitude() - g1.getLongitude());
		double lat1 = toRad(g1.getLatitude());
		double lat2 = toRad(g2.getLatitude());

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		return EARTH_RADIUS * c;
	}

	/**
	 * Law of cosines is a reasonable 1-line alternative to the haversine formula for many purposes.
	 * The choice may be driven by coding context, available trig functions (in different
	 * languages), etc.
	 * 
	 * @return distance in meters
	 */
	public static double getSphericalLawOfCosinesDistance(GeoPoint g1, GeoPoint g2) {
		return Math.acos(Math.sin(toRad(g1.getLatitude())) * Math.sin(toRad(g2.getLatitude())) + Math.cos(toRad(g1.getLatitude())) * Math.cos(toRad(g2.getLatitude())) * Math.cos(toRad(g2.getLongitude()) - toRad(g1.getLongitude()))) * EARTH_RADIUS;
	}

	/**
	 * If performance is an issue and accuracy less important, for small distances Pythagoras’
	 * theorem can be used on an equirectangular projection:
	 * 
	 * @return distance in meters
	 */
	public static double getEquirectangularApproximationDistance(GeoPoint g1, GeoPoint g2) {
		double x = (toRad(g2.getLongitude()) - toRad(g1.getLongitude())) * Math.cos((toRad(g1.getLatitude()) + toRad(g2.getLatitude())) / 2);
		double y = (toRad(g2.getLatitude()) - toRad(g1.getLatitude()));
		return Math.sqrt(x * x + y * y) * EARTH_RADIUS;
	}

	public static double toRad(double a) {
		return a * Math.PI / 180;
	}

	/**
	 * Inclination between the points.
	 * 
	 * @return angle in degree, could be negative
	 */
	public static double getInclinationDegree(GeoPoint g1, GeoPoint g2) {
		double elevation = g2.getElevation() - g1.getElevation();

		// avoid NaN
		if (elevation == 0) {
			return 0;
		}

		double distance = getEquirectangularApproximationDistance(g1, g2);

		// avoid NaN maybe should throw an IllegalStateException because is impossible
		// that the same point have different elevation
		if (distance == 0) {
			return 0;
		}

		return Math.toDegrees(Math.atan(elevation / distance));
	}

	/**
	 * Return the slope percentage between 2 points
	 * 
	 * @return slope percentage, could be negative
	 */
	public static double getSlopePercentage(GeoPoint g1, GeoPoint g2) {
		double elevation = g2.getElevation() - g1.getElevation();

		// avoid NaN
		if (elevation == 0) {
			return 0;
		}

		double distance = getEquirectangularApproximationDistance(g1, g2);

		// avoid NaN maybe should throw an IllegalStateException because is impossible
		// that the same point have different elevation
		if (distance == 0) {
			return 0;
		}
		return 100 * (elevation / distance);
	}

}
