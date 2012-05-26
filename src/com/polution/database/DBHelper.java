

package com.polution.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.polution.map.model.PolutionPoint;

/**
 * The Class DBHelper.
 */
public class DBHelper {
  
	
   private static String DEBUG_TAG = "DATABASE_SQLite";
   /** The Constant DB_NAME. */
   public   static  final String DB_NAME = "PolutionAppDatabase";
                                                                     
   /** The Constant DB_TABLE_POLLUTION_POINTS. */
   public   static  final String DB_TABLE_POLLUTION_POINTS = "POLLUTION_POINTS";
                                                                  
   /** The Constant DB_VERSION. */
   public   static  final int DB_VERSION = 1;
 //  private static final String CLASSNAME = DBHelper.class.getSimpleName();
   
   /** The Constant COLS. */
 private static final String[] COLS = new String[]
     { "_id", "lat", "lon", "timestamp", "sensor_1_val", "sensor_2_val", "sensor_3_val"," battery_val" };
   
   /** The db. */
   private SQLiteDatabase db;
   
   /** The db open helper. */
   public DBOpenHelper dbOpenHelper;
   
                                                   
   /**
    * The Class DBOpenHelper.
    */
   private static class DBOpenHelper extends
                                                      
       SQLiteOpenHelper {
       
       /** The Constant DB_CREATE. */
       private static final String DB_CREATE = "CREATE TABLE "
               
+ DBHelper.DB_TABLE_POLLUTION_POINTS
                    
+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT,lat REAL,"
                    
+ "lon REAL, timestamp INTEGER, intensity INTEGER , sensor_1_val REAL, sensor_2_val REAL, sensor_3_val REAL, battery_val REAL);";
       
       /**
        * Instantiates a new dB open helper.
        *
        * @param context the context
        * @param dbName the db name
        * @param version the version
        */
       public DBOpenHelper(Context context, String dbName, int version) {
          super(context, DBHelper.DB_NAME, null, DBHelper.DB_VERSION);
       }
                                                          
       /* (non-Javadoc)
        * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
        */
       @Override
                                                               
       public void onCreate(SQLiteDatabase db) {
          try {
              db.execSQL(DBOpenHelper.DB_CREATE);
              Log.d(DEBUG_TAG,"onCreate: baza de date creata" + DB_TABLE_POLLUTION_POINTS);
          } catch (SQLException e) {
              Log.d(DEBUG_TAG,"onCreate : Baza de date nu a putut fi creata");
          }
       }
       
       /* (non-Javadoc)
        * @see android.database.sqlite.SQLiteOpenHelper#onOpen(android.database.sqlite.SQLiteDatabase)
        */
       @Override
       public void onOpen(SQLiteDatabase db) {
          super.onOpen(db);
       }
                                                                  
                                                                     
       /* (non-Javadoc)
        * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
        */
       @Override
                                                                     
       public void onUpgrade(SQLiteDatabase db, int oldVersion,
          int newVersion) {
          db.execSQL("DROP TABLE IF EXISTS " + DBHelper.DB_TABLE_POLLUTION_POINTS);
          this.onCreate(db);
       }
   }
   
   
   /**
    * Instantiates a new dB helper.
    *
    * @param context the context
    */
   public DBHelper(Context context) {
	   
	      this.dbOpenHelper = new DBOpenHelper(context, "WR_DATA", 1);
	      this.establishDb();
	      
	   }
	                                                                                                        
	   /**
   	 * Establish db.
   	 */
   	private void establishDb() {                               
	      if (this.db == null) {
	
	          this.db = this.dbOpenHelper.getWritableDatabase();
	       }
	    }
	    
    	/**
    	 * Cleanup.
    	 */
    	public void cleanup() {           
	                                   
	       if (this.db != null) {          
	          this.db.close();
	          this.db = null;
	       }
	    }
   
	    /**
    	 * Insert.
    	 *
    	 * @param point the point
    	 */
    	public void insert(PolutionPoint point) {
	       ContentValues values = new ContentValues();
	       values.put("lat", point.lat);
	       values.put("lon", point.lon);
	       values.put("intensity", point.intensity);
	       values.put("sensor_1_val", point.sensor_1);
	       values.put("sensor_2_val", point.sensor_2);
	       values.put("sensor_3_val", point.sensor_3);
	       values.put("battery_val", point.batteryVoltage);
	       values.put("timestamp", point.timestamp);
	       //values.put("date", point.date);
	    
	       try{
	    	   long error = this.db.insertOrThrow(DBHelper.DB_TABLE_POLLUTION_POINTS, null, values);
	    	   Log.d(DEBUG_TAG,"insert : eroare:  ? "+error);
	       
	       }
	       catch(SQLException e){
	    	   e.printStackTrace();
	    	   
	    	   Log.d(DEBUG_TAG,"insert :Nu s-a putut insera inregistrarea in baza de date"+ e.getMessage());
	       }
	       Log.d(DEBUG_TAG,"insert : Insert location "+values.toString() );
	    }
	    
