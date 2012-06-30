package com.polution.bluetooth;

public class PollutionSensor {
	
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
	
	
	/**
	 *  CO datasheet sensor details
	 *  sensor no :502242 
	 *  range 0.5 - 500 ppm 
	 *  Concentration range : can withstand 1% CO in air
	 */
	
	
	/**
	 * NO datasheet sensor details
	 * sensor no 502248
	 * range 0.1 - 2 ppm 100 - 2000 ppb
	 */
	
	
	/** calculate variable resistor so that we can determine the readings for a specific sensor
	*   the returned value is in ohmi
	*/
	public static Float getResitanceValue(float Vo, float Rs, float Vcc){
		
		float Rx = 0;
		Vcc = (Vcc*4.4f)/1024;
		Vo = (Vo*4.4f)/1024;
		Rx = ((Vcc*Rs)-Vo*Rs)/Vo; 
		System.out.println(" Vcc : "+Vcc+"Vo "+ Vo+" Rs " +Rs+" Rx "+Rx+" ");
		return Rx;
	}
	/**
	 * Determines the sensor value in ppm : particles per million
	 * It uses the sensors datasheet to find out correspondence between variable resistor Rs and
	 * the ppm or ppb value depending on the sensor type 
	 * @param sensorType
	 * @param Rs
	 * @return
	 */
	public static int getSensorValue(int sensorType, float Rs){
		
		int value = 0;
		
		switch(sensorType){
		
		case CO_SENSOR : {
			//100k - 1ppm
			//10k - 10ppm
			//1k - 100ppm
			//0k - 500ppm
			
			if(Rs>100000)
				return 1;
			
			if(Rs<=100000 && Rs>10000){
				return (int)((-1/10)*(Rs/1000)+11);
			}
					
			if(Rs<=10000 && Rs>=1000){
				return (int)(-10*(Rs/1000)+200);
			}
			
			return (int)((Rs/1000)*(-490) + 500);
			
		} 
		
		case NO_SENSOR : {
			
			if(Rs < 10000)
				return 100;
			
			if(Rs < 100000 && Rs >= 10000)
				return (int)((40/9)*(Rs/1000) + (500/9));
			
			if(Rs > 100000)
				return (int)(12*(Rs/1000) - 900);
			
		} break;
		
		case AIR_Q_SENSOR : {} break;
		}
		
		return value;
	}
	
	public static float getBatteryVoltage(String battery){
		
		float val= 0;
    	val = Float.parseFloat(battery);
    	val = (val *1.1f* 4.3f)/1024;
    	
    	return val;
	}
	
}
