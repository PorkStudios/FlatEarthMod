package it.unibo.direction.google;

import it.unibo.direction.AbstractWebDirection;
import it.unibo.direction.DirectionParser;
import it.unibo.entity.GeoPoint;

import java.util.List;

/**
 * @see http://cesaregerbino.wordpress.com/tag/google-map-api/
 * 
 * @author Simone Rondelli - simone.rondelli2@studio.unibo.it
 */
public class GoogleDirectionApi extends AbstractWebDirection {

	public static final String BASE_URL = "https://maps.googleapis.com/maps/api/directions/";
	public boolean alternatives;

	public GoogleDirectionApi() throws Exception {
		this(XML, true, new GoogleDirectionSaxParser());
	}

	public GoogleDirectionApi(String format, boolean alternatives, DirectionParser parser) {
		super(XML, parser);
		alternatives = true;
	}

	@Override
	public String buildUrl(GeoPoint from, GeoPoint to) {
		StringBuffer url = new StringBuffer(buildBaseUrl());
		url.append("origin=" + Double.toString(from.getLatitude()) + COMMA + Double.toString(from.getLongitude()));
		url.append("&destination=" + Double.toString(to.getLatitude()) + COMMA + Double.toString(to.getLongitude()));
		url.append("&sensor=true");
		url.append("&units=metric");
		url.append("&alternatives=" + alternatives);
		return url.toString();
	}

	@Override
	public String buildUrl(GeoPoint from, GeoPoint to, List<GeoPoint> waypoints) {
		StringBuffer url = new StringBuffer(buildBaseUrl());
		url.append("origin=" + Double.toString(from.getLatitude()) + COMMA + Double.toString(from.getLongitude()));
		url.append("&destination=" + Double.toString(to.getLatitude()) + COMMA + Double.toString(to.getLongitude()));
		url.append("&sensor=true");
		url.append("&units=metric");
		url.append("&alternatives=" + alternatives);
		url.append("&waypoints=");
		int size = waypoints.size();

		for (int i = 0; i < size; i++) {
			url.append(waypoints.get(i).getLatitude() + COMMA + waypoints.get(i).getLongitude() + (i < (size - 1) ? PIPE : ""));
		}
		return url.toString();
	}

	@Override
	public String buildUrl(String from, String to) {
		StringBuffer url = new StringBuffer(buildBaseUrl());
		url.append("origin=" + from);
		url.append("&destination=" + to);
		url.append("&sensor=true");
		url.append("&units=metric");
		return url.toString();
	}

	@Override
	public String buildUrl(String from, String to, List<String> waypoints) {
		StringBuffer url = new StringBuffer(buildBaseUrl());
		url.append("origin=" + from);
		url.append("&destination=" + to);
		url.append("&sensor=true");
		url.append("&units=metric");

		int size = waypoints.size();

		for (int i = 0; i < size; i++) {
			url.append(waypoints.get(i) + (i < (size - 1) ? PIPE : ""));
		}

		return url.toString();
	}

	/**
	 * Build the base URL with specified output format
	 * <p>
	 * (ie: http://maps.googleapis.com/maps/api/elevation/xml?)
	 * 
	 * 
	 * @return The base url with format
	 */
	private String buildBaseUrl() {
		return BASE_URL + getFormat() + "?";
	}

}
