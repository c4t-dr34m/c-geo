package carnero.cgeo.mapinterfaces;

public interface MapViewBase {

	void invalidate();

	void setSatellite(boolean b);

	void setBuiltInZoomControls(boolean b);

	void displayZoomControls(boolean b);

	void preLoad();

	void clearOverlays();
	
	void addOverlay(OverlayImpl ovl);

	MapControllerBase getMapController();

	void destroyDrawingCache();

	boolean isSatellite();

	GeoPointBase getMapViewCenter();

	int getLatitudeSpan();

	int getLongitudeSpan();

	int getZoomLevel();

	int getWidth();

	int getHeight();

}
