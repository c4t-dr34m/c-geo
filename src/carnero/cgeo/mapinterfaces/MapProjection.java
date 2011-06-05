package carnero.cgeo.mapinterfaces;

import android.graphics.Point;

public interface MapProjection {

	void toPixels(GeoPointBase leftGeo, Point left);

}
