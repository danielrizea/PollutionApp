
package com.polution.map.model;

import java.io.Serializable;


/**
 * @author Daniel
 *
 */
public class PollutionPoint implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int CO = 0;
	public static final int NO = 1;
	public static final int AIR_Q = 2;
	public static final int ALL_GAS = 3;
	
	public double lat;
	public double lon;
	public int intensity;
	
	public int intensity_CO;
	public int intensity_NO;
	public int intensity_AirQ;
	
	public float sensor_1;
	public float sensor_2;
	public float sensor_3;
	public float batteryVoltage;
	public long timestamp;
	public int id;
	public int flag;
	
	public PollutionPoint(float lat, float lon, int intensity) {
		this.lat = lat;
		this.lon = lon;
	}
	
	public PollutionPoint(){
		this(0f,0f,0);
	}
	
	public PollutionPoint(float lat, float lon){
		this(lat,lon,1);
	}
	
	public int calculatePollutionIntensityValue(){

		/*
		//random return object
		Random rand = new Random();
		return rand.nextInt(255);
		*/
		
		//return intensity;
		this.intensity_CO = (int)((sensor_1 * 255)/500);
		this.intensity_NO = (int)((sensor_2  * 255)/2000);
		this.intensity_AirQ = (int)((sensor_3 * 255)/100);
		
		//TODO figure out a better formula to calculate overall point intensity
		this.intensity = (this.intensity_CO + this.intensity_AirQ + this.intensity_NO)/3;
		
		return this.intensity;
	}
	
	@Override
	public String toString() {
		// toString custom method
		return "[ lat:" + lat +" lon:" + lon +" sensors:" + sensor_1 + ", " + sensor_2 + ", " + sensor_3 + ", " + batteryVoltage + ", intensity: ["+ intensity_CO + ", " + intensity_NO +", " + intensity_AirQ + ", " +intensity + "], timestamp " + timestamp + " ]" ; 
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
