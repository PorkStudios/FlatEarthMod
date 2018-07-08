package it.unibo.direction.google;

import it.unibo.entity.GeoPoint;
import it.unibo.entity.Leg;
import it.unibo.entity.Route;
import it.unibo.entity.Step;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Simone Rondelli - simone.rondelli2@studio.it.unibo.it
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
    private final List<Route> routes;
    private String tmpString = "";
    private String tmpValue = "";
    private GeoPoint tmpLocation;
    private Step tmpStep;
    private Leg tmpLeg;
    private Route tmpRoute;
    private List<Step> steps;
    private List<Leg> legs;

    public GoogleDirectionHandler() {
        this.routes = new ArrayList<Route>();
        this.steps = new ArrayList<Step>();
        this.legs = new ArrayList<Leg>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (START_LOCATION.equals(qName)) {
            this.tmpLocation = new GeoPoint();
        } else if (END_LOCATION.equals(qName)) {
            this.tmpLocation = new GeoPoint();
        } else if (STEP.equals(qName)) {
            this.tmpStep = new Step();
            this.tmpLeg.setSteps(this.steps);
        } else if (LEG.equals(qName)) {
            this.tmpLeg = new Leg();
            this.steps = new ArrayList<Step>();
            this.tmpRoute.setLegs(this.legs);
        } else if (ROUTE.equals(qName)) {
            this.tmpRoute = new Route();
            this.legs = new ArrayList<Leg>();
            this.tmpRoute.setLegs(this.legs);
        }
        this.tmpString = new String();
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        String chars = new String(ch, start, length).trim();
        this.tmpString = this.tmpString.concat(chars);
    }

    @Override
    public void endElement(String uri, String localName, String qName) {

        if (this.tmpStep != null) {
            if (LATITUDE.equals(qName)) {
                this.tmpLocation.setLatitude(Float.parseFloat(this.tmpString));
            } else if (LONGITUDE.equals(qName)) {
                this.tmpLocation.setLongitude(Float.parseFloat(this.tmpString));
            } else if (START_LOCATION.equals(qName)) {
                this.tmpStep.setStartLocation(this.tmpLocation);
            } else if (END_LOCATION.equals(qName)) {
                this.tmpStep.setEndLocation(this.tmpLocation);
            } else if (POINTS.equals(qName)) {
                this.tmpStep.setPolyline(this.tmpString);
            } else if (VALUE.equals(qName)) {
                this.tmpValue = this.tmpString;
            } else if (DURATION.equals(qName)) {
                this.tmpStep.setDuration(Integer.parseInt(this.tmpValue));
            } else if (DISTANCE.equals(qName)) {
                this.tmpStep.setDistance(Integer.parseInt(this.tmpValue));
            } else if (HTML.equals(qName)) {
                this.tmpStep.setHtmlInstructions(this.tmpString);
            } else if (STEP.equals(qName)) {
                this.steps.add(this.tmpStep);
                this.tmpStep = null;
            }
        } else if (this.tmpLeg != null) {
            if (START_LOCATION.equals(qName)) {
                this.tmpLeg.setStartLocation(this.tmpLocation);
            } else if (END_LOCATION.equals(qName)) {
                this.tmpLeg.setEndLocation(this.tmpLocation);
            } else if (START_ADDRESS.equals(qName)) {
                this.tmpLeg.setStartAddress(this.tmpString);
            } else if (END_ADDRESS.equals(qName)) {
                this.tmpLeg.setEndAddress(this.tmpString);
            } else if (DURATION.equals(qName)) {
                this.tmpLeg.setDuration(Integer.parseInt(this.tmpValue));
            } else if (DISTANCE.equals(qName)) {
                this.tmpLeg.setDistance(Integer.parseInt(this.tmpValue));
            } else if (VALUE.equals(qName)) {
                this.tmpValue = this.tmpString;
            } else if (LEG.equals(qName)) {
                this.legs.add(this.tmpLeg);
                this.tmpLeg = null;
            }
        } else if (this.tmpRoute != null) {
            if (SOUTWEST.equals(qName)) {
                this.tmpRoute.setSouthWestBound(this.tmpLocation);
            } else if (NORTHEAST.equals(qName)) {
                this.tmpRoute.setNorthEastBound(this.tmpLocation);
            } else if (POINTS.equals(qName)) {
                this.tmpRoute.setOverviewPolyline(this.tmpString);
            } else if (COPYRIGHTS.equals(qName)) {
                this.tmpRoute.setCopyrights(this.tmpString);
            } else if (SUMMARY.equals(qName)) {
                this.tmpRoute.setSummary(this.tmpString);
            } else if (ROUTE.equals(qName)) {
                this.routes.add(this.tmpRoute);
                this.tmpRoute = null;
            }
        }
    }

    public List<Route> getRoutes() {
        return this.routes;
    }
}