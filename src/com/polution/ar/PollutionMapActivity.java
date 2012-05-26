package com.polution.ar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.ar.test.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.polution.database.AlarmNotifier;
import com.polution.database.DatabaseTools;
import com.polution.database.GEOPoint;
import com.polution.database.PollutionContentProvider;
import com.polution.map.HeatMapOverlay;
import com.polution.map.SimpleMapView;
import com.polution.map.events.PanChangeListener;
import com.polution.map.model.PolutionPoint;

public class PollutionMapActivity extends MapActivity {

	//private DBHelper database;
	private HeatMapOverlay overlay;
	private ContentResolver contentResolver;
	
	private static String DEBUG_TAG = "PollutionMapActivity";
	
	
	//add-ons -------------------------------------------------------
	MapController mc;
	GeoPoint p;
	
	
	String mCurrentProvider;
	Boolean mLocationEnabled = false;
	
	final float MINIMUM_DISTANCECHANGE_FOR_UPDATE = 25; // in Meters
	final long MINIMUM_TIME_BETWEEN_UPDATE = 5000; // in Milliseconds
	
	/**
	 * Minimum distance in meters for a point to be recognize and to be
	 * drawn
	 */
	
	protected static final int NEARPOINT_MAX_DISTANCE = 10000000; // 10.000km

	final Handler mHandler = new Handler();

	/*
	 * Stuff we need to save as fields, because we want to use multiple of them
	 * in different methods.
	 */
	protected boolean doUpdates = true;
	protected MapController myMapController = null;

	protected LocationManager myLocationManager = null;
	protected Location myLocation;
	/** List of friends in */
	protected ArrayList<GEOPoint> nearPoints = new ArrayList<GEOPoint>();
	protected MyLocationOverlay myLocationOverlay;
	
	private MyLocListener myLocListener;
	
	// ===========================================================
	// Extra-Classes
	// ===========================================================
	
	
	
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		//this.database = new DBHelper(this);
		setContentView(R.layout.polution_mapoverlay);
		contentResolver = this.getContentResolver();
		
		final SimpleMapView mapView = (SimpleMapView)findViewById(R.id.mapview_polutionoverlay);
		this.overlay = new HeatMapOverlay(200, mapView);
		mapView.getOverlays().add(overlay);
		
		mapView.addPanChangeListener(new PanChangeListener() {
			
			@Override
			public void onPan(GeoPoint old, GeoPoint current) {
				
				Uri  uri= Uri.parse(PollutionContentProvider.CONTENT_URI_POINTS + "/" + mapView.getBounds()[0][0] + "/" + mapView.getBounds()[0][1] + "/" + mapView.getBounds()[1][0] + "/" + mapView.getBounds()[1][1]);
				Cursor values = managedQuery(uri, null, null, null, null);
				
				List<PolutionPoint> points = DatabaseTools.getPointsInBounds(values);
				
				if(points.size() > 0){
					overlay.update(points);
				}
				
			}
		});
		
		mc = mapView.getController();

		MyLocListener myLocListener = new MyLocListener();
		
		double lat, longit;
		lat = 44.4;
		longit = 26.1;

	//	findLocationAtCoordinates(lat, longit);

		// Piata Unirii: (Lat, Longit) = (44.4, 26.1)

	//	mc.setZoom(17);
	//	mapView.invalidate();
		

        this.myMapController = mapView.getController();
       
        /* With these objects we are capable of
         * drawing graphical stuff on top of the map */

       
		List<Overlay> listOfOverlays = mapView.getOverlays();
		listOfOverlays.clear();
		
		
		myLocationOverlay = new MyLocationOverlay(this, mapView);
		
		
		listOfOverlays.add(this.overlay);
		listOfOverlays.add(myLocationOverlay);
		
		//myLocationOverlay.enableCompass();
        myLocationOverlay.enableMyLocation();

