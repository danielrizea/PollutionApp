package com.polution.bluetooth;

public class DeviceState {

	
	public double sensor_1_val;
	public double sensor_2_val;
	public double sensor_3_val;
	public double battery_status;
	public long timestamp;
	
	
	public DeviceState(double sensor_1_val, double sensor_2_val,
			double sensor_3_val, double battery_status, long timestamp) {
		super();
		this.sensor_1_val = sensor_1_val;
		this.sensor_2_val = sensor_2_val;
		this.sensor_3_val = sensor_3_val;
		this.battery_status = battery_status;
		this.timestamp = timestamp;
	};
	
	
}
