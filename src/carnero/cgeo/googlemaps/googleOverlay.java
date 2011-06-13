package carnero.cgeo.googlemaps;

import android.graphics.Canvas;
import carnero.cgeo.mapinterfaces.MapViewImpl;
import carnero.cgeo.mapinterfaces.OverlayBase;
import carnero.cgeo.mapinterfaces.OverlayImpl;

import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class googleOverlay extends Overlay implements OverlayImpl {

	private OverlayBase overlayBase;
	
	public googleOverlay(OverlayBase overlayBaseIn) {
		overlayBase = overlayBaseIn;
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
		
		overlayBase.draw(canvas, (MapViewImpl) mapView, shadow);
	}

}
