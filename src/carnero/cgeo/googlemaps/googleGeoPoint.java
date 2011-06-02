package carnero.cgeo.googlemaps;

import carnero.cgeo.mapinterfaces.GeoPointBase;

import com.google.android.maps.GeoPoint;

public class googleGeoPoint extends GeoPoint implements GeoPointBase {

	public googleGeoPoint(int latitudeE6, int longitudeE6) {
		super(latitudeE6, longitudeE6);
	}

}
