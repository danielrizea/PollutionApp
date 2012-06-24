package com.polution.ar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.View;

import com.pollution.R;

public class MapScaleView extends View{

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

	Bitmap backbuffer;
	Canvas myCanvas;
	
	public MapScaleView(Context context, AttributeSet attrs){
		super(context,attrs);
		System.out.println("Width height " + getWidth() + " " + getHeight());
		
		 TypedArray a = getContext().obtainStyledAttributes(attrs,R.styleable.MapScaleView);
		 
			final int N = a.getIndexCount();
			for (int i = 0; i < N; ++i)
			{
			    int attr = a.getIndex(i);
			    switch (attr)
			    {

			        case R.styleable.MapScaleView_width:
			            this.width = a.getInt(attr, 40);
			            break;
  
			        case R.styleable.MapScaleView_height:
			            this.height = a.getInt(attr,150);
			            break;
			    }
			}
			a.recycle();
		
		
		backbuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		myCanvas = new Canvas(backbuffer);
	}
	
	
	public MapScaleView(Context context) {
		super(context);
		hasInitialized = false;
		
		backbuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		myCanvas = new Canvas(backbuffer);
		Paint p = new Paint();
		p.setStyle(Paint.Style.FILL);
		p.setColor(Color.TRANSPARENT);
		this.width = getWidth();
		this.height = getHeight();
		myCanvas.drawRect(0, 0, width, height, p);

	}

	
    @Override  
    protected void onDraw(Canvas canvas) {  

    	Paint paint = new Paint();
    	paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
    	myCanvas.drawPaint(paint);
    	paint.setXfermode(new PorterDuffXfermode(Mode.SRC));

    	
	    drawBackground(myCanvas);  
	 
	    drawScale(myCanvas);
	    
	    Paint p = new Paint();
	    p.setColor(Color.argb(255, 10, 10, 10));
	    /*
	    myCanvas.drawText("100", 0, 10, p);
	    myCanvas.drawText("0", 0, height, p);
	    */
	    
		canvas.drawBitmap(backbuffer,0,0,null);
		
    }  
	
    private void drawBackground(Canvas canvas) {  
    	
    	
    	//canvas.drawLine(0, getWidth()/2, getWidth(), getHeight()/2, paintRadarGrid);  
    	//canvas.drawLine(getWidth()/2, 0, getWidth()/2, getHeight(), paintRadarGrid); 
    	
    	LinearGradient lg = new LinearGradient(0, 0, 0, height, Color.argb(255, 0, 0, 0), Color.argb(0, 0, 0, 0), TileMode.CLAMP);
    	
    	Paint p = new Paint();
    	p.setShader(lg);
    	
    	canvas.drawRect(0, 0, width, height, p);
    	
    	int[] pixels = new int[(int) (this.width * this.height)];
		backbuffer.getPixels(pixels, 0, this.width, 0, 0, this.width,
				this.height);

		for (int i = 0; i < pixels.length; i++) {
			int r = 0, g = 0, b = 0, tmp = 0;
			int alpha = 255 & (pixels[i] >>> 24);

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
			
			pixels[i] = Color.argb(255, r, g, b);
		}
		backbuffer.setPixels(pixels, 0, this.width, 0, 0, this.width,
				this.height);
				
    }

    
   
    
    private void drawScale(Canvas canvas){
    	
    	 int w = width;
         int h = height;

         Paint p = new Paint();
         p.setColor(Color.argb(255, 0, 0, 0));
         
         int step = 20;
         
         //canvas.drawLine(0, 0+1, w, 0+5, p);
         for(int i=step;i<h;i+=step){
        	 
        	 canvas.drawLine(0, i, w, i, p);
         }
         
         //canvas.drawLine(0, h-1, w, h-5, p);
    }
    
}
