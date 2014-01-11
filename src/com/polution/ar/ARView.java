package com.polution.ar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MyLocationOverlay;
import com.pollution.R;
import com.polution.database.DatabaseTools;
import com.polution.database.GEOPoint;
import com.polution.database.PollutionContentProvider;
import com.polution.map.model.PollutionPoint;

// ----------------------------------------------------------------------

public class ARView extends Activity implements SensorEventListener{    
    private Preview mPreview;
    private DrawOnTop mDrawOnTop;

    private ContentResolver contentResolver;
    
    public final String TAG = "ARView";
    
    private PollutionCameraOverlay pollutionOverlay;
    private Context context;
    
    private TextView pollutionIntensity;
    //1.000000 = 111 KM lat
    //1.000000 = 71 KM lon
    //Location 
    
    public int LOCATION_CONSTANT = 100000;
    
    
	protected MapController mc;
	protected boolean doUpdates = true;
	protected MapController myMapController = null;

	protected LocationManager myLocationManager = null;
	protected Location myLocation;
	/** List of friends in */
	protected ArrayList<GEOPoint> nearPoints = new ArrayList<GEOPoint>();
	protected MyLocationOverlay myLocationOverlay;
	
	private MyLocListener myLocListener;

	String mCurrentProvider = LocationManager.NETWORK_PROVIDER;
	Boolean mLocationEnabled = false;
	
	final float MINIMUM_DISTANCECHANGE_FOR_UPDATE = 1; // in Meters
	final long MINIMUM_TIME_BETWEEN_UPDATE = 2000; // in Milliseconds

    private double lastAngle = 0;

    
    private ExecutorService executorService = null;
    //private CompassView compassView; 
    SensorManager sensorManager;

	float[] mValues=new float[3]; // magnetic sensor values
	float[] aValues=new float[3]; // accelerometer sensor values 
	float[] oValues=new float[3]; // orientation sensor values
	
	public static final int ORIENT = 1;
	public static final int ACC_MAGN = 2;
	private ProgressDialog progressDialog;
	
	public static float EPS = 5;
	
	float[] oldOvalues = null;
	/*
	 * time smoothing constant for low-pass filter
	 * 0 ≤ α ≤ 1 ; a smaller value basically means more smoothing
	 * See: http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
	 */
	static final float ALPHA = 0.1f;
    
    
    private CompassView compassView;
    // in landscape mode subtract 90
    private float rotation = 0;
   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // Hide the window title.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.ar_view);
        
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
 
        // Create our Preview view and set it as the content of our activity.
        // Create our DrawOnTop view.
        
        compassView = new CompassView(this);
        
		//updateOrientation(compassView, new float[] {0, 0, 0});
        
        Display display = getWindowManager().getDefaultDisplay();

        pollutionOverlay = new PollutionCameraOverlay(this, display.getWidth(), display.getHeight());
        
        //mDrawOnTop = new DrawOnTop(this);
        mPreview = new Preview(this, pollutionOverlay,compassView);
    
        
        
        context = this;
        addContentView(mPreview,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
        addContentView(pollutionOverlay, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

        addContentView(compassView, new LayoutParams(100,100));
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        
        
        executorService = Executors.newFixedThreadPool(1);
        
        pollutionIntensity = new TextView(this);
        
        pollutionIntensity.setPadding(180, 10, 0, 0);
        pollutionIntensity.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        
        pollutionIntensity.setText("No data");
        pollutionIntensity.setLayoutParams(params);
        addContentView(pollutionIntensity, params);
        /*
        TextView text = new TextView(this);
        
        text.setPadding(150, 30, 0, 0);
        text.setText("COLOR :");
        text.setLayoutParams(params);
        addContentView(text,params);
        */
        int test = getResources().getConfiguration().orientation;
        if(Configuration.ORIENTATION_LANDSCAPE == test) {
                    rotation = 90f;
                }
                else {
                    rotation = 0f;
                }
  
        this.contentResolver = getContentResolver();
        // Initialize the LocationManager
        this.myLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        
		
		myLocListener = new MyLocListener();
        
		String provider = myLocationManager.getBestProvider(new Criteria(), true);
		
		Location loc = myLocationManager.getLastKnownLocation(provider);
		
        setupForLocationAutoUPDATES();
		
        if(loc != null){			
			getData(loc);
		}
    }
    
    public void onSensorChanged(SensorEvent event) {
    	
		  if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
		      aValues = lowPass(event.values, aValues);
		    if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
		      mValues = lowPass(event.values,mValues);

		    
		  oValues = computeOrientation(aValues, mValues);
		  
		  if(oldOvalues != null){
			  
			  if(Math.abs(oldOvalues[0]-oValues[0])< EPS){
				  oValues = oldOvalues;
			  }
		  }
		  oldOvalues = oValues;
		  
		  //System.out.println("OValues " + oValues[0] + " " + oValues[1] + " " + oValues[2] );
		  
		  updateOrientation(compassView, oValues);

	}
    protected float[] lowPass( float[] input, float[] output ) {
	    if ( output == null ) return input;

	    for ( int i=0; i<input.length; i++ ) {
	        output[i] = output[i] + ALPHA * (input[i] - output[i]);
	    }
	    return output;
	}
	
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
	
	private void updateOrientation(CompassView cv, float[] values){
		
		if(cv!=null){
			cv.setAzimuth(values[0]  + rotation);
			cv.setPitch(values[1]);
			cv.setRoll(-values[2]);
	
			//invalidate on camera preview redraw
			//cv.invalidate();
		}
	}
		
	
	private float[] computeOrientation(float[] aValues, float[] mValues) {
		  float[] values = new float[3];
		  float[] R = new float[9];
		  
		  SensorManager.getRotationMatrix(R, null, aValues, mValues);
		  SensorManager.getOrientation(R, values);
		  // Convert from Radians to Degrees.
		  values[0] = (float) Math.toDegrees(values[0]);
		  values[1] = (float) Math.toDegrees(values[1]);
		  values[2] = (float) Math.toDegrees(values[2]);
		  return values;
	}

	 @Override
	    protected void onResume()
	    {
	       // if (Config.DEBUG) Log.d(TAG, "onResume");
	        super.onResume();


			 sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_GAME);
			 sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
			  
	       
	    }

