package com.polution.database;


import java.util.ArrayList;
import java.util.List;

import org.apache.http.conn.ManagedClientConnection;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.polution.map.model.PolutionPoint;

public class DatabaseTools extends Activity{

	private static String DEBUG_TAG = "DatabaseTools";
	
	public static List<PolutionPoint> getPointsInBounds(Cursor values){

		List<PolutionPoint> points = new ArrayList<PolutionPoint>();
		
		if(values == null)
			return points;
		
		if(values.moveToFirst()){
			do{
				PolutionPoint point = new PolutionPoint();
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
				points.add(point);
				
			}while(values.moveToNext());
		}
		
		Log.d(DEBUG_TAG,"Returning " + points.size() + " points");
		
		return points;
	}
	
	public static ContentValues getContentValues(PolutionPoint point){	
		
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
