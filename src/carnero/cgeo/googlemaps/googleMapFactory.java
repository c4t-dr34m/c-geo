package carnero.cgeo.googlemaps;

import android.content.Context;
import carnero.cgeo.R;
import carnero.cgeo.cgCoord;
import carnero.cgeo.cgUser;
import carnero.cgeo.mapinterfaces.CacheOverlayItemBase;
import carnero.cgeo.mapinterfaces.GeoPointBase;
import carnero.cgeo.mapinterfaces.MapFactory;
import carnero.cgeo.mapinterfaces.OverlayImpl;
import carnero.cgeo.mapinterfaces.OverlayBase;
import carnero.cgeo.mapinterfaces.UserOverlayItemBase;

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
	public OverlayImpl getOverlayBaseWrapper(OverlayBase ovlIn) {
		googleOverlayBase baseOvl = new googleOverlayBase(ovlIn);
		return baseOvl;
	}
	
	@Override
	public CacheOverlayItemBase getCacheOverlayItem(cgCoord coordinate, String type) {
		googleCacheOverlayItem baseItem = new googleCacheOverlayItem(coordinate, type);
		return baseItem;
	}

	@Override
	public UserOverlayItemBase getUserOverlayItemBase(Context context, cgUser userOne) {
		googleUsersOverlayItem baseItem = new googleUsersOverlayItem(context, userOne);
		return baseItem;
	}

}
