package carnero.cgeo.mapsforge;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.Projection;

import carnero.cgeo.mapinterfaces.GeoPointImpl;
import carnero.cgeo.mapinterfaces.MapProjectionImpl;
import android.graphics.Point;

public class mfMapProjection implements MapProjectionImpl {

	private Projection projection;

	public mfMapProjection(Projection projectionIn) {
		projection = projectionIn;
	}

	@Override
	public void toPixels(GeoPointImpl leftGeo, Point left) {
		projection.toPixels((GeoPoint) leftGeo, left);
	}

	@Override
	public Object getImpl() {
		return projection;
	}

}
