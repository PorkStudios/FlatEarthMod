package it.unibo.elevation.google;

import it.unibo.elevation.AbstractElevationHandler;
import it.unibo.entity.GeoPoint;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * 
 * @author Simone Rondelli - simone.rondelli2@studio.unibo.it
 *
 */
public class GoogleElevationHandler extends AbstractElevationHandler {

	private static final String RESULT = "result";
	private static final String LATITUDE = "lat";
	private static final String LONGITUDE = "lng";
	private static final String ELEVATION = "elevation";
	private static final String RESOLUTION = "resolution";

	/**
	 * @see it.unibo.elevation.AbstractElevationHandler#AbstractElevationHandler()
	 */
	public GoogleElevationHandler() {
		super();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (RESULT.equals(qName)) {
			tmpGeoPoint = new GeoPoint();
		} else {
			tmpString = new String("");
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (LATITUDE.equals(qName)) {
			tmpGeoPoint.setLatitude(Double.parseDouble(tmpString));
		} else if (LONGITUDE.equals(qName)) {
			tmpGeoPoint.setLongitude(Double.parseDouble(tmpString));
		} else if (ELEVATION.equals(qName)) {
			tmpGeoPoint.setElevation(Double.parseDouble(tmpString));
		} else if (RESOLUTION.equals(qName)) {
			tmpGeoPoint.setResolution(Double.parseDouble(tmpString));
		} else if (RESULT.equals(qName)) {
			geoPoints.add(tmpGeoPoint);
		}
	}
}
