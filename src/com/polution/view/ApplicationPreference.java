package com.polution.view;


import java.util.ArrayList;
import java.util.Set;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.pollution.R;
import com.polution.bluetooth.QueryService;

public class ApplicationPreference extends PreferenceActivity implements OnSharedPreferenceChangeListener{
	
	
    private BluetoothAdapter mBtAdapter;
    private ArrayList<String> mPairedDevicesArrayAdapter;
    private ArrayList<String> mPairedDevicesArrayAdapterValues;
    
    private ArrayList<String> mNewDevicesArrayAdapter;
    private ArrayList<String> mNewDevicesArrayAdapterValues;
	
    CharSequence[] entrys = null;
    CharSequence[] entryValues = null;
    
    private boolean isBluetoothActivated = false;
    
    private ProgressDialog progressDialog;
    
    ListPreference deviceMAC;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preference_screen);
		setContentView(R.layout.preference_main);
		Context context = getApplicationContext();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		//register preference listener
		prefs.registerOnSharedPreferenceChangeListener(this);
		
		CharSequence key = "deviceMAC";
		deviceMAC = (ListPreference)findPreference(key);
		
		
		Button start_scan_button = (Button) findViewById(R.id.scan_device_button);
		
		
		
		System.out.println("Preference " + deviceMAC + " " + deviceMAC.getKey());
		
		Log.d("pref", prefs.getString("deviceMAC", "defalutValue"));
	        
        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		
		isBluetoothActivated = false;
		
		if(mBtAdapter.isEnabled()==true)
			isBluetoothActivated = true;
		
        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
		mPairedDevicesArrayAdapter = new ArrayList<String>();
		mPairedDevicesArrayAdapterValues = new ArrayList<String>();
		
		mNewDevicesArrayAdapter = new ArrayList<String>();
		mNewDevicesArrayAdapterValues = new ArrayList<String>();

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        String mac = prefs.getString("deviceMAC", "no");
        
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Wait for device discovery");
        
        entrys = new String[20];
        entryValues = new String[20];
        
        
        start_scan_button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(mBtAdapter.isEnabled()){
				
		      	progressDialog.show();

	        	// Get a set of currently paired devices
	            Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

	            // If there are paired devices, add each one to the ArrayAdapter
	            if (pairedDevices.size() > 0) {
	                for (BluetoothDevice device : pairedDevices) {
	                    mPairedDevicesArrayAdapter.add(device.getName() );
	                    mPairedDevicesArrayAdapterValues.add(device.getAddress());
	                }
	            } else {
	                String noDevices = getResources().getText(R.string.none_paired).toString();
	                mPairedDevicesArrayAdapter.add(noDevices);
	                mPairedDevicesArrayAdapterValues.add("no mac");
	            }
	        	
	            // Indicate scanning in the title
	            setProgressBarIndeterminateVisibility(true);
		        // Request discover from BluetoothAdapter
	            mBtAdapter.startDiscovery();
		        Log.d("TAG", "Start discovery");
		        
		        entrys = new String[mPairedDevicesArrayAdapter.size()];
		        entryValues = new String[mPairedDevicesArrayAdapterValues.size()];
		        
		        for(int i=0;i<mPairedDevicesArrayAdapter.size();i++){
		        	entrys[i] = mPairedDevicesArrayAdapter.get(i);
		        	if(i<mPairedDevicesArrayAdapterValues.size())
		        		entryValues[i] = mPairedDevicesArrayAdapterValues.get(i);
		        }
		        deviceMAC.setEntries(entrys);
		        deviceMAC.setEntryValues(entryValues);
			
				}else
				{
					Toast.makeText(getApplicationContext(), "Activate Bluetooth adapter", Toast.LENGTH_SHORT).show();
				}
				
			}
				
		});
        
        
        if(isBluetoothActivated && mac.equals("no")){

        	progressDialog.show();

        	// Get a set of currently paired devices
            Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

            // If there are paired devices, add each one to the ArrayAdapter
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    mPairedDevicesArrayAdapter.add(device.getName() );
                    mPairedDevicesArrayAdapterValues.add(device.getAddress());
                }
            } else {
                String noDevices = getResources().getText(R.string.none_paired).toString();
                mPairedDevicesArrayAdapter.add(noDevices);
                mPairedDevicesArrayAdapterValues.add("no mac");
            }
        	
            // Indicate scanning in the title
            setProgressBarIndeterminateVisibility(true);
	        // Request discover from BluetoothAdapter
            mBtAdapter.startDiscovery();
	        Log.d("TAG", "Start discovery");
	        
	        entrys = new String[mPairedDevicesArrayAdapter.size()];
	        entryValues = new String[mPairedDevicesArrayAdapterValues.size()];
	        
	        for(int i=0;i<mPairedDevicesArrayAdapter.size();i++){
	        	entrys[i] = mPairedDevicesArrayAdapter.get(i);
	        	if(i<mPairedDevicesArrayAdapterValues.size())
	        		entryValues[i] = mPairedDevicesArrayAdapterValues.get(i);
	        }
	        deviceMAC.setEntries(entrys);
	        deviceMAC.setEntryValues(entryValues);
        }
        else
        {
        	entrys = new String[1];
        	entryValues = new String[1];
        	entrys[0] = "Default sensor selected";
        	entryValues[0] = prefs.getString("deviceMAC", "not set");
        	
	        deviceMAC.setEntries(entrys);
	        deviceMAC.setEntryValues(entryValues);
        }
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub
		if(key.equals("enable_adaptive")){
			
			if(sharedPreferences.getBoolean("enable_adaptive", false) == false){
			
				QueryService.wakeupInterval = QueryService.BASE_DEFAULT_WAKEUP_TIME;
				
				//then set normal wakeup time interval
				QueryService.scheduleQueryServiceWakeup(this, QueryService.BASE_DEFAULT_WAKEUP_TIME);
				
			}
				
		}
		else
			if(key.equals("enable_query_service")){
				
				if(sharedPreferences.getBoolean("enable_query_service", true)){
					
					QueryService.wakeupInterval = QueryService.BASE_DEFAULT_WAKEUP_TIME;
					
					//then set normal wakeup time interval
					QueryService.scheduleQueryServiceWakeup(this, QueryService.BASE_DEFAULT_WAKEUP_TIME);
					Toast.makeText(this, "Query Service scheduled", Toast.LENGTH_SHORT).show();
				}
				else{
					QueryService.cancelQueryServiceWakeup(this);
					Toast.makeText(this,"Query Service canceled" , Toast.LENGTH_SHORT).show();
				}
			}
		
		
	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	    // Set up a listener whenever a key changes
	    getPreferenceScreen().getSharedPreferences()
	            .registerOnSharedPreferenceChangeListener(this);
	    
	    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
	}

	@Override
	protected void onPause() {
	    super.onPause();
	    // Unregister the listener whenever a key changes
	    getPreferenceScreen().getSharedPreferences()
	            .unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();

		this.unregisterReceiver(mReceiver);
		
		 getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		 
	}
	
	// The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                	Toast.makeText(context, "Discovered " + device.getName(), Toast.LENGTH_SHORT).show();
                	
                    mNewDevicesArrayAdapter.add(device.getName() );
                    mNewDevicesArrayAdapterValues.add(device.getAddress());
                }
            // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            	setProgressBarIndeterminateVisibility(false);
            	
                Log.d("TAG", "Stop discovery " +" "+ mNewDevicesArrayAdapter.size()+ " "+ mNewDevicesArrayAdapter);
            	
                
            	if (mNewDevicesArrayAdapter.size() == 0) {
                
            		Toast.makeText(context, "No new devices found", Toast.LENGTH_SHORT);
            		/*
                    String noDevices = "No new devices detected"; 
                    		//getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                    mNewDevicesArrayAdapterValues.add(noDevices);
                	*/
                }
            	

                
            	CharSequence[] entrysOld = entrys;
            	CharSequence[] entryValuesOld = entryValues;
            	System.out.println("Out values " + entrysOld.length + " " + entryValuesOld.length);
            	entrys = new String[mNewDevicesArrayAdapter.size() + entrysOld.length];
                entryValues = new String[mNewDevicesArrayAdapterValues.size() + entryValuesOld.length];
                
                Log.d("TAG", "Stop discovery " +" "+ mNewDevicesArrayAdapter.size()+ " "+mNewDevicesArrayAdapterValues.size() + " "+ mNewDevicesArrayAdapter.toArray());
            	
                for(int i=0;i<entrysOld.length;i++){
                	entrys[i] = entrysOld[i];
                	entryValues[i] = entryValuesOld[i];
                	System.out.println("Old " + entrys[i]+ " " + entryValues[i]);
                 }
                Log.d("TAG", "Stop discovery " +" "+ mNewDevicesArrayAdapter.size()+ " "+mNewDevicesArrayAdapterValues.size() + " "+ mNewDevicesArrayAdapter.toArray());
            	
                
                for(int i=0;i<mNewDevicesArrayAdapter.size();i++){
                	entrys[i + entrysOld.length] = mNewDevicesArrayAdapter.get(i);
                	entryValues[i + entrysOld.length] = mNewDevicesArrayAdapterValues.get(i);
                	System.out.println("New " + entrys[i] + " " + entryValues[i] );
                }
                
                for(int i=0;i<entrys.length;i++){
                	System.out.println(" Entry and values " + i + " " + entrys[i] + " " + entryValues[i]);
                	
                }
            	
                deviceMAC.setEntries(entrys);
                deviceMAC.setEntryValues(entryValues);
                
            	progressDialog.dismiss();
            }
        }
    };
}

