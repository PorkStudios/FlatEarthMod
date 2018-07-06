package it.unibo.elevation.mapquest;

import it.unibo.elevation.AbstractWebElevation;
import it.unibo.elevation.ElevationParser;
import it.unibo.elevation.google.GoogleElevationSaxParser;
import it.unibo.entity.GeoPoint;

import java.util.List;

/**
 * 
 * @deprecated Experimental, currently there are no further developments.
 * Use instead Google or SRTM implementation
 * 
 * @author Simone Rondelli - simone.rondelli2@studio.unibo.it
 */
@Deprecated
public class MapQuestElevationAPI extends AbstractWebElevation {

	public static final String BASE_URL = "http://open.mapquestapi.com/elevation/v1/profile";

	public MapQuestElevationAPI() throws Exception {
		this(XML, new GoogleElevationSaxParser());
	}

	public MapQuestElevationAPI(String format, ElevationParser parser) {
		super(parser, format);
	}

	/**
	 * Build the base URL with specified output format. The default outShapeFormat is cmp
	 * (polylines)
	 * <p>
	 * (ie:http://open.mapquestapi.com/elevation/v1/profile?outFormat=xml&unit=m
	 * &outShapeFormat=cmp)
	 * 
	 * @return The base url with format
	 */
	protected String buildBaseUrl() {
		return BASE_URL + "?outFormat=" + getFormat() + "&unit=m&outShapeFormat=cmp";
	}

	@Override
	public String buildUrl(double lat, double lon) {
		StringBuffer url = new StringBuffer(buildBaseUrl());
		url.append("&latLngCollection=" + lat + COMMA + lon);
		return url.toString();
	}

	@Override
	public String buildUrl(List<GeoPoint> points) {
		StringBuffer url = new StringBuffer(buildBaseUrl());
		int size = points.size();
		url.append("&latLngCollection=");
		for (int i = 0; i < size; i++) {
			url.append(points.get(i).getLatitude() + COMMA + points.get(i).getLongitude() + (i < (size - 1) ? COMMA : ""));
		}
		return url.toString();
	}

	@Override
	public String buildUrl(String polyline) {
		StringBuffer url = new StringBuffer(buildBaseUrl());
		url.append("&inShapeFormat=cmp");
		url.append("&latLngCollection=" + polyline);
		return url.toString();
	}

}