        myLocationOverlay.runOnFirstFix(new Runnable() {
            public void run() {
                mc.animateTo(myLocationOverlay.getMyLocation());
                
                
            	Uri  uri= Uri.parse(PollutionContentProvider.CONTENT_URI_POINTS + "/" + mapView.getBounds()[0][0] + "/" + mapView.getBounds()[0][1] + "/" + mapView.getBounds()[1][0] + "/" + mapView.getBounds()[1][1]);
        		Cursor values = managedQuery(uri, null, null, null, null);
        		
        		List<PolutionPoint> points = DatabaseTools.getPointsInBounds(values);
        		
        		if(points.size() > 0){
        			overlay.update(points);
        		}
                
                GeoPoint myloc = myLocationOverlay.getMyLocation();
                // 44.423115 26.115126
                for(int i=0;i<3;i++){
                	
                	float latRandom = (float)(myloc.getLatitudeE6() / 1E6);
                	float lonRandom = (float)(myloc.getLongitudeE6()/ 1E6);
                	
                	Random rand = new Random();
                	
                	Log.d(DEBUG_TAG, "Generated point " + "lat:" + latRandom + (float)rand.nextInt(10)/10000 + " " + "lon :" + lonRandom + (float)rand.nextInt(10)/10000);
                	
                	PolutionPoint point = new PolutionPoint(latRandom + (float)rand.nextInt(10)/10000, lonRandom + (float)rand.nextInt(10)/10000);
                	
                	//Uri uri = Uri.parse(PollutionContentProvider.CONTENT_URI_POINTS + "/insert");
                	//contentResolver.insert(uri, DatabaseTools.getContentValues(point));
                	
                	//database.insert(point);
                }
            }
        });
       
        // Initialize the LocationManager
        this.myLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        
      // this.updateView();
       
        /* Prepare the things, that will give
         * us the ability, to receive Information
         * about our GPS-Position. OR USE NETWORK */
       // this.setupForLocationAutoUPDATES();
       
        /* Update the list of our friends once on the start,
         * as they are not(yet) moving, no updates to them are necessary */
        //this.refreshFriendsList(NEARPOINT_MAX_DISTANCE);

		
        //get my location and add dummy points;
        
       
        
        //schedule event
        
        // get a Calendar object with current time
        Calendar cal = Calendar.getInstance();
        // add 5 minutes to the calendar object
        cal.add(Calendar.SECOND, 30);
        
        Intent intent = new Intent(this, AlarmNotifier.class);
       
        intent.putExtra("alarm_message", "A message for the app");
        // In reality, you would want to have a static variable for the request code instead of 192837
        PendingIntent sender = PendingIntent.getBroadcast(this, 192837, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        
        // Get the AlarmManager service
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
       // am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
        //5 seconds
        //am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 5000, sender);
        //cancel alarm
        //am.cancel(sender);
	}

	
	class MyLocListener implements LocationListener{

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			//do something 
			mc.animateTo(myLocationOverlay.getMyLocation());
			
			//Uri uri = Uri.parse(PollutionContentProvider.CONTENT_URI_POINTS + "/insert");
        	//contentResolver.insert(uri, DatabaseTools.getContentValues(point));

        	
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			List<String> mProviders = myLocationManager.getProviders(true);
            if (mProviders.size() > 0) {
                mCurrentProvider = LocationManager.NETWORK_PROVIDER;
                for (String string : mProviders) {
                    if (string.equals(LocationManager.GPS_PROVIDER))
                        mCurrentProvider = LocationManager.GPS_PROVIDER;
                }
                myLocation = myLocationManager
                        .getLastKnownLocation(mCurrentProvider);
                myLocationManager.requestLocationUpdates(mCurrentProvider,
                       MINIMUM_TIME_BETWEEN_UPDATE, MINIMUM_DISTANCECHANGE_FOR_UPDATE, this );
            }
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			List<String> mProviders = myLocationManager.getProviders(true);
            if (mProviders.size() > 0) {
                mCurrentProvider = LocationManager.NETWORK_PROVIDER;
                for (String string : mProviders) {
                    if (string.equals(LocationManager.GPS_PROVIDER))
                        mCurrentProvider = LocationManager.GPS_PROVIDER;
                }
                myLocation = myLocationManager
                        .getLastKnownLocation(mCurrentProvider);
                myLocationManager.requestLocationUpdates(mCurrentProvider,
                       MINIMUM_TIME_BETWEEN_UPDATE, MINIMUM_DISTANCECHANGE_FOR_UPDATE, this );
            }
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}	
	}
}


