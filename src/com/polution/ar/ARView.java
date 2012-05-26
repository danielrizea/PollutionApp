package com.polution.ar;

/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.IOException;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

// ----------------------------------------------------------------------

public class ARView extends MapActivity {    
    private Preview mPreview;
    private DrawOnTop mDrawOnTop;
    private MapView mapView;
    private RadarView radarView;

    private double lastAngle = 0;
    
    private double radarSensitivityEps = 0;
    private double radarIgnore = 25;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    //private CompassView compassView;
    private float[] mValues;

    // in landscape mode subtract 90
    private float rotation = 0;
    
 // sclae radar onclick
    private boolean scaled = false;
    private int scaleFactor = 2;

    private final SensorEventListener mListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
           
        	mValues = event.values;
        	mValues[0] = mValues[0] + rotation;
        	
        	if(mValues[0] > 360)
        		mValues[0] = mValues[0] - 360;
        	
        	
        	
        	Log.d("debug",
                    "sensorChanged (" + mValues[0] + ", " + event.values[1] + ", " + event.values[2] + ")");
            
        	
        	
        	if(Math.abs(lastAngle - mValues[0]) > radarSensitivityEps){
            	
        	radarView.mValues = mValues;
        	lastAngle = mValues[0]; 
        	}
        	
            if (radarView!= null) {
                radarView.invalidate();
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
        
    };
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // Hide the window title.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        

        
        
        // Create our Preview view and set it as the content of our activity.
        // Create our DrawOnTop view.
        mDrawOnTop = new DrawOnTop(this);
        radarView = new RadarView(this);
        mPreview = new Preview(this, mDrawOnTop,radarView);
        mapView = new MapView(this, "0d2yLfG2hHgd_L1LeWQqtoOX-tPzafQ_ATUX1Fg");
    
        setContentView(mPreview);
        addContentView(mDrawOnTop, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        
        addContentView(radarView, new LayoutParams(100, 100));
 
        radarView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stuff
				
				if(scaled == false){
				ViewGroup.LayoutParams params = radarView.getLayoutParams();
				    params.height = scaleFactor * params.height;
				    params.width = scaleFactor * params.width;
				    
				    radarView.setLayoutParams(params);
				    radarView.scaleFactor = radarView.scaleFactor * scaleFactor;
				    
				    scaled = true;
				}
				else
				{
					
					ViewGroup.LayoutParams params = radarView.getLayoutParams();
				    params.height = params.height / scaleFactor;
				    params.width =  params.width / scaleFactor;
				    
				    radarView.setLayoutParams(params);
				    radarView.scaleFactor = radarView.scaleFactor / scaleFactor;
				    
					
					scaled = false;
					
				}
			}
		});
        
        int test = getResources().getConfiguration().orientation;
        if(Configuration.ORIENTATION_LANDSCAPE == test) {
                    rotation = 90f;
                }
                else {
                    rotation = 0f;
                }

        
        
    }


	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	 @Override
	    protected void onResume()
	    {
	       // if (Config.DEBUG) Log.d(TAG, "onResume");
	        super.onResume();

	        mSensorManager.registerListener(mListener, mSensor,
	                SensorManager.SENSOR_DELAY_UI);
	       
	    }

	    @Override
	    protected void onStop()
	    {
	        //if (Config.DEBUG) Log.d(TAG, "onStop");
	        mSensorManager.unregisterListener(mListener);
	        super.onStop();
	    }

}





// ----------------------------------------------------------------------

class Preview extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder mHolder;
    Camera mCamera;
    DrawOnTop mDrawOnTop;
    RadarView radarView;
    boolean mFinished;

    Preview(Context context, DrawOnTop drawOnTop,RadarView radar) {
        super(context);
        
        mDrawOnTop = drawOnTop;
        mFinished = false;
        radarView = radar;
        

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
           
           // Preview callback used whenever new viewfinder frame is available
           mCamera.setPreviewCallback(new PreviewCallback() {
        	  public void onPreviewFrame(byte[] data, Camera camera)
        	  {
        		  if ( (radarView == null) || mFinished )
        			  return;
        		 
    			  radarView.invalidate();
    			  
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
    	
    	mCamera.setDisplayOrientation(0);
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(320, 240);
        parameters.setPreviewFrameRate(15);
        parameters.setSceneMode(Camera.Parameters.SCENE_MODE_NIGHT);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        mCamera.setParameters(parameters);
        mCamera.startPreview();
    }

}
