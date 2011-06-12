package carnero.cgeo.mapinterfaces;

import carnero.cgeo.cgSettings;
import carnero.cgeo.mapcommon.cgMapOverlay;
import carnero.cgeo.mapcommon.cgUsersOverlay;
import android.content.Context;
import android.graphics.drawable.Drawable;

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

	MapProjection getMapProjection();

	Context getContext();

	cgMapOverlay createAddMapOverlay(cgSettings settings, Context context,
			Drawable drawable, boolean fromDetailIntent);

	cgUsersOverlay createAddUsersOverlay(Context context, Drawable markerIn);

}
