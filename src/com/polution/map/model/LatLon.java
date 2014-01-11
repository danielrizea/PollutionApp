
package com.polution.map.model;

public class LatLon {

	public static final int EARTH_RADIUS = 6371; // Earth Radius in KM

	public float lat;
	public float lon;

	public LatLon(float lat, float lon) {
		this.lat = lat;
		this.lon = lon;
	}

	public LatLon(int lat, int lon) {
		this.lat = lat / 1E6f;

		this.lon = lon / 1E6f;
	}

	/**
	 * Returns the haversine distance between this point and a target point
	 * 
	 * @param target
	 *            - End point
	 * @return - The distance in Kilometers
	 */
	public float distance(LatLon target) {
		float distance = 0f;

		float dlat = target.lat - this.lat;
		float dlon = target.lon - this.lon;
		double a = (Math.sin(Math.toRadians(dlat) / 2) * Math.sin(Math
				.toRadians(dlat) / 2))
				+ Math.cos(Math.toRadians(this.lat))
				* Math.cos(Math.toRadians(target.lat))
				* (Math.sin(Math.toRadians(dlon) / 2) * Math.sin(Math
						.toRadians(dlon) / 2));
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		distance = (float) (EARTH_RADIUS * c);
		return distance;
	}

	/**
	 * Calculates the endpoint from this point given a distance in meters and an
	 * angle
	 * 
	 * @param distance
	 *            - Distance from the start point in meters
	 * @param bearing
	 *            - Angle from the start point
	 * @return A new coordinate of the target point
	 */
	public LatLon getDestination(double distance, double bearing) {
		double d = distance / EARTH_RADIUS;
		double lat1 = Math.toRadians(this.lat);
		double lon1 = Math.toRadians(this.lon);
		double lat = Math.asin((Math.sin(lat1) * Math.cos(d))
				+ (Math.cos(lat1) * Math.sin(d) * Math.cos(Math
						.toRadians(bearing))));

		double lon = lon1
				+ Math.atan2(Math.sin(Math.toRadians(bearing)) * Math.sin(d)
						* Math.cos(lat1),
						Math.cos(d) - Math.sin(lat1) * Math.sin(lat));
		return new LatLon((float) Math.toDegrees(lat),
				(float) Math.toDegrees(lon));
	}

	/**
	 * Returns the (initial) bearing from this point to the supplied point, in
	 * degrees see http://williams.best.vwh.net/avform.htm#Crs
	 * 
	 * @param target
	 *            : Latitude/longitude of destination point
	 * @returns Initial bearing in degrees from North
	 */
	public float bearingTo(LatLon target) {
		float bearing = 0f;

		float dlon = target.lon - this.lon;
		Double lat1 = Math.toRadians(this.lat);
		Double lat2 = Math.toRadians(target.lat);
		Double y = Math.sin(Math.toRadians(dlon)) * Math.cos(lat1);
		Double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
				* Math.cos(lat2) * Math.cos(Math.toRadians(dlon));
		bearing = (float) Math.toDegrees(Math.atan2(y, x));

		return bearing;
	}

	/**
	 * Returns final bearing arriving at supplied destination point from this
	 * point; the final bearing will differ from the initial bearing by varying
	 * degrees according to distance and latitude
	 * 
	 * @param target
	 *            : Latitude/longitude of destination point
	 * @returns Final bearing in degrees from North
	 */
	public float finalBearingTo(LatLon target) {
		return bearingTo(target) + 180;
	}

