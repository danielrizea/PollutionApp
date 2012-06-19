package com.polution.ar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;

public class DrawOnTop extends View{
	Bitmap mBitmap;
	Paint mPaintBlack;
	Paint mPaintYellow;
	Paint mPaintRed;
	Paint mPaintGreen;
	Paint mPaintBlue;
	byte[] mYUVData;
	int[] mRGBData;
	int mImageWidth, mImageHeight;
	int[] mRedHistogram;
	int[] mGreenHistogram;
	int[] mBlueHistogram;
	double[] mBinSquared;

    public DrawOnTop(Context context) {
        super(context);
        
        mPaintBlack = new Paint();
        mPaintBlack.setStyle(Paint.Style.FILL);
        mPaintBlack.setColor(Color.BLACK);
        mPaintBlack.setTextSize(25);
        
        mPaintYellow = new Paint();
        mPaintYellow.setStyle(Paint.Style.FILL);
        mPaintYellow.setColor(Color.YELLOW);
        mPaintYellow.setTextSize(25);
        
        mPaintRed = new Paint();
        mPaintRed.setStyle(Paint.Style.FILL);
        mPaintRed.setColor(Color.RED);
        mPaintRed.setTextSize(25);
        
        mPaintGreen = new Paint();
        mPaintGreen.setStyle(Paint.Style.FILL);
        mPaintGreen.setColor(Color.GREEN);
        mPaintGreen.setTextSize(25);
        
        mPaintBlue = new Paint();
        mPaintBlue.setStyle(Paint.Style.FILL);
        mPaintBlue.setColor(Color.BLUE);
        mPaintBlue.setTextSize(25);
        
        mBitmap = null;
        mYUVData = null;
        mRGBData = null;
        mRedHistogram = new int[256];
        mGreenHistogram = new int[256];
        mBlueHistogram = new int[256];
        mBinSquared = new double[256];
        for (int bin = 0; bin < 256; bin++)
        {
        	mBinSquared[bin] = ((double)bin) * bin;
        } // bin
    }

