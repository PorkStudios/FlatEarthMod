package it.unibo.elevation.google;

import it.unibo.elevation.AbstractElevationHandler;
import it.unibo.entity.GeoPoint;
import org.xml.sax.Attributes;

/**
 *
 * @author Simone Rondelli - simone.rondelli2@studio.it.unibo.it
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
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
		if (RESULT.equals(qName)) {
            this.tmpGeoPoint = new GeoPoint();
		} else {
            this.tmpString = new String("");
		}
	}

	@Override
    public void endElement(String uri, String localName, String qName) {
		if (LATITUDE.equals(qName)) {
            this.tmpGeoPoint.setLatitude(Double.parseDouble(this.tmpString));
		} else if (LONGITUDE.equals(qName)) {
            this.tmpGeoPoint.setLongitude(Double.parseDouble(this.tmpString));
		} else if (ELEVATION.equals(qName)) {
            this.tmpGeoPoint.setElevation(Double.parseDouble(this.tmpString));
		} else if (RESOLUTION.equals(qName)) {
            this.tmpGeoPoint.setResolution(Double.parseDouble(this.tmpString));
		} else if (RESULT.equals(qName)) {
            this.geoPoints.add(this.tmpGeoPoint);
		}
	}
}
