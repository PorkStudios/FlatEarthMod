package it.unibo.entity;

/**
 *
 * @author Simone Rondelli - simone.rondelli2@studio.it.unibo.it
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
        return this.startLocation;
	}

	public void setStartLocation(GeoPoint startLocation) {
		this.startLocation = startLocation;
	}

	public GeoPoint getEndLocation() {
        return this.endLocation;
	}

	public void setEndLocation(GeoPoint endLocation) {
		this.endLocation = endLocation;
	}

	public int getDuration() {
        return this.duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getDistance() {
        return this.distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public String getPolyline() {
        return this.polyline;
	}

	public void setPolyline(String polyline) {
		this.polyline = polyline;
	}

	public String getHtmlInstructions() {
        return this.htmlInstructions;
	}

	public void setHtmlInstructions(String htmlInstructions) {
		this.htmlInstructions = htmlInstructions;
	}

	@Override
	public String toString() {
        return "Step [startLocation=" + this.startLocation + ", endLocation=" + this.endLocation + ", duration=" + this.duration + ", distance=" + this.distance + ", polyline=" + this.polyline + "]";
	}

}
