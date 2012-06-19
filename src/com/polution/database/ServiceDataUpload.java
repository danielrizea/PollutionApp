





package com.polution.database;

import java.util.List;

import com.polution.map.model.PolutionPoint;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * The Class ServiceDataUpload.
 * 
 * Because most started services don't need to handle multiple requests simultaneously (which can actually be a dangerous multi-threading scenario), it's probably best if you implement your service using the IntentService class.

The IntentService does the following:

    Creates a default worker thread that executes all intents delivered to onStartCommand() separate from your application's main thread.
    Creates a work queue that passes one intent at a time to your onHandleIntent() implementation, so you never have to worry about multi-threading.
    Stops the service after all start requests have been handled, so you never have to call stopSelf().
    Provides default implementation of onBind() that returns null.
    Provides a default implementation of onStartCommand() that sends the intent to the work queue and then to your onHandleIntent() implementation.

All this adds up to the fact that all you need to do is implement onHandleIntent() to do the work provided by the client. (Though, you also need to provide a small constructor for the service.)
 * 
 */
public class ServiceDataUpload extends IntentService{

	private String DEBUG_TAG = "SERVICEDataUpload";

	private ContentResolver contentResolver;
	
	/** 
	   * A constructor is required, and must call the super IntentService(String)
	   * constructor with a name for the worker thread.
	*/
	public ServiceDataUpload() {
		// TODO Auto-generated constructor stub
		super("ServiceDataUpload");
		Log.d(DEBUG_TAG, "Application context initialised");
	}
	
	/* 
	 * Called on service start
	 */
	@Override
	public ComponentName startService(Intent service) {
		return super.startService(service);
	}
	 /**
	   * The IntentService calls this method from the default worker thread with
	   * the intent that started the service. When this method returns, IntentService
	   * stops the service, as appropriate.
	   */
	@Override
	protected void onHandleIntent(Intent arg0) {
		// TODO Auto-generated method stub
		
		this.contentResolver = this.getContentResolver();
		  // Normally we would do some work here, like download a file.
	      // For our sample, we just sleep for 5 seconds.
	      long endTime = System.currentTimeMillis() + 5*1000;
	      
	      
	      //database.getAll();
	      
	      //working content resolver
	      
	      //idea dirty bit push data to server
	      
	      /*
	      	Uri  uri= Uri.parse(PollutionContentProvider.CONTENT_URI_POINTS + "/" + 0 + "/" + 90 + "/" + 0 + "/" + 90);
			Cursor values = contentResolver.query(uri, null, null, null, null);
			
			List<PolutionPoint> points = DatabaseTools.getPointsInBounds(values);
	      */
	      
	      Log.d(DEBUG_TAG, "Service started to do work");
	      while (System.currentTimeMillis() < endTime) {
	          synchronized (this) {
	              try {
	                  wait(endTime - System.currentTimeMillis());
	              } catch (Exception e) {
	              }
	          }
	      }
	      Log.d(DEBUG_TAG, "Service finished work");

	}

	/* 
	 * Destroy method called when the service is no longer used.
	 */
	@Override
	public void onDestroy() {
		Log.d(DEBUG_TAG,"Destroy service");
		//must call super
		super.onDestroy();
	}

}
