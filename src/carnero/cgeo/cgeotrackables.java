package carnero.cgeo;

import android.app.Activity;
import android.app.ProgressDialog;
import java.util.ArrayList;
import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

public class cgeotrackables extends Activity {
	private ArrayList<cgTrackable> trackables = new ArrayList<cgTrackable>();
	private String geocode = null;
	private cgeoapplication app = null;
	private cgSettings settings = null;
	private cgBase base = null;
	private cgWarning warning = null;
	private Context activity = null;
	private LayoutInflater inflater = null;
	private LinearLayout addList = null;
	private ProgressDialog waitDialog = null;
	private Handler loadInventoryHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			try {
				if (addList == null) {
					addList = (LinearLayout) findViewById(R.id.trackable_list);
				}

				if (trackables.isEmpty()) {
					if (waitDialog != null) {
						waitDialog.dismiss();
					}

					warning.showToast("Sorry, c:geo failed to load cache inventory.");

					finish();
					return;
				} else {
					LinearLayout oneTbPre = null;
					LinearLayout addList = (LinearLayout) findViewById(R.id.trackable_list);
					for (cgTrackable trackable : trackables) {
						if (settings.skin == 1) {
							oneTbPre = (LinearLayout) inflater.inflate(R.layout.trackable_button_light, null);
						} else {
							oneTbPre = (LinearLayout) inflater.inflate(R.layout.trackable_button_dark, null);
						}

						Button oneTb = (Button) oneTbPre.findViewById(R.id.button);

						if (trackable.name != null) {
							oneTb.setText(Html.fromHtml(trackable.name), TextView.BufferType.SPANNABLE);
						} else {
							oneTb.setText("some trackable");
						}
						oneTb.setClickable(true);
						oneTb.setOnClickListener(new buttonListener(trackable.guid, trackable.geocode, trackable.name));
						addList.addView(oneTbPre);
					}
				}

				if (waitDialog != null) {
					waitDialog.dismiss();
				}
			} catch (Exception e) {
				if (waitDialog != null) {
					waitDialog.dismiss();
				}
				Log.e(cgSettings.tag, "cgeotrackables.loadInventoryHandler: " + e.toString());
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// init
		activity = this;
		app = (cgeoapplication) this.getApplication();
		settings = new cgSettings(this, getSharedPreferences(cgSettings.preferences, 0));
		base = new cgBase(app, settings, getSharedPreferences(cgSettings.preferences, 0));
		warning = new cgWarning(this);

		// set layout
		setTitle("inventory");
		base.sendAnal(activity, "/trackable/detail");
		if (settings.skin == 1) {
			setContentView(R.layout.trackables_light);
		} else {
			setContentView(R.layout.trackables_dark);
		}
		inflater = getLayoutInflater();

		// get parameters
		Bundle extras = getIntent().getExtras();

		// try to get data from extras
		if (extras != null) {
			geocode = extras.getString("geocode");
		}

		if (geocode == null) {
			warning.showToast("Sorry, c:geo forgot for what cache you want to load trackables.");
			finish();
			return;
		}

		waitDialog = ProgressDialog.show(this, null, "loading cache inventory...", true);
		waitDialog.setCancelable(true);

		(new loadInventory()).start();
	}

	private class loadInventory extends Thread {

		@Override
		public void run() {
			try {
				trackables = app.loadInventory(geocode);

				loadInventoryHandler.sendMessage(new Message());
			} catch (Exception e) {
				Log.e(cgSettings.tag, "cgeotrackables.loadInventory.run: " + e.toString());
			}
		}
	}

	private class buttonListener implements View.OnClickListener {

		private String guid = null;
		private String geocode = null;
		private String name = null;

		public buttonListener(String guidIn, String geocodeIn, String nameIn) {
			guid = guidIn;
			geocode = geocodeIn;
			name = nameIn;
		}

		public void onClick(View arg0) {
			Intent trackableIntent = new Intent(activity, cgeotrackable.class);
			trackableIntent.putExtra("guid", guid);
			trackableIntent.putExtra("geocode", geocode);
			trackableIntent.putExtra("name", name);
			activity.startActivity(trackableIntent);

			finish();
			return;
		}
	}
}
