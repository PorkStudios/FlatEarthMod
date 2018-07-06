package it.unibo.entity;

/**
 * 
 * @author Simone Rondelli - simone.rondelli2@studio.unibo.it
 *
 */
public class GeoPoint {

	private double latitude;
	private double longitude;
	private double elevation;
	private double resolution;

	public GeoPoint() {
	}

	public GeoPoint(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public GeoPoint(double latitude, double longitude, double altitude) {
		this.latitude = latitude;
		this.longitude = longitude;
		elevation = altitude;
	}

	public GeoPoint(double latitude, double longitude, double altitude, double resolution) {
		this.latitude = latitude;
		this.longitude = longitude;
		elevation = altitude;
		this.resolution = resolution;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getElevation() {
		return elevation;
	}

	public void setElevation(double elevation) {
		this.elevation = elevation;
	}

	public double getResolution() {
		return resolution;
	}

	public void setResolution(double resolution) {
		this.resolution = resolution;
	}

	/**
	 * Longitudine in micro-gradi (* 1E6)
	 * 
	 * @return longitude * 1E6
	 */
	public int getMicroLongitude() {
		return (int) (longitude * 1E6);
	}

	/**
	 * Latitudine in micro-gradi (* 1E6)
	 * 
	 * @return latitudine * 1E6
	 */
	public int getMicroLatitude() {
		return (int) (latitude * 1E6);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other instanceof GeoPoint) {
			GeoPoint oth = (GeoPoint) other;
			return oth.getLatitude() == latitude && oth.getLongitude() == longitude && oth.getElevation() == elevation;
		}
		return false;
	}

	@Override
	public String toString() {
		return "GeoPoint [lat=" + latitude + " lon=" + longitude + " ele=" + elevation + "]";
	}
}
