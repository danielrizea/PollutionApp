package com.polution.ar;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Message;

public class RadarListener extends Thread implements LocationListener{

	Context context;
	
	public RadarListener(Context context){
		
		this.context = context;
		
	}
	
	
	@Override
	public void onLocationChanged(Location location) {
		setCurrentGpsLocation(location);
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	 private void setCurrentGpsLocation(Location location) {  
		    if (location == null) {  
		    	
		    LocationManager locationManager;
		    
		    locationManager = (LocationManager) context.getSystemService(Activity.LOCATION_SERVICE);  
		    locationManager.requestLocationUpdates(  
		     LocationManager.GPS_PROVIDER, 0, 0, this);  
		    location = locationManager  
		     .getLastKnownLocation(LocationManager.GPS_PROVIDER);  
		    }  
		    
		  
		    Message msg = new Message();  
		  //  msg.what = UPDATE_LOCATION;  
		    
		  //  BeerRadar.this.updateHandler.sendMessage(msg);  
		    }  
}
