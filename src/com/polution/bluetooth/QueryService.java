package com.polution.bluetooth;

import java.util.List;
import java.util.StringTokenizer;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.polution.database.DatabaseTools;
import com.polution.database.PollutionContentProvider;
import com.polution.map.model.PolutionPoint;

public class QueryService extends IntentService{

	
	String mCurrentProvider = LocationManager.NETWORK_PROVIDER;
	
	Boolean mLocationEnabled = false;
	
	private Location lastLocation;
	
	protected LocationManager myLocationManager = null;
	
	protected Location myLocation;
	
	private MyLocListener myLocListener;
	
	final float MINIMUM_DISTANCECHANGE_FOR_UPDATE = 5; // in Meters
	final long MINIMUM_TIME_BETWEEN_UPDATE = 1000; // in Milliseconds
	
	private ContentResolver contentResolver;
	
    private BluetoothAdapter mBluetoothAdapter = null;
	
    private String TAG = "QueryService";
    
 // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;
    private boolean D = true;
    
    
	public QueryService(){
		super("SensorQueryService");
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		
		this.contentResolver = this.getContentResolver();

        this.myLocListener = new MyLocListener();
        // Initialize the LocationManager
        this.myLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
       
        setupForLocationAutoUPDATES();
		
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Log.d(TAG,"Bluetooth not working");
        }
		
		SharedPreferences preferance = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		

		Log.d(TAG, "QueryService start");
		
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
        	mBluetoothAdapter.enable();
            
        while(!mBluetoothAdapter.isEnabled());
        
        // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
        }
        
        if(mChatService == null)
        	setupChat();
        
		if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
              // Start the Bluetooth chat services
            Log.d(TAG,"Start chat ");	
              mChatService.start();
            }
        }
		
		
		if(preferance.contains("deviceMAC") == true){
			
			String address = preferance.getString("deviceMAC", "not set");
	
			//Get the BLuetoothDevice object
			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
			//Attempt to connect to the device
			Log.d(TAG, "Connect to device " + device.getAddress());
			
			Log.d(TAG,"Connect with " + mChatService);
			mChatService.connect(device);
		}

	}

	   private void setupChat() {
	        Log.d(TAG, "setupChat()");

	        // Initialize the BluetoothChatService to perform bluetooth connections
	        mChatService = new BluetoothChatService(this, mHandler);

	    }
	   /*
	   
       // Stop the Bluetooth chat services
       
       
       if (mChatService != null) mChatService.stop();
       if(D) Log.e(TAG, "--- ON DESTROY ---");
       
       
       if(mBluetoothAdapter != null){
       	Toast.makeText(this, "Bluetooth is disabled", Toast.LENGTH_LONG).show();
       	mBluetoothAdapter.cancelDiscovery();
       	//mBluetoothAdapter.disable();
       }
       mBluetoothAdapter = null;
       */
       
       
       // The Handler that gets information back from the BluetoothChatService
       private final Handler mHandler = new Handler() {
           @Override
           public void handleMessage(Message msg) {
               switch (msg.what) {
               case MESSAGE_STATE_CHANGE:
                   if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                   switch (msg.arg1) {
                   
                   case BluetoothChatService.STATE_CONNECTED:
                       if(D) Log.d(TAG,"Connected to " + mConnectedDeviceName);
                       //stop connection
                       sendMessage(PolutionDeviceConstants.MESSAGE_QUERY_DEVICE);
                       
                       break;
                       
                   case BluetoothChatService.STATE_CONNECTING:
                	   if(D) Log.d(TAG,"Connecting");
                       break;
                       
                   case BluetoothChatService.STATE_LISTEN:
                   case BluetoothChatService.STATE_NONE:
                	   if(D) Log.d(TAG,"Not connected");
                       break;
                   }
                   break;
               case MESSAGE_WRITE:
                   byte[] writeBuf = (byte[]) msg.obj;
                   // construct a string from the buffer
                   String writeMessage = new String(writeBuf);
                   if(D) Log.d(TAG,"Write message " + "Me:  " + writeMessage);
                   break;
               case MESSAGE_READ:
                   byte[] readBuf = (byte[]) msg.obj;
                   // construct a string from the valid bytes in the buffer
                   String readMessage = new String(readBuf, 0, msg.arg1);
                   
                   //logic to get message
                   
                   PolutionPoint p = decodeMessageRead(readMessage);
                   if(D) Log.d(TAG,"Point received from sensor");
                   
                   //mChatService.stop();
                   mBluetoothAdapter.disable();
                   
                   Location myLoc = myLocationManager.getLastKnownLocation(mCurrentProvider);
                   
                   Log.d(TAG,"Prepare to add point");
                   
                   
                   if(myLoc != null){
                	   if(!myLoc.equals(lastLocation)){
	                	   p.lat = myLoc.getLatitude();
	                	   p.lon = myLoc.getLongitude();
	                	   p.timestamp = (int)System.currentTimeMillis();
	                	   p.intensity = p.calculatePollutionIntensityValue();
	                	   
	                	   Uri uri = Uri.parse(PollutionContentProvider.CONTENT_URI_POINTS + "/insert");
	                	   contentResolver.insert(uri, DatabaseTools.getContentValues(p));
	                	   Log.d(TAG,"Add point " + p);		
                	   }
                   }
                   lastLocation = myLoc;
                   mChatService.stop();
                   System.out.println("Point " + p.toString());
                   break;
               case MESSAGE_DEVICE_NAME:
                   // save the connected device's name
                   mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                   if(D) Log.d(TAG,"Connected to " + mConnectedDeviceName);
                   break;
               case MESSAGE_TOAST:
            	   if(D) Log.d(TAG,"Toast " + msg.getData().getString("toast"));
			
                   break;
               }
           }
           public void sendMessage(String message) {
               // Check that we're actually connected before trying anything
               if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
                   return;
               }

               // Check that there's actually something to send
               if (message.length() > 0) {
                   // Get the message bytes and tell the BluetoothChatService to write
                   byte[] send = message.getBytes();
                   mChatService.write(send);

               }
           }
       };

       /* Function that decodes information from message
        * protocol : ~val1|val2|val3|batteryStatus 
        * gets different values and post's the on screen
        */
       private PolutionPoint decodeMessageRead(String readMessage){

       	StringTokenizer st = new StringTokenizer(readMessage,"~| ");
       	
       	PolutionPoint point = new PolutionPoint();
       	
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


       						point.sensor_1 = Float.parseFloat(val1);
       						point.sensor_2 = Float.parseFloat(val2);
       						point.sensor_3 = Float.parseFloat(val3);
       						point.batteryVoltage = Float.parseFloat(battery);
       						
       						
       					//	LocationManager locationProvider = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
       						// Or use LocationManager.GPS_PROVIDER

       					//	Location lastKnownLocation = myLocationManager.getLastKnownLocation(mCurrentProvider);
       					/*	
       						if(lastKnownLocation != null){
       							point.lat = (float)lastKnownLocation.getLatitude();
       							point.lon = (float)lastKnownLocation.getLongitude();
       						}
       						
       						Uri uri = Uri.parse(PollutionContentProvider.CONTENT_URI_POINTS + "/insert");
       	                	contentResolver.insert(uri, DatabaseTools.getContentValues(point));
       	                	Log.d("debug","Point added " + point.lat + " " + point.lon );
       						Toast.makeText(this, "Point saved", Toast.LENGTH_SHORT);
       					*/	
       						return point;
       						
       					}else
       						return null; 
       				}
       				else
       					return null;
       			}
       			else
       				return null;	
       	}
       	else
       		return null;

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
   	
       class MyLocListener implements LocationListener{

   		@Override
   		public void onLocationChanged(Location location) {
   			// TODO Auto-generated method stub
   			//stuff to do
           	
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
