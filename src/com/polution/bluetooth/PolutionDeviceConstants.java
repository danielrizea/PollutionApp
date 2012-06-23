package com.polution.bluetooth;

public class PolutionDeviceConstants {
	
	public static String MESSAGE_QUERY_DEVICE = "a";
	
	public static String MESSAGE_BEEP_START = "b";
	
	public static String MESSAGE_BEEP_STOP = "c";
	
	public static final int CO_SENSOR = 0;
	
	public static final int NO_SENSOR = 1;
	
	public static final int AIR_Q_SENSOR = 2;
	
	//this is the CO sensor
	public static String SENSOR_1 = "sensor_1";
	
	//this is the NO sensor
	public static String SENSOR_2 = "sensor_2";
	
	//this is the Air Quality sensor
	public static String SENSOR_3 = "sensor_3";
	
	public static String BATTERY_SENSOR = "Batery";
	
	//resistors are measured in ohmi
	public static final float CO_Rs = 330;
	
	public static final float NO_Rs = 3300;
	
	public static final float AIR_Q_Rs = 1000;
	
	
	
	/** calculate variable resistor so that we can determine the readings for a specific sensor
	*   the returned value is in ohmi
	*/
	public static Float getResitanceValue(float Vo, float Rs, float Vcc){
		
		float Rx = 0;
		
		Rx = ((Vcc*Rs)-Vo*Rs)/Vo; 
		
		return Rx;
	}
	/**
	 * Determines the sensor value in ppm : particles per million
	 * It uses the sensors datasheet to find out correspondence between variable resistor Rs and
	 * the ppm value
	 * @param sensorType
	 * @param Rs
	 * @return
	 */
	public static int getSensorValue(int sensorType, float Rs){
		
		int value = 0;
		
		switch(sensorType){
		
		case CO_SENSOR : {} break;
		
		case NO_SENSOR : {} break;
		
		case AIR_Q_SENSOR : {} break;
		}
		
		return value;
	}
	
	
}
