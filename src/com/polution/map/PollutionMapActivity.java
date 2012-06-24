package com.polution.map;

import java.util.ArrayList;
import java.util.List;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pollution.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.polution.bluetooth.QueryService;
import com.polution.database.DatabaseTools;
import com.polution.database.GEOPoint;
import com.polution.database.PollutionContentProvider;
import com.polution.map.SimpleMapView.OnLongpressListener;
import com.polution.map.events.PanChangeListener;
import com.polution.map.model.PollutionPoint;

public class PollutionMapActivity extends MapActivity {

	
	
	//scale
	private TextView maxValueText;
	private TextView minValueText;
	
	//private DBHelper database;
	private PollutionMapOverlay overlay;
	private ContentResolver contentResolver;
	
	private static String TAG = "PollutionMapActivity";
	
	//available gases
	private static final int CO = 0;
	private static final int NO = 1;
	private static final int AIR_Q = 2;
	private static final int ALL_GAS = 3;
	
	private int selectedFlagForDesplay = PollutionPoint.CO;
	//add-ons -------------------------------------------------------
	MapController mc;
	SimpleMapView mapView;
	GeoPoint p;
	GeoPoint myLoc;
	private TextView currentPointCoordinates;
	private TextView selectedGas;
	
	String mCurrentProvider;
	//Boolean mLocationEnabled = false;
	
	final float MINIMUM_DISTANCECHANGE_FOR_UPDATE = 5; // in Meters
	final long MINIMUM_TIME_BETWEEN_UPDATE = 1000; // in Milliseconds
	
	/**
	 * Minimum distance in meters for a point to be recognize and to be
	 * drawn
	 */
	
	protected static final int NEARPOINT_MAX_DISTANCE = 10; // 10.000km

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
	public static int ZOOM_LEVEL = 16;
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
		
		currentPointCoordinates = (TextView) findViewById(R.id.location_coordinates);
		mapView = (SimpleMapView)findViewById(R.id.mapview_polutionoverlay);
		selectedGas = (TextView)findViewById(R.id.shown_gas);
		maxValueText = (TextView)findViewById(R.id.maxScaleValue);
		minValueText = (TextView)findViewById(R.id.minScaleValue);
		
		//set street level depth
		this.overlay = new PollutionMapOverlay(200, mapView);
		mapView.getOverlays().add(overlay);
		
		mapView.addPanChangeListener(new PanChangeListener() {
			
			@Override
			public void onPan(GeoPoint old, GeoPoint current) {
				
				updatePollutionOverlay();
			}
		});
		
		mc = mapView.getController();

		myLocListener = new MyLocListener();
		
		double lat, longit;
		lat = 44.4;
		longit = 26.1;

		mapView.setOnLongpressListener(new OnLongpressListener() {
			
			@Override
			public void onLongpress(MapView view, GeoPoint longpressLocation) {
				// TODO Auto-generated method stub
				float lat = (float)(longpressLocation.getLatitudeE6()/1E6);
				float lon = (float)(longpressLocation.getLongitudeE6()/1E6);
				
				PollutionPoint point = new PollutionPoint(lat, lon);
            	
            	Uri uri = Uri.parse(PollutionContentProvider.CONTENT_URI_POINTS + "/insert");
            	contentResolver.insert(uri, DatabaseTools.getContentValues(point));
            	
            	updatePollutionOverlay();
            	
            	Log.d(TAG, "Dummy point added " + lat +" " + lon + " " + point.intensity);
			}
		});
		
	//	findLocationAtCoordinates(lat, longit);

		// Piata Unirii: (Lat, Longit) = (44.4, 26.1)

		mc.setZoom(ZOOM_LEVEL);
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
             
                updatePollutionOverlay();
                
