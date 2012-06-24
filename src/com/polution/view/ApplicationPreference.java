package com.polution.view;


import java.util.ArrayList;
import java.util.Set;
import java.util.StringTokenizer;

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
import android.widget.ArrayAdapter;

import com.pollution.R;

public class ApplicationPreference extends PreferenceActivity implements OnSharedPreferenceChangeListener{
	
	
    private BluetoothAdapter mBtAdapter;
    private ArrayList<String> mPairedDevicesArrayAdapter;
    private ArrayList<String> mPairedDevicesArrayAdapterValues;
    
    private ArrayList<String> mNewDevicesArrayAdapter;
    private ArrayList<String> mNewDevicesArrayAdapterValues;
	
    CharSequence[] entrys;
    CharSequence[] entryValues;
    
    private boolean isBluetoothActivated = false;
    
    ListPreference deviceMAC;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference_screen);
		Context context = getApplicationContext();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		//register preference listener
		prefs.registerOnSharedPreferenceChangeListener(this);
		
		CharSequence key = "deviceMAC";
		deviceMAC = (ListPreference)findPreference(key);

		
		
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

        if(isBluetoothActivated){
	        	
        	// Register for broadcasts when a device is discovered
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            this.registerReceiver(mReceiver, filter);

            // Register for broadcasts when discovery has finished
            filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            this.registerReceiver(mReceiver, filter);

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
		
	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	    // Set up a listener whenever a key changes
	    getPreferenceScreen().getSharedPreferences()
	            .registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
	    super.onPause();
	    // Unregister the listener whenever a key changes
	    getPreferenceScreen().getSharedPreferences()
	            .unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		if(isBluetoothActivated == true)
			this.unregisterReceiver(mReceiver);
		
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
                    mNewDevicesArrayAdapter.add(device.getName() );
                    mNewDevicesArrayAdapterValues.add(device.getAddress());
                }
            // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            	setProgressBarIndeterminateVisibility(false);
            	
            	
            	CharSequence[] entrysOld = entrys;
            	CharSequence[] entryValuesOld = entryValues;
            	
            	entrys = new String[mNewDevicesArrayAdapter.size() + entrysOld.length];
                entryValues = new String[mNewDevicesArrayAdapterValues.size() + entryValuesOld.length];
                
                for(int i=0;i<entrysOld.length;i++){
                	entrys[i] = entrysOld[i];
                	entryValues[i] = entryValuesOld[i];
                 }
                
                for(int i=entrysOld.length;i<mNewDevicesArrayAdapter.size();i++){
                	entrys[i] = mNewDevicesArrayAdapter.get(i);
                	entryValues[i] = mNewDevicesArrayAdapterValues.get(i);
                }
                
                deviceMAC.setEntries(entrys);
                deviceMAC.setEntryValues(entryValues);

                Log.d("TAG", "Stop discovery " +" "+ mNewDevicesArrayAdapter.size()+ " "+ mNewDevicesArrayAdapter.toArray());
            	
            	if (mNewDevicesArrayAdapter.size() == 0) {
                	
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };
}

