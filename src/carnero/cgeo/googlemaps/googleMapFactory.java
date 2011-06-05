package carnero.cgeo.googlemaps;

import android.app.Activity;
import carnero.cgeo.R;
import carnero.cgeo.cgBase;
import carnero.cgeo.cgSettings;
import carnero.cgeo.mapcommon.cgOverlayScale;
import carnero.cgeo.mapinterfaces.GeoPointBase;
import carnero.cgeo.mapinterfaces.MapFactory;
import carnero.cgeo.mapinterfaces.OverlayImpl;

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

	@Override
	public OverlayImpl getOverlayScale(Activity activity, cgBase base,
			cgSettings settings) {
		cgOverlayScale scaleOvl = new cgOverlayScale(activity, base, settings);
		googleOverlayBase baseOvl = new googleOverlayBase(scaleOvl);
		return baseOvl;
	}

}
