package carnero.cgeo.mapsforge;

import org.mapsforge.android.maps.ItemizedOverlay;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.Projection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import carnero.cgeo.cgSettings;
import carnero.cgeo.mapcommon.cgMapOverlay;
import carnero.cgeo.mapinterfaces.ItemizedOverlayImpl;
import carnero.cgeo.mapinterfaces.MapProjection;
import carnero.cgeo.mapinterfaces.MapViewBase;


public class mfCacheOverlay extends ItemizedOverlay<mfCacheOverlayItem> implements ItemizedOverlayImpl {

	private cgMapOverlay _base;

	public mfCacheOverlay(cgSettings settingsIn, Context contextIn, Drawable markerIn, Boolean fromDetailIn) {
		super(boundCenterBottom(markerIn));
		_base = new cgMapOverlay(settingsIn, this, contextIn, fromDetailIn);
	}
	
	@Override
	public cgMapOverlay getBase() {
		return _base;
	}

	@Override
	protected mfCacheOverlayItem createItem(int i) {
		if (_base == null)
			return null;

		return (mfCacheOverlayItem) _base.createItem(i);
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
	protected void drawOverlayBitmap(Canvas canvas, Point drawPosition,
			Projection projection, byte drawZoomLevel) {
		_base.drawOverlayBitmap(canvas, drawPosition, new mfMapProjection(projection), drawZoomLevel);
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
		// nothing to do
	}

	@Override
	public boolean superOnTap(int index) {
		return super.onTap(index);
	}

	@Override
	public void superDraw(Canvas canvas, MapViewBase mapView, boolean shadow) {
		// nothing to do here...
	}

	@Override
	public void superDrawOverlayBitmap(Canvas canvas, Point drawPosition,
			MapProjection projection, byte drawZoomLevel) {
		super.drawOverlayBitmap(canvas, drawPosition, (Projection) projection.getImpl(), drawZoomLevel);
	}

}

