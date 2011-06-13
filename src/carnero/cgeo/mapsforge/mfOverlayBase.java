package carnero.cgeo.mapsforge;

import org.mapsforge.android.maps.Overlay;
import org.mapsforge.android.maps.Projection;

import android.graphics.Canvas;
import android.graphics.Point;
import carnero.cgeo.mapinterfaces.OverlayBase;
import carnero.cgeo.mapinterfaces.OverlayImpl;

public class mfOverlayBase extends Overlay implements OverlayImpl {

	private OverlayBase _overlayImpl;
	
	public mfOverlayBase(OverlayBase overlayImpl) {
		_overlayImpl = overlayImpl;
	}
	
	@Override
	protected void drawOverlayBitmap(Canvas canvas, Point drawPosition,
			Projection projection, byte drawZoomLevel) {
		
		_overlayImpl.drawOverlayBitmap(canvas, drawPosition, new mfMapProjection(projection), drawZoomLevel);
	}

}
