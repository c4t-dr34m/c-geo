package carnero.cgeo;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

public class cgOOverlayItem extends OverlayItem {
	private cgCoord coordinate;

	public cgOOverlayItem(cgCoord coordinate) {
		super(coordinate.name, "", new GeoPoint((int)(coordinate.latitude * 1e6), (int)(coordinate.longitude * 1e6)));

		this.coordinate = coordinate;
	}

	public cgCoord getCoord() {
		return this.coordinate;
	}
}
