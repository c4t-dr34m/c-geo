package carnero.cgeo.mapsforge;

import org.mapsforge.android.maps.GeoPoint;

import carnero.cgeo.mapinterfaces.GeoPointImpl;

public class mfGeoPoint extends GeoPoint implements GeoPointImpl {

	public mfGeoPoint(int latitudeE6, int longitudeE6) {
		super(latitudeE6, longitudeE6);
	}
}
