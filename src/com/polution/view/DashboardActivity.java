package com.polution.view;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.ar.test.R;
import com.polution.ar.ARView;
import com.polution.ar.PollutionMapActivity;
import com.polution.bluetooth.BluetoothChatActivity;

public class DashboardActivity extends Activity{


	private Button feature_1;
	
	private Button feature_2;
	
	private Button feature_3;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard);
		
		feature_1 = (Button) findViewById(R.id.home_btn_feature1);
		
		feature_2 = (Button) findViewById(R.id.home_btn_feature2);
		
		feature_3 = (Button) findViewById(R.id.home_btn_feature3);
		
		
		
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
		
	}
}
