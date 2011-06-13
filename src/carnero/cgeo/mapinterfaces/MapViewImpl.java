package carnero.cgeo.mapinterfaces;

import carnero.cgeo.cgSettings;
import carnero.cgeo.mapcommon.cgMapOverlay;
import carnero.cgeo.mapcommon.cgUsersOverlay;
import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * Defines common functions of the provider-specific
 * MapView implementations
 * @author rsudev
 *
 */
public interface MapViewImpl {

	void invalidate();

	void setSatellite(boolean b);

	void setBuiltInZoomControls(boolean b);

	void displayZoomControls(boolean b);

	void preLoad();

	void clearOverlays();
	
	void addOverlay(OverlayImpl ovl);

	MapControllerImpl getMapController();

	void destroyDrawingCache();

	boolean isSatellite();

	GeoPointImpl getMapViewCenter();

	int getLatitudeSpan();

	int getLongitudeSpan();

	int getMapZoomLevel();

	int getWidth();

	int getHeight();

	MapProjectionImpl getMapProjection();

	Context getContext();

	cgMapOverlay createAddMapOverlay(cgSettings settings, Context context,
			Drawable drawable, boolean fromDetailIntent);

	cgUsersOverlay createAddUsersOverlay(Context context, Drawable markerIn);

	void setMapFile(String mapFile);

	boolean needsScaleOverlay();

	void setBuiltinScale(boolean b);

}