	    @Override
	    protected void onStop()
	    {
	        //if (Config.DEBUG) Log.d(TAG, "onStop");
	    	super.onStop();
			sensorManager.unregisterListener(this);
			myLocationManager.removeUpdates(myLocListener);
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
	             
	             //Log.d(TAG,"Provider chosen " + mCurrentProvider);

	             myLocationManager.requestLocationUpdates(mCurrentProvider,
	            		 MINIMUM_TIME_BETWEEN_UPDATE, MINIMUM_DISTANCECHANGE_FOR_UPDATE,myLocListener);
	         }
	         return false;
			

		}
	    
	    public void getData(Location locationProvided){
	    	
	    	
	    	final Location location = locationProvided;
	    	
	    	Runnable runnable = new Runnable() {
				
				@Override
				public void run() {
			    	if(location == null)
			    		return;
			    	/*runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							progressDialog = ProgressDialog.show(context, "Fetch data", "Get local points");
						}
					});
					*/
			    	GeoPoint myloc = new GeoPoint((int)(location.getLatitude()*1E6), (int)(location.getLongitude()*1E6));
					int startLat = myloc.getLatitudeE6() - LOCATION_CONSTANT;
					int stopLat = myloc.getLatitudeE6() + LOCATION_CONSTANT;
					int startLon = myloc.getLongitudeE6() - LOCATION_CONSTANT;
					int stopLon = myloc.getLongitudeE6() + LOCATION_CONSTANT;
						
					Uri  uri= Uri.parse(PollutionContentProvider.CONTENT_URI_POINTS + "/" + startLat + "/" + startLon + "/" + stopLat + "/" + stopLon);
					Cursor values = contentResolver.query(uri, null, null, null, null);
						
					List<PollutionPoint> points = DatabaseTools.getPointsInBounds(values);
					values.close();
					
					Log.d(TAG,"Location changed");
					
					float pollution = 0;
					if(points.size()>0){
						
						for(int i=0;i<points.size();i++){
							
							pollution += points.get(i).intensity;
						}
						
						
						pollution = pollution/points.size();
						
						//pollutionOverlay.value = points.get(0).calculatePollutionIntensityValue();
						pollutionOverlay.value = (int)pollution;
						System.out.println("Pollution average value" + pollutionOverlay.value );
						
						int percentage = (int)(pollution * 100)/255;
						pollutionIntensity.setText(percentage + " %");
					}
						

					/*
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							progressDialog.dismiss();
						}
					});
					*/
				}
			};
			
			executorService.submit(runnable);
			
	    }
	    
	    class MyLocListener implements LocationListener{

			@Override
			public void onLocationChanged(Location location) {
		
				getData(location);

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
	            Toast.makeText(getApplicationContext(), "Provider changed to" + mCurrentProvider, Toast.LENGTH_SHORT);
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





// ----------------------------------------------------------------------

class Preview extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder mHolder;
    Camera mCamera;
    PollutionCameraOverlay pollutionOverlay;
    CompassView compassView;
    boolean mFinished;

    Preview(Context context, PollutionCameraOverlay pollutionOverlay,CompassView compassView) {
        super(context);
        
        this.pollutionOverlay = pollutionOverlay;
        mFinished = false;
        this.compassView = compassView;
        

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = Camera.open();
        try {
           
           mCamera.setPreviewDisplay(holder);
           
           // Preview callback used whenever new camera frame is available
           mCamera.setPreviewCallback(new PreviewCallback() {
        	  public void onPreviewFrame(byte[] data, Camera camera)
        	  {
        		  
        		  pollutionOverlay.invalidate();
        		  
        		  if ( (compassView == null) || mFinished )
        			  return;
        		 
    			  compassView.invalidate();
    			  
    			  
        	  }
           });
        } 
        catch (IOException exception) {
            mCamera.release();
            mCamera = null;
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
    	
    	mFinished = true;
    	mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
    	
    	   Camera.Parameters parameters = mCamera.getParameters();
    	    List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();

    	    // You need to choose the most appropriate previewSize for your app
    	    Camera.Size previewSize = previewSizes.get(0);// .... select one of previewSizes here

    	    

    	mCamera.setDisplayOrientation(0);
        //Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(previewSize.width, previewSize.height);
        //parameters.setPreviewSize(320, 240);
        //parameters.setPreviewFrameRate(15);
        //parameters.setSceneMode(Camera.Parameters.SCENE_MODE_NIGHT);
        //parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        mCamera.setParameters(parameters);
        mCamera.startPreview();
    }

}
