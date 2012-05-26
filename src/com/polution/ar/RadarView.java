package com.polution.ar;

import java.util.ArrayList;

import com.polution.database.GEOPoint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.location.Location;
import android.util.Log;
import android.view.View;

public class RadarView extends View{

	private boolean hasInitialized;

	private int width;
	private int height;
	private float startAngle = 0;
	private float minimumDrawSize;
	
	private Paint paintScreenText;
	private Paint paintRadarGrid;
	private Paint paintRadar;
	private Paint paintRadarBackground;
	private Paint paintRadarSpot;

	public int scaleFactor = 1;
	
	//compass
	private Paint paintRadarCompass;
	private Paint   mPaint = new Paint();
    private Path    mPath = new Path();
    private boolean mAnimate;
    public float mValues[]={0,0,0};
    
    
    
    //------------------
	
	private ArrayList<GEOPoint> points;
	
	//hard coded values 4 testing
	double currentLatitude = 40;
	double currentLongitude = 70;
	
	
	
	public RadarView(Context context) {
		super(context);
		hasInitialized = false;
		
		paintRadarBackground = new Paint();
		
		paintRadarGrid = new Paint();
		paintScreenText = new Paint();
		paintRadar = new Paint();
		paintRadarCompass = new Paint();
		paintRadarSpot = new Paint();
		
		
		paintRadar.setColor(Color.LTGRAY);
		paintRadarGrid.setAntiAlias(true);
		paintRadarGrid.setColor(Color.WHITE);
		paintRadarGrid.setStyle(Paint.Style.STROKE);
		paintRadarGrid.setStrokeWidth(0.7f * scaleFactor);
		
		// TODO Auto-generated constructor stub
		
		paintRadarBackground.setARGB(90, 73, 56, 255);
		paintRadarBackground.setAntiAlias(true);
		
		paintRadarCompass.setARGB(100,10,10,10);
		paintRadarCompass.setAntiAlias(true);
		paintRadarCompass.setStyle(Style.FILL);
		
		paintRadarSpot.setColor(Color.RED);
		paintRadarSpot.setAntiAlias(true);
		paintRadarSpot.setStyle(Style.FILL);
		
		
		 mPath.moveTo(0, -10 * scaleFactor);
         mPath.lineTo(-4 * scaleFactor, 12 * scaleFactor);
         mPath.lineTo(0, 10 * scaleFactor);
         mPath.lineTo(4 * scaleFactor, 12 * scaleFactor);
         mPath.close();
         
         //for testing 
         points = new ArrayList<GEOPoint>();
         
         GEOPoint point = new GEOPoint(40.001,70.001);
         points.add(point);
         
         point = new GEOPoint(40.002,70.000);
         points.add(point);
         
         point = new GEOPoint(40.004,70.000);
         points.add(point);
         
         point = new GEOPoint(40.006,70.000);
         points.add(point);
         
		
	}

	private void initializeConstants(Canvas canvas){
		width = getWidth();
		height = getHeight();
		if(width < height)
			minimumDrawSize = width;
		else
			minimumDrawSize = height;
		
		//transparent background
		canvas.drawARGB(0, 0, 0, 0);
		hasInitialized = true;
		
	}
	
    @Override  
    protected void onDraw(Canvas canvas) {  
    if(!hasInitialized) {  
    initializeConstants(canvas);  
    }  
    
    drawRadar(canvas);
    drawRadarGrid(canvas);  
      
    drawPoints(canvas,points);
    
    drawCompass(canvas);
    String currentLocation = "( " + " )";  
    //canvas.drawText(currentLocation, midpointX-30, midpointY-5, paintScreenText);  
    }  
	
    private void drawRadarGrid(Canvas canvas) {  
    	
    	
    	//canvas.drawLine(0, getWidth()/2, getWidth(), getHeight()/2, paintRadarGrid);  
    	//canvas.drawLine(getWidth()/2, 0, getWidth()/2, getHeight(), paintRadarGrid); 
    		
    	canvas.drawCircle(getWidth()/2, getHeight()/2, 45 * scaleFactor, paintRadarBackground);
    	
    	canvas.drawCircle(getWidth()/2, getHeight()/2, 20 * scaleFactor, paintRadarGrid);
    	
    	
    	canvas.drawCircle(getWidth()/2, getHeight()/2, 40 * scaleFactor, paintRadarGrid);
    	
    	
    }
    
    private void drawRadar(Canvas canvas) {  
    if (startAngle > 360) {  
    startAngle = 0;  
    }  
    float x = (float) (minimumDrawSize / 2 * Math.cos(startAngle  
    * (Math.PI / 180)));  
    float y = (float) (minimumDrawSize / 2 * Math.sin(startAngle  
    * (Math.PI / 180)));  
    canvas.drawLine(getWidth() / 2, getHeight() / 2, x + getWidth() / 2, y  
    + getHeight() / 2, paintRadar);  
    startAngle += 3 * scaleFactor;  
    }  
    
    
    private void drawPoints(Canvas canvas, ArrayList<GEOPoint> pointLocations) {  
    	
   // if(beerIcon == null) {  
   // beerIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.beer);  
   // }  
    
    canvas.save();
    canvas.rotate(-mValues[0],getWidth()/2,getHeight()/2);
    for(GEOPoint pointLoc : pointLocations) {  
    
    	float resultsLon[]= {0,0,0};
    	float resultsLat[]= {0,0,0};
    	
   
    	
    	Location.distanceBetween(currentLatitude, 0, pointLoc.latitude, 0, resultsLat);
    	
    	
    	
    	if(pointLoc.latitude > currentLatitude)
    		resultsLat[0] = - resultsLat[0];
    	
    		
    	Location.distanceBetween(0, currentLongitude, 0,pointLoc.longitude , resultsLon);
    	
    	
    	if(pointLoc.longitude < currentLongitude)
    		resultsLon[0] = - resultsLon[0];
    	
    	
    	
    	
    	// in metri
    	float rad = 500;
    	
    	
    	//TODO
    	
    	
    	float x =  (float)( (resultsLon[0] * getWidth()/2 )/rad + getWidth()/2);  
    
    	float y = (float)( (resultsLat[0] * getHeight()/2)/rad + getHeight()/2);
    	
    
    	//x = -(float)Math.sin((mValues[0] * Math.PI)/180) * x + getWidth()/2;
    	
    	//y = -(float)Math.cos((mValues[0] * Math.PI)/180) * y + getHeight()/2;

    
    	Log.d("distance","Rot angle:"+mValues[0]+"Lat:" + resultsLat[0] + "m "+"Lon:"+ resultsLon[0]+"m" + " X:"+x+" " + "Y:" + y);
     
        canvas.drawCircle(x, y, 4, paintRadarSpot);  
          
    }
    
    
    canvas.restore();
    
    	
    }
    
    private void drawCompass(Canvas canvas){
    	
    	 int w = getWidth();
         int h = getHeight();
         int cx = w / 2;
         int cy = h / 2;
         canvas.save();

         canvas.translate(cx, cy);
         if (mValues != null) {
             //canvas.rotate(-mValues[0]);
         }
      
         canvas.drawPath(mPath, mPaint);
         canvas.restore();
    }
    
}
