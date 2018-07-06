package it.unibo.elevation;

import static it.unibo.util.Network.getInputStream;
import it.unibo.entity.GeoPoint;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * This class is used for fetching elevation data from a web service like Google or MapQuest. The
 * subclasses must implement the build of the URL according to the web services specification.
 * 
 * @author Simone Rondelli - simone.rondelli2@studio.unibo.it
 */
public abstract class AbstractWebElevation implements ElevationAPI {

	private String format;

	public static final String XML = "xml";
	public static final String JSON = "json";

	public static final String PIPE = "%7C";
	public static final String COMMA = "%2C";

	private ElevationParser parser;

	/**
	 * Creates a AbstractWebElevation that gets the information from the specified Web Service, with
	 * the specified parser and specified format.
	 */
	public AbstractWebElevation(ElevationParser parser, String format) {
		this.format = XML;
		this.parser = parser;
	}

	protected abstract String buildUrl(double lat, double lon);

	protected abstract String buildUrl(List<GeoPoint> points);

	protected abstract String buildUrl(String polyline);

	/**
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	@Override
	public double getElevation(double lat, double lon) throws Exception {
		String url = buildUrl(lat, lon);
		InputStream is = getInputStream(url);
		return parser.getElevation(is);
	}

	/**
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	@Override
	public double getElevation(GeoPoint point) throws Exception {
		String url = buildUrl(point.getLatitude(), point.getLongitude());
		InputStream is = getInputStream(url);
		return parser.getElevation(is);
	}

	/**
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	@Override
	public void setElevation(GeoPoint point) throws Exception {
		String url = buildUrl(point.getLatitude(), point.getLongitude());
		InputStream is = getInputStream(url);
		point.setElevation(parser.getElevation(is));
	}

	/**
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	@Override
	public void setElevations(List<GeoPoint> points) throws Exception {
		String url = buildUrl(points);
		InputStream is = getInputStream(url);
		double[] elevation = parser.getElevations(is);
		for (int i = 0; i < elevation.length; i++) {
			points.get(i).setElevation(elevation[i]);
		}
	}

	/**
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	@Override
	public List<GeoPoint> getElevations(String polyline) throws Exception {
		String url = buildUrl(polyline);
		InputStream is = getInputStream(url);
		return parser.getPoints(is);
	}

	@Override
	public List<GeoPoint> getElevations(List<GeoPoint> points) throws Exception {
		String url = buildUrl(points);
		InputStream is = getInputStream(url);
		return parser.getPoints(is);
	}
	


	/**
	 * Returns the input format
	 * 
	 * @return input format
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * Set the format of the input, remember to set also the related parser
	 * 
	 * @param format <ul>
	 *            <li>AbstractWebElevation.XML</li>
	 *            <li>AbstractWebElevation.JSON</li>
	 *            </ul>
	 * @see #setParser(ElevationParser)
	 */
	public void setFormat(String format) {
		this.format = format;
	}
}
