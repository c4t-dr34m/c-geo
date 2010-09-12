package carnero.cgeo;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.os.Build;
import android.view.Display;
import android.view.Surface;

public class cgDirection {
	private cgDirection dir = null;
	private cgeoapplication app = null;
	private Context context = null;
	private cgWarning warning = null;
	private SensorManager sensorManager = null;
	private cgeoSensorListener sensorListener = null;
	private cgUpdateDir dirUpdate = null;
	private cg8wrap cg8 = null;
	private boolean userWarned = false;

	public Float directionNow = null;

	public cgDirection(cgeoapplication appIn, Context contextIn, cgUpdateDir dirUpdateIn, cgWarning warningIn) {
		app = appIn;
		context = contextIn;
		dirUpdate = dirUpdateIn;
		warning = warningIn;

		try {
			final int sdk = new Integer(Build.VERSION.SDK).intValue();
			if (sdk >= 8) cg8 = new cg8wrap((Activity)context);
		} catch (Exception e) {
			// nothing
		}
		
		sensorListener = new cgeoSensorListener();
	}

	public void initDir() {
		dir = this;

		if (sensorManager == null) {
			sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
		}
		sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void closeDir() {
		if (sensorManager != null && sensorListener != null) {
			sensorManager.unregisterListener(sensorListener);
		}
	}

	public void replaceUpdate(cgUpdateDir dirUpdateIn) {
		dirUpdate = dirUpdateIn;

		if (dirUpdate != null && directionNow != null) dirUpdate.updateDir(dir);
	}

	private class cgeoSensorListener implements SensorEventListener {
		@Override
		 public void onAccuracyChanged(Sensor sensor, int accuracy) {
			if (accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW && app.warnedCompassCalibration == false && warning != null) {
				warning.showToast("Compass in your device needs calibration.\nYou can do it with waving phone around in figures of number eight in the air.");
				app.warnedCompassCalibration = true;
			}
		 }

		@Override
		public void onSensorChanged(SensorEvent event) {
			Float directionNowPre = event.values[0];

			if (cg8 != null) {
				final int rotation = cg8.getRotation();
				if (rotation == Surface.ROTATION_90) directionNowPre = directionNowPre + 90;
				else if (rotation == Surface.ROTATION_180) directionNowPre = directionNowPre + 180;
				else if (rotation == Surface.ROTATION_270) directionNowPre = directionNowPre + 270;
			} else {
				final Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
				final int rotation = display.getOrientation();
				if (rotation == Configuration.ORIENTATION_LANDSCAPE) directionNowPre = directionNowPre + 90;
			}

			directionNow = directionNowPre;

			if (dirUpdate != null && directionNow != null) dirUpdate.updateDir(dir);
		}
	}
}
