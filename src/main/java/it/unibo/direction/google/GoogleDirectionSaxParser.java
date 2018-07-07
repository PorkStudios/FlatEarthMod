package it.unibo.direction.google;

import it.unibo.direction.DirectionParser;
import it.unibo.entity.Route;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.List;

/**
 *
 * @author Simone Rondelli - simone.rondelli2@studio.it.unibo.it
 *
 */
public class GoogleDirectionSaxParser implements DirectionParser {

    private final SAXParser parser;

	public GoogleDirectionSaxParser() throws Exception {
        this.parser = SAXParserFactory.newInstance().newSAXParser();
	}

	@Override
	public List<Route> getRoutes(InputStream is) throws Exception {
		GoogleDirectionHandler handler = new GoogleDirectionHandler();
        this.parser.parse(is, handler);
		return handler.getRoutes();
	}

}
