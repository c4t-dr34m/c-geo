package carnero.cgeo.googlemaps;

import android.graphics.Canvas;
import carnero.cgeo.mapinterfaces.MapViewBase;
import carnero.cgeo.mapinterfaces.OverlayBase;
import carnero.cgeo.mapinterfaces.OverlayImpl;

import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class googleOverlayBase extends Overlay implements OverlayImpl {

	private OverlayBase _overlayImpl;
	
	public googleOverlayBase(OverlayBase overlayImpl) {
		_overlayImpl = overlayImpl;
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
		
		_overlayImpl.draw(canvas, (MapViewBase) mapView, shadow);
	}

}
