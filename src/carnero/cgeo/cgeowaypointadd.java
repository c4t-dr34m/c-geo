package carnero.cgeo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.util.HashMap;

public class cgeowaypointadd extends Activity {
    private cgeoapplication app = null;
	private cgSettings settings = null;
	private cgBase base = null;
	private cgWarning warning = null;
    private Context activity = null;
    private String geocode = null;
    private int id = -1;
	private cgGeo geo = null;
	private cgUpdateLoc geoUpdate = new update();
	private EditText latEdit = null;
	private EditText lonEdit = null;
	private ProgressDialog waitDialog = null;
	private cgWaypoint waypoint = null;
	private String type = "own";
	private String prefix = "OWN";
	private String lookup = "---";
	
	private Handler loadWaypointHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				if (waypoint == null) {
					if (waitDialog != null) {
						waitDialog.dismiss();
						waitDialog = null;
					}

					id = -1;
				} else {
					geocode = waypoint.geocode;
					type = waypoint.type;
					prefix = waypoint.prefix;
					lookup = waypoint.lookup;
					
					app.setAction(geocode);

					((EditText)findViewById(R.id.latitude)).setText(base.formatCoordinate(waypoint.latitude, "lat", true));
					((EditText)findViewById(R.id.longitude)).setText(base.formatCoordinate(waypoint.longitude, "lon", true));
					((EditText)findViewById(R.id.name)).setText(Html.fromHtml(waypoint.name.trim()).toString());
					((EditText)findViewById(R.id.note)).setText(Html.fromHtml(waypoint.note.trim()).toString());

					if (waitDialog != null) {
						waitDialog.dismiss();
						waitDialog = null;
					}
				}
			} catch (Exception e) {
				if (waitDialog != null) {
					waitDialog.dismiss();
					waitDialog = null;
				}
				Log.e(cgSettings.tag, "cgeowaypointadd.loadWaypointHandler: " + e.toString());
			}
		}
	};

   @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		// init
		activity = this;
        app = (cgeoapplication)this.getApplication();
		settings = new cgSettings(activity, activity.getSharedPreferences(cgSettings.preferences, 0));
		base = new cgBase(app, settings, activity.getSharedPreferences(cgSettings.preferences, 0));
		warning = new cgWarning(activity);

		// set layout
		setTitle("waypoint");
		if (settings.skin == 1) setContentView(R.layout.waypointadd_light);
		else  setContentView(R.layout.waypointadd_dark);

        if (geo == null) geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);

		// get parameters
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			geocode = extras.getString("geocode");
			id = extras.getInt("waypoint");
		}

        if ((geocode == null || geocode.length() == 0) && id <= 0) {
            warning.showToast("Sorry, c:geo doesn\'t know to which cache you want to add waypoint.");

            finish();
            return;
        }

		if (id <= 0) setTitle("add waypoint");
		else setTitle("edit waypoint");

		if (geocode != null) app.setAction(geocode);

		Button buttonCurrent = (Button)findViewById(R.id.current);
		buttonCurrent.setClickable(true);
		buttonCurrent.setOnTouchListener(new cgViewTouch(settings, buttonCurrent, 0));
		buttonCurrent.setOnClickListener(new currentListener());

		Button addWaypoint = (Button)findViewById(R.id.add_waypoint);
		addWaypoint.setClickable(true);
		addWaypoint.setOnTouchListener(new cgViewTouch(settings, addWaypoint, 0));
		addWaypoint.setOnClickListener(new coordsListener());

		if (id > 0) {
			waitDialog = ProgressDialog.show(this, null, "loading waypoint...", true);
			waitDialog.setCancelable(true);

			(new loadWaypoint()).start();
	   }
	}

	@Override
	public void onResume() {
		super.onResume();

        if (geo == null) geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);

		if (id > 0) {
			if (waitDialog == null) {
				waitDialog = ProgressDialog.show(this, null, "loading waypoint...", true);
				waitDialog.setCancelable(true);
				
				(new loadWaypoint()).start();
			}
		}
	}

	@Override
	public void onDestroy() {
		if (geo != null) geo = app.removeGeo(geo);

		super.onDestroy();
	}

	@Override
	public void onStop() {
		if (geo != null) geo = app.removeGeo(geo);

		super.onStop();
	}

	@Override
	public void onPause() {
		if (geo != null) geo = app.removeGeo(geo);

		super.onPause();
	}

	private class update extends cgUpdateLoc {
		@Override
		public void updateLoc(cgGeo geo) {
			if (geo == null) return;

			try {
				if (latEdit == null) latEdit = (EditText)findViewById(R.id.latitude);
				if (lonEdit == null) lonEdit = (EditText)findViewById(R.id.longitude);

				latEdit.setHint(base.formatCoordinate(geo.latitudeNow, "lat", false));
				lonEdit.setHint(base.formatCoordinate(geo.longitudeNow, "lon", false));
			} catch (Exception e) {
				Log.w(cgSettings.tag, "Failed to update location.");
				e.printStackTrace();
			}
		}
	}

	private class loadWaypoint extends Thread {
	   @Override
	   public void run() {
			try {
				waypoint = app.loadWaypoint(id);

				loadWaypointHandler.sendMessage(new Message());
			} catch (Exception e) {
				Log.e(cgSettings.tag, "cgeowaypoint.loadWaypoint.run: " + e.toString());
			}
	   }
	}

	private class currentListener implements View.OnClickListener {
		public void onClick(View arg0) {
			if (geo == null || geo.latitudeNow == null || geo.longitudeNow == null) {
				warning.showToast("Sorry, c:geo can\'t recognize where you are.");
				return;
			}

			((EditText)findViewById(R.id.latitude)).setText(base.formatCoordinate(geo.latitudeNow, "lat", true));
			((EditText)findViewById(R.id.longitude)).setText(base.formatCoordinate(geo.longitudeNow, "lon", true));
		}
	}
	
	private class coordsListener implements View.OnClickListener {
		public void onClick(View arg0) {
			// find caches by coordinates
            final String name = ((EditText)findViewById(R.id.name)).getText().toString();
			final String latText = ((EditText)findViewById(R.id.latitude)).getText().toString();
			final String lonText = ((EditText)findViewById(R.id.longitude)).getText().toString();
            final String note = ((EditText)findViewById(R.id.note)).getText().toString();

			if (latText == null || latText.length() == 0 || lonText == null || lonText.length() == 0) {
				warning.helpDialog("need some help?", "Fill at least latitude and longitude. Use for example following format: \"N 50 03.480\" and \"E 14 23.324\".");
				return;
			}

			final HashMap latParsed = base.parseCoordinate(latText, "lat");
			final HashMap lonParsed = base.parseCoordinate(lonText, "lon");

			if (latParsed == null || latParsed.get("coordinate") == null || latParsed.get("string") == null) {
				warning.showToast("Sorry, c:geo can\'t parse latitude.");
				return;
			}

			if (lonParsed == null || lonParsed.get("coordinate") == null || lonParsed.get("string") == null) {
				warning.showToast("Sorry, c:geo can\'t parse longitude.");
				return;
			}

            final cgWaypoint waypoint = new cgWaypoint();
            waypoint.type = type;
			waypoint.geocode = geocode;
            waypoint.prefix = prefix;
            waypoint.lookup = lookup;
            waypoint.name = name;
            waypoint.latitude = (Double)latParsed.get("coordinate");
            waypoint.longitude = (Double)lonParsed.get("coordinate");
            waypoint.latitudeString = (String)latParsed.get("string");
            waypoint.longitudeString = (String)lonParsed.get("string");
			waypoint.note = note;

            if (app.saveOwnWaypoint(id, geocode, waypoint) == true) {
                finish();
                return;
            } else {
                warning.showToast("Sorry, c:geo failed to add your waypoint.");
            }
		}
	}
}