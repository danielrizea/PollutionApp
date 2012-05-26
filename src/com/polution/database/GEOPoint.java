package com.polution.database;

public class GEOPoint {

	public double latitude;
	public double longitude;
	public long id;
	
	public float sensor_1;
	public float sensor_2;
	public float sensor_3;
	public float batteryVoltage;
	public int timestamp;
	
	// 255 (red) -> bad 
	// 0 (green) -> good
	
	public int value;
	
	public GEOPoint(){
		
	}
	public GEOPoint(double lat, double lon){
		
		latitude = lat;
		longitude = lon;
	}
	
	public static double getDistance(double lat1, double lon1, double lat2,double lon2){
		
		double R = 6371; // km
		double dLat = (lat2-lat1)* 0.0174532925199433;
		double dLon = (lon2-lon1)* 0.0174532925199433;
		lat1 = lat1 * 0.0174532925199433;
		lat2 = lat2 * 0.0174532925199433;

		
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		        Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2); 
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
		return R * c;
		
	}
}
