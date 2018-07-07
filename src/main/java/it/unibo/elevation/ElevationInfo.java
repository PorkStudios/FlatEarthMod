package it.unibo.elevation;

import it.unibo.entity.GeoPoint;
import it.unibo.util.GeoUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Collect some elevation information about a given set of points
 *
 * @author Simone Rondelli - simone.rondelli2@studio.it.unibo.it
 * 
 */
public class ElevationInfo {

	public static final double DEFAULT_THRESHOLD = 2;

	private double downhill;
	private double uphill;
	private double flat;

	private final double cumulativeDownhillOffset;
	private final double cumulativeUphillOffset;
	private final double flatSectionMarginalOffset;
	private final double estimatedTotalOffsetInAltitude;

	private double averageDownhillSlope;
	private double averageUphillSlope;
	private double averageFlatMarginalSlope;

	private double averageSlope;

	private double maxDownhillSlope;
	private double maxUphillSlope;

	private final double threshold;

	private double totalDistance;

	private int precision;

	public ElevationInfo(List<GeoPoint> points) {
		this(points, DEFAULT_THRESHOLD);
	}

	public ElevationInfo(List<GeoPoint> points, double threshold) {
		this.threshold = threshold;
		this.totalDistance = 0;
		this.precision = 0;
		this.downhill = 0;
		this.uphill = 0;
		this.flat = 0;

		this.averageDownhillSlope = 0;
		this.averageUphillSlope = 0;
		this.averageFlatMarginalSlope = 0;

		this.averageSlope = 0;

		this.maxDownhillSlope = 0;
		this.maxUphillSlope = 0;

		GeoPoint prev = null;

		for (GeoPoint point : points) {
			if (prev != null) {
				double slope = GeoUtil.getSlopePercentage(prev, point);
				double distance = GeoUtil.getEquirectangularApproximationDistance(prev, point);

				this.averageSlope += slope * distance;
				this.totalDistance += distance;

				if (slope < -threshold) {
					this.downhill += distance;
					this.averageDownhillSlope += slope * distance;

					if (slope < this.maxDownhillSlope) {
						this.maxDownhillSlope = slope;
					}
				} else if (slope > threshold) {
					this.uphill += distance;
					this.averageUphillSlope += slope * distance;

					if (slope > this.maxUphillSlope) {
						this.maxUphillSlope = slope;
					}
				} else {
					this.flat += distance;
					this.averageFlatMarginalSlope += slope * distance;
				}

			}
			prev = point;
		}

		this.averageDownhillSlope /= this.downhill;
		this.averageUphillSlope /= this.uphill;
		this.averageFlatMarginalSlope /= this.flat;
		this.averageSlope /= this.totalDistance;

		this.cumulativeDownhillOffset = this.downhill * this.averageDownhillSlope / 100;
		this.cumulativeUphillOffset = this.uphill * this.averageUphillSlope / 100;
		this.flatSectionMarginalOffset = this.flat * this.averageFlatMarginalSlope / 100;
		//should be the difference between last point and first point
		this.estimatedTotalOffsetInAltitude = this.cumulativeDownhillOffset + this.cumulativeUphillOffset + this.flatSectionMarginalOffset;
	}

	/**
	 * Distance traveled downhill in m
	 */
	public double getDownhill() {
		return round(this.downhill, this.precision);
	}

	/**
	 * Distance traveled uphill in m
	 */
	public double getUphill() {
		return round(this.uphill, this.precision);
	}

	/**
	 * Distance traveled flat in m
	 */
	public double getFlat() {
		return round(this.flat, this.precision);
	}

	/**
	 * Maximum downhill slope encountered in percentage
	 */
	public double getMaxDownhillSlope() {
		return round(this.maxDownhillSlope, this.precision);
	}

	/**
	 * Maximum Uphill slope encountered in percentage
	 */
	public double getMaxUphillSlope() {
		return round(this.maxUphillSlope, this.precision);
	}

	/**
	 * Average downhill slope in percentage
	 */
	public double getAverageDownhillSlope() {
		return round(this.averageDownhillSlope, this.precision);
	}

	/**
	 * Average uphill slope in percentage
	 */
	public double getAverageUphillSlope() {
		return round(this.averageUphillSlope, this.precision);
	}

	/**
	 * Average flat marginal slope in percentage
	 */
	public double getAverageFlatMarginalSlope() {
		return round(this.averageFlatMarginalSlope, this.precision);
	}

	/**
	 * Average total average slope in percentage
	 */
	public double getAverageSlope() {
		return round(this.averageSlope, this.precision);
	}

	/**
	 * Is the threshold within which a slope is considered flat in percentage.
	 * <p>
	 * <b>downhill < -treshold <= flat <= treshold < uphill
	 */
	public double getThreshold() {
		return round(this.threshold, this.precision);
	}

	/**
	 * Total distance from strat point to end point in m
	 */
	public double getTotalDistance() {
		return round(this.totalDistance, this.precision);
	}

	public double getCumulativeDownhillOffset() {
		return round(this.cumulativeDownhillOffset, this.precision);
	}

	public double getFlatSectionMarginalOffset() {
		return round(this.flatSectionMarginalOffset, this.precision);
	}

	public double getCumulativeUphillOffset() {
		return round(this.cumulativeUphillOffset, this.precision);
	}

	public double getEstimatedTotalOffsetInAltitude() {
		return round(this.estimatedTotalOffsetInAltitude, this.precision);
	}

	public int getPrecision() {
		return this.precision;
	}

	/**
	 * The number of decimal places after the decimal point
	 * 
	 * @param precision: a number o decimal places or 0 for not round
	 */
	public void setPrecision(int precision) {
		this.precision = precision;
	}

	public static double round(double num, int digit) {
		if (digit == 0) {
			return num;
		}
		return new BigDecimal(num).setScale(digit, RoundingMode.HALF_EVEN).doubleValue();
	}
}
