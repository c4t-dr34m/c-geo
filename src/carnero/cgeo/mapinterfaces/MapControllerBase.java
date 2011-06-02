package carnero.cgeo.mapinterfaces;

public interface MapControllerBase {

	void setZoom(int mapzoom);

	void setCenter(GeoPointBase geoPoint);

	void animateTo(GeoPointBase geoPoint);

	void zoomToSpan(int latSpanE6, int lonSpanE6);

}
