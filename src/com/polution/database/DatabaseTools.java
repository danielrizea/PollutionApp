package com.polution.database;


import java.util.ArrayList;
import java.util.List;

import org.apache.http.conn.ManagedClientConnection;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.polution.map.model.PollutionPoint;

public class DatabaseTools extends Activity{

	private static String DEBUG_TAG = "DatabaseTools";
	
	public static List<PollutionPoint> getPointsInBounds(Cursor values){

		List<PollutionPoint> points = new ArrayList<PollutionPoint>();
		
		if(values == null)
			return points;
		
		if(values.moveToFirst()){
			do{
				PollutionPoint point = new PollutionPoint();
				point.lon = (values.getFloat(values.getColumnIndex("lon")));
				point.lat = (values.getFloat(values.getColumnIndex("lat")));
				
				point.sensor_1 = (values.getFloat(values.getColumnIndex("sensor_1_val")));
				point.sensor_2 = (values.getFloat(values.getColumnIndex("sensor_2_val")));
				point.sensor_3 = (values.getFloat(values.getColumnIndex("sensor_3_val")));
				point.batteryVoltage = (values.getFloat(values.getColumnIndex("battery_val")));
				point.id = (values.getInt(values.getColumnIndex("_id")));
				
				//get precalculated point pollution intensity
				point.intensity = (values.getInt(values.getColumnIndex("intensity")));
				point.timestamp = values.getInt(values.getColumnIndex("timestamp"));
				//calculate sensor intensity values
				point.calculatePollutionIntensityValue();
				//System.out.println(point);
				points.add(point);
				
			}while(values.moveToNext());
		}
		
		Log.d(DEBUG_TAG,"Returning " + points.size() + " points");
		
		return points;
	}
	
	public static ContentValues getContentValues(PollutionPoint point){	
		
		   ContentValues values = new ContentValues();
		   
	       values.put("lat", point.lat);
	       values.put("lon", point.lon);
	       values.put("intensity", point.intensity);
	       values.put("sensor_1_val", point.sensor_1);
	       values.put("sensor_2_val", point.sensor_2);
	       values.put("sensor_3_val", point.sensor_3);
	       values.put("battery_val", point.batteryVoltage);
	       values.put("timestamp", point.timestamp);
		
		return values;
	}
	
	
	
}
