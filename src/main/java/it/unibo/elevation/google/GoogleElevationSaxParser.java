package it.unibo.elevation.google;

import it.unibo.elevation.ElevationParser;
import it.unibo.entity.GeoPoint;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.List;

/**
 * Sax implementations of ElevationParser
 *
 * @author Simone Rondelli - simone.rondelli2@studio.it.unibo.it
 */
public class GoogleElevationSaxParser implements ElevationParser {

    private final SAXParser parser;

    public GoogleElevationSaxParser() throws Exception {
        this.parser = SAXParserFactory.newInstance().newSAXParser();
    }

    @Override
    public double getElevation(InputStream is) throws Exception {
        GoogleElevationHandler handler = new GoogleElevationHandler();
        this.parser.parse(is, handler);
        return handler.getLastGeoPoint().getElevation();
    }

    @Override
    public double[] getElevations(InputStream is) throws Exception {
        GoogleElevationHandler handler = new GoogleElevationHandler();
        this.parser.parse(is, handler);
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
        this.parser.parse(is, handler);
        return handler.getLastGeoPoint();
    }

    @Override
    public List<GeoPoint> getPoints(InputStream is) throws Exception {
        GoogleElevationHandler handler = new GoogleElevationHandler();
        this.parser.parse(is, handler);
        return handler.getGeoPoints();
    }

}
