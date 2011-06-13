package carnero.cgeo.mapsforge;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.Overlay;
import org.mapsforge.android.maps.Projection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import carnero.cgeo.cgSettings;
import carnero.cgeo.mapcommon.cgMapOverlay;
import carnero.cgeo.mapcommon.cgUsersOverlay;
import carnero.cgeo.mapinterfaces.GeoPointBase;
import carnero.cgeo.mapinterfaces.MapControllerBase;
import carnero.cgeo.mapinterfaces.MapProjection;
import carnero.cgeo.mapinterfaces.MapViewBase;
import carnero.cgeo.mapinterfaces.OverlayImpl;

public class mfMapView extends MapView implements MapViewBase{

	public mfMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
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
		// nothing to do here
	}

	@Override
	public MapControllerBase getMapController() {
		return new mfMapController(getController());
	}

	@Override
	public GeoPointBase getMapViewCenter() {
		GeoPoint point = getMapCenter();
		return new mfGeoPoint(point.getLatitudeE6(), point.getLongitudeE6());
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
	public MapProjection getMapProjection() {
		return new mfMapProjection(getProjection());
	}

	@Override
	public cgMapOverlay createAddMapOverlay(cgSettings settings,
			Context context, Drawable drawable, boolean fromDetailIntent) {
		
		mfCacheOverlay ovl = new mfCacheOverlay(settings, context, drawable, fromDetailIntent);
		getOverlays().add(ovl);
		return ovl.getBase();
	}
	
	@Override
	public cgUsersOverlay createAddUsersOverlay(Context context, Drawable markerIn) {
		mfUsersOverlay ovl = new mfUsersOverlay(context, markerIn);
		getOverlays().add(ovl);
		return ovl.getBase();
	}

	@Override
	public int getLatitudeSpan() {

		Projection projection = getProjection();
		
		GeoPoint low = projection.fromPixels(0, 0);
		GeoPoint high = projection.fromPixels(0, getHeight());

		return Math.abs(high.getLatitudeE6() - low.getLatitudeE6());
	}

	@Override
	public int getLongitudeSpan() {
		Projection projection = getProjection();
		
		GeoPoint low = projection.fromPixels(0, 0);
		GeoPoint high = projection.fromPixels(getWidth(), 0);

		return Math.abs(high.getLongitudeE6() - low.getLongitudeE6());
	}

	@Override
	public boolean isSatellite() {
		return false;
	}

	@Override
	public void preLoad() {
		// Nothing to do here
	}

	@Override
	public void setSatellite(boolean b) {
		// Nothing to do here
	}

	@Override
	public int getMapZoomLevel() {
		return getZoomLevel();
	}
}
