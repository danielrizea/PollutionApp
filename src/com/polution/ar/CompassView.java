package com.polution.ar;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.ar.test.R;

public class CompassView extends View {
	private float azimuth;
	private Paint markerPaint;
	private Paint textPaint;
	private Paint circlePaint;
	private Paint cardinalPaint;
	private Paint northCardinalPaint;
	private String northString;
	private String eastString;
	private String southString;
	private String westString;
	private int textHeight;
	
	float pitch = 0;
	float roll = 0;

	public CompassView(Context context) {
		super(context);
		initCompassView();
		
	}
	
	public CompassView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initCompassView();
	}
	
	public CompassView(Context context, AttributeSet ats, int defaultStyle) {
		super(context, ats, defaultStyle);
		initCompassView();
	}
	
	protected void initCompassView() {
		setFocusable(true);
		
		Resources r = this.getResources();
		northString = r.getString(R.string.cardinal_north);
		southString = r.getString(R.string.cardinal_south);
		westString = r.getString(R.string.cardinal_west);
		eastString = r.getString(R.string.cardinal_east);
		
		circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		circlePaint.setColor(r.getColor(R.color.background_color));
		circlePaint.setStrokeWidth(1);
		circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
		
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setColor(r.getColor(R.color.text_color));
		
		markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		markerPaint.setColor(r.getColor(R.color.marker_color));
		
		cardinalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		cardinalPaint.setColor(r.getColor(R.color.cardinal_color));
		cardinalPaint.setTextSize(20);
		
		northCardinalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		northCardinalPaint.setColor(r.getColor(R.color.cardinal_color_north));
		northCardinalPaint.setTextSize(20);
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		int px = getMeasuredWidth()/2;
		int py = getMeasuredHeight()/2;
		
		int radius = Math.min(px, py) - 5;
		
		canvas.drawCircle(px, py, radius, circlePaint);
		
		canvas.save();
		canvas.rotate(-azimuth, px, py);
				
		int textWidth = (int)textPaint.measureText("W");
		textHeight = textWidth;
		int cardinalTextWidth = (int)cardinalPaint.measureText("W");
		int cardinalTextHeight = (int)cardinalPaint.measureText("W");
		int cardinalX = px - cardinalTextWidth/2;
		int cardinalY = py - cardinalTextHeight/2;
		
		for(int i=0;i<24;i++) {
			//draws the line
			canvas.drawLine(px, py-radius,px,py-radius+10, markerPaint);
			canvas.save();//put the current canvas on the stack
			canvas.translate(0, py-3*textHeight);//translates the stack
			
			//write down the text
			if(i%6 == 0) {//if I need to write down the cardinal point
				String dirString = "";
				switch(i) {
				case(0): 
					dirString = northString;
					int arrowY = py+2*textWidth-10;
					
					canvas.drawLine(px, arrowY+10, px-14, py-1*textHeight+10, markerPaint);
					canvas.drawLine(px, arrowY+10, px+14, py-1*textHeight+10, markerPaint);
					
					cardinalX = px - ((int)cardinalPaint.measureText(dirString))/2;
					cardinalY = py +10;//- ((int)cardinalPaint.measureText(dirString))/2;
					canvas.drawText(dirString, cardinalX, cardinalY, northCardinalPaint);
					break;
				case(6):
					dirString = eastString;
					cardinalX = px - ((int)cardinalPaint.measureText(dirString))/2;
					cardinalY = py +15;//- ((int)cardinalPaint.measureText(dirString))/2;
					canvas.drawText(dirString, cardinalX, cardinalY, cardinalPaint);
					break;
				case(12):
					dirString = southString;
					cardinalX = px - ((int)cardinalPaint.measureText(dirString))/2;
					cardinalY = py +15;//- ((int)cardinalPaint.measureText(dirString))/2;
					canvas.drawText(dirString, cardinalX, cardinalY, cardinalPaint);
					break;
				case(18):
					dirString = westString;
					cardinalX = px - ((int)cardinalPaint.measureText(dirString))/2;
					cardinalY = py +15;//- ((int)cardinalPaint.measureText(dirString))/2;
					canvas.drawText(dirString, cardinalX, cardinalY, cardinalPaint);
					break;
				}//end of switch

			} else {//draw the text every 45 degrees
				String angle = String.valueOf(i*15);
				float angleTextWidth = textPaint.measureText(angle);
				int angleTextX = (int)(px - angleTextWidth/2);
				int angleTextY = (int)(py+textHeight);
				
				//canvas.drawText(angle, angleTextX, angleTextY, textPaint);
			}
			
			canvas.restore();//restore the previous state of the canvas
			canvas.rotate(15, px, py);//rotates the entire canvas 15 degrees
		}
		
		canvas.restore();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int measureWidth = measure(widthMeasureSpec);
		int measureHeight = measure(heightMeasureSpec);
		
		int d = Math.min(measureWidth, measureHeight);
		setMeasuredDimension(d,d);
	}
	
	private int measure(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		
		if(specMode == MeasureSpec.UNSPECIFIED) {
			result = 200;
		} else {
			return specSize;
		}
		return result;
	}
	
	public void setAzimuth(float _azimuth) {
		azimuth = _azimuth - 180;
		invalidate();
	}
	
	public float getAzimuth() {
		return azimuth;
	}
	
	public float getPitch() {
	  return pitch;
	}
	public void setPitch(float pitch) {
	  this.pitch = pitch;
	}
	public float getRoll() {
	  return roll;
	}
	public void setRoll(float roll) {
	  this.roll = roll;
	}

	
}
