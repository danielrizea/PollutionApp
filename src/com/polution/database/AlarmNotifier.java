

package com.polution.database;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.polution.bluetooth.QueryService;

/**
 * The Class AlarmNotifier. used to schedule updates from sensor
 */
public class AlarmNotifier extends BroadcastReceiver{

	public static int Intent_code = 192837;
	
	/** The DEBU g_ tag. */
	private static String DEBUG_TAG = "AlarmNotifier";

	
	
	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
	
		
		Log.d(DEBUG_TAG," received intent start serviceIntent");
		try {
			//add notification
		
			
			//start sensor query service
			Intent serviceIntent = new Intent(context, QueryService.class);
 		   	context.startService(serviceIntent);
 		   
		    } catch (Exception e) {
		    	Log.d(DEBUG_TAG, "Error receiving and decoding intent" + e.getMessage());
		    }
	}
}
