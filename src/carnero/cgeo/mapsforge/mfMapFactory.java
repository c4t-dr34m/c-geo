package carnero.cgeo.mapsforge;

import android.content.Context;
import carnero.cgeo.R;
import carnero.cgeo.cgCoord;
import carnero.cgeo.cgUser;
import carnero.cgeo.mapinterfaces.CacheOverlayItemBase;
import carnero.cgeo.mapinterfaces.GeoPointBase;
import carnero.cgeo.mapinterfaces.MapFactory;
import carnero.cgeo.mapinterfaces.OverlayBase;
import carnero.cgeo.mapinterfaces.OverlayImpl;
import carnero.cgeo.mapinterfaces.UserOverlayItemBase;

public class mfMapFactory implements MapFactory{

	@Override
	public Class getMapClass() {
		return mfMapActivity.class;
	}

	@Override
	public int getMapViewId() {
		return R.id.mfmap;
	}

	@Override
	public int getMapLayoutId() {
		return R.layout.mfmap;
	}

	@Override
	public GeoPointBase getGeoPointBase(int latE6, int lonE6) {
		return new mfGeoPoint(latE6, lonE6);
	}

	@Override
	public OverlayImpl getOverlayBaseWrapper(OverlayBase ovlIn) {
		mfOverlayBase baseOvl = new mfOverlayBase(ovlIn);
		return baseOvl;
	}
	
	@Override
	public CacheOverlayItemBase getCacheOverlayItem(cgCoord coordinate, String type) {
		mfCacheOverlayItem baseItem = new mfCacheOverlayItem(coordinate, type);
		return baseItem;
	}

	@Override
	public UserOverlayItemBase getUserOverlayItemBase(Context context, cgUser userOne) {
		mfUsersOverlayItem baseItem = new mfUsersOverlayItem(context, userOne);
		return baseItem;
	}

}
