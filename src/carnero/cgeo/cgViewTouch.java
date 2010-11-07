package carnero.cgeo;

import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;

public class cgViewTouch implements View.OnTouchListener {
	private cgSettings settings = null;
	private View view = null;
	private Drawable bcg = null;
	private int type = 0;

	public cgViewTouch(cgSettings settingsIn, View viewIn, int typeIn) {
		settings = settingsIn;
		view = viewIn;
		type = typeIn;
		bcg = view.getBackground();
	}

	public boolean onTouch(View v, MotionEvent event) {
		if (view == null) return false;

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (type == 0) {
				view.setBackgroundResource(settings.buttonPressed);
			} else if (type == 1) {
				if (settings.skin == 1) {
					view.setBackgroundResource(R.color.background_light);
				} else {
					view.setBackgroundResource(R.color.background_dark);
				}
			}
		}

		if (event.getAction() == MotionEvent.ACTION_UP ||
			event.getAction() == MotionEvent.ACTION_OUTSIDE ||
			event.getAction() == MotionEvent.ACTION_CANCEL ||
			event.getAction() == MotionEvent.ACTION_MOVE) {
			(new cgViewRelease(settings, view, bcg)).start();
		}

		return false;
	}
}
