
package com.polution.database;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;


/**
 * The Class PollutionContentProvider.
 */
public class PollutionContentProvider extends ContentProvider {
	
	

	/** The Pollution Provider. */
	public static PollutionContentProvider pollutionContentProvider;
	// Remove server initialization
    /** The Constant protocol. */
	public static final String protocol = "http://";
    
    /** The Constant host. */
    public static final String host = "embedded.cs.pub.ro/";
    
    /** The Constant serverURL. */
    public static final String serverURL = "si/zigbee/";
    		
    /** The content resolver. */
    public static ContentResolver contentResolver;
   
    /** The Constant baseUri. */
    public static final String baseUri = "http://embedded.cs.pub.ro/si/zigbee/";
    
	   
	/** The Constant PROVIDER_NAME. */
	public static final String PROVIDER_NAME = "com.polution.database.PollutionContentProvider";
	
	/** The Constant PROVIDER. */
	public static final String PROVIDER = "content://" + PROVIDER_NAME;
	
	/** The Constant CONTENT_URI_DEVICES. */
	public static final String CONTENT_URI_POINTS = "content://"+ PROVIDER_NAME + "/points";

	
	/** The Constant DEBUG. */
	public static final String DEBUG_TAG = "PollutionContentP";
	
	// column names

	public static final String POINT_ID_COL = "_id";
	public static final String POINT_LAT_COL = "lat";
	public static final String POINT_LON_COL = "lon";
	public static final String POINT_SENSOR_1_COL = "sensor_1_val";
	public static final String POINT_SENSOR_2_COL = "sensor_2_val";
	public static final String POINT_SENSOR_3_COL = "sensor_3_val";
	public static final String POINT_BATTERY_VAL_COL = "battery_val";
	public static final String POINT_INTENSITY_COL = "intensity";
	public static final String POINT_TIMESTAMP = "timestamp";
	
	
	
    // routes
    
    /** The Constant DEVICES. */
    private static final int DEVICES = 0;
    
    /** The Constant DEVICE_CLUSTER_VALUES. */
    private static final int POINTS_IN_BOUNDS = 1;

    /** The Constant INSERT_POINT. */
    private static final int INSERT_POINT = 2;
    
    /** The Constant UPDATE_POINT. */
    private static final int UPDATE_POINT = 3;
    
    private static final int DELETE_POINT = 4; 
    
    
    // UriMatcher
    /** The Constant uriMatcher. */
    private static final UriMatcher uriMatcher;
	static{
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		
		uriMatcher.addURI(PROVIDER_NAME, "devices", DEVICES);
		uriMatcher.addURI(PROVIDER_NAME, "points/#/#/#/#", POINTS_IN_BOUNDS);
		uriMatcher.addURI(PROVIDER_NAME, "points/insert", INSERT_POINT);
		uriMatcher.addURI(PROVIDER_NAME, "points/update/#", UPDATE_POINT);
		
	}
   
	//---for database use---
	/** The smarthome db. */
	public static SQLiteDatabase pollutionDatabase = null;
	
	/** The Constant DATABASE_NAME. */
	private static final String DATABASE_NAME = "PollutionDatabase";
	
	/** The Constant DATABASE_TABLE_DEVICES. */
	private static final String DATABASE_TABLE_POINTS = "points";
	

	/** The Constant DATABASE_VERSION. */
	private static final int DATABASE_VERSION = 1;
		   
		   /**
   		 * The Class DatabaseHelper.
   		 */
   		private static class DatabaseHelper extends SQLiteOpenHelper 
		   {
		     
   			/** The Constant DB_CREATE. */
   	       private static final String DB_CREATE = "CREATE TABLE "
   	               
	   		+ DATABASE_TABLE_POINTS
	   	                    
	   		+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT,lat REAL,"
	   	                    
	   		+ "lon REAL, timestamp INTEGER, intensity INTEGER , sensor_1_val REAL, sensor_2_val REAL, sensor_3_val REAL, battery_val REAL);";
	   	       
      		/**
      		 * Instantiates a new database helper.
      		 *
      		 * @param context the context
      		 */
      		DatabaseHelper(Context context) {
		         super(context, DATABASE_NAME, null, DATABASE_VERSION);
		      }

		      /* (non-Javadoc)
      		 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
      		 */
      		@Override
		      public void onCreate(SQLiteDatabase db)
		      {
      			 try {
      	              db.execSQL(DB_CREATE);
      	              Log.d(DEBUG_TAG,"onCreate: baza de date creata" + PollutionContentProvider.DATABASE_TABLE_POINTS);
      	          } catch (SQLException e) {
      	              Log.d(DEBUG_TAG,"onCreate : Baza de date nu a putut fi creata");
      	          }
		      }
		      
		      /* (non-Javadoc)
      		 * @see android.database.sqlite.SQLiteOpenHelper#onOpen(android.database.sqlite.SQLiteDatabase)
      		 */
      		@Override
		      public void onOpen(SQLiteDatabase db)
		      {
      			 super.onOpen(db);
  
		      }
		      
