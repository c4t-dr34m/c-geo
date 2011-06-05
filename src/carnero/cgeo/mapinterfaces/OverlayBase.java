package carnero.cgeo.mapinterfaces;

import android.graphics.Canvas;

public interface OverlayBase {

	void draw(Canvas canvas, MapViewBase mapView, boolean shadow);

}
