package carnero.cgeo;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

public class cgViewRelease extends Thread {
	private cgSettings settings = null;
	private View view = null;
	private Drawable bcg = null;

	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			if (view != null) {
				if (bcg == null) {
					if (settings.skin == 1) view.setBackgroundResource(R.drawable.action_button_light);
					else view.setBackgroundResource(R.drawable.action_button_dark);
				} else {
					view.setBackgroundDrawable(bcg);
				}
			}
		}
	};

	public cgViewRelease(cgSettings settingsIn, View viewIn, Drawable bcgIn) {
		settings = settingsIn;
		view = viewIn;
		bcg = bcgIn;
	}

	@Override
	public void run() {
		try {
			sleep(100);

			this.handler.sendMessage(new Message());
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgViewPush.run: " + e.toString());
		}
	}
}