	    /**
    	 * Update a pollution point in the SQL database
    	 *
    	 * @param point the point
    	 */
    	public void update(PolutionPoint point) {
	       ContentValues values = new ContentValues();
	                                                                                                                                                   
	       values.put("lat", point.lat);
	       values.put("lon", point.lon);
	       values.put("intensity", point.intensity);
	       values.put("sensor_1_val", point.sensor_1);
	       values.put("sensor_2_val", point.sensor_2);
	       values.put("sensor_3_val", point.sensor_3);
	       values.put("battery_val", point.batteryVoltage);
	       values.put("timestamp", point.timestamp);

	       this.db.update(DBHelper.DB_TABLE_POLLUTION_POINTS, values, "_id=" + point.id, null);
	    }
	    
    	/**
    	 * Delete.
    	 *
    	 * @param id the id
    	 */
    	public void delete(long id) {
	       this.db.delete(DBHelper.DB_TABLE_POLLUTION_POINTS, "_id=" + id, null);
	    }

	    
	                                                  
	  /**
  	 * Gets the all.
  	 *
  	 * @return the all
  	 */
  	public ArrayList<PolutionPoint> getAll() {
	     ArrayList<PolutionPoint> ret = new ArrayList<PolutionPoint>();
	     Cursor cursor = null;
	     try {
	         cursor = this.db.query(DBHelper.DB_TABLE_POLLUTION_POINTS, DBHelper.COLS, null,
	           null, null, null, null);
	         int numRows = cursor.getCount();
	         
	        Log.d(DEBUG_TAG,"getAll : Se returneaza "+numRows+" inregistrari");
	        cursor.moveToFirst();
	        
	        List<PolutionPoint> points = new ArrayList<PolutionPoint>(cursor.getCount());
			if(cursor.moveToFirst()){
				do {
					PolutionPoint point = new PolutionPoint();
					point.lon = (cursor.getFloat(cursor.getColumnIndex("lon")));
					point.lat = (cursor.getFloat(cursor.getColumnIndex("lat")));
					
					point.sensor_1 = (cursor.getFloat(cursor.getColumnIndex("sensor_1_val")));
					point.sensor_2 = (cursor.getFloat(cursor.getColumnIndex("sensor_2_val")));
					point.sensor_3 = (cursor.getFloat(cursor.getColumnIndex("sensor_3_val")));
					point.batteryVoltage = (cursor.getFloat(cursor.getColumnIndex("battery_val")));
					point.id = (cursor.getInt(cursor.getColumnIndex("_id")));
					
					//get precalculated point pollution intensity
					point.intensity = (cursor.getInt(cursor.getColumnIndex("intensity")));
					
					
					points.add(point);
				} while (cursor.moveToNext());
	        }
	     } catch (SQLException e) {
	        
	     } finally {
	        if (cursor != null && !cursor.isClosed()) {
	            cursor.close();
	        }
	     }
	     return ret;
	  }

	  
	  /**
  	 * Load points.
  	 * return points from SQLite database that are in the specified bounds of the map and also calculate points intensity
  	 * @param bounds the bounds
  	 * @return the list
  	 */
  	public List<PolutionPoint> loadPoints(int[][] bounds){
			String sql = "SELECT * FROM " + DB_TABLE_POLLUTION_POINTS + " WHERE lat >= " + bounds[0][0]/1E6 + " AND lat <= " + bounds[1][0]/1E6 + " AND lon >= " + bounds[0][1]/1E6 + " AND lon <= " + bounds[1][1]/1E6;
			Cursor cursor = null;
			List<PolutionPoint> points = new ArrayList<PolutionPoint>();
		try {
			
			cursor = this.db.rawQuery(sql, null);
			
			if(cursor.moveToFirst()){
				do {
					PolutionPoint point = new PolutionPoint();
					point.lon = (cursor.getFloat(cursor.getColumnIndex("lon")));
					point.lat = (cursor.getFloat(cursor.getColumnIndex("lat")));
					
					point.sensor_1 = (cursor.getFloat(cursor.getColumnIndex("sensor_1_val")));
					point.sensor_2 = (cursor.getFloat(cursor.getColumnIndex("sensor_2_val")));
					point.sensor_3 = (cursor.getFloat(cursor.getColumnIndex("sensor_3_val")));
					point.batteryVoltage = (cursor.getFloat(cursor.getColumnIndex("battery_val")));
					point.id = (cursor.getInt(cursor.getColumnIndex("_id")));
					
					//get precalculated point pollution intensity
					point.intensity = (cursor.getInt(cursor.getColumnIndex("intensity")));
					
					
					points.add(point);
				} while (cursor.moveToNext());
			}
		 } catch (SQLException e) {
		        
	     } finally {
	        if (cursor != null && !cursor.isClosed()) {
	            cursor.close();
	        }
	     }
			
			Log.d(DEBUG_TAG, "loadPoints : return no :" + points.size());
			return points;
		}
	  
}


