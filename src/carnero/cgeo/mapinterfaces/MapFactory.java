package carnero.cgeo.mapinterfaces;

import android.content.Context;
import carnero.cgeo.cgCoord;
import carnero.cgeo.cgUser;

public interface MapFactory {

	public Class getMapClass();

	public int getMapViewId();

	public int getMapLayoutId();

	public GeoPointBase getGeoPointBase(int latE6, int lonE6);

	public OverlayImpl getOverlayBaseWrapper(OverlayBase ovlIn);

	CacheOverlayItemBase getCacheOverlayItem(cgCoord coordinate, String type);

	public UserOverlayItemBase getUserOverlayItemBase(Context context,
			cgUser userOne);

}
