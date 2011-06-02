package carnero.cgeo.mapinterfaces;

import java.util.List;

public interface MapViewBase {

	void invalidate();

	void setSatellite(boolean b);

	void setBuiltInZoomControls(boolean b);

	void displayZoomControls(boolean b);

	void preLoad();

	void clearOverlays();
	
	void addOverlay(OverlayBase ovl);

	MapControllerBase getMapController();

	void destroyDrawingCache();

	boolean isSatellite();

	GeoPointBase getMapViewCenter();

	int getLatitudeSpan();

	int getLongitudeSpan();

	int getZoomLevel();

}
