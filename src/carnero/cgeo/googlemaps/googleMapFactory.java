package carnero.cgeo.googlemaps;

import carnero.cgeo.R;
import carnero.cgeo.mapinterfaces.GeoPointBase;
import carnero.cgeo.mapinterfaces.MapFactory;

public class googleMapFactory implements MapFactory{

	@Override
	public Class getMapClass() {
		return googleMapActivity.class;
	}

	@Override
	public int getMapViewId() {
		return R.id.map;
	}

	@Override
	public int getMapLayoutId() {
		return R.layout.googlemap;
	}

	@Override
	public GeoPointBase getGeoPointBase(int latE6, int lonE6) {
		return new googleGeoPoint(latE6, lonE6);
	}

}
