package carnero.cgeo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.util.Log;

public class cgDirection {
	private cgDirection dir = null;
	private Context context = null;
	private cgWarning warning = null;
	private SensorManager sensorManager = null;
	private cgeoSensorListener sensorListener = null;
	private cgUpdateDir dirUpdate = null;
	private boolean userWarned = false;

	public Float directionNow = null;

	public cgDirection(Context contextIn, cgUpdateDir dirUpdateIn, cgWarning warningIn) {
		context = contextIn;
		dirUpdate = dirUpdateIn;
		warning = warningIn;
		
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
			directionNow = event.values[0];

			if (directionNow != null) dirUpdate.updateDir(dir);
		}
	}
}
