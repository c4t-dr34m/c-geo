package carnero.cgeo.googlemaps;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;

import carnero.cgeo.mapinterfaces.GeoPointBase;
import carnero.cgeo.mapinterfaces.MapControllerBase;

public class googleMapController implements MapControllerBase {

	private MapController _mapController;
	
	public googleMapController(MapController mapController) {
		_mapController = mapController;
	}

	@Override
	public void animateTo(GeoPointBase geoPoint) {
		_mapController.animateTo((GeoPoint)geoPoint);
	}

	@Override
	public void setCenter(GeoPointBase geoPoint) {
		_mapController.setCenter((GeoPoint)geoPoint);
	}

	@Override
	public void setZoom(int mapzoom) {
		_mapController.setZoom(mapzoom);
	}

	@Override
	public void zoomToSpan(int latSpanE6, int lonSpanE6) {
		_mapController.zoomToSpan(latSpanE6, lonSpanE6);
	}
	
}
