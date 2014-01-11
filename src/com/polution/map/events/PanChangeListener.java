
package com.polution.map.events;

import com.google.android.maps.GeoPoint;

public interface PanChangeListener {
	public void onPan(GeoPoint old, GeoPoint current);
}
