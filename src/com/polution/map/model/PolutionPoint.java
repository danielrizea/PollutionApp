
package com.polution.map.model;

import java.util.Random;

/**
 * @author Daniel
 *
 */
public class PolutionPoint {
	
	public double lat;
	public double lon;
	public int intensity;
	
	
	public float sensor_1;
	public float sensor_2;
	public float sensor_3;
	public float batteryVoltage;
	public int timestamp;
	public int id;
	
	public PolutionPoint(float lat, float lon, int intensity) {
		this.lat = lat;
		this.lon = lon;
		//TODO random
		this.intensity = calculatePollutionIntensityValue();
	}
	
	public PolutionPoint(){
		this(0f,0f,0);
	}
	
	public PolutionPoint(float lat, float lon){
		this(lat,lon,1);
	}
	
	public int calculatePollutionIntensityValue(){
		
		int intensity = 0;
		
		//random return object
		Random rand = new Random();
		return rand.nextInt(120);
		
		//return intensity;
		//this.intensity = (int)((sensor_1 * 255)/1024);
		
		//return intensity;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "[ lat:" + lat +" lon:" + lon +" sensors:" + sensor_1 + ", " + sensor_2 + ", " + sensor_3 + ", " + batteryVoltage + ", intensity:" +intensity + ", timestamp " + timestamp + " ]" ; 
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
