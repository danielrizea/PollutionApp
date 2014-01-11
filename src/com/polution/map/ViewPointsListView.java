package com.polution.map;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.pollution.R;
import com.polution.bluetooth.QueryService;
import com.polution.database.DatabaseTools;
import com.polution.database.PollutionContentProvider;
import com.polution.map.model.PollutionPoint;
import com.polution.server.FileOperations;
import com.polution.server.ServerComm;
import com.polution.server.XMLProtocol;

public class ViewPointsListView extends Activity{

	
	private ListView lv;
	
	private ListPointsAdapter listAdapter;
	
	private List<PollutionPoint> points;
	
	private TextView pointsNo;
	
	ProgressDialog progressDialog = null;
	
	Context context = null;
	
	private ContentResolver contentResolver;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.points_list);

		lv = (ListView) findViewById(R.id.data_points_list);
		pointsNo= (TextView) findViewById(R.id.data_points_no);
		
		contentResolver = getContentResolver();
		
		try{
			Uri  uri= Uri.parse(PollutionContentProvider.CONTENT_URI_POINTS);
			Cursor values = managedQuery(uri, null, null, null, null);
			
			points = DatabaseTools.getPointsInBounds(values);
			
			values.close();
		}catch(Exception e){};
		
		pointsNo.setText("No points :"+points.size()); 
		
		listAdapter = new ListPointsAdapter(this, R.layout.point_list_item, points);
		lv.setAdapter(listAdapter);
		context = this;
		listAdapter.notifyDataSetChanged();
		
	}
	
	  class ListPointsAdapter extends ArrayAdapter<PollutionPoint> {
	    	private static final String tag = "PolutionPointAdapter";
	    	
	    	private TextView loc;
	    	private TextView sensors;
	    	private TextView timestamp;
	    	
	    	private List<PollutionPoint> records = new ArrayList<PollutionPoint>();

	    	public ListPointsAdapter(Context context, int textViewResourceId,
	    			List<PollutionPoint> objects) {
	    		super(context, textViewResourceId, objects);
	    		this.records = objects;
	    	}

	    	public int getCount() {
	    		return this.records.size();
	    	}

	    	public PollutionPoint getItem(int index) {
	    		return this.records.get(index);
	    	}

	    	public View getView(int position, View convertView, ViewGroup parent) {
	    		View row = convertView;
	    		
	    		// Get item
	    		PollutionPoint record = getItem(position);

	    		//if it is the right type of view
			    if (row == null ) {
			    	
				    	Log.d(tag, "Starting XML Row Inflation ... ");
				    	LayoutInflater inflater = (LayoutInflater) this.getContext()
				    					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				    	row = inflater.inflate(R.layout.point_list_item, parent, false);
				    	Log.d(tag, "Successfully completed XML Row Inflation!");
			    }

		    		// Get reference to TextView - title
		    		loc = (TextView) row.findViewById(R.id.point_coord);
		    		
		    		// Get reference to TextView - author
		    		sensors = (TextView) row.findViewById(R.id.sensor_readings);
		    		
		    		//Get reference to TextView - record Publisher date+publisher
		    		timestamp = (TextView) row.findViewById(R.id.timestamp);
		    
		    		
		    		//System.out.println("Row" + record.getTitle() + " " + record.getAuthor() + " " + record.getDueDate() + " " + record.getRenewals());
		    		loc.setText(record.lat + " " + record.lon);
		    		sensors.setText(record.sensor_1 + " " + record.sensor_2 + " " + record.sensor_3);
		    		timestamp.setText(record.timestamp+"");
		    		
	    		return row;
	    	}
	    }
	  
	  @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.list_points_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	  
	  @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		  switch(item.getItemId()){
			
			case R.id.load_database : {
			

				
				progressDialog = ProgressDialog.show(context, "Server Communication", "Loading database");
				
				
				
				Runnable runnable = new Runnable() {
					
					@Override
					public void run() {
						List<PollutionPoint> points = ServerComm.downloadPoints();
						
						//DELETE old database
						Uri uri = Uri.parse(PollutionContentProvider.CONTENT_URI_POINTS + "/delete/point_table" );
						contentResolver.delete(uri, null, null);
						
						//Insert points from database file
						for(int i=0;i<points.size();i++){
							uri = Uri.parse(PollutionContentProvider.CONTENT_URI_POINTS + "/insert");
		                	contentResolver.insert(uri, DatabaseTools.getContentValues(points.get(i)));
						}
						
						progressDialog.dismiss();
					}
				};
				//List<PollutionPoint> points = XMLProtocol.parseXMLDocument(FileOperations.readFile(this, FileOperations.DATABASE_FILE_NAME));
				
				Thread thread = new Thread(runnable);
				thread.start();
				
				
				
			}break;
			
			case R.id.save_database : 
			{
				final String content = XMLProtocol.retrieveXMLData(points);
				//FileOperations.saveFile(this, FileOperations.DATABASE_FILE_NAME, content);
				
				Log.d("XML", content);
				
				progressDialog = ProgressDialog.show(context, "Server Communication", "Push database to server");
				
				
				Runnable runnable = new Runnable() {
					
					@Override
					public void run() {
						ServerComm.uploadPoints(content);
						progressDialog.dismiss();
					}
				};
				
				Thread threads = new Thread(runnable);
				threads.start();
				
				
				//and on SD card
				//FileOperations.saveFileToSDCard(FileOperations.DATABASE_FILE_NAME, content);
			}break;

		}
		
		return super.onOptionsItemSelected(item);
	}
}
