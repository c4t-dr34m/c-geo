package carnero.cgeo.mapinterfaces;

public interface MapFactory {

	public Class getMapClass();

	public int getMapViewId();

	public int getMapLayoutId();

	public GeoPointBase getGeoPointBase(int latE6, int lonE6);
}
