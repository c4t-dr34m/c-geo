package carnero.cgeo.mapsforge;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.Projection;

import carnero.cgeo.mapinterfaces.GeoPointBase;
import carnero.cgeo.mapinterfaces.MapProjection;
import android.graphics.Point;

public class mfMapProjection implements MapProjection {

	private Projection _projection;

	public mfMapProjection(Projection projection) {
		_projection = projection;
	}

	@Override
	public void toPixels(GeoPointBase leftGeo, Point left) {
		_projection.toPixels((GeoPoint) leftGeo, left);
	}

	@Override
	public Object getImpl() {
		return _projection;
	}

}
