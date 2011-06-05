package carnero.cgeo.googlemaps;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.Projection;

import android.graphics.Point;
import carnero.cgeo.mapinterfaces.GeoPointBase;
import carnero.cgeo.mapinterfaces.MapProjection;

public class googleMapProjection implements MapProjection {
	
	private Projection _projection;

	public googleMapProjection(Projection projection) {
		_projection = projection;
	}

	@Override
	public void toPixels(GeoPointBase leftGeo, Point left) {
		_projection.toPixels((GeoPoint) leftGeo, left);
	}

}
