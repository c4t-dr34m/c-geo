package carnero.cgeo.mapinterfaces;

import android.graphics.Point;

public interface MapProjection {
	
	Object getImpl();

	void toPixels(GeoPointBase leftGeo, Point left);

}
