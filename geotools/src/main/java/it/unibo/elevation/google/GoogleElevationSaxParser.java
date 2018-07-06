package it.unibo.elevation.google;

import it.unibo.elevation.ElevationParser;
import it.unibo.entity.GeoPoint;

import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Sax implementations of ElevationParser
 * 
 * @author Simone Rondelli - simone.rondelli2@studio.unibo.it
 */
public class GoogleElevationSaxParser implements ElevationParser {

	private SAXParser parser;

	public GoogleElevationSaxParser() throws Exception {
		parser = SAXParserFactory.newInstance().newSAXParser();
	}

	@Override
	public double getElevation(InputStream is) throws Exception {
		GoogleElevationHandler handler = new GoogleElevationHandler();
		parser.parse(is, handler);
		return handler.getLastGeoPoint().getElevation();
	}

	@Override
	public double[] getElevations(InputStream is) throws Exception {
		GoogleElevationHandler handler = new GoogleElevationHandler();
		parser.parse(is, handler);
		List<GeoPoint> geoPoints = handler.getGeoPoints();
		double[] elevations = new double[geoPoints.size()];
		for (int i = 0; i < elevations.length; i++) {
			elevations[i] = geoPoints.get(i).getElevation();
		}
		return elevations;
	}

	@Override
	public GeoPoint getPoint(InputStream is) throws Exception {
		GoogleElevationHandler handler = new GoogleElevationHandler();
		parser.parse(is, handler);
		return handler.getLastGeoPoint();
	}

	@Override
	public List<GeoPoint> getPoints(InputStream is) throws Exception {
		GoogleElevationHandler handler = new GoogleElevationHandler();
		parser.parse(is, handler);
		return handler.getGeoPoints();
	}

}
