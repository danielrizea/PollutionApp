
package com.polution.database;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetWatcher extends BroadcastReceiver{

	
	private String DEBUG_TAG = "BroadcastReceiver";
	/* 
	 * Detect change in network 
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		  // TODO Auto-generated method stub

		  //here, check that the network connection is available. If yes, start your service. If not, stop your service.
	       ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	       NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
	       
	       
	       //verify in preferences what kind of transfers are enabled if they are enabled
	       
	       if(activeNetworkInfo != null){
	    	   
		       if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
		            //start service
		    	   if(activeNetworkInfo.isConnected()){
		    		   Intent serviceIntent = new Intent(context, ServiceDataUpload.class);
		    		   context.startService(serviceIntent);
		    		   Log.d(DEBUG_TAG,"Start service wifi connectivity detected");
		    		   
		    	   }
		        } 
		       
		       if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE){
		    	   //start service
		    	   Log.d(DEBUG_TAG, "Start service on Mobile data connection");
		       }
	       }
	       else
	       {
	    	   Log.d(DEBUG_TAG, "No active network detected");
	       }
	}
	
}
