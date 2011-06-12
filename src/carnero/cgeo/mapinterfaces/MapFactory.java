package carnero.cgeo.mapinterfaces;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import carnero.cgeo.cgBase;
import carnero.cgeo.cgCoord;
import carnero.cgeo.cgSettings;
import carnero.cgeo.mapcommon.cgMapOverlay;
import carnero.cgeo.mapcommon.cgOverlayScale;

public interface MapFactory {

	public Class getMapClass();

	public int getMapViewId();

	public int getMapLayoutId();

	public GeoPointBase getGeoPointBase(int latE6, int lonE6);

	public OverlayImpl getOverlayBaseWrapper(OverlayBase ovlIn);

	OverlayItemBase getCacheOverlayItem(cgCoord coordinate, String type);

}
