package carnero.cgeo;

import gnu.android.app.appmanualclient.*;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class cgeowaypointadd extends Activity {

	private cgeoapplication app = null;
	private Resources res = null;
	private cgSettings settings = null;
	private cgBase base = null;
	private cgWarning warning = null;
	private Activity activity = null;
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
	/**
	 * number of waypoints that the corresponding cache has until now
	 */
	private int wpCount = 0;
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

					((EditText) findViewById(R.id.latitude)).setText(base.formatCoordinate(waypoint.latitude, "lat", true));
					((EditText) findViewById(R.id.longitude)).setText(base.formatCoordinate(waypoint.longitude, "lon", true));
					((EditText) findViewById(R.id.name)).setText(Html.fromHtml(waypoint.name.trim()).toString());
					((EditText) findViewById(R.id.note)).setText(Html.fromHtml(waypoint.note.trim()).toString());

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
		res = this.getResources();
		app = (cgeoapplication) this.getApplication();
		settings = new cgSettings(activity, activity.getSharedPreferences(cgSettings.preferences, 0));
		base = new cgBase(app, settings, activity.getSharedPreferences(cgSettings.preferences, 0));
		warning = new cgWarning(activity);

		// set layout
		if (settings.skin == 1) {
			setTheme(R.style.light);
		} else {
			setTheme(R.style.dark);
		}
		setContentView(R.layout.waypoint_new);
		base.setTitle(activity, "waypoint");

		// google analytics
		base.sendAnal(activity, "/waypoint/new");

		if (geo == null) {
			geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);
		}

		// get parameters
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			geocode = extras.getString("geocode");
			wpCount = extras.getInt("count", 0);
			id = extras.getInt("waypoint");
		}

		if ((geocode == null || geocode.length() == 0) && id <= 0) {
			warning.showToast(res.getString(R.string.err_waypoint_cache_unknown));

			finish();
			return;
		}

		if (id <= 0) {
			base.setTitle(activity, res.getString(R.string.waypoint_add_title));
		} else {
			base.setTitle(activity, res.getString(R.string.waypoint_edit_title));
		}

		if (geocode != null) {
			app.setAction(geocode);
		}

		Button buttonCurrent = (Button) findViewById(R.id.current);
		buttonCurrent.setOnClickListener(new currentListener());

		Button addWaypoint = (Button) findViewById(R.id.add_waypoint);
		addWaypoint.setOnClickListener(new coordsListener());

		ArrayList<String> wayPointNames = new ArrayList<String>(cgBase.waypointTypes.values());
        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.name);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, wayPointNames);
        textView.setAdapter(adapter);


		if (id > 0) {
			waitDialog = ProgressDialog.show(this, null, res.getString(R.string.waypoint_loading), true);
			waitDialog.setCancelable(true);

			(new loadWaypoint()).start();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (geo == null) {
			geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);
		}

		if (id > 0) {
			if (waitDialog == null) {
				waitDialog = ProgressDialog.show(this, null, res.getString(R.string.waypoint_loading), true);
				waitDialog.setCancelable(true);

				(new loadWaypoint()).start();
			}
		}
	}

	@Override
	public void onDestroy() {
		if (geo != null) {
			geo = app.removeGeo();
		}

		super.onDestroy();
	}

	@Override
	public void onStop() {
		if (geo != null) {
			geo = app.removeGeo();
		}

		super.onStop();
	}

	@Override
	public void onPause() {
		if (geo != null) {
			geo = app.removeGeo();
		}

		super.onPause();
	}

	private class update extends cgUpdateLoc {

		@Override
		public void updateLoc(cgGeo geo) {
			if (geo == null) {
				return;
			}

			try {
				if (latEdit == null) {
					latEdit = (EditText) findViewById(R.id.latitude);
				}
				if (lonEdit == null) {
					lonEdit = (EditText) findViewById(R.id.longitude);
				}

				latEdit.setHint(base.formatCoordinate(geo.latitudeNow, "lat", false));
				lonEdit.setHint(base.formatCoordinate(geo.longitudeNow, "lon", false));
			} catch (Exception e) {
				Log.w(cgSettings.tag, "Failed to update location.");
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
				warning.showToast(res.getString(R.string.err_point_unknown_position));
				return;
			}

			((EditText) findViewById(R.id.latitude)).setText(base.formatCoordinate(geo.latitudeNow, "lat", true));
			((EditText) findViewById(R.id.longitude)).setText(base.formatCoordinate(geo.longitudeNow, "lon", true));
		}
	}

	private class coordsListener implements View.OnClickListener {

		public void onClick(View arg0) {
			ArrayList<Double> coords = new ArrayList<Double>();
			Double latitude = null;
			Double longitude = null;

			final String bearingText = ((EditText) findViewById(R.id.bearing)).getText().toString();
			final String distanceText = ((EditText) findViewById(R.id.distance)).getText().toString();
			final String latText = ((EditText) findViewById(R.id.latitude)).getText().toString();
			final String lonText = ((EditText) findViewById(R.id.longitude)).getText().toString();

			if ((bearingText == null || bearingText.length() == 0) && (distanceText == null || distanceText.length() == 0)
							&& (latText == null || latText.length() == 0) && (lonText == null || lonText.length() == 0)) {
				warning.helpDialog(res.getString(R.string.err_point_no_position_given_title), res.getString(R.string.err_point_no_position_given));
				return;
			}

			if (latText != null && latText.length() > 0 && lonText != null && lonText.length() > 0) {
				// latitude & longitude
				HashMap<String, Object> latParsed = base.parseCoordinate(latText, "lat");
				HashMap<String, Object> lonParsed = base.parseCoordinate(lonText, "lat");

				if (latParsed == null || latParsed.get("coordinate") == null || latParsed.get("string") == null) {
					warning.showToast(res.getString(R.string.err_parse_lat));
					return;
				}

				if (lonParsed == null || lonParsed.get("coordinate") == null || lonParsed.get("string") == null) {
					warning.showToast(res.getString(R.string.err_parse_lon));
					return;
				}

				latitude = (Double) latParsed.get("coordinate");
				longitude = (Double) lonParsed.get("coordinate");
			} else {
				if (geo == null || geo.latitudeNow == null || geo.longitudeNow == null) {
					warning.showToast(res.getString(R.string.err_point_curr_position_unavailable));
					return;
				}

				latitude = geo.latitudeNow;
				longitude = geo.longitudeNow;
			}

			if (bearingText != null && bearingText.length() > 0 && distanceText != null && distanceText.length() > 0) {
				// bearing & distance
				Double bearing = null;
				try {
					bearing = new Double(bearingText);
				} catch (Exception e) {
					// probably not a number
				}
				if (bearing == null) {
					warning.helpDialog(res.getString(R.string.err_point_bear_and_dist_title), res.getString(R.string.err_point_bear_and_dist));
					return;
				}

				Double distance = null; // km

				final Pattern patternA = Pattern.compile("^([0-9\\.\\,]+)[ ]*m$", Pattern.CASE_INSENSITIVE); // m
				final Pattern patternB = Pattern.compile("^([0-9\\.\\,]+)[ ]*km$", Pattern.CASE_INSENSITIVE); // km
				final Pattern patternC = Pattern.compile("^([0-9\\.\\,]+)[ ]*ft$", Pattern.CASE_INSENSITIVE); // ft - 0.3048m
				final Pattern patternD = Pattern.compile("^([0-9\\.\\,]+)[ ]*yd$", Pattern.CASE_INSENSITIVE); // yd - 0.9144m
				final Pattern patternE = Pattern.compile("^([0-9\\.\\,]+)[ ]*mi$", Pattern.CASE_INSENSITIVE); // mi - 1609.344m

				Matcher matcherA = patternA.matcher(distanceText);
				Matcher matcherB = patternB.matcher(distanceText);
				Matcher matcherC = patternC.matcher(distanceText);
				Matcher matcherD = patternD.matcher(distanceText);
				Matcher matcherE = patternE.matcher(distanceText);

				if (matcherA.find() == true && matcherA.groupCount() > 0) {
					distance = (new Double(matcherA.group(1))) * 0.001;
				} else if (matcherB.find() == true && matcherB.groupCount() > 0) {
					distance = new Double(matcherB.group(1));
				} else if (matcherC.find() == true && matcherC.groupCount() > 0) {
					distance = (new Double(matcherC.group(1))) * 0.0003048;
				} else if (matcherD.find() == true && matcherD.groupCount() > 0) {
					distance = (new Double(matcherD.group(1))) * 0.0009144;
				} else if (matcherE.find() == true && matcherE.groupCount() > 0) {
					distance = (new Double(matcherE.group(1))) * 1.609344;
				} else {
					try {
						if (settings.units == cgSettings.unitsImperial) {
							distance = (new Double(distanceText)) * 1.609344; // considering it miles
						} else {
							distance = (new Double(distanceText)) * 0.001; // considering it meters
						}
					} catch (Exception e) {
						// probably not a number
					}
				}

				if (distance == null) {
					warning.showToast(res.getString(R.string.err_parse_dist));
					return;
				}

				Double latParsed = null;
				Double lonParsed = null;

				HashMap<String, Double> coordsDst = base.getRadialDistance(latitude, longitude, bearing, distance);

				latParsed = coordsDst.get("latitude");
				lonParsed = coordsDst.get("longitude");

				if (latParsed == null || lonParsed == null) {
					warning.showToast(res.getString(R.string.err_point_location_error));
					return;
				}

				coords.add(0, (Double) latParsed);
				coords.add(1, (Double) lonParsed);
			} else if (latitude != null && longitude != null) {
				coords.add(0, latitude);
				coords.add(1, longitude);
			} else {
				warning.showToast(res.getString(R.string.err_point_location_error));
				return;
			}

			String name = ((EditText) findViewById(R.id.name)).getText().toString().trim();
			// if no name is given, just give the waypoint its number as name
			if (name.isEmpty()) {
				name = res.getString(R.string.waypoint) + " " + String.valueOf(wpCount + 1);
			}
			final String note = ((EditText) findViewById(R.id.note)).getText().toString().trim();

			final cgWaypoint waypoint = new cgWaypoint();
			waypoint.type = type;
			waypoint.geocode = geocode;
			waypoint.prefix = prefix;
			waypoint.lookup = lookup;
			waypoint.name = name;
			waypoint.latitude = coords.get(0);
			waypoint.longitude = coords.get(1);
			waypoint.latitudeString = base.formatCoordinate(coords.get(0), "lat", true);
			waypoint.longitudeString = base.formatCoordinate(coords.get(1), "lon", true);
			waypoint.note = note;

			if (app.saveOwnWaypoint(id, geocode, waypoint) == true) {
				app.removeCacheFromCache(geocode);

				finish();
				return;
			} else {
				warning.showToast(res.getString(R.string.err_waypoint_add_failed));
			}
		}
	}

	public void goHome(View view) {
		base.goHome(activity);
	}

	public void goManual(View view) {
		try {
			if (id >= 0) {
				AppManualReaderClient.openManual(
					"c-geo",
					"c:geo-waypoint-edit",
					activity,
					"http://cgeo.carnero.cc/manual/"
				);
			} else {
				AppManualReaderClient.openManual(
					"c-geo",
					"c:geo-waypoint-new",
					activity,
					"http://cgeo.carnero.cc/manual/"
				);
			}
		} catch (Exception e) {
			// nothing
		}
	}
}