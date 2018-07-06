package it.unibo.entity;

import java.util.List;

/**
 * 
 * @author Simone Rondelli - simone.rondelli2@studio.unibo.it
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
		return legs;
	}

	public void setLegs(List<Leg> legs) {
		this.legs = legs;
	}

	public GeoPoint getSouthWestBound() {
		return southWestBound;
	}

	public void setSouthWestBound(GeoPoint southWestBound) {
		this.southWestBound = southWestBound;
	}

	public GeoPoint getNorthEastBound() {
		return northEastBound;
	}

	public void setNorthEastBound(GeoPoint northEastBound) {
		this.northEastBound = northEastBound;
	}

	public void setOverviewPolyline(String overviewPolyline) {
		this.overviewPolyline = overviewPolyline;
	}

	public String getOverviewPolyline() {
		return overviewPolyline;
	}

	public String getCopyrights() {
		return copyrights;
	}

	public void setCopyrights(String copyrights) {
		this.copyrights = copyrights;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	@Override
	public String toString() {
		return "Route [legs=" + legs + ", southWestBound=" + southWestBound + ", northEastBound=" + northEastBound + "]";
	}
}