		      /* (non-Javadoc)
      		 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
      		 */
      		@Override
		      public void onUpgrade(SQLiteDatabase db, int oldVersion, 
		      int newVersion) {
      			db.execSQL("DROP TABLE IF EXISTS " + PollutionContentProvider.DATABASE_TABLE_POINTS);
                this.onCreate(db);onCreate(db);
		      }
		   }   
		   
		   
		   
		   
		   /* (non-Javadoc)
   		 * @see android.content.ContentProvider#getType(android.net.Uri)
   		 */
   		@Override
		   public String getType(Uri uri) {
		      switch (uriMatcher.match(uri)){
		      	//--get all devices --
		      	case DEVICES:
		      		 return PROVIDER_NAME + "/devices";
		      	case POINTS_IN_BOUNDS:
		      		 return PROVIDER_NAME + "/points/#/#/#/#";
		      	case INSERT_POINT :
		      		 return PROVIDER_NAME + "/points/insert";
		      	case UPDATE_POINT :
		      		 return PROVIDER_NAME + "/points/update/#";
		        default:
		            throw new IllegalArgumentException("Unsupported URI: " + uri);        
		      }   
		   }
		   
		   /* (non-Javadoc)
   		 * @see android.content.ContentProvider#onCreate()
   		 */
   		@Override
		   public boolean onCreate() {
		      Context context = getContext();
		      DatabaseHelper dbHelper = new DatabaseHelper(context);
		      pollutionDatabase = dbHelper.getWritableDatabase();
		      pollutionContentProvider = this;
		      return (pollutionDatabase == null)? false:true;
		   }
		   
		   /* (non-Javadoc)
   		 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
   		 */
   		@Override
		   public Cursor query(Uri uri, String[] projection, String selection,
		      String[] selectionArgs, String sortOrder) {
		           
		      SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
		      
		      switch (uriMatcher.match(uri)){

			      case POINTS_IN_BOUNDS:
			    	  
			    	  sqlBuilder.setTables(DATABASE_TABLE_POINTS);
			    	  
			    	  int bounds[][] = new int[2][2];
			    	  
			    	  Log.d(DEBUG_TAG, "Value of bounds : " + uri.getPathSegments().get(1) + " " +uri.getPathSegments().get(2) + uri.getPathSegments().get(3) + uri.getPathSegments().get(4) );
			    	  
			    	  bounds[0][0] = Integer.parseInt(uri.getPathSegments().get(1));
			    	  bounds[0][1] = Integer.parseInt(uri.getPathSegments().get(2));
			    	  bounds[1][0] = Integer.parseInt(uri.getPathSegments().get(3));
			    	  bounds[1][1] = Integer.parseInt(uri.getPathSegments().get(4));

			    	  sqlBuilder.appendWhere("lat >= " + bounds[0][0]/1E6 + " AND lat <= " + bounds[1][0]/1E6 + " AND lon >= " + bounds[0][1]/1E6 + " AND lon <= " + bounds[1][1]/1E6);

			    	  //if (sortOrder==null || sortOrder=="") sortOrder = "timestamp";
			    	  break;
  
			      default: throw new SQLException("Failed to process " + uri);
		      }	      
		      Cursor c = sqlBuilder.query(pollutionDatabase, projection, selection, selectionArgs, null, null, sortOrder);
		      //---register to watch a content URI for changes---
		      c.setNotificationUri(getContext().getContentResolver(), uri);
		      return c;
		   }
		   
		   /* (non-Javadoc)
   		 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
   		 */
   		@Override
		   public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) 
		   {
		      int changed = 0;
		      
		      switch (uriMatcher.match(uri)){
		    	  
		      case UPDATE_POINT : {
		    	  
		    	  Integer id = Integer.parseInt(uri.getPathSegments().get(3));
		    	  
		    	  SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
		    	  sqlBuilder.setTables(DATABASE_TABLE_POINTS);
		    	  
		    	  //update point with values
		    	  
		      }
		      	break;
			      default: throw new SQLException("Failed to process " + uri);
		      }
		      return changed;
		   }
		   
		   /* (non-Javadoc)
   		 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
   		 */
   		@Override
		   public Uri insert(Uri uri, ContentValues values) {
			   // we will not insert new sensors or actuators
   			
   			switch (uriMatcher.match(uri)){
	    	  
		      case INSERT_POINT : {
		    	  
		    	  long id = pollutionDatabase.insert(DATABASE_TABLE_POINTS, null, values);
				  getContext().getContentResolver().notifyChange(uri, null);
		    	  Log.d(DEBUG_TAG, "polution point inserted " + id);
				  //insert point 	  
		      }
		      	break;
		      	
			      default: throw new SQLException("Failed to process " + uri);
		      }
   			
			   return null;
		   }
		   
		   /* (non-Javadoc)
   		 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
   		 */
   		@Override
		   public int delete(Uri uri, String arg1, String[] arg2) {
   			
   			switch (uriMatcher.match(uri)){
	    	  
		      case DELETE_POINT : {
		    	  
		    	  Integer id = Integer.parseInt(uri.getPathSegments().get(3));
		    	  
		    	  pollutionDatabase.delete(DATABASE_TABLE_POINTS, " _id= " + id, null);
		    	  
		    	  //update point with values
		      }
		      	break;
			      default: throw new SQLException("Failed to process " + uri);
		      }
			   return 0;
		   }
}
