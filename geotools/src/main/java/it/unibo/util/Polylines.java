package it.unibo.util;

import it.unibo.entity.GeoPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Simone Rondelli - simone.rondelli2@studio.unibo.it
 * 
 */
public class Polylines {

	public static List<GeoPoint> decode(String encoded) {

		List<GeoPoint> poly = new ArrayList<GeoPoint>();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;
			GeoPoint p = new GeoPoint(lat / 1E5, lng / 1E5);
			poly.add(p);
		}

		return poly;
	}

	public static String encode(List<GeoPoint> points) {
		int oldLat = 0, oldLng = 0;
		int len = points.size(), index = 0;
		String encoded = "";
		while (index < len) {
			// Round to N decimal places
			int lat = floor1e5(points.get(index).getLatitude());
			int lng = floor1e5(points.get(index).getLongitude());

			// Encode the differences between the points
			encoded += encodeSignedNumber(lat - oldLat);
			encoded += encodeSignedNumber(lng - oldLng);

			oldLat = lat;
			oldLng = lng;
			index++;
		}
		return encoded;
	}

	private static String encodeNumber(int num) {

		StringBuffer encodeString = new StringBuffer();

		while (num >= 0x20) {
			int nextValue = (0x20 | (num & 0x1f)) + 63;
			encodeString.append((char) (nextValue));
			num >>= 5;
		}

		num += 63;
		encodeString.append((char) (num));

		return encodeString.toString();
	}

	public static String encodeSignedNumber(int num) {
//	int num = inNum << 1;
//	if (num < 0) {
//	    num = ~(num);
//	}
//	String encoded = "";
//	while (num >= 0x20) {
//	    encoded += fromCharCode((0x20 | (num & 0x1f)) + 63);
//	    num >>= 5;
//	}
//	encoded += fromCharCode(num + 63);
//	return encoded;
		int sgn_num = num << 1;
		if (num < 0) {
			sgn_num = ~(sgn_num);
		}
		return (encodeNumber(sgn_num));
	}

	private static int floor1e5(double coordinate) {
		return (int) Math.floor(coordinate * 1e5);
	}

}
