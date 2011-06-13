package carnero.cgeo.mapsforge;

import org.mapsforge.android.maps.ItemizedOverlay;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.Projection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import carnero.cgeo.mapcommon.cgUsersOverlay;
import carnero.cgeo.mapinterfaces.ItemizedOverlayImpl;
import carnero.cgeo.mapinterfaces.MapProjection;
import carnero.cgeo.mapinterfaces.MapViewBase;

public class mfUsersOverlay extends ItemizedOverlay<mfUsersOverlayItem> implements ItemizedOverlayImpl {

	private cgUsersOverlay _base;

	public mfUsersOverlay(Context contextIn, Drawable markerIn) {
		super(boundCenter(markerIn));
		_base = new cgUsersOverlay(this, contextIn);
	}
	
	@Override
	public cgUsersOverlay getBase() {
		return _base;
	}

	@Override
	protected mfUsersOverlayItem createItem(int i) {
		if (_base == null)
			return null;

		return (mfUsersOverlayItem) _base.createItem(i);
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
		// Nothing to do here
	}

	@Override
	public boolean superOnTap(int index) {
		return super.onTap(index);
	}

	@Override
	public void superDraw(Canvas canvas, MapViewBase mapView, boolean shadow) {
		// Nothing to do here
	}

	@Override
	public void superDrawOverlayBitmap(Canvas canvas, Point drawPosition,
			MapProjection projection, byte drawZoomLevel) {
		
		super.drawOverlayBitmap(canvas, drawPosition, (Projection) projection.getImpl(), drawZoomLevel);
	}

}