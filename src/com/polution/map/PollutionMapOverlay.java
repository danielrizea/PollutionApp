
package com.polution.map;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Shader.TileMode;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import com.polution.map.model.PollutionPoint;

/**
 * An overlay that draws a Pollution Map based using the concept of a value heat map.
 * The Class was inspired by the density heat map class of the mapex project
 */
public class PollutionMapOverlay extends Overlay {
	
	private Bitmap layer;
	private float radius;
	private MapView mapView;
	private ReentrantLock lock;
	
	public PollutionMapOverlay(float radius, MapView mapview){
		this.radius = radius;
		this.mapView = mapview;
		this.lock = new ReentrantLock();
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
		if(layer != null){
			canvas.drawBitmap(layer, 0,0,null);
		}
		
	}
	/**
	 * Does not draw the hitmap 
	 *
	 */
	@Override
	public boolean onTouchEvent(MotionEvent e, MapView mapView) {
		
		if(e.getAction() == MotionEvent.ACTION_DOWN){
			lock.lock();
			this.layer = null;	
			lock.unlock();
		}
		
		return super.onTouchEvent(e, mapView);
	}
	
	/**
	 * Updates the heatmap canvas. Note that for each point, it's lat/lon values should be in decimal format, not 1E6 as used by
	 * GoogleMaps. The class will convert it.
	 * @param points
	 */
	public void update(List<PollutionPoint> points, int flag){
		float pxRadius = (float) (mapView.getProjection().metersToEquatorPixels(radius) * 1/Math.cos(Math.toRadians(mapView.getMapCenter().getLatitudeE6()/1E6)));
		HeatTask task = new HeatTask(mapView.getWidth(), mapView.getHeight(), pxRadius, points, flag);
		new Thread(task).start();
	}	
	
	private class HeatTask implements Runnable{
		
		private Canvas myCanvas;
		private Bitmap backbuffer;
		private int width;
		private int height;
		private float radius;
		private List<PollutionPoint> points;

		//decide which values for sensor to have from point
		private int flag = 0; 
		
