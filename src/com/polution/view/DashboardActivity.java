/*
 * Pollution Android Application
 * Diploma Project
 * Copyright Daniel-Octavian Rizea
 */
package com.polution.view;


import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.pollution.R;
import com.polution.ar.ARView;
import com.polution.bluetooth.BluetoothChatActivity;
import com.polution.database.AlarmNotifier;
import com.polution.map.PollutionMapActivity;

/**
 * The Class DashboardActivity.
 */
public class DashboardActivity extends Activity{


	/** The feature_1. */
	private Button feature_1;
	
	/** The feature_2. */
	private Button feature_2;
	
	/** The feature_3. */
	private Button feature_3;
	
	/** The feature_4. */
	private Button feature_4;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard);
		
		feature_1 = (Button) findViewById(R.id.home_btn_feature1);
		
		feature_2 = (Button) findViewById(R.id.home_btn_feature2);
		
		feature_3 = (Button) findViewById(R.id.home_btn_feature3);
		
		feature_4 = (Button) findViewById(R.id.home_btn_feature4);
		
		feature_1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//start pollution map
				Intent intent = new Intent(getBaseContext(),PollutionMapActivity.class);
				startActivity(intent);
			}
		});
		
		
		feature_2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// start augmented reality
				Intent intent = new Intent(getBaseContext(),ARView.class);
				startActivity(intent);
			}
		});
		
		feature_3.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// start sensor 
				Intent intent = new Intent(getBaseContext(),BluetoothChatActivity.class);
				startActivity(intent);
			}
		});
		
		feature_4.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				Intent intent = new Intent(getBaseContext(),ApplicationPreference.class);
				startActivity(intent);
			}
		});
		
		
	}
}
