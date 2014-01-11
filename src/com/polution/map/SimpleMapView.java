
package com.polution.map;


import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.polution.map.events.PanChangeListener;
import com.polution.map.events.ZoomChangeListener;

public class SimpleMapView extends SupportMapFragment {
	
	private int currentZoomLevel = -1;
	private GeoPoint currentCenter;
	private List<ZoomChangeListener> zoomEvents = new ArrayList<ZoomChangeListener>();
	private List<PanChangeListener> panEvents = new ArrayList<PanChangeListener>();
	
	
	private GoogleMap map;
	//longPress section
    /**
     * Time in ms before the OnLongpressListener is triggered.
     */
    static final int LONGPRESS_THRESHOLD = 500;
 
    /**
     * Keep a record of the center of the map, to know if the map
     * has been panned.
     */
    private GeoPoint lastMapCenter;
 
    private Timer longpressTimer = new Timer();
    private SimpleMapView.OnLongpressListener longpressListener;
    
    
    
    // Define the interface we will interact with from our Map
    public interface OnLongpressListener {
    	public void onLongpress(MapView view, GeoPoint longpressLocation);
    }
    
    public void setOnLongpressListener(SimpleMapView.OnLongpressListener listener) {
        longpressListener = listener;
    }
    // end of custom longPress section
    public SimpleMapView() {
    	map = getMap();
    }
    
    @Override
    public View onCreateView(LayoutInflater arg0, ViewGroup arg1, Bundle arg2) {
        View v = super.onCreateView(arg0, arg1, arg2);
        Fragment fragment = getParentFragment();

        map = getMap();
        
        return v;
    }
    
	/**
	 * 
	 * @return
	 */
	public int[][] getBounds() {
		
		
		LatLng center_new = map.getCameraPosition().target;
		GeoPoint center = new GeoPoint((int)(center_new.latitude * 1E6),(int)(center_new.longitude * 1E6));

		int[][] bounds = new int[2][2];

		/*
		bounds[0][0] = center.getLatitudeE6() - (latitudeSpan / 2);
		bounds[0][1] = center.getLongitudeE6() - (longtitudeSpan / 2);

		bounds[1][0] = center.getLatitudeE6() + (latitudeSpan / 2);
		bounds[1][1] = center.getLongitudeE6() + (longtitudeSpan / 2);
		*/
		VisibleRegion region = map.getProjection().getVisibleRegion();
		
		LatLngBounds region_bounds = region.latLngBounds;
		
		bounds[0][0] = (int)(region_bounds.southwest.latitude * 1E6);
		bounds[0][1] = (int)(region_bounds.southwest.longitude * 1E6);
		
		bounds[1][0] = (int)(region_bounds.northeast.latitude * 1E6);
		bounds[1][1] = (int)(region_bounds.northeast.longitude * 1E6);
		
		return bounds;
	}
	/*
	public boolean onTouchEvent(final MotionEvent ev) {
		
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            // Finger has touched screen.
            longpressTimer = new Timer();
            longpressTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    GeoPoint longpressLocation = map.getProjection().   getProjection().fromPixels((int)ev.getX(),
                            (int)ev.getY()); 
 
                    
                    //  Fire the listener. We pass the map location
                     // of the longpress as well, in case it is needed
                     // by the caller.
                     
                    longpressListener.onLongpress(SimpleMapView.this, longpressLocation);
                }
 
            }, LONGPRESS_THRESHOLD);
 
            lastMapCenter = getMapCenter();
        }
 
        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
 
            if (!getMapCenter().equals(lastMapCenter)) {
                // User is panning the map, this is no longpress
                longpressTimer.cancel();
            }
 
            lastMapCenter = getMapCenter();
        }
 
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            // User has removed finger from map.
            longpressTimer.cancel();
        }
 
            if (ev.getPointerCount() > 1) {
                        // This is a multitouch event, probably zooming.
                longpressTimer.cancel();
            }
		
		
		
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            GeoPoint centerGeoPoint = this.getMapCenter();
            if (currentCenter == null || 
                    (currentCenter.getLatitudeE6() != centerGeoPoint.getLatitudeE6()) ||
                    (currentCenter.getLongitudeE6() != centerGeoPoint.getLongitudeE6()) ) {
            	firePanEvent(currentCenter, this.getMapCenter());
            }
            currentCenter = this.getMapCenter();
        }
        
        return super.onTouchEvent(ev);
    }

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if(getZoomLevel() != currentZoomLevel){
			fireZoomLevel(currentZoomLevel, getZoomLevel());
			currentZoomLevel = getZoomLevel();
		}
	}
	*/
	private void fireZoomLevel(int old, int current){
		for(ZoomChangeListener event : zoomEvents){
			event.onZoom(old, current);
		}
	}
	
	private void firePanEvent(GeoPoint old, GeoPoint current){
		for(PanChangeListener event : panEvents){
			event.onPan(old, current);
		}
	}
	
	public void addZoomChangeListener(ZoomChangeListener listener){
		this.zoomEvents.add(listener);
	}
	
	public void addPanChangeListener(PanChangeListener listener){
		this.panEvents.add(listener);
	}
	

}
