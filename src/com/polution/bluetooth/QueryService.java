package com.polution.bluetooth;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;

public class QueryService extends IntentService{

	private ContentResolver contentResolver;
	
	private QueryService(){
		super("SensorQueryService");
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		
	}

	
}
