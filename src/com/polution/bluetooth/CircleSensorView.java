package com.polution.bluetooth;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class CircleSensorView extends View {

    Paint mPaint = new Paint();
    
    public int percent = 75;
    
    public String measurement = "%";
    
    private int circleColor;
    
    private int size = 250;
    
    public CircleSensorView(Context context) {
        super(context);            
    }

    public CircleSensorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    public void onDraw(Canvas canvas) {
 
        Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG |
                Paint.DITHER_FLAG |
                Paint.ANTI_ALIAS_FLAG);
        mPaint.setDither(true);
        mPaint.setColor(Color.GRAY);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(20);

        int red = (255*percent)/100;
        int green = (255*(100-percent))/100; 
        int blue = 0;
        circleColor = Color.rgb(red, green, blue);
        
        int radius = 230;
        int delta = size - radius;
        int arcSize = (size - (delta / 2)) * 2;

        //Thin circle
        canvas.drawCircle(size, size, radius, mPaint);

        
        Typeface tf = Typeface.create("SANS_SERIF",Typeface.NORMAL);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTypeface(tf);
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(150);
		canvas.drawText(percent + measurement, size-120, size+40, mPaint);
        
		
		
        //Arc
		//Color.parseColor("#33b5e5")
       // mPaint.setColor(circleColor);
		
		//save state
		mPaint.setStyle(Paint.Style.STROKE);
		Shader gradient = new SweepGradient (size, size, Color.GREEN, Color.RED);
		//rotate
		Matrix matrix = new Matrix();
		matrix.postRotate(-90, size, size);
		gradient.setLocalMatrix(matrix);
		
		mPaint.setShader(gradient);
        mPaint.setStrokeWidth(35);
        RectF box = new RectF(delta, delta, arcSize, arcSize);
        float sweep = 360 * percent * 0.01f;
        canvas.drawArc(box, -90, sweep, false, mPaint);

    }

    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    	final int desiredHSpec = MeasureSpec.makeMeasureSpec(2*size,  heightMeasureSpec);
    	final int desiredWSpec = MeasureSpec.makeMeasureSpec(2*size, widthMeasureSpec);

    	setMeasuredDimension(desiredWSpec, desiredHSpec);

    }
}
