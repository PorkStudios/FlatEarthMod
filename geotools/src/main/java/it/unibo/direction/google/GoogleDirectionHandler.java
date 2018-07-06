package it.unibo.direction.google;

import it.unibo.entity.GeoPoint;
import it.unibo.entity.Leg;
import it.unibo.entity.Route;
import it.unibo.entity.Step;

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
public class GoogleDirectionHandler extends DefaultHandler {

	private static final String ROUTE = "route";
	private static final String LEG = "leg";
	private static final String STEP = "step";
	private static final String SOUTWEST = "southwest";
	private static final String NORTHEAST = "northeast";
	private static final String START_LOCATION = "start_location";
	private static final String END_LOCATION = "end_location";
	private static final String POINTS = "points";
	private static final String LATITUDE = "lat";
	private static final String LONGITUDE = "lng";
	private static final String DURATION = "duration";
	private static final String DISTANCE = "distance";
	private static final String VALUE = "value";
	private static final String COPYRIGHTS = "copyrights";
	private static final String HTML = "html_instructions";
	private static final String SUMMARY = "summary";
	private static final String START_ADDRESS = "start_address";
	private static final String END_ADDRESS = "end_address";

	private String tmpString = "";
	private String tmpValue = "";
	private GeoPoint tmpLocation;
	private Step tmpStep;
	private Leg tmpLeg;
	private Route tmpRoute;
	private List<Step> steps;
	private List<Leg> legs;
	private List<Route> routes;

	public GoogleDirectionHandler() {
		routes = new ArrayList<Route>();
		steps = new ArrayList<Step>();
		legs = new ArrayList<Leg>();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (START_LOCATION.equals(qName)) {
			tmpLocation = new GeoPoint();
		} else if (END_LOCATION.equals(qName)) {
			tmpLocation = new GeoPoint();
		} else if (STEP.equals(qName)) {
			tmpStep = new Step();
			tmpLeg.setSteps(steps);
		} else if (LEG.equals(qName)) {
			tmpLeg = new Leg();
			steps = new ArrayList<Step>();
			tmpRoute.setLegs(legs);
		} else if (ROUTE.equals(qName)) {
			tmpRoute = new Route();
			legs = new ArrayList<Leg>();
			tmpRoute.setLegs(legs);
		}
		tmpString = new String();
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		String chars = new String(ch, start, length).trim();
		tmpString = tmpString.concat(chars);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		if (tmpStep != null) {
			if (LATITUDE.equals(qName)) {
				tmpLocation.setLatitude(Float.parseFloat(tmpString));
			} else if (LONGITUDE.equals(qName)) {
				tmpLocation.setLongitude(Float.parseFloat(tmpString));
			} else if (START_LOCATION.equals(qName)) {
				tmpStep.setStartLocation(tmpLocation);
			} else if (END_LOCATION.equals(qName)) {
				tmpStep.setEndLocation(tmpLocation);
			} else if (POINTS.equals(qName)) {
				tmpStep.setPolyline(tmpString);
			} else if (VALUE.equals(qName)) {
				tmpValue = tmpString;
			} else if (DURATION.equals(qName)) {
				tmpStep.setDuration(Integer.parseInt(tmpValue));
			} else if (DISTANCE.equals(qName)) {
				tmpStep.setDistance(Integer.parseInt(tmpValue));
			} else if (HTML.equals(qName)) {
				tmpStep.setHtmlInstructions(tmpString);
			} else if (STEP.equals(qName)) {
				steps.add(tmpStep);
				tmpStep = null;
			}
		} else if (tmpLeg != null) {
			if (START_LOCATION.equals(qName)) {
				tmpLeg.setStartLocation(tmpLocation);
			} else if (END_LOCATION.equals(qName)) {
				tmpLeg.setEndLocation(tmpLocation);
			} else if (START_ADDRESS.equals(qName)) {
				tmpLeg.setStartAddress(tmpString);
			} else if (END_ADDRESS.equals(qName)) {
				tmpLeg.setEndAddress(tmpString);
			} else if (DURATION.equals(qName)) {
				tmpLeg.setDuration(Integer.parseInt(tmpValue));
			} else if (DISTANCE.equals(qName)) {
				tmpLeg.setDistance(Integer.parseInt(tmpValue));
			} else if (VALUE.equals(qName)) {
				tmpValue = tmpString;
			} else if (LEG.equals(qName)) {
				legs.add(tmpLeg);
				tmpLeg = null;
			}
		} else if (tmpRoute != null) {
			if (SOUTWEST.equals(qName)) {
				tmpRoute.setSouthWestBound(tmpLocation);
			} else if (NORTHEAST.equals(qName)) {
				tmpRoute.setNorthEastBound(tmpLocation);
			} else if (POINTS.equals(qName)) {
				tmpRoute.setOverviewPolyline(tmpString);
			} else if (COPYRIGHTS.equals(qName)) {
				tmpRoute.setCopyrights(tmpString);
			} else if (SUMMARY.equals(qName)) {
				tmpRoute.setSummary(tmpString);
			} else if (ROUTE.equals(qName)) {
				routes.add(tmpRoute);
				tmpRoute = null;
			}
		}
	}

	public List<Route> getRoutes() {
		return routes;
	}
}