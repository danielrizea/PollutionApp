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

public class PollutionCameraOverlay extends View{

	private boolean hasInitialized;

	private Paint paintBackground;

	private int width;
	
	private int height;
	
	
	public int value = 0;
	
	public PollutionCameraOverlay(Context context,int width, int height) {
		super(context);
		hasInitialized = false;
		
		paintBackground = new Paint();
		this.width = width;
		this.height = height;
		
		// TODO Auto-generated constructor stub
		
		paintBackground.setARGB(255, 73, 56, 255);
		paintBackground.setAntiAlias(true);
		paintBackground.setStyle(Style.FILL);
		
	}

	@Override
	protected void onDraw(Canvas canvas) {
	
		super.onDraw(canvas);
		int r = 0, g = 0, b = 0, tmp = 0;
		
		if (value <= 255 && value >= 235) {
			tmp = 255 - value;
			r = 255 - tmp;
			g = tmp * 12;
		} else if (value <= 234 && value>= 200) {
			tmp = 234 - value;
			r = 255 - (tmp * 8);
			g = 255;
		} else if (value <= 199 && value >= 150) {
			tmp = 199 - value;
			g = 255;
			b = tmp * 5;
		} else if (value <= 149 && value >= 100) {
			tmp = 149 - value;
			g = 255 - (tmp * 5);
			b = 255;
		} else
			b = 255;
		
		paintBackground.setColor(Color.argb(128, r, g, b));
		
		canvas.drawRect(0,0,width,height, paintBackground);
	}

    
}
