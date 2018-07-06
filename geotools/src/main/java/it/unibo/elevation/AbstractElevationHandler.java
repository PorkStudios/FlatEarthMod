package it.unibo.elevation;

import it.unibo.entity.GeoPoint;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * @author Simone Rondelli - simone.rondelli2@studio.unibo.it
 *
 */
public abstract class AbstractElevationHandler extends DefaultHandler {

	protected List<GeoPoint> geoPoints;
	protected double[] elevations;
	protected GeoPoint tmpGeoPoint;
	protected String tmpString;

	public AbstractElevationHandler() {
		geoPoints = new ArrayList<GeoPoint>();
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		String chars = new String(ch, start, length).trim();
		tmpString = tmpString.concat(chars);
	}

	@Override
	public abstract void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException;

	@Override
	public abstract void endElement(String uri, String localName, String qName) throws SAXException;

	/**
	 * Returns a list of parsed GeoPoints
	 * 
	 * @return parsed GeoPoints
	 */
	public List<GeoPoint> getGeoPoints() {
		return geoPoints;
	}

	/**
	 * Returns a single GeoPoint, the point is the last found from the parser, if we expect only a
	 * single point the point is this.
	 * 
	 * @return A single Point
	 */
	public GeoPoint getLastGeoPoint() {
		return tmpGeoPoint;
	}
}
