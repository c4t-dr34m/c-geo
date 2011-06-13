package carnero.cgeo.mapsforge;

import org.mapsforge.android.maps.Overlay;
import org.mapsforge.android.maps.Projection;

import android.graphics.Canvas;
import android.graphics.Point;
import carnero.cgeo.mapinterfaces.OverlayBase;
import carnero.cgeo.mapinterfaces.OverlayImpl;

public class mfOverlay extends Overlay implements OverlayImpl {

	private OverlayBase overlayBase;
	
	public mfOverlay(OverlayBase overlayBaseIn) {
		overlayBase = overlayBaseIn;
	}
	
	@Override
	protected void drawOverlayBitmap(Canvas canvas, Point drawPosition,
			Projection projection, byte drawZoomLevel) {
		
		overlayBase.drawOverlayBitmap(canvas, drawPosition, new mfMapProjection(projection), drawZoomLevel);
	}

}