		public HeatTask(int width, int height, float radius, List<PollutionPoint> points, int flag){
			backbuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			myCanvas = new Canvas(backbuffer);
			Paint p = new Paint();
			p.setStyle(Paint.Style.FILL);
			p.setColor(Color.TRANSPARENT);
			this.width = width;
			this.height = height;
			this.points = points;
			myCanvas.drawRect(0, 0, width, height, p);
			this.flag = flag;
			this.radius = radius;
			
			//System.out.println("Out " + radius +" " + width +" "+ height);
		}
	/*
	 *  Not used, it is a slow implementation, better use the Potter.DUFF method
	 */
		synchronized public void putCircleMask(int xoff, int yoff, int radius, int[] pixels ){	
			  
			  int w = 2*(radius+2);
			  int mask[][] = new int[w][w];	
			
			  int x0 = radius +1;
			  int y0 = radius +1;
			  
			  
			  int f = 1 - radius;
			  int ddF_x = 1;
			  int ddF_y = -2 * radius;
			  int x = 0;
			  int y = radius;
			  //System.out.println( " bounds " + x0 + " " + y0 + " " + y0+radius + " w: " + w );
			  
			  mask[x0][ y0+radius]= 1;
					  //(x0, y0 + radius);
			  mask[x0][ y0-radius]= 1;
					  //(x0, y0 - radius);
			  
			  mask[(x0 + radius)][ y0]= 1;
					  //(x0 + radius, y0);
			  mask[(x0 - radius)][ y0]= 1;
			  		  //(x0 - radius, y0);
			 
			  while(x < y)
			  {
			    // ddF_x == 2 * x + 1;
			    // ddF_y == -2 * y;
			    // f == x*x + y*y - radius*radius + 2*x - y + 1;
			    if(f >= 0) 
			    {
			      y--;
			      ddF_y += 2;
			      f += ddF_y;
			    }
			    x++;
			    ddF_x += 2;
			    f += ddF_x;    
			    mask[x0 + x][y0 + y] = 1;
			    mask[x0 - x][y0 + y] = 1;
			    mask[x0 + x][y0 - y] = 1;
			    mask[x0 - x][y0 - y] = 1;
			    mask[x0 + y][y0 + x] = 1;
			    mask[x0 - y][y0 + x] = 1;
			    mask[x0 + y][y0 - x] = 1;
			    mask[x0 - y][y0 - x] = 1;
			  }
			 // System.out.println();
			  
			  for(int i=0;i<w;i++){
				  boolean fill = false;
				  
				  for(int j=0;j<w;j++){
					  if(mask[i][j] == 1 && j<w/2)
						  fill = true;
					  else
						  if(mask[i][j] == 1 && j > w/2)
							  fill = false;
					  
					  if(fill){
						  mask[i][j] = 1;
					  	}
					  //System.out.print(mask[i][j]);
				  }
				 // System.out.println();
			  }
			  
			  
			  for(int i=0;i<w;i++)
				  for(int j=0;j<w;j++){
					  if(mask[i][j] == 1)
						  if(mask[i][j] == 1){					  
							  mask[i][j] = 255 -(255/radius+1)* (Math.abs(x0-i) + Math.abs(y0 - j));
							 
							  if(mask[i][j]< 0)
								  mask[i][j] = 0;
					  	}
				  }
			  
			  //System.out.println();
			  System.out.println(" X y " + xoff + " " + yoff + " ");
			  for(int i=0;i<w;i++){
				  for(int j=0;j<w;j++){
					  
					  if((xoff-w/2+i)*width + (yoff+j) > 0 && (xoff-w/2+i)*width + (yoff+j) < pixels.length)
						  if(pixels[(xoff-w/2+i)*width + (yoff+j)] < mask[i][j])
							  pixels[(xoff-w/2+i)*width + (yoff+j)] =   mask[i][j];
					  
					  //linear_mask[i*w+j] = mask[i][j]; 
					 //System.out.print(" " + mask[i][j]);
				  }
				//System.out.println();
			  }
			  
			  return;
		}
		
		
		@Override
		public void run() {
			Projection proj = mapView.getProjection();
/*			
			int[] pixels = new int[(int) (this.width * this.height)];
			backbuffer.getPixels(pixels, 0, this.width, 0, 0, this.width,
					this.height);
*/			
			
			Point out = new Point(1, 1);
			Paint gp = new Paint();
			for(PollutionPoint p : points){
				GeoPoint in = new GeoPoint((int)(p.lat*1E6),(int)(p.lon*1E6));
				proj.toPixels(in, out);
				addPoint(out.x, out.y, p ,gp);
			}

			colorize(0, 0);
			
			lock.lock();
			layer = backbuffer;
			lock.unlock();
			mapView.postInvalidate();
		}
		
		
		private void addPoint(float x, float y, PollutionPoint p, Paint gp) {
	
			int value = 0;
			//get poit value
			switch(this.flag){
			
			case PollutionPoint.CO : { value = p.intensity_CO;} break;
			case PollutionPoint.NO : { value = p.intensity_NO;} break;
			case PollutionPoint.AIR_Q : {value = p.intensity_AirQ;} break;
			case PollutionPoint.ALL_GAS : {value = p.intensity;} break;
			
			}
			
			//System.out.println("Value " + value + " " + p.intensity_CO + " " + p.intensity_NO);
			
			RadialGradient g = new RadialGradient(x, y, radius, Color.argb(
					255, value, 0, 0), Color.argb(255, 0, 0, 0),
					TileMode.CLAMP);

            gp.setShader(null);
			gp.setShader(g);
			//XOR is OK but we can do better
			
			//this is what we want  : [Sa + Da - Sa*Da, Sc*(1 - Da) + Dc*(1 - Sa) + max(Sc, Dc)] 
			// where Sa, Da, are 1 and we get only the max of color from the two
			gp.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN));
			myCanvas.drawCircle(x, y, radius, gp);

			gp.setXfermode(null);

		}

		private void colorize(float x, float y) {
			int[] pixels = new int[(int) (this.width * this.height)];
			backbuffer.getPixels(pixels, 0, this.width, 0, 0, this.width,
					this.height);

			for (int i = 0; i < pixels.length; i++) {
				int r = 0, g = 0, b = 0, tmp = 0;
				//the coe has change, alfa here is the red value
				int alpha = 255 & (pixels[i] >>> 16);
				
				//if(alpha > 255 || alpha < 0 )

				if (alpha == 0) {
					pixels[i] = Color.argb((int) alpha / 2, r, g, b);
					continue;
				}
				
				if (alpha <= 255 && alpha >= 235) {
					tmp = 255 - alpha;
					r = 255 - tmp;
					g = tmp * 12;
				} else if (alpha <= 234 && alpha >= 200) {
					tmp = 234 - alpha;
					r = 255 - (tmp * 8);
					g = 255;
				} else if (alpha <= 199 && alpha >= 150) {
					tmp = 199 - alpha;
					g = 255;
					b = tmp * 5;
				} else if (alpha <= 149 && alpha >= 100) {
					tmp = 149 - alpha;
					g = 255 - (tmp * 5);
					b = 255;
				} else
					b = 255;
				
				pixels[i] = Color.argb((int) alpha/2 , r, g, b);
			}
			backbuffer.setPixels(pixels, 0, this.width, 0, 0, this.width,
					this.height);
		
		System.out.println("Finish overlay");
		}
		
	}
	
}
