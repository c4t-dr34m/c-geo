package carnero.cgeo.mapinterfaces;

import carnero.cgeo.mapcommon.cgMapOverlay;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

public interface ItemizedOverlayImpl {

	cgMapOverlay getBase();

	void superPopulate();

	void superSetLastFocusedItemIndex(int i);

	Drawable superBoundCenterBottomMarker(Drawable marker);

	boolean superOnTap(int index);

	void superDraw(Canvas canvas, MapViewBase mapView, boolean shadow);

}
