package com.polution.bluetooth;

import com.polution.bluetooth.QueryService.MyLocListener;

import android.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

public class QueryAdvancedService extends Service {

	private static final String TAG = "BackgroundService";

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
	
	
	
	
	private ThreadGroup myThreads = new ThreadGroup("ServiceWorker");

	@Override
	public void onCreate() {
		super.onCreate();
		Log.v(TAG, "in onCreate()");
		
		myLocationManager.removeUpdates(myLocListener);
		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		int counter = intent.getExtras().getInt("counter");
		Log.v(TAG, "in onStartCommand(), counter = " + counter + ", startId = "
				+ startId);
		new Thread(myThreads, new ServiceWorker(counter), "BackgroundService")
				.start();
		return START_STICKY;
	}

	class ServiceWorker implements Runnable {
		private int counter = -1;

		public ServiceWorker(int counter) {
			this.counter = counter;
		}

		public void run() {
			final String TAG2 = "ServiceWorker:"
					+ Thread.currentThread().getId();
			// do background processing here... we'll just sleep...

			
			
		}
	}

	@Override
	public void onDestroy() {
		Log.v(TAG,
				"in onDestroy(). Interrupting threads and cancelling notifications");
		myThreads.interrupt();

		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.v(TAG, "in onBind()");
		return null;
	}
	
	
}
