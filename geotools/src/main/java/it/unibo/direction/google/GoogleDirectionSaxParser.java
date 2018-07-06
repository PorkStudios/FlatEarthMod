package it.unibo.direction.google;

import it.unibo.direction.DirectionParser;
import it.unibo.entity.Route;

import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * 
 * @author Simone Rondelli - simone.rondelli2@studio.unibo.it
 *
 */
public class GoogleDirectionSaxParser implements DirectionParser {

	private SAXParser parser;

	public GoogleDirectionSaxParser() throws Exception {
		parser = SAXParserFactory.newInstance().newSAXParser();
	}

	@Override
	public List<Route> getRoutes(InputStream is) throws Exception {
		GoogleDirectionHandler handler = new GoogleDirectionHandler();
		parser.parse(is, handler);
		return handler.getRoutes();
	}

}
