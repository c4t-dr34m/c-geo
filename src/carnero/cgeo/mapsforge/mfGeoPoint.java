package carnero.cgeo.mapsforge;

import org.mapsforge.android.maps.GeoPoint;

import carnero.cgeo.mapinterfaces.GeoPointBase;

public class mfGeoPoint extends GeoPoint implements GeoPointBase {

	public mfGeoPoint(int latitudeE6, int longitudeE6) {
		super(latitudeE6, longitudeE6);
	}
}
