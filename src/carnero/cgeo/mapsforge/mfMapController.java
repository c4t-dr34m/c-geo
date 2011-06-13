package carnero.cgeo.mapsforge;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.MapController;

import carnero.cgeo.mapinterfaces.GeoPointBase;
import carnero.cgeo.mapinterfaces.MapControllerBase;

public class mfMapController implements MapControllerBase {

	private MapController _mapController;
	
	public mfMapController(MapController mapController) {
		_mapController = mapController;
	}

	@Override
	public void animateTo(GeoPointBase geoPoint) {
		_mapController.setCenter((GeoPoint)geoPoint);
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
		
		if (latSpanE6 != 0 && lonSpanE6 != 0) {
			// calculate zoomlevel
			int distDegree = Math.max(latSpanE6, lonSpanE6);
			int zoomLevel = (int) Math.floor(Math.log(360.0*1e6/distDegree)/Math.log(2));
			_mapController.setZoom(zoomLevel);
			}		
	}	
}

