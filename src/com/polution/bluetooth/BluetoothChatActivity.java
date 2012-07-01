/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.polution.bluetooth;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pollution.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.polution.database.AlarmNotifier;
import com.polution.database.DatabaseTools;
import com.polution.database.GEOPoint;
import com.polution.database.PollutionContentProvider;
import com.polution.map.PollutionMapOverlay;
import com.polution.map.SimpleMapView;
import com.polution.map.model.PollutionPoint;

/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothChatActivity extends MapActivity {
	

    // Debugging
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Layout Views
    private TextView mTitle;

    private TextView param1;
    private TextView param2;
    private TextView param3;
    private TextView batteryStatus;
    
    private ContentResolver contentResolver;
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;

    private static String VOLT_SIGN = "V";
    
    
    //---------------------- Pollution Map Components -------------------------------------//
    
	private PollutionMapOverlay overlay;
	MapController mc;
	SimpleMapView mapView;
	GeoPoint p;
	GeoPoint myLoc;
	private TextView currentPointCoordinates;
	
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
    
	//zoom from 1 (large) - 21 (focus)
    final int MAP_ZOOM_LEVEL = 18;
    
    private TextView providerNameTextView;
    
    private TextView applicationStatusTextView;
    
    
    
    
    public String transformToSensorOutput(String stringValue){
    	
    DecimalFormat maxDigitsFormatter = new DecimalFormat("#.#");

    	
    	float val = PollutionSensor.getBatteryVoltage(stringValue);
    	
    	return maxDigitsFormatter.format(val);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");

        // Set up the window layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.bluetooth);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        
        this.contentResolver = getContentResolver();
        // Initialize the LocationManager
        this.myLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        
		mapView = (SimpleMapView)findViewById(R.id.mapview_polutionoverlay_sensors);
		this.applicationStatusTextView = (TextView) findViewById(R.id.application_status);
		this.providerNameTextView = (TextView) findViewById(R.id.provider_name);
		System.out.println(mapView);
		
		this.overlay = new PollutionMapOverlay(200, mapView);
		
		mapView.getOverlays().add(overlay);
		
		mc = mapView.getController();
		mc.setZoom(MAP_ZOOM_LEVEL);
        this.myMapController = mapView.getController();
        
		List<Overlay> listOfOverlays = mapView.getOverlays();
		listOfOverlays.clear();
		
		
		myLocationOverlay = new MyLocationOverlay(this, mapView);
		
		myLocListener = new MyLocListener();
		
		listOfOverlays.add(this.overlay);
		listOfOverlays.add(myLocationOverlay);
		
		//myLocationOverlay.enableCompass();
        myLocationOverlay.enableMyLocation();
        
        setupForLocationAutoUPDATES();

        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);
        
        param1 = (TextView) findViewById(R.id.param1);
        
        param2 = (TextView) findViewById(R.id.param2); 
        
        param3 = (TextView) findViewById(R.id.param3); 
        
        batteryStatus = (TextView) findViewById(R.id.batteryStatus);
        
        
        param1.setText("---.-");
        param2.setText("---.-");
        param3.setText("---.-");
        batteryStatus.setText("--.-V");
        
        //decodeMessageRead("23.3333|342.233|34.34334|323.33");
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
              // Start the Bluetooth chat services
              mChatService.start();
            }
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        myLocationManager.removeUpdates(myLocListener);
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        
        
        if (mChatService != null) mChatService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
        
        
        if(mBluetoothAdapter != null){
        	Toast.makeText(this, "Bluetooth is disabled", Toast.LENGTH_LONG).show();
        	mBluetoothAdapter.cancelDiscovery();
        	//mBluetoothAdapter.disable();
        }
        mBluetoothAdapter = null;

    }

    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }

    // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener =
        new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            if(D) Log.i(TAG, "END onEditorAction");
            return true;
        }
    };

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                
                case BluetoothChatService.STATE_CONNECTED:
                    mTitle.setText(R.string.title_connected_to);
                    mTitle.append(mConnectedDeviceName);
                    break;
                    
                case BluetoothChatService.STATE_CONNECTING:
                    mTitle.setText(R.string.title_connecting);
                    break;
                    
                case BluetoothChatService.STATE_LISTEN:
                case BluetoothChatService.STATE_NONE:
                    mTitle.setText(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);

                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                
                //logic to get message
                
                if(decodeMessageRead(readMessage))
                	Toast.makeText(getApplicationContext(),"Updates received from device",Toast.LENGTH_LONG);
                else
                	Toast.makeText(getApplicationContext(), "Can't decode senzor info : " + readMessage, Toast.LENGTH_LONG);
                
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

    /* Function that decodes information from message
     * protocol : ~val1|val2|val3|batteryStatus 
     * gets different values and post's the on screen
     */
    private boolean decodeMessageRead(String readMessage){

    	StringTokenizer st = new StringTokenizer(readMessage,"~| ");
    	
    	if(st.hasMoreTokens()){
    		
    		String val1 = st.nextToken();
    		Log.d("Message"," String1:"+val1 );
    		
    			if(st.hasMoreTokens()){
    				String val2 = st.nextToken();
    				Log.d("Message"," String2:"+val2 );
    				
    				if(st.hasMoreTokens()){
    					String val3 = st.nextToken();
    					Log.d("Message"," String3:"+val3 );
    					
    					if(st.hasMoreTokens()){
    						String battery = st.nextToken();
    						Log.d("Message"," String4:"+battery );
    						batteryStatus.setText(transformToSensorOutput(battery) + VOLT_SIGN);


    						PollutionPoint point = new PollutionPoint();
    						point.batteryVoltage = PollutionSensor.getBatteryVoltage(battery);
    						
    						
    						float CO_Rx = PollutionSensor.getResitanceValue(Float.parseFloat(val1), point.batteryVoltage,PollutionSensor.CO_SENSOR);
    						float co_ppm = PollutionSensor.getSensorValue(PollutionSensor.CO_SENSOR, CO_Rx); 
    						point.sensor_1 = co_ppm;
    						param1.setText(co_ppm+"");
    						
    						float NO_Rx = PollutionSensor.getResitanceValue(Float.parseFloat(val2), point.batteryVoltage, PollutionSensor.NO_SENSOR);
    						float no_ppb = PollutionSensor.getSensorValue(PollutionSensor.NO_SENSOR, NO_Rx);
    						point.sensor_2 = no_ppb;
    						DecimalFormat fm = new DecimalFormat("#.#");
    						param2.setText(fm.format(no_ppb)+"");
    						
    						System.out.println(" [CO : Rx: "+CO_Rx+" ppm:"+co_ppm +"]");
    						System.out.println(" [NO : Rx: "+NO_Rx+" ppm:"+no_ppb +"]");
    						//percent
    						//float Air_Q_Rx = PollutionSensor.getResitanceValue(Float.parseFloat(val3), point.batteryVoltage, PollutionSensor.AIR_Q_SENSOR);
    						float Air_Q = PollutionSensor.getSensorValue(PollutionSensor.AIR_Q_SENSOR,Float.parseFloat(val3));
    						point.sensor_3 = Air_Q;
    						param3.setText(fm.format(Air_Q).toString());
    						point.sensor_3 = Air_Q;

    						point.timestamp = System.currentTimeMillis();
    						// Or use LocationManager.GPS_PROVIDER

    						Location lastKnownLocation = myLocationManager.getLastKnownLocation(mCurrentProvider);
    						
    						if(lastKnownLocation != null){
    							point.lat = (float)lastKnownLocation.getLatitude();
    							point.lon = (float)lastKnownLocation.getLongitude();
    						}
    						
    						Uri uri = Uri.parse(PollutionContentProvider.CONTENT_URI_POINTS + "/insert");
    	                	contentResolver.insert(uri, DatabaseTools.getContentValues(point));
    	                	Log.d("debug","Point added " + point );
    						Toast.makeText(this, "Point saved " + point, Toast.LENGTH_LONG).show();
    						
    						return true;
    						
    					}else
    						return false; 
    				}
    				else
    					return false;
    			}
    			else
    				return false;	
    	}
    	else
    		return false;

    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                mChatService.connect(device);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupChat();
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.scan:
            // Launch the DeviceListActivity to see devices and do scan
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return true;
        case R.id.discoverable:
            // Ensure this device is discoverable by others
            ensureDiscoverable();
            return true;
            
        case R.id.query_device :
        		sendMessage(PollutionSensor.MESSAGE_QUERY_DEVICE);
        	return true;
        /*	
        case R.id.beep_device_start : 
        	sendMessage(PolutionDeviceConstants.MESSAGE_BEEP_START);
        	return true;
        	
        case R.id.beep_device_stop : 
        	sendMessage(PolutionDeviceConstants.MESSAGE_BEEP_STOP);
        	return true;
       */
        case R.id.start_sampling :{
        	
        	
            // get a Calendar object with current time
            //used to trigger at time
        	QueryService.scheduleQueryServiceWakeup(this, QueryService.BASE_DEFAULT_WAKEUP_TIME);
            Log.d(TAG,"Start sampling");
        	
        }break;
       
        
        case R.id.stop_sampling : {
        	 QueryService.cancelQueryServiceWakeup(this);
             Log.d(TAG,"Stop sampling");
        }
        
        }
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

		 
         List<String> mProviders = myLocationManager.getProviders(true);
         if (mProviders.size() > 0) {
             mCurrentProvider = LocationManager.NETWORK_PROVIDER;
             for (String string : mProviders) {
                 if (string.equals(LocationManager.GPS_PROVIDER))
                     mCurrentProvider = LocationManager.GPS_PROVIDER;
             }
             myLocation = myLocationManager
                     .getLastKnownLocation(mCurrentProvider);
             
             Log.d(TAG,"Provider chosen " + mCurrentProvider);
             providerNameTextView.setText(""+mCurrentProvider+"");
             myLocationManager.requestLocationUpdates(mCurrentProvider,
            		 MINIMUM_TIME_BETWEEN_UPDATE, MINIMUM_DISTANCECHANGE_FOR_UPDATE,myLocListener);
         }
         return false;
		

	}
    
    
    class MyLocListener implements LocationListener{

		@Override
		public void onLocationChanged(Location location) {
	
			mc.animateTo(myLocationOverlay.getMyLocation());
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
                providerNameTextView.setText(""+mCurrentProvider+"");
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
                providerNameTextView.setText(""+mCurrentProvider+"");
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

   
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}