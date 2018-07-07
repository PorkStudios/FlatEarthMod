package it.unibo.entity;

import java.util.List;

/**
 *
 * @author Simone Rondelli - simone.rondelli2@studio.it.unibo.it
 *
 */
public class Route {

	private List<Leg> legs;
	private GeoPoint southWestBound;
	private GeoPoint northEastBound;
	private String overviewPolyline;
	private String summary;

	private String copyrights;

	public Route() {
		// TODO Auto-generated constructor stub
	}

	public List<Leg> getLegs() {
        return this.legs;
	}

	public void setLegs(List<Leg> legs) {
		this.legs = legs;
	}

	public GeoPoint getSouthWestBound() {
        return this.southWestBound;
	}

	public void setSouthWestBound(GeoPoint southWestBound) {
		this.southWestBound = southWestBound;
	}

	public GeoPoint getNorthEastBound() {
        return this.northEastBound;
	}

	public void setNorthEastBound(GeoPoint northEastBound) {
		this.northEastBound = northEastBound;
	}

	public void setOverviewPolyline(String overviewPolyline) {
		this.overviewPolyline = overviewPolyline;
	}

	public String getOverviewPolyline() {
        return this.overviewPolyline;
	}

	public String getCopyrights() {
        return this.copyrights;
	}

	public void setCopyrights(String copyrights) {
		this.copyrights = copyrights;
	}

	public String getSummary() {
        return this.summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	@Override
	public String toString() {
        return "Route [legs=" + this.legs + ", southWestBound=" + this.southWestBound + ", northEastBound=" + this.northEastBound + "]";
	}
}
