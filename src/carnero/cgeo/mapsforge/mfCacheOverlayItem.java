package carnero.cgeo.mapsforge;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.OverlayItem;

import android.graphics.drawable.Drawable;

import carnero.cgeo.cgCoord;
import carnero.cgeo.mapinterfaces.CacheOverlayItemBase;

public class mfCacheOverlayItem extends OverlayItem implements CacheOverlayItemBase {
	private String cacheType = null;
	private cgCoord coord;

	public mfCacheOverlayItem(cgCoord coordinate, String type) {
		super(new GeoPoint((int)(coordinate.latitude * 1e6), (int)(coordinate.longitude * 1e6)), coordinate.name, "");

		this.cacheType = type;
		this.coord = coordinate;
	}
	
	public cgCoord getCoord() {
		return coord;
	}

	public String getType() {
		return cacheType;
	}

	@Override
	public Drawable getMarker(int index) {
		return getMarker();
	}

}
