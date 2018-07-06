package it.unibo.direction;

import static it.unibo.util.Network.getInputStream;
import it.unibo.entity.GeoPoint;
import it.unibo.entity.Route;

import java.io.InputStream;
import java.util.List;

/**
 * 
 * @author Simone Rondelli - simone.rondelli2@studio.unibo.it
 *
 */
public abstract class AbstractWebDirection implements DirectionAPI {

	private String format;

	public static final String XML = "xml";
	public static final String JSON = "json";

	public static final String PIPE = "%7C";
	public static final String COMMA = "%2C";

	private DirectionParser parser;

	public AbstractWebDirection(String format, DirectionParser parser) {
		this.format = format;
		this.parser = parser;
	}

	public abstract String buildUrl(GeoPoint from, GeoPoint to);

	public abstract String buildUrl(GeoPoint from, GeoPoint to, List<GeoPoint> waypoints);

	public abstract String buildUrl(String from, String to);

	public abstract String buildUrl(String from, String to, List<String> waypoints);

	@Override
	public List<Route> getDirectionsBetween(GeoPoint from, GeoPoint to) throws Exception {
		String url = buildUrl(from, to);
		InputStream is = getInputStream(url);
		return parser.getRoutes(is);
	}

	@Override
	public List<Route> getDirectionsBetween(GeoPoint from, GeoPoint to, List<GeoPoint> waypoints) throws Exception {
		String url = buildUrl(from, to, waypoints);
		InputStream is = getInputStream(url);
		return parser.getRoutes(is);
	}

	@Override
	public List<Route> getDirectionsBetween(String from, String to) throws Exception {
		String url = buildUrl(from, to);
		InputStream is = getInputStream(url);
		return parser.getRoutes(is);
	}

	@Override
	public List<Route> getDirectionsBetween(String from, String to, List<String> waypoints) throws Exception {
		String url = buildUrl(from, to, waypoints);
		InputStream is = getInputStream(url);
		return parser.getRoutes(is);
	}

	public String getFormat() {
		return format;
	}

}