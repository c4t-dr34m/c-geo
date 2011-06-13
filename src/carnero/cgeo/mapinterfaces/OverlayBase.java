package carnero.cgeo.mapinterfaces;

import android.graphics.Canvas;
import android.graphics.Point;

public interface OverlayBase {

	void draw(Canvas canvas, MapViewBase mapView, boolean shadow);

	void drawOverlayBitmap(Canvas canvas, Point drawPosition,
			MapProjection projection, byte drawZoomLevel);

}
