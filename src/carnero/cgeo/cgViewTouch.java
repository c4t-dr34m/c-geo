package carnero.cgeo;

import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;

public class cgViewTouch implements View.OnTouchListener {
	private cgSettings settings = null;
	private View view = null;
	private Drawable bcg = null;

	public cgViewTouch(cgSettings settingsIn, View viewIn) {
		settings = settingsIn;
		view = viewIn;
		bcg = view.getBackground();
	}

	public boolean onTouch(View v, MotionEvent event) {
		if (view == null) return false;

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			view.setBackgroundResource(settings.buttonPressed);
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
