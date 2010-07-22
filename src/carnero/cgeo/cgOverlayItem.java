package carnero.cgeo;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class cgOverlayItem extends OverlayItem {
	private cgCoord coordinate;

	public cgOverlayItem(cgCoord coordinate) {
		super(new GeoPoint((int)(coordinate.latitude * 1e6), (int)(coordinate.longitude * 1e6)), coordinate.name, "");

		this.coordinate = coordinate;
	}

	public cgCoord getCoord() {
		return this.coordinate;
	}
}
