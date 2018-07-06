package it.unibo.elevation.srtm;

import it.unibo.elevation.ElevationAPI;
import it.unibo.entity.GeoPoint;
import it.unibo.util.Polylines;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SrtmElevationAPI implements ElevationAPI {

	private SrtmHelper osmSrtm;

	/**
	 * Init the SRTM based ElevationApi
	 * 
	 * @param localDir The local folder that contains the .hgt or .zip srtm files
	 */
	public SrtmElevationAPI(File localDir) {
		osmSrtm = new SrtmHelper(localDir);
	}

	@Override
	public double getElevation(double lat, double lon) throws IOException {
		return osmSrtm.srtmHeight(lat, lon);
	}

	@Override
	public double getElevation(GeoPoint p) throws IOException {
		return osmSrtm.srtmHeight(p.getLatitude(), p.getLongitude());
	}

	@Override
	public void setElevation(GeoPoint p) throws IOException {
		p.setElevation(getElevation(p));
	}

	@Override
	public List<GeoPoint> getElevations(List<GeoPoint> points) throws IOException {
		List<GeoPoint> newPoints = new ArrayList<GeoPoint>(points.size());
		GeoPoint p;
		for (int i = 0; i < points.size(); i++) {
			p = new GeoPoint(points.get(i).getLatitude(), points.get(i).getLongitude());
			setElevation(p);
			newPoints.add(p);
		}
		return newPoints;
	}

	@Override
	public void setElevations(List<GeoPoint> points) throws IOException {
		for (GeoPoint p : points) {
			setElevation(p);
		}
	}

	@Override
	public List<GeoPoint> getElevations(String polyline) throws IOException {
		List<GeoPoint> points = Polylines.decode(polyline);
		setElevations(points);
		return points;
	}

}