                myLoc = myLocationOverlay.getMyLocation();
            }
        });
       
        // Initialize the LocationManager
        this.myLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        
        setupForLocationAutoUPDATES();
        
        
        
        String[] gasList = new String[]{"CO gas", "NO gas", "Air quality", "All gas"};
        Spinner spinner = (Spinner) findViewById(R.id.spinner_choose_gas);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, gasList );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());

      // this.updateView();
       
        /* Prepare the things, that will give
         * us the ability, to receive Information
         * about our GPS-Position. OR USE NETWORK */
        //this.setupForLocationAutoUPDATES();
       
        /* Update the list of our friends once on the start,
         * as they are not(yet) moving, no updates to them are necessary */
        //this.refreshFriendsList(NEARPOINT_MAX_DISTANCE);

	}

	
	@Override
	protected void onResume() {
		super.onResume();
		setupForLocationAutoUPDATES();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		myLocationManager.removeUpdates(myLocListener);
	}
    private class MyOnItemSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent,
            View view, int pos, long id) {
        	
        	String selection = "";
        	switch(pos){
	        	case CO : {
	        		selectedFlagForDesplay = PollutionPoint.CO;
	        		selection = "CO";
	        		maxValueText.setText("500 ppm ");
	        		minValueText.setText("0 ppm ");
	        	} break;
	        	case NO : {
	        		selectedFlagForDesplay = PollutionPoint.NO;
	        		selection = "NO";
	        		maxValueText.setText("2000 ppb ");
	        		minValueText.setText("100 ppb ");
	        	} break;
	        	case AIR_Q : {
	        		selectedFlagForDesplay = PollutionPoint.AIR_Q;
	        		selection = "Air Q";
	        		selection = "NO";
	        		
	        		maxValueText.setText("100 % ");
	        		minValueText.setText("0 % ");
	        	} break;
	        	case ALL_GAS : {
	        		selectedFlagForDesplay = PollutionPoint.ALL_GAS;
	        		selection = "All gas";
	        		maxValueText.setText("100 % ");
	        		minValueText.setText("0 % ");
	        	} break;
        	}
        	updatePollutionOverlay();
        	selectedGas.setText(selection);
        	
          Toast.makeText(parent.getContext(), "The selected gas is :" + parent.getAdapter().getItem(pos), Toast.LENGTH_SHORT).show();
        }

        public void onNothingSelected(AdapterView parent) {
          // Do nothing.
        }
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
	
	private void updatePollutionOverlay(){
		
		List<PollutionPoint> points = new ArrayList<PollutionPoint>();
		try{
			Uri  uri= Uri.parse(PollutionContentProvider.CONTENT_URI_POINTS + "/" + mapView.getBounds()[0][0] + "/" + mapView.getBounds()[0][1] + "/" + mapView.getBounds()[1][0] + "/" + mapView.getBounds()[1][1]);
			Cursor values = managedQuery(uri, null, null, null, null);
			
			points = DatabaseTools.getPointsInBounds(values);
			
			values.close();
		}catch(Exception e){};


		
		if(points.size() > 0){
			overlay.update(points,selectedFlagForDesplay);
		}

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.map_menu, menu);
	    return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		
		switch(item.getItemId()){
		
			case R.id.delete_points : {
			
				Uri uri = Uri.parse(PollutionContentProvider.CONTENT_URI_POINTS + "/delete/point_table" );
				contentResolver.delete(uri, null, null);
			}break;
			
			case R.id.add_point : 
			{
				Intent serviceIntent = new Intent(this, QueryService.class);
	 		   	startService(serviceIntent);
			}break;
		
			case R.id.view_points:
			{
				Intent viewPoints = new Intent(this,ViewPointsListView.class);
				startActivity(viewPoints);
			}break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	class MyLocListener implements LocationListener{

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			//do something 
			mc.animateTo(myLocationOverlay.getMyLocation());
    		
    		myLoc = new GeoPoint((int)(location.getLatitude()*1E6),(int)(location.getLongitude()*1E6));
 
    		currentPointCoordinates.setText("lat:" + (double)(myLoc.getLatitudeE6()/1E6) + " lon:" + (double)(myLoc.getLongitudeE6()/1E6));

        	updatePollutionOverlay();
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
                
                Log.d(TAG,"Provider disabled, new provider " + mCurrentProvider);
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
                
                Log.d(TAG,"Provider enabled , new provider " + mCurrentProvider);
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


