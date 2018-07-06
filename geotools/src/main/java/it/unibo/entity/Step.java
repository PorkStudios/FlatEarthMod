package it.unibo.entity;

/**
 * 
 * @author Simone Rondelli - simone.rondelli2@studio.unibo.it
 *
 */
public class Step {

	private GeoPoint startLocation;
	private GeoPoint endLocation;
	/** Duration in seconds */
	private int duration;
	/** Distance in meter */
	private int distance;
	/** String that rapresents a polylines */
	private String polyline;
	/** Html instructions */
	private String htmlInstructions;

	public GeoPoint getStartLocation() {
		return startLocation;
	}

	public void setStartLocation(GeoPoint startLocation) {
		this.startLocation = startLocation;
	}

	public GeoPoint getEndLocation() {
		return endLocation;
	}

	public void setEndLocation(GeoPoint endLocation) {
		this.endLocation = endLocation;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public String getPolyline() {
		return polyline;
	}

	public void setPolyline(String polyline) {
		this.polyline = polyline;
	}

	public String getHtmlInstructions() {
		return htmlInstructions;
	}

	public void setHtmlInstructions(String htmlInstructions) {
		this.htmlInstructions = htmlInstructions;
	}

	@Override
	public String toString() {
		return "Step [startLocation=" + startLocation + ", endLocation=" + endLocation + ", duration=" + duration + ", distance=" + distance + ", polyline=" + polyline + "]";
	}

}
