package com.polution.bluetooth;

import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.polution.database.AlarmNotifier;
import com.polution.database.DatabaseTools;
import com.polution.database.PollutionContentProvider;
import com.polution.map.PollutionMapActivity;
import com.polution.map.model.PollutionPoint;

public class QueryService extends CustomIntentService{
	
	//15 seconds
	public static int BASE_DEFAULT_WAKEUP_TIME = 15000;
	
	//15 minutes
	public static int MAX_WAKEUP_TIME = 9000000;
	
	public static int wakeupInterval = 15000;
	
	String mCurrentProvider = LocationManager.NETWORK_PROVIDER;
	
	Boolean mLocationEnabled = false;
	
	private static Location lastLocation;
	
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
    
    private WakeLock wl;
	public QueryService(){
		super("SensorQueryService");
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		setIntentRedelivery(true);
		
	}
	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		Log.d(TAG, "Query Service started");
		
		PowerManager pm = (PowerManager)getApplicationContext().getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		
		if(wl != null && wl.isHeld() == false)
			wl.acquire();

		IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		this.registerReceiver(bluetoothOnReceiver, filter);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		myLocationManager.removeUpdates(myLocListener);
		Log.d(TAG, "Query service stop");
		wl.release();
	
		this.unregisterReceiver(bluetoothOnReceiver);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub

		
		Log.d(TAG, "Start handle method");
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
  
        }         
        else {
	        
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
			}else
				Log.d(TAG, "No device mac");
        }
	}

	private BroadcastReceiver bluetoothOnReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			//on state changed
			 if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
		            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
		                                                 BluetoothAdapter.ERROR);
		            switch (state) {
		            case BluetoothAdapter.STATE_OFF:

		                break;
		            case BluetoothAdapter.STATE_TURNING_OFF:

		                break;
		            case BluetoothAdapter.STATE_ON:
		            	 
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
		    			SharedPreferences preferance = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		    			
		    			if(preferance.contains("deviceMAC") == true){
		    				
		    				String address = preferance.getString("deviceMAC", "not set");
		    		
		    				//Get the BLuetoothDevice object
		    				BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		    				//Attempt to connect to the device
		    				Log.d(TAG, "Connect to device " + device.getAddress());
		    				
		    				Log.d(TAG,"Connect with " + mChatService);
		    				mChatService.connect(device);
		    			}else
		    				Log.d(TAG, "No device mac");
		            	
		                break;
		            case BluetoothAdapter.STATE_TURNING_ON:

		                break;
		            }
		        }

			
		}
	};
	
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
       
       private static final int HELLO_ID = 1;
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
                       sendMessage(PollutionSensor.MESSAGE_QUERY_DEVICE);
                       
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
                   
                   PollutionPoint p = decodeMessageRead(readMessage);
                   if(D) Log.d(TAG,"Point received from sensor");
                   
                   Location myLoc = myLocationManager.getLastKnownLocation(mCurrentProvider);
                   

           		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
           		boolean alertsEnabled = prefs.getBoolean("enable_warnings", false);
           		
           
           		if(alertsEnabled){
                   manageAlerts(p, prefs);
           		}

           		if(prefs.getBoolean("enable_adaptive", false))
           			adaptiveWakeupAlgorithm(myLoc, lastLocation);
           		
           		//send broadcast intent
           		Intent intent = new Intent();
           		intent.setAction(BluetoothChatActivity.UPDATE_SENSOR_VALUES_INTENT);
           		intent.putExtra("pollution_point", p);
           		getApplicationContext().sendBroadcast(intent);
           		
           		
                   if(myLoc != null){
                	   
                	 
                	   p.lat = myLoc.getLatitude();
                	   p.lon = myLoc.getLongitude();
                	   p.timestamp = System.currentTimeMillis();
                	   p.calculatePollutionIntensityValue();
	                	   
                	   Uri uri = Uri.parse(PollutionContentProvider.CONTENT_URI_POINTS + "/insert");
                	   contentResolver.insert(uri, DatabaseTools.getContentValues(p));
                	   Log.d(TAG,"Add point " + p);		
                	   Toast.makeText(getApplicationContext(), "Add point to database", Toast.LENGTH_SHORT).show();
                	   
                   }
                   lastLocation = myLoc;

                   //disable the threads in here.
                   
                   mChatService.stop();
                   mBluetoothAdapter.disable();
                   System.out.println("Point " + p.toString());
                   //stop the thread from waiting
                   stopServiceCustom();
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
       private PollutionPoint decodeMessageRead(String readMessage){

       	StringTokenizer st = new StringTokenizer(readMessage,"~| ");
       	
       	PollutionPoint point = new PollutionPoint();
       	
       	if(st.hasMoreTokens()){
       		
       		String val1 = st.nextToken();
       		//Log.d("Message"," String1:"+val1 );

       			if(st.hasMoreTokens()){
       				String val2 = st.nextToken();
       				//Log.d("Message"," String2:"+val2 );

       				if(st.hasMoreTokens()){
       					String val3 = st.nextToken();
       					//Log.d("Message"," String3:"+val3 );

       					if(st.hasMoreTokens()){
       						String battery = st.nextToken();
       						//Log.d("Message"," String4:"+battery );

    						point.batteryVoltage = (1.1f*4.3f*Float.parseFloat(battery))/1024;
    						
    						float CO_Rx = PollutionSensor.getResitanceValue(Float.parseFloat(val1), point.batteryVoltage, PollutionSensor.CO_SENSOR);
    						float co_ppm = PollutionSensor.getSensorValue(PollutionSensor.CO_SENSOR, CO_Rx); 
    						point.sensor_1 = co_ppm;

    						
    						float NO_Rx = PollutionSensor.getResitanceValue(Float.parseFloat(val2), point.batteryVoltage, PollutionSensor.NO_SENSOR);
    						float no_ppb = PollutionSensor.getSensorValue(PollutionSensor.NO_SENSOR, NO_Rx);
    						point.sensor_2 = no_ppb;

    						System.out.println(" [CO : Rx: "+CO_Rx+" ppm:"+co_ppm +"]");
    						System.out.println(" [NO : Rx: "+NO_Rx+" ppm:"+no_ppb +"]");
    						//percent

    						//System.out.println();
    						//float Air_Q_Rx = PollutionSensor.getResitanceValue(Float.parseFloat(val3), point.batteryVoltage, PollutionSensor.AIR_Q_SENSOR);
    						float Air_Q = PollutionSensor.getSensorValue(PollutionSensor.AIR_Q_SENSOR, Float.parseFloat(val3));
    						//System.out.println(Float.parseFloat(val3));
    						point.sensor_3 = Air_Q;
    						point.timestamp = System.currentTimeMillis();

       						point.batteryVoltage = Float.parseFloat(battery);

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
 
    private void adaptiveWakeupAlgorithm(Location myLoc,Location oldLoc){
    
    	if(myLoc == null || oldLoc == null){
    		//cancelQueryServiceWakeup(this);
    		scheduleQueryServiceWakeup(this, QueryService.BASE_DEFAULT_WAKEUP_TIME);
    		return;
    	}
    		//else then execute algorithm
		int oldWakeupTimeInterval = QueryService.wakeupInterval;	
    	if(myLoc.getLatitude() == oldLoc.getLatitude() && myLoc.getLongitude() == oldLoc.getLongitude()){
    		//same location 	

    		int newWakeupTimeInterval = 2*oldWakeupTimeInterval;
    		
    		if(newWakeupTimeInterval > MAX_WAKEUP_TIME){
    			newWakeupTimeInterval = MAX_WAKEUP_TIME;
    		}
    		
    		scheduleQueryServiceWakeup(this, newWakeupTimeInterval);
    		QueryService.wakeupInterval = newWakeupTimeInterval;
    		Toast.makeText(getApplicationContext(), "New scheduled time :" + newWakeupTimeInterval/1000 + " seconds", Toast.LENGTH_SHORT).show();
    		
    	}else
    	{
    		//check to see if wakeup time greater than BASE_WAKEUP_TIME
    		if(oldWakeupTimeInterval > BASE_DEFAULT_WAKEUP_TIME){
    			scheduleQueryServiceWakeup(this, BASE_DEFAULT_WAKEUP_TIME);
    			QueryService.wakeupInterval = BASE_DEFAULT_WAKEUP_TIME;
    			Toast.makeText(this, "Revert wakeup time interval back to base time of " + BASE_DEFAULT_WAKEUP_TIME, Toast.LENGTH_SHORT).show();
    		}
    			
    	}
    		
    	
    }
       
    private void manageAlerts(PollutionPoint p, SharedPreferences prefs){
    	
    	int CO_limit = prefs.getInt("co_sensitivity", -1);
   		int NO_limit = prefs.getInt("no_sensitivity",-1);
   		int AIR_Q_limit = prefs.getInt("air_q_sensitivity", -1);
    	
    	if(p.sensor_1>CO_limit || p.sensor_2 > NO_limit || p.sensor_3 > AIR_Q_limit){
            //notification 
            String ns = Context.NOTIFICATION_SERVICE;
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
            
            int icon = android.R.drawable.btn_minus;
            CharSequence tickerText = "Pollution Alert!";
            long when = System.currentTimeMillis();

            Notification notification = new Notification(icon, tickerText, when);
            
            Context context = getApplicationContext();
            
            String contentTitle = "";
            String contentText = "";
            
            if(p.sensor_1 > CO_limit){
         	   contentTitle += " CO ";
         	   contentText += " CO is " + p.sensor_1 + " ppm over " + CO_limit;
            }
            if(p.sensor_2 > NO_limit){
         	   contentTitle += " NO" ;
         	   contentText += " NO is " + p.sensor_2 + " ppb over " + NO_limit;
            }
            if(p.sensor_3 > AIR_Q_limit){
         	   contentTitle += " Air quality";
         	   contentText += "Air Q is " + p.sensor_3 + " over " + AIR_Q_limit;
            }
            contentTitle += "alert!";
            notification.defaults=Notification.DEFAULT_ALL;
            Intent notificationIntent = new Intent(getApplicationContext(), PollutionMapActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

            notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

            mNotificationManager.notify(HELLO_ID, notification);
            //sendMessage(PollutionSensor.MESSAGE_BEEP_START);
        }
    	
    }
    
    public static boolean scheduleQueryServiceWakeup(Context context,int intervalMillis){
    	Calendar cal = Calendar.getInstance();  
        // add 5 minutes to the calendar object
        //cal.add(Calendar.SECOND, 30);
        
        //set the sensor sampling period
    	Log.d("QueryServiceStatic", "Set alarm " + AlarmNotifier.Intent_code);
    	
        Intent intent = new Intent(context, AlarmNotifier.class);
       
        intent.putExtra("alarm_message", "Query device");
        // In reality, you would want to have a static variable for the request code instead of 192837
        PendingIntent sender = PendingIntent.getBroadcast(context, AlarmNotifier.Intent_code, intent, 0);
        
        // Get the AlarmManager service
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        //am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
        //5 seconds
        am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis()+intervalMillis, intervalMillis, sender);
        QueryService.wakeupInterval = intervalMillis;
        
        return true;
    }
    
    public static boolean cancelQueryServiceWakeup(Context context){
    	
    	Intent intent = new Intent(context, AlarmNotifier.class);
        
    	Log.d("QueryServiceStatic", "cnacel alarm " + AlarmNotifier.Intent_code);
        intent.putExtra("alarm_message", "A message for the app");
        // In reality, you would want to have a static variable for the request code instead of 192837
       
        
        PendingIntent sender = PendingIntent.getBroadcast(context, AlarmNotifier.Intent_code, intent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        
        am.cancel(sender);
        
        return true;
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
