package carnero.cgeo.googlemaps;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import carnero.cgeo.cgSettings;
import carnero.cgeo.mapinterfaces.GeoPointBase;
import carnero.cgeo.mapinterfaces.MapControllerBase;
import carnero.cgeo.mapinterfaces.MapViewBase;
import carnero.cgeo.mapinterfaces.OverlayBase;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class googleMapView extends MapView implements MapViewBase{

	public googleMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public googleMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public googleMapView(Context context, String apiKey) {
		super(context, apiKey);
	}

	@Override
	public void draw(Canvas canvas) {
		try {
			if (getZoomLevel() >= 22) { // to avoid too close zoom level (mostly on Samsung Galaxy S series)
				getController().setZoom(22);
			}

			super.draw(canvas);
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgMapView.draw: " + e.toString());
		}
	}

	@Override
	public void displayZoomControls(boolean takeFocus) {
		try {
			super.displayZoomControls(takeFocus);
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgMapView.displayZoomControls: " + e.toString());
		}
	}

	@Override
	public MapControllerBase getMapController() {
		return new googleMapController(getController());
	}

	@Override
	public GeoPointBase getMapViewCenter() {
		GeoPoint point = getMapCenter();
		return new googleGeoPoint(point.getLatitudeE6(), point.getLongitudeE6());
	}

	@Override
	public void addOverlay(OverlayBase ovl) {
		getOverlays().add((Overlay)ovl);
	}

	@Override
	public void clearOverlays() {
		getOverlays().clear();
	}
}
