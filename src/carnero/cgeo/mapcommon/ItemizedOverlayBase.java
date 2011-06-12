package carnero.cgeo.mapcommon;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import carnero.cgeo.mapinterfaces.ItemizedOverlayImpl;
import carnero.cgeo.mapinterfaces.MapViewBase;
import carnero.cgeo.mapinterfaces.OverlayItemBase;

public abstract class ItemizedOverlayBase {
	
	private ItemizedOverlayImpl _ovlImpl;

	public ItemizedOverlayBase(ItemizedOverlayImpl ovlImplIn) {
		_ovlImpl = ovlImplIn;
	}

	void populate() {
		_ovlImpl.superPopulate();
	}
	
	public boolean onTap(int index) {
		return _ovlImpl.superOnTap(index);
	}
	
	Drawable boundCenter(Drawable markerIn) {
		return _ovlImpl.superBoundCenter(markerIn);
	}
	
	Drawable boundCenterBottom(Drawable markerIn) {
		return _ovlImpl.superBoundCenterBottom(markerIn);
	}
	
	void setLastFocusedItemIndex(int index){
		_ovlImpl.superSetLastFocusedItemIndex(index);
	}
	
	public void draw(Canvas canvas, MapViewBase mapView, boolean shadow) {
		_ovlImpl.superDraw(canvas, mapView, shadow);
	}
	
	public abstract OverlayItemBase createItem(int index);
	
	public abstract int size();
}