    @Override
    protected void onDraw(Canvas canvas) {
    	if (mBitmap != null)
        {
        	int canvasWidth = canvas.getWidth();
        	int canvasHeight = canvas.getHeight();
        	int newImageWidth = canvasWidth;
        	int newImageHeight = canvasHeight;
        	int marginWidth = (canvasWidth - newImageWidth)/2;
        	        	
        	// Convert from YUV to RGB
        	decodeYUV420SP(mRGBData, mYUVData, mImageWidth, mImageHeight);
        	
        	// Draw bitmap
        	mBitmap.setPixels(mRGBData, 0, mImageWidth, 0, 0, 
        			mImageWidth, mImageHeight);
        	Rect src = new Rect(0, 0, mImageWidth, mImageHeight);
        	Rect dst = new Rect(marginWidth, 0, 
        			canvasWidth-marginWidth, canvasHeight);
        	canvas.drawBitmap(mBitmap, src, dst, mPaintBlack);
        	
        	// Draw black borders        	        	
       	canvas.drawRect(0, 0, marginWidth, canvasHeight, mPaintBlack);
        	canvas.drawRect(canvasWidth - marginWidth, 0, 
       			canvasWidth, canvasHeight, mPaintBlack);
        	
        	// Calculate histogram
        	calculateIntensityHistogram(mRGBData, mRedHistogram, 
        			mImageWidth, mImageHeight, 0);
        	calculateIntensityHistogram(mRGBData, mGreenHistogram, 
        			mImageWidth, mImageHeight, 1);
        	calculateIntensityHistogram(mRGBData, mBlueHistogram, 
        			mImageWidth, mImageHeight, 2);
        	
        	// Calculate mean
        	double imageRedMean = 0, imageGreenMean = 0, imageBlueMean = 0;
        	double redHistogramSum = 0, greenHistogramSum = 0, blueHistogramSum = 0;
        	for (int bin = 0; bin < 256; bin++)
        	{
        		imageRedMean += mRedHistogram[bin] * bin;
        		redHistogramSum += mRedHistogram[bin];
        		imageGreenMean += mGreenHistogram[bin] * bin;
        		greenHistogramSum += mGreenHistogram[bin];
        		imageBlueMean += mBlueHistogram[bin] * bin;
        		blueHistogramSum += mBlueHistogram[bin];
        	} // bin
        	imageRedMean /= redHistogramSum;
        	imageGreenMean /= greenHistogramSum;
        	imageBlueMean /= blueHistogramSum;
        	
        	// Calculate second moment
        	double imageRed2ndMoment = 0, imageGreen2ndMoment = 0, imageBlue2ndMoment = 0;
        	for (int bin = 0; bin < 256; bin++)
        	{
        		imageRed2ndMoment += mRedHistogram[bin] * mBinSquared[bin];
        		imageGreen2ndMoment += mGreenHistogram[bin] * mBinSquared[bin];
        		imageBlue2ndMoment += mBlueHistogram[bin] * mBinSquared[bin];
        	} // bin
        	imageRed2ndMoment /= redHistogramSum;
        	imageGreen2ndMoment /= greenHistogramSum;
        	imageBlue2ndMoment /= blueHistogramSum;
        	double imageRedStdDev = Math.sqrt( imageRed2ndMoment - imageRedMean*imageRedMean );
        	double imageGreenStdDev = Math.sqrt( imageGreen2ndMoment - imageGreenMean*imageGreenMean );
        	double imageBlueStdDev = Math.sqrt( imageBlue2ndMoment - imageBlueMean*imageBlueMean );
        	
        	// Draw mean
        	String imageMeanStr = "Mean (R,G,B): " + String.format("%.4g", imageRedMean) + ", " + String.format("%.4g", imageGreenMean) + ", " + String.format("%.4g", imageBlueMean);
        	canvas.drawText(imageMeanStr, marginWidth+10-1, 30-1, mPaintBlack);
        	canvas.drawText(imageMeanStr, marginWidth+10+1, 30-1, mPaintBlack);
        	canvas.drawText(imageMeanStr, marginWidth+10+1, 30+1, mPaintBlack);
        	canvas.drawText(imageMeanStr, marginWidth+10-1, 30+1, mPaintBlack);
        	canvas.drawText(imageMeanStr, marginWidth+10, 30, mPaintYellow);
        	
        	// Draw standard deviation
        	String imageStdDevStr = "Std Dev (R,G,B): " + String.format("%.4g", imageRedStdDev) + ", " + String.format("%.4g", imageGreenStdDev) + ", " + String.format("%.4g", imageBlueStdDev);
        	canvas.drawText(imageStdDevStr, marginWidth+10-1, 60-1, mPaintBlack);
        	canvas.drawText(imageStdDevStr, marginWidth+10+1, 60-1, mPaintBlack);
        	canvas.drawText(imageStdDevStr, marginWidth+10+1, 60+1, mPaintBlack);
        	canvas.drawText(imageStdDevStr, marginWidth+10-1, 60+1, mPaintBlack);
        	canvas.drawText(imageStdDevStr, marginWidth+10, 60, mPaintYellow);
        	
        	// Draw red intensity histogram
        	float barMaxHeight = 3000;
        	float barWidth = ((float)newImageWidth) / 256;
        	float barMarginHeight = 2;
        	RectF barRect = new RectF();
        	barRect.bottom = canvasHeight - 200;
        	barRect.left = marginWidth;
        	barRect.right = barRect.left + barWidth;
        	for (int bin = 0; bin < 256; bin++)
        	{
        		float prob = (float)mRedHistogram[bin] / (float)redHistogramSum;
        		barRect.top = barRect.bottom - 
        			Math.min(80,prob*barMaxHeight) - barMarginHeight;
        		canvas.drawRect(barRect, mPaintBlack);
        		barRect.top += barMarginHeight;
        		canvas.drawRect(barRect, mPaintRed);
        		barRect.left += barWidth;
        		barRect.right += barWidth;
        	} // bin
        	
        	// Draw green intensity histogram
        	barRect.bottom = canvasHeight - 100;
        	barRect.left = marginWidth;
        	barRect.right = barRect.left + barWidth;
        	for (int bin = 0; bin < 256; bin++)
        	{
        		barRect.top = barRect.bottom - Math.min(80, ((float)mGreenHistogram[bin])/((float)greenHistogramSum) * barMaxHeight) - barMarginHeight;
        		canvas.drawRect(barRect, mPaintBlack);
        		barRect.top += barMarginHeight;
        		canvas.drawRect(barRect, mPaintGreen);
        		barRect.left += barWidth;
        		barRect.right += barWidth;
        	} // bin
        	
        	// Draw blue intensity histogram
        	barRect.bottom = canvasHeight;
        	barRect.left = marginWidth;
        	barRect.right = barRect.left + barWidth;
        	for (int bin = 0; bin < 256; bin++)
        	{
        		barRect.top = barRect.bottom - Math.min(80, ((float)mBlueHistogram[bin])/((float)blueHistogramSum) * barMaxHeight) - barMarginHeight;
        		canvas.drawRect(barRect, mPaintBlack);
        		barRect.top += barMarginHeight;
        		canvas.drawRect(barRect, mPaintBlue);
        		barRect.left += barWidth;
        		barRect.right += barWidth;
        	} // bin
        } // end if statement
        
        super.onDraw(canvas);
        
    } // end onDraw method

    static public void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
    	final int frameSize = width * height;
    	
    	for (int j = 0, yp = 0; j < height; j++) {
    		int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
    		for (int i = 0; i < width; i++, yp++) {
    			int y = (0xff & ((int) yuv420sp[yp])) - 16;
    			if (y < 0) y = 0;
    			if ((i & 1) == 0) {
    				v = (0xff & yuv420sp[uvp++]) - 128;
    				u = (0xff & yuv420sp[uvp++]) - 128;
    			}
    			
    			int y1192 = 1192 * y;
    			int r = (y1192 + 1634 * v);
    			int g = (y1192 - 833 * v - 400 * u);
    			int b = (y1192 + 2066 * u);
    			
    			if (r < 0) r = 0; else if (r > 262143) r = 262143;
    			if (g < 0) g = 0; else if (g > 262143) g = 262143;
    			if (b < 0) b = 0; else if (b > 262143) b = 262143;
    			
    			rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
    		}
    	}
    }
    
    static public void decodeYUV420SPGrayscale(int[] rgb, byte[] yuv420sp, int width, int height)
    {
    	final int frameSize = width * height;
    	
    	for (int pix = 0; pix < frameSize; pix++)
    	{
    		int pixVal = (0xff & ((int) yuv420sp[pix])) - 16;
    		if (pixVal < 0) pixVal = 0;
    		if (pixVal > 255) pixVal = 255;
    		rgb[pix] = 0xff000000 | (pixVal << 16) | (pixVal << 8) | pixVal;
    	} // pix
    }
    
    static public void calculateIntensityHistogram(int[] rgb, int[] histogram, int width, int height, int component)
    {
    	for (int bin = 0; bin < 256; bin++)
    	{
    		histogram[bin] = 0;
    	} // bin
    	if (component == 0) // red
    	{
    		for (int pix = 0; pix < width*height; pix += 3)
    		{
	    		int pixVal = (rgb[pix] >> 16) & 0xff;
	    		histogram[ pixVal ]++;
    		} // pix
    	}
    	else if (component == 1) // green
    	{
    		for (int pix = 0; pix < width*height; pix += 3)
    		{
	    		int pixVal = (rgb[pix] >> 8) & 0xff;
	    		histogram[ pixVal ]++;
    		} // pix
    	}
    	else // blue
    	{
    		for (int pix = 0; pix < width*height; pix += 3)
    		{
	    		int pixVal = rgb[pix] & 0xff;
	    		histogram[ pixVal ]++;
    		} // pix
    	}
    }
}