	/**
	 * Returns the midpoint between this point and the supplied point. see
	 * http://mathforum.org/library/drmath/view/51822.html for derivation
	 * 
	 * @param target
	 *            : Latitude/longitude of destination point
	 * @returns Midpoint between this point and the supplied point
	 */
	public LatLon midPointTo(LatLon target) {
		double lat1 = Math.toRadians(this.lat);
		double lat2 = Math.toRadians(target.lat);
		double lon1 = Math.toRadians(this.lon);
		double dLon = Math.toRadians(target.lon = this.lon);
		double Bx = Math.cos(lat2) * Math.cos(dLon);
		double By = Math.cos(lat2) * Math.sin(dLon);
		double lat3 = Math.atan2(
				Math.sin(lat1) + Math.sin(lat2),
				Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By
						* By));
		double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);
		return new LatLon((float)Math.toDegrees(lat3), (float)Math.toDegrees(lon3));
	}

	/**
	 * Returns the point of intersection of two paths defined by point and
	 * bearing
	 * 
	 * see http://williams.best.vwh.net/avform.htm#Intersection
	 * 
	 * @param p1
	 *            : First point
	 * @param brng1
	 *            : Initial bearing from first point
	 * @param p2
	 *            : Second point
	 * @param brng2
	 *            : Initial bearing from second point
	 * @returns Destination point (null if no unique intersection defined)
	 */
	public LatLon intersection(LatLon p1, double brng1, LatLon p2, double brng2) {
		double lat1 = Math.toRadians(p1.lat), lon1 = Math.toRadians(p1.lon);
		double lat2 = Math.toRadians(p2.lat), lon2 = Math.toRadians(p2.lon);
		double brng13 = Math.toRadians(brng1), brng23 = Math.toRadians(brng2);
		double dLat = lat2 - lat1, dLon = lon2 - lon1;

		double dist12 = 2 * Math.asin(Math.sqrt(Math.sin(dLat / 2)
				* Math.sin(dLat / 2) + Math.cos(lat1) * Math.cos(lat2)
				* Math.sin(dLon / 2) * Math.sin(dLon / 2)));
		if (dist12 == 0)
			return null;

		// initial/final bearings between points
		Double brngA = Math.acos((Math.sin(lat2) - Math.sin(lat1)
				* Math.cos(dist12))
				/

				(Math.sin(dist12) * Math.cos(lat1)));
		if (brngA.isNaN())
			brngA = 0.0; // protect against rounding
		Double brngB = Math.acos((Math.sin(lat1) - Math.sin(lat2)
				* Math.cos(dist12))
				/ (Math.sin(dist12) * Math.cos(lat2)));
		double brng12, brng21;
		if (Math.sin(lon2 - lon1) > 0) {
			brng12 = brngA;
			brng21 = 2 * Math.PI - brngB;
		} else {
			brng12 = 2 * Math.PI - brngA;
			brng21 = brngB;
		}

		double alpha1 = (brng13 - brng12 + Math.PI) % (2 * Math.PI) - Math.PI; // angle
																				// 2-1-3
		double alpha2 = (brng21 - brng23 + Math.PI) % (2 * Math.PI) - Math.PI; // angle
																				// 1-2-3

		if (Math.sin(alpha1) == 0 && Math.sin(alpha2) == 0)
			return null; // infinite intersections
		if (Math.sin(alpha1) * Math.sin(alpha2) < 0)
			return null; // ambiguous intersection

		// alpha1 = Math.abs(alpha1);
		// alpha2 = Math.abs(alpha2);
		// ... Ed Williams takes abs of alpha1/alpha2, but seems to break
		// calculation?

		double alpha3 = Math.acos(-Math.cos(alpha1) * Math.cos(alpha2)
				+ Math.sin(alpha1) * Math.sin(alpha2) * Math.cos(dist12));
		double dist13 = Math.atan2(
				Math.sin(dist12) * Math.sin(alpha1) * Math.sin(alpha2),
				Math.cos(alpha2) + Math.cos(alpha1) * Math.cos(alpha3));
		double lat3 = Math.asin(Math.sin(lat1) * Math.cos(dist13)
				+ Math.cos(lat1) * Math.sin(dist13) * Math.cos(brng13));
		double dLon13 = Math.atan2(
				Math.sin(brng13) * Math.sin(dist13) * Math.cos(lat1),
				Math.cos(dist13) - Math.sin(lat1) * Math.sin(lat3));
		double lon3 = lon1 + dLon13;
		lon3 = (lon3 + Math.PI) % (2 * Math.PI) - Math.PI; // normalise to
															// -180..180�

		return new LatLon((float)Math.toDegrees(lat3), (float)Math.toDegrees(lon3));
	}

	/**
	 * Returns the distance from this point to the supplied point, in km,
	 * travelling along a rhumb line
	 * 
	 * see http://williams.best.vwh.net/avform.htm#Rhumb
	 * 
	 * @param target
	 *            : Latitude/longitude of destination point
	 * @returns Distance in km between this point and destination point
	 */
	public float rhumbDistanceTo(LatLon target) {

		double lat1 = Math.toRadians(this.lat);
		double lat2 = Math.toRadians(target.lat);
		double dLat = Math.toRadians(target.lat - this.lat);
		double dLon = Math.toRadians(target.lon = this.lon);

		double dPhi = Math.log(Math.tan(lat2 / 2 + Math.PI / 4)
				/ Math.tan(lat1 / 2 + Math.PI / 4));
		double q = (!Double.valueOf((dLat / dPhi)).isNaN()) ? dLat / dPhi
				: Math.cos(lat1); // E-W line gives dPhi=0
		// if dLon over 180� take shorter rhumb across 180� meridian:
		if (dLon > Math.PI)
			dLon = 2 * Math.PI - dLon;
		double dist = Math.sqrt(dLat * dLat + q * q * dLon * dLon)
				* EARTH_RADIUS;
		return (float) dist;
	}

	/**
	 * Returns the bearing from this point to the supplied point along a rhumb
	 * line, in degrees
	 * 
	 * @param target
	 *            : Latitude/longitude of destination point
	 * @returns Bearing in degrees from North
	 */
	public float rhumbBearingTo(LatLon target) {
		double lat1 = Math.toRadians(this.lat);
		double lat2 = Math.toRadians(target.lat);
		double dLon = Math.toRadians(target.lon = this.lon);

		double dPhi = Math.log(Math.tan(lat2 / 2 + Math.PI / 4)
				/ Math.tan(lat1 / 2 + Math.PI / 4));
		if (Math.abs(dLon) > Math.PI)
			dLon = dLon > 0 ? -(2 * Math.PI - dLon) : (2 * Math.PI + dLon);
		Double brng = Math.toDegrees(Math.atan2(dLon, dPhi));
		return brng.floatValue();
	}
	
	/**
	 * Returns the destination point from this point having travelled the given distance (in km) on the 
	 * given bearing along a rhumb line
	 *
	 * @param    brng: Bearing in degrees from North
	 * @param    dist: Distance in km
	 * @returns  Destination point
	 */
	public LatLon rhumbDestinationPoint(double brng, float dist){
		  
		  double d = dist/EARTH_RADIUS;  // d = angular distance covered on earth's surface
		  double lat1 = Math.toRadians(this.lat);
		  double lon1 = Math.toRadians(this.lon);
		  brng = Math.toRadians(brng);

		  double lat2 = lat1 + d*Math.cos(brng);
		  double dLat = lat2-lat1;
		  double dPhi = Math.log(Math.tan(lat2/2+Math.PI/4)/Math.tan(lat1/2+Math.PI/4));
		  double q = (!Double.valueOf((dLat/dPhi)).isNaN()) ? dLat/dPhi : Math.cos(lat1);  // E-W line gives dPhi=0
		  double dLon = d*Math.sin(brng)/q;
		  // check for some daft bugger going past the pole
		  if (Math.abs(lat2) > Math.PI/2) lat2 = lat2>0 ? Math.PI-lat2 : -(Math.PI-lat2);
		  double lon2 = (lon1+dLon+3*Math.PI)%(2*Math.PI) - Math.PI;
		 
		  return new LatLon((float)Math.toDegrees(lat2), (float)Math.toDegrees(lon2));
	}

}
