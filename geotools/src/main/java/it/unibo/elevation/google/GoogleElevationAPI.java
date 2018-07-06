package it.unibo.elevation.google;

import it.unibo.elevation.AbstractWebElevation;
import it.unibo.elevation.ElevationParser;
import it.unibo.entity.GeoPoint;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Implementations of Google Elevation API. Actually works with the version 3
 * 
 * @author Simone Rondelli - simone.rondelli2@studio.unibo.it
 */
public class GoogleElevationAPI extends AbstractWebElevation {

	public static final String BASE_URL = "http://maps.googleapis.com/maps/api/elevation/";

	public GoogleElevationAPI() throws Exception {
		this(XML, new GoogleElevationSaxParser());
	}

	public GoogleElevationAPI(String format, ElevationParser parser) throws Exception {
		super(parser, format);
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

	@Override
	public String buildUrl(double lat, double lon) {
		StringBuffer url = new StringBuffer(buildBaseUrl());
		url.append("locations=" + lat + COMMA + lon);
		url.append("&sensor=false");
		return url.toString();
	}

	@Override
	public String buildUrl(String polyline) {
		StringBuffer url = new StringBuffer(buildBaseUrl());
		try {
			url.append("locations=enc:" + URLEncoder.encode(polyline, "UTF8"));
		} catch (UnsupportedEncodingException ex) {
			throw new IllegalStateException("Impossible to encode URL", ex);
		}
		url.append("&sensor=false");
		return url.toString();
	}

	@Override
	public String buildUrl(List<GeoPoint> points) {
		StringBuffer url = new StringBuffer(buildBaseUrl());
		url.append("locations=");
		int size = points.size();

		for (int i = 0; i < size; i++) {
			url.append(points.get(i).getLatitude() + COMMA + points.get(i).getLongitude() + (i < (size - 1) ? PIPE : ""));
		}

		url.append("&sensor=false");
		return url.toString();
	}
}
