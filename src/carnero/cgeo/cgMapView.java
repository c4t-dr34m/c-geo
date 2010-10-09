package carnero.cgeo;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import com.google.android.maps.MapView;

public class cgMapView extends MapView {
	public cgMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public cgMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public cgMapView(Context context, String apiKey) {
		super(context, apiKey);
	}

	@Override
	public void draw(Canvas canvas) {
        try {
			if (getZoomLevel() >= 21) { // to avoid too close zoom level (mostly on Samsung Galaxy S series)
				getController().setZoom(20);
			}

			super.draw(canvas);
        } catch (Exception e) {
            Log.e(cgSettings.tag, "cgMapView.draw: " + e.toString());
        }
    }

	@Override
	public void displayZoomControls(boolean takeFocus) {
        try {
            super.displayZoomControls(takeFocus);
        } catch (Exception e) {
            Log.e(cgSettings.tag, "cgMapView.displayZoomControls: " + e.toString());
        }
	}
}
