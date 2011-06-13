package carnero.cgeo.mapinterfaces;

import carnero.cgeo.mapcommon.ItemizedOverlayBase;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

public interface ItemizedOverlayImpl {

	ItemizedOverlayBase getBase();

	void superPopulate();

	void superSetLastFocusedItemIndex(int i);

	Drawable superBoundCenter(Drawable markerIn);

	Drawable superBoundCenterBottom(Drawable marker);

	boolean superOnTap(int index);

	void superDraw(Canvas canvas, MapViewBase mapView, boolean shadow);

	void superDrawOverlayBitmap(Canvas canvas, Point drawPosition, MapProjection projection,
			byte drawZoomLevel);

}
