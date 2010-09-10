package carnero.cgeo;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.view.Surface;

public class cgDirection {
	private cgDirection dir = null;
	private Context context = null;
	private cgWarning warning = null;
	private SensorManager sensorManager = null;
	private cgeoSensorListener sensorListener = null;
	private cgUpdateDir dirUpdate = null;
	private cg8wrap cg8 = null;
	private static boolean is8 = false;
	private boolean userWarned = false;

	public Float directionNow = null;

	static {
		try {
			cg8wrap.check();
			is8 = true;
		} catch (Throwable t) {
			is8 = false;
		}
	}

	public cgDirection(Context contextIn, cgUpdateDir dirUpdateIn, cgWarning warningIn) {
		context = contextIn;
		dirUpdate = dirUpdateIn;
		warning = warningIn;

		if (is8 == true) {
			cg8 = new cg8wrap((Activity)context);
		}
		
		sensorListener = new cgeoSensorListener();

		initDir();
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

	private class cgeoSensorListener implements SensorEventListener {
		@Override
		 public void onAccuracyChanged(Sensor sensor, int accuracy) {
			if (accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW && userWarned == false && warning != null) {
				warning.showToast("Compass in your device needs calibration.\nYou can do it with waving phone around in figures of number eight in the air.");
				userWarned = true;
			}
		 }

		@Override
		public void onSensorChanged(SensorEvent event) {
			Float directionNowPre = event.values[0];

			if (cg8 != null) {
				final int rotation = cg8.getRotation();
				if (rotation == Surface.ROTATION_90) directionNowPre = directionNowPre - 90;
				if (rotation == Surface.ROTATION_180) directionNowPre = directionNowPre - 180;
				if (rotation == Surface.ROTATION_270) directionNowPre = directionNowPre - 270;
			}

			directionNow = directionNow;

			if (directionNow != null) dirUpdate.updateDir(dir);
		}
	}
}
