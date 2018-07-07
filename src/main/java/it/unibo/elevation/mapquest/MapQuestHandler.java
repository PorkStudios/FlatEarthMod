package it.unibo.elevation.mapquest;

import it.unibo.elevation.AbstractElevationHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author Simone Rondelli - simone.rondelli2@studio.it.unibo.it
 *
 */
public class MapQuestHandler extends AbstractElevationHandler {

	@Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
		// TODO Auto-generated method stub
	}

	@Override
    public void endElement(String uri, String localName, String qName) {
		// TODO Auto-generated method stub
	}

	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub
		super.endDocument();
	}

}
