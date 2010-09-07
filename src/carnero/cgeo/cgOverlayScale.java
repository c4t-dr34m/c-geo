package carnero.cgeo;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class cgOverlayScale extends Overlay {
	private cgBase base = null;
	private cgSettings settings = null;
    private Paint scale = null;
    private Paint scaleShadow = null;
	private double pixels = 0d;
	private double distance = 0d;
	private double distanceRound = 0d;
	private String units = null;

    public cgOverlayScale(cgBase baseIn, cgSettings settingsIn) {
		base = baseIn;
		settings = settingsIn;
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);

		final double span = mapView.getLatitudeSpan() / 1e6;

		pixels = mapView.getWidth() / 2; // pixels related to following latitude span
		distance = base.getDistance(0d, 0d, (span / 2), 0d);
		distanceRound = 0d;

		if(settings.units == settings.unitsImperial) {
			distance *= base.kmInMiles;

			if (distance > 100) { // 100+ mi > 1xx mi
				distanceRound = Math.floor(distance / 100) * 100;
				units = "mi";
			} else if (distance > 10) { // 10 - 100 mi > 1x mi
				distanceRound = Math.floor(distance / 10) * 10;
				units = "mi";
			} else if (distance > 1) { // 1 - 10 mi > 1.x mi
				distanceRound = Math.floor(distance);
				units = "mi";
			} else if (distance > 0.1) { // 0.1 mi - 1.0 mi > 1xx ft
				distance *= 5280;
				distanceRound = Math.floor(distance / 100) * 100;
				units = "ft";
			} else { // 1 - 100 ft > 1x ft
				distance *= 5280;
				distanceRound = Math.round(distance / 10) * 10;
				units = "ft";
			}
		} else {
			if (distance > 100) { // 100+ km > 1xx km
				distanceRound = Math.floor(distance / 100) * 100;
				units = "km";
			} else if (distance > 10) { // 10 - 100 km > 1x km
				distanceRound = Math.floor(distance / 10) * 10;
				units = "km";
			} else if (distance > 1) { // 1 - 10 km > 1.x km
				distanceRound = Math.floor(distance);
				units = "km";
			} else if (distance > 0.1) { // 100 m - 1 km > 1xx m
				distance *= 1000;
				distanceRound = Math.floor(distance / 100) * 100;
				units = "m";
			} else { // 1 - 100 m > 1x m
				distance *= 1000;
				distanceRound = Math.round(distance / 10) * 10;
				units = "m";
			}
		}

		Log.d(cgSettings.tag, "Scale: " + String.format("%.0f", distanceRound) + " " + units + " of " + String.format("%.0f", distance) + " " + units);

		pixels = Math.round((pixels / distance) * distanceRound);

		if (scaleShadow == null) {
			scaleShadow = new Paint();
			scaleShadow.setAntiAlias(true);
			scaleShadow.setStrokeWidth(7.0f);
			scaleShadow.setColor(0x66000000);
		}

		if (scale == null) {
			scale = new Paint();
			scale.setAntiAlias(true);
			scale.setStrokeWidth(3.0f);
			scale.setColor(0xFFFFFFFF);
		}

		canvas.drawLine(10, 40, (int)(10 + distanceRound), 40, scaleShadow);
		canvas.drawLine(10, 40, (int)(10 + distanceRound), 40, scale);
    }
}