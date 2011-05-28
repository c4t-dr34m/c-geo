package carnero.cgeo;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class cgOverlayItem extends OverlayItem {
	private cgCoord coordinate;
	private String cacheType = null;

	public cgOverlayItem(cgCoord coordinate, String type) {
		super(new GeoPoint((int)(coordinate.latitude * 1e6), (int)(coordinate.longitude * 1e6)), coordinate.name, "");

		this.coordinate = coordinate;
		this.cacheType = type;
	}

	public cgCoord getCoord() {
		return coordinate;
	}
	
	public String getType() {
		return cacheType;
	}
}
