package carnero.cgeo.googlemaps;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

import carnero.cgeo.cgSettings;
import carnero.cgeo.mapcommon.cgMapOverlay;
import carnero.cgeo.mapinterfaces.ItemizedOverlayImpl;
import carnero.cgeo.mapinterfaces.MapProjection;
import carnero.cgeo.mapinterfaces.MapViewBase;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;

public class googleCacheOverlay extends ItemizedOverlay<googleCacheOverlayItem> implements ItemizedOverlayImpl {

	private cgMapOverlay _base;

	public googleCacheOverlay(cgSettings settingsIn, Context contextIn, Drawable markerIn, Boolean fromDetailIn) {
		super(boundCenterBottom(markerIn));
		_base = new cgMapOverlay(settingsIn, this, contextIn, fromDetailIn);
	}
	
	@Override
	public cgMapOverlay getBase() {
		return _base;
	}

	@Override
	protected googleCacheOverlayItem createItem(int i) {
		if (_base == null)
			return null;

		return (googleCacheOverlayItem) _base.createItem(i);
	}

	@Override
	public int size() {
		if (_base == null)
			return 0;

		return _base.size();
	}

	@Override
	protected boolean onTap(int arg0) {
		if (_base == null)
			return false;
		
		return _base.onTap(arg0);
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		_base.draw(canvas, (MapViewBase) mapView, shadow);
	}

	@Override
	public void superPopulate() {
		populate();
	}

	@Override
	public Drawable superBoundCenter(Drawable markerIn) {
		return super.boundCenter(markerIn);
	}

	@Override
	public Drawable superBoundCenterBottom(Drawable marker) {
		return super.boundCenterBottom(marker);
	}

	@Override
	public void superSetLastFocusedItemIndex(int i) {
		super.setLastFocusedIndex(i);
	}

	@Override
	public boolean superOnTap(int index) {
		return super.onTap(index);
	}

	@Override
	public void superDraw(Canvas canvas, MapViewBase mapView, boolean shadow) {
		super.draw(canvas, (MapView) mapView, shadow);
	}

	@Override
	public void superDrawOverlayBitmap(Canvas canvas, Point drawPosition,
			MapProjection projection, byte drawZoomLevel) {
		// Nothing to do here...
	}

}
