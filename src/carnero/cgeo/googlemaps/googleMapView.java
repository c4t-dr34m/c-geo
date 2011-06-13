package carnero.cgeo.googlemaps;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import carnero.cgeo.cgSettings;
import carnero.cgeo.mapcommon.cgMapOverlay;
import carnero.cgeo.mapcommon.cgUsersOverlay;
import carnero.cgeo.mapinterfaces.GeoPointImpl;
import carnero.cgeo.mapinterfaces.MapControllerImpl;
import carnero.cgeo.mapinterfaces.MapProjectionImpl;
import carnero.cgeo.mapinterfaces.MapViewImpl;
import carnero.cgeo.mapinterfaces.OverlayImpl;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class googleMapView extends MapView implements MapViewImpl{

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
			if (getMapZoomLevel() >= 22) { // to avoid too close zoom level (mostly on Samsung Galaxy S series)
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
	public MapControllerImpl getMapController() {
		return new googleMapController(getController());
	}

	@Override
	public GeoPointImpl getMapViewCenter() {
		GeoPoint point = getMapCenter();
		return new googleGeoPoint(point.getLatitudeE6(), point.getLongitudeE6());
	}

	@Override
	public void addOverlay(OverlayImpl ovl) {
		getOverlays().add((Overlay)ovl);
	}

	@Override
	public void clearOverlays() {
		getOverlays().clear();
	}

	@Override
	public MapProjectionImpl getMapProjection() {
		return new googleMapProjection(getProjection());
	}

	@Override
	public cgMapOverlay createAddMapOverlay(cgSettings settings,
			Context context, Drawable drawable, boolean fromDetailIntent) {
		
		googleCacheOverlay ovl = new googleCacheOverlay(settings, context, drawable, fromDetailIntent);
		getOverlays().add(ovl);
		return ovl.getBase();
	}
	
	@Override
	public cgUsersOverlay createAddUsersOverlay(Context context, Drawable markerIn) {
		googleUsersOverlay ovl = new googleUsersOverlay(context, markerIn);
		getOverlays().add(ovl);
		return ovl.getBase();
	}

	@Override
	public int getMapZoomLevel() {
		return getZoomLevel();
	}

	@Override
	public void setMapFile(String mapFile) {
		// nothing to do for google maps...
	}
}
