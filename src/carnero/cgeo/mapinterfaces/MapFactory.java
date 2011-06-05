package carnero.cgeo.mapinterfaces;

import android.app.Activity;
import carnero.cgeo.cgBase;
import carnero.cgeo.cgSettings;
import carnero.cgeo.mapcommon.cgOverlayScale;

public interface MapFactory {

	public Class getMapClass();

	public int getMapViewId();

	public int getMapLayoutId();

	public GeoPointBase getGeoPointBase(int latE6, int lonE6);

	public OverlayImpl getOverlayScale(Activity activity, cgBase base,
			cgSettings settings);
}
