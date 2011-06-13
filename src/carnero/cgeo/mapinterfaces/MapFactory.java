package carnero.cgeo.mapinterfaces;

import android.content.Context;
import carnero.cgeo.cgCoord;
import carnero.cgeo.cgUser;

/**
 * Defines functions of a factory class to get implementation specific objects
 * (GeoPoints, OverlayItems, ...) 
 * @author rsudev
 *
 */
public interface MapFactory {

	public Class getMapClass();

	public int getMapViewId();

	public int getMapLayoutId();

	public GeoPointImpl getGeoPointBase(int latE6, int lonE6);

	public OverlayImpl getOverlayBaseWrapper(OverlayBase ovlIn);

	CacheOverlayItemImpl getCacheOverlayItem(cgCoord coordinate, String type);

	public UserOverlayItemImpl getUserOverlayItemBase(Context context,
			cgUser userOne);

}
