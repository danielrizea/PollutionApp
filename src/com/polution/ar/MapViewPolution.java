package com.polution.ar;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.RectF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;

import com.ar.test.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.polution.database.DBHelper;
import com.polution.database.GEOPoint;

public class MapViewPolution extends MapActivity {

	DBHelper database;

	MapView mapView;
	MapController mc;
	GeoPoint p;

	MyLocListener myLocListener;
	
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
	
	// ===========================================================
	// Extra-Classes
	// ===========================================================

	/**
	 * This method is so huge, because it does a lot of FANCY painting. We could
	 * shorten this method to a few lines. But as users like eye-candy apps <img
	 * src="http://www.anddev.org/images/smilies/wink.png" alt=";)" title="Wink"
	 * /> ...
	 */
	

	class MapOverlay extends com.google.android.maps.Overlay {
		@Override
		public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
				long when) {
			super.draw(canvas, mapView, shadow);

			/*
			// ---translate the GeoPoint to screen pixels---
			Point screenPts = new Point();
			mapView.getProjection().toPixels(p, screenPts);

			// ---add the marker---
			Bitmap bmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.icon);
			canvas.drawBitmap(bmp, screenPts.x, screenPts.y - 50, null);
			
			////////////////////
			*/
			
			Paint paint = new Paint();
			paint.setTextSize(14);
			Double lat;
			Double lng;
			RectF oval;
			Point myScreenCoords;
			// Create a Point that represents our GPS-Location
			if(myLocation != null){
				
			lat = myLocation.getLatitude() * 1E6;
			lng = myLocation.getLongitude() * 1E6;
			GeoPoint point = new GeoPoint(lat.intValue(), lng.intValue());

			myScreenCoords = new Point();
			// Converts lat/lng-Point to OUR coordinates on the screen.
			mapView.getProjection().toPixels(point, myScreenCoords);

			// Draw a circle for our location
			oval = new RectF(myScreenCoords.x - 7,
					myScreenCoords.y + 7, myScreenCoords.x + 7,
					myScreenCoords.y - 7);

			// Setup a color for our location
			paint.setStyle(Style.FILL);
			paint.setARGB(255, 80, 150, 30); // Nice strong Android-Green
			// Draw our name
			canvas.drawText("pos",
					myScreenCoords.x + 9, myScreenCoords.y, paint);

			// Change the paint to a 'Lookthrough' Android-Green
			paint.setARGB(80, 156, 192, 36);
			paint.setStrokeWidth(1);
			// draw an oval around our location
			canvas.drawOval(oval, paint);

			// With a black stroke around the oval we drew before.
			paint.setARGB(255, 0, 0, 0);
			paint.setStyle(Style.STROKE);
			canvas.drawCircle(myScreenCoords.x, myScreenCoords.y, 7, paint);
			}
			
			
			
			Point friendScreenCoords = new Point();
			
			// Draw each friend with a line pointing to our own location.
			for (GEOPoint myPoint : nearPoints) {
				
				lat = myPoint.latitude * 1E6;
				lng = myPoint.longitude * 1E6;
				GeoPoint loc = new GeoPoint(lat.intValue(),lng.intValue());

				mapView.getProjection().toPixels(loc,friendScreenCoords);
				// Converts lat/lng-Point to coordinates on the screen.
				
				if (Math.abs(friendScreenCoords.x) < 2000
						&& Math.abs(friendScreenCoords.y) < 2000) {
					// Draw a circle for this friend and his name
					oval = new RectF(friendScreenCoords.x - 7,
							friendScreenCoords.y + 7,
							friendScreenCoords.x + 7,
							friendScreenCoords.y - 7);

					// Setup a color for all friends
					paint.setStyle(Style.FILL);
					paint.setARGB(255, 255, 0, 0); // Nice red
					canvas.drawText("point", friendScreenCoords.x + 9,
							friendScreenCoords.y, paint);

					// Draw a line connecting us to the current Friend
					paint.setARGB(80, 255, 0, 0); // Nice red, more look
													// through...

				//paint.setStrokeWidth(2);
				//	canvas.drawLine(myScreenCoords.x, myScreenCoords.y,
				//			friendScreenCoords.x, friendScreenCoords.y, paint);
				//	paint.setStrokeWidth(1);
					// draw an oval around our friends location
					canvas.drawOval(oval, paint);

					// With a black stroke around the oval we drew before.
					paint.setARGB(255, 0, 0, 0);
					paint.setStyle(Style.STROKE);
					canvas.drawCircle(friendScreenCoords.x,
							friendScreenCoords.y, 7, paint);
				}
			}
		
			
			
			////////////////////
			return true;
		}
	}
	
	class MyLocListener implements LocationListener{

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			//do something 
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

	Context context;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mapview);

		mapView = (MapView) findViewById(R.id.map);

		mc = mapView.getController();
		
		context = this;
		
		DrawOnTop mDrawOnTop = new DrawOnTop(this);
		addContentView(mDrawOnTop, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		RadarView radarView = new RadarView(this);
		addContentView(radarView, new LayoutParams(100, 100));
		
		radarView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				Intent intent = new Intent(context,ARView.class);
				
				startActivity(intent);
			}
		});
		
		database = new DBHelper(MapViewPolution.this);

		myLocListener = new MyLocListener();
		
		
		
		
		
		double lat, longit;
		lat = 44.4;
		longit = 26.1;

	//	findLocationAtCoordinates(lat, longit);

		// Piata Unirii: (Lat, Longit) = (44.4, 26.1)

	//	mc.setZoom(17);
	//	mapView.invalidate();
		
		

        this.myMapController = this.mapView.getController();
       
        /* With these objects we are capable of
         * drawing graphical stuff on top of the map */
        MapOverlay mapOverlay = new MapOverlay();
        
		List<Overlay> listOfOverlays = mapView.getOverlays();
		listOfOverlays.clear();
		
		
		myLocationOverlay = new MyLocationOverlay(this, mapView);
		
		
		listOfOverlays.add(mapOverlay);
		listOfOverlays.add(myLocationOverlay);
		
		//myLocationOverlay.enableCompass();
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.runOnFirstFix(new Runnable() {
            public void run() {
                mc.animateTo(myLocationOverlay.getMyLocation());
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


	}

	public void findLocationAtCoordinates(double lat, double lon) {

		p = new GeoPoint((int) (lat * 1E6), (int) (lon * 1E6));
		mc.animateTo(p);
		mc.setZoom(15);

		MapOverlay mapOverlay = new MapOverlay();
		List<Overlay> listOfOverlays = mapView.getOverlays();
		listOfOverlays.clear();
		listOfOverlays.add(mapOverlay);
		
		
		
		
		mapView.invalidate();

	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	
	private Boolean setupForLocationAutoUPDATES() {
		/*
		 * Register with out LocationManager to send us an intent (whos
		 * Action-String we defined above) when an intent to the location
		 * manager, that we want to get informed on changes to our own position.
		 * This is one of the hottest features in Android.
		 */
	
		// Get the first provider available
		
		//get the best provider
		 mLocationEnabled = true;
		 
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
            		 MINIMUM_TIME_BETWEEN_UPDATE, MINIMUM_DISTANCECHANGE_FOR_UPDATE,myLocListener);
         }
         return false;
		

	}
	
	
	
	
	private void updateView() {
		// Refresh our gps-location
		//this.myLocation = myLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		
		/*
		 * Redraws the mapViee, which also makes our OverlayController redraw
		 * our Circles and Lines
		 */
		mapView.invalidate();

		/*
		 * As the location of our Friends is static and for performance-reasons,
		 * we do not call this
		 */
		// this.refreshFriendsList(NEARFRIEND_MAX_DISTANCE);
	}

	/**
	 * Restart the receiving, when we are back on line.
	 */
	@Override
	public void onResume() {
		super.onResume();
		this.doUpdates = true;

		myLocationOverlay.enableMyLocation();
		//myLocationOverlay.enableCompass();
		
		/*
		myLocationManager.requestLocationUpdates(mCurrentProvider,MINIMUM_TIME_BETWEEN_UPDATE, MINIMUM_DISTANCECHANGE_FOR_UPDATE,myLocListener);
		mLocationEnabled = true;
		*/
		
		/*
		 * As we only want to react on the LOCATION_CHANGED intents we made the
		 * OS send out, we have to register it along with a filter, that will
		 * only "pass through" on LOCATION_CHANGED-Intents.
		 */

	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	
		//disabling sensors
		myLocationOverlay.disableMyLocation();
		//myLocationOverlay.disableCompass();
		
		/*
		myLocationManager.removeUpdates(myLocListener);
		mLocationEnabled = false;
		*/
	}

	/**
	 * Make sure to stop the animation when we're no longer on screen, failing
	 * to do so will cause a lot of unnecessary cpu-usage!
	 */
//	@Override
//	public void onFreeze(Bundle icicle) {
//		super.onFreeze(icicle);
//		this.doUpdates = false;
//		//unregisterReceiver(this.myIntentReceiver);
//		
//	}

	// ===========================================================
	// Overridden methods
	// ===========================================================

	// Called only the first time the options menu is displayed.
	// Create the menu entries.
	// Menu adds items in the order shown.


	private void refreshFriendsList(long maxDistanceInMeter) {
		
		

		// Moves the cursor to the first row
		// and returns true if there is sth. to get
		
	}

}