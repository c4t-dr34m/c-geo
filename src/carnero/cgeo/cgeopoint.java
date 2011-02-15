package carnero.cgeo;

import gnu.android.app.appmanualclient.*;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class cgeopoint extends Activity {
	private Resources res = null;
	private cgeoapplication app = null;
	private cgSettings settings = null;
	private SharedPreferences prefs = null;
	private cgBase base = null;
	private cgWarning warning = null;
	private Activity activity = null;
	private GoogleAnalyticsTracker tracker = null;
	private cgGeo geo = null;
	private cgUpdateLoc geoUpdate = new update();
	private EditText latEdit = null;
	private EditText lonEdit = null;
	private boolean changed = false;

   @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		// init
		activity = this;
		app = (cgeoapplication)this.getApplication();
		res = this.getResources();
		settings = new cgSettings(activity, activity.getSharedPreferences(cgSettings.preferences, 0));
		prefs = getSharedPreferences(cgSettings.preferences, 0);
		base = new cgBase(app, settings, activity.getSharedPreferences(cgSettings.preferences, 0));
		warning = new cgWarning(activity);

		// set layout
		if (settings.skin == 1) {
			setTheme(R.style.light);
		} else {
			setTheme(R.style.dark);
		}
		setContentView(R.layout.point);
		base.setTitle(activity, res.getString(R.string.search_destination));

		// google analytics
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start(cgSettings.analytics, this);
		tracker.dispatch();
		base.sendAnal(activity, tracker, "/point");

		init();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		init();
	}

	@Override
	public void onResume() {
		super.onResume();

        init();
	}

	@Override
	public void onDestroy() {
		if (geo != null) geo = app.removeGeo();
		if (tracker != null) tracker.stop();

		super.onDestroy();
	}

	@Override
	public void onStop() {
		if (geo != null) geo = app.removeGeo();

		super.onStop();
	}

	@Override
	public void onPause() {
		if (geo != null) geo = app.removeGeo();

		super.onPause();
	}

	private void init() {
        if (geo == null) geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);

		EditText latitudeEdit = (EditText)findViewById(R.id.latitude);
		latitudeEdit.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int i, KeyEvent k) {
				changed = true;

				return false;
			}
		});

		EditText longitudeEdit = (EditText)findViewById(R.id.longitude);
		longitudeEdit.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int i, KeyEvent k) {
				changed = true;

				return false;
			}
		});

		if (prefs.contains("anylatitude") == true && prefs.contains("anylongitude") == true) {
			latitudeEdit.setText(base.formatCoordinate(new Double(prefs.getFloat("anylatitude", 0f)), "lat", true));
			longitudeEdit.setText(base.formatCoordinate(new Double(prefs.getFloat("anylongitude", 0f)), "lon", true));
		}

		Button buttonCurrent = (Button)findViewById(R.id.current);
		buttonCurrent.setOnClickListener(new currentListener());

		Button buttonCompass = (Button)findViewById(R.id.compass);
		buttonCompass.setOnClickListener(new compassListener());

		Button buttonRadar = (Button)findViewById(R.id.radar);
		buttonRadar.setOnClickListener(new radarListener());

		Button buttonTurn = (Button)findViewById(R.id.turn);
		buttonTurn.setOnClickListener(new turnListener());

		Button buttonMap = (Button)findViewById(R.id.map);
		buttonMap.setOnClickListener(new mapListener());

		Button buttonMapExt = (Button)findViewById(R.id.map_ext);
		buttonMapExt.setOnClickListener(new mapExtListener());
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
			}
		}
	}

	private class currentListener implements View.OnClickListener {
		public void onClick(View arg0) {
			if (geo == null || geo.latitudeNow == null || geo.longitudeNow == null) {
				warning.showToast(res.getString(R.string.err_point_unknown_position));
				return;
			}

			((EditText)findViewById(R.id.latitude)).setText(base.formatCoordinate(geo.latitudeNow, "lat", true));
			((EditText)findViewById(R.id.longitude)).setText(base.formatCoordinate(geo.longitudeNow, "lon", true));

			changed = false;
		}
	}

	private class compassListener implements View.OnClickListener {
		public void onClick(View arg0) {
			ArrayList<Double> coords = getDestination();

			if (coords == null || coords.size() < 2) return;

			Intent navigateIntent = new Intent(activity, (new cgeonavigate()).getClass());
			navigateIntent.putExtra("latitude", coords.get(0));
			navigateIntent.putExtra("longitude", coords.get(1));
			navigateIntent.putExtra("geocode", "");
			navigateIntent.putExtra("name", "some destination");

			activity.startActivity(navigateIntent);

			finish();
			return;
		}
	}

	private class radarListener implements View.OnClickListener {
		public void onClick(View arg0) {
			ArrayList<Double> coords = getDestination();

			if (coords == null || coords.size() < 2) return;

			try {
				Intent radarIntent = new Intent("com.google.android.radar.SHOW_RADAR");
				radarIntent.putExtra("latitude", new Float(coords.get(0)));
				radarIntent.putExtra("longitude", new Float(coords.get(1)));
				activity.startActivity(radarIntent);

				finish();
				return;
			} catch (Exception e) {
				warning.showToast(res.getString(R.string.err_radar_generic));
				Log.w(cgSettings.tag, "Radar not installed");
			}
		}
	}

	private class turnListener implements View.OnClickListener {
		public void onClick(View arg0) {
			ArrayList<Double> coords = getDestination();

			if (coords == null || coords.size() < 2) return;

			if (settings.useGNavigation == 1) {
				try {
					// turn-by-turn navigation
					activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q="+ coords.get(0) + "," + coords.get(1))));
				} catch (Exception e) {
					try {
						// google maps directions
						if (geo != null && geo.latitudeNow != null && geo.longitudeNow != null) {
							activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?f=d&saddr="+ geo.latitudeNow + "," + geo.longitudeNow + "&daddr="+ coords.get(0) + "," + coords.get(1))));
						} else {
							activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?f=d&daddr="+ coords.get(0) + "," + coords.get(1))));
						}

						finish();
						return;
					} catch (Exception e2) {
						Log.d(cgSettings.tag, "cgeodetail.turnTo: No navigation application available.");
						warning.showToast(res.getString(R.string.err_navigation_not_found));
					}
				}
			} else if (settings.useGNavigation == 0) {
				try {
					// google maps directions
					if (geo != null && geo.latitudeNow != null && geo.longitudeNow != null) {
						activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?f=d&saddr="+ geo.latitudeNow + "," + geo.longitudeNow + "&daddr="+ coords.get(0) + "," + coords.get(1))));
					} else {
						activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?f=d&daddr="+ coords.get(0) + "," + coords.get(1))));
					}

					finish();
					return;
				} catch (Exception e) {
					Log.d(cgSettings.tag, "cgeopoint.turnTo: No navigation application available.");
					warning.showToast(res.getString(R.string.err_navigation_not_found));
				}
			}
		}
	}

	private class mapListener implements View.OnClickListener {
		public void onClick(View arg0) {
			ArrayList<Double> coords = getDestination();

			if (coords == null || coords.size() < 2) return;

			try {
				cgeomap mapActivity = new cgeomap();

				Intent mapIntent = new Intent(activity, mapActivity.getClass());
				mapIntent.putExtra("detail", false);
				mapIntent.putExtra("latitude", coords.get(0));
				mapIntent.putExtra("longitude", coords.get(1));

				activity.startActivity(mapIntent);

				finish();
				return;
			} catch (Exception e) {
				// nothing
			}
		}
	}

	private class mapExtListener implements View.OnClickListener {
		public void onClick(View arg0) {
			ArrayList<Double> coords = getDestination();

			if (coords == null || coords.size() < 2) return;

			try {
				base.runExternalMap(activity, res, warning, tracker, coords.get(0), coords.get(1));

				finish();
				return;
			} catch (Exception e) {
				// nothing
			}
		}
	}

	private ArrayList<Double> getDestination() {
		ArrayList<Double> coords = new ArrayList<Double>();
		Double latitude = null;
		Double longitude = null;

		String bearingText = ((EditText)findViewById(R.id.bearing)).getText().toString();
		String distanceText = ((EditText)findViewById(R.id.distance)).getText().toString();
		String latText = ((EditText)findViewById(R.id.latitude)).getText().toString();
		String lonText = ((EditText)findViewById(R.id.longitude)).getText().toString();

		if (
				(bearingText == null || bearingText.length() == 0) && (distanceText == null || distanceText.length() == 0) &&
				(latText == null || latText.length() == 0) && (lonText == null || lonText.length() == 0)
			) {
			warning.helpDialog(res.getString(R.string.err_point_no_position_given_title), res.getString(R.string.err_point_no_position_given));
			return null;
		}

		if (latText != null && latText.length() > 0 && lonText != null && lonText.length() > 0) {
			// latitude & longitude
			HashMap latParsed = base.parseCoordinate(latText, "lat");
			HashMap lonParsed = base.parseCoordinate(lonText, "lat");

			if (latParsed == null || latParsed.get("coordinate") == null || latParsed.get("string") == null) {
				warning.showToast(res.getString(R.string.err_parse_lat));
				return null;
			}

			if (lonParsed == null || lonParsed.get("coordinate") == null || lonParsed.get("string") == null) {
				warning.showToast(res.getString(R.string.err_parse_lon));
				return null;
			}

			latitude = (Double)latParsed.get("coordinate");
			longitude = (Double)lonParsed.get("coordinate");
		} else {
			if (geo == null || geo.latitudeNow == null || geo.longitudeNow == null) {
				warning.showToast(res.getString(R.string.err_point_curr_position_unavailable));
				return null;
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
				return null;
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
					if (settings.units == settings.unitsImperial) {
						distance = (new Double(distanceText)) * 1.609344; // considering it miles
					} else {
						distance = (new Double(distanceText)) * 0.001; // considering it meters
					}
				} catch (Exception e) {
					// probably not a number
				}
			}

			if (bearing == null) {
				warning.showToast(res.getString(R.string.err_parse_bear));
				return null;
			}

			if (distance == null) {
				warning.showToast(res.getString(R.string.err_parse_dist));
				return null;
			}

			Double latParsed = null;
			Double lonParsed = null;

			HashMap<String, Double> coordsDst = base.getRadialDistance(latitude, longitude, bearing, distance);

			latParsed = coordsDst.get("latitude");
			lonParsed = coordsDst.get("longitude");

			if (latParsed == null || lonParsed == null) {
				warning.showToast(res.getString(R.string.err_point_location_error));
				return null;
			}

			coords.add(0, (Double)latParsed);
			coords.add(1, (Double)lonParsed);
		} else if (latitude != null && longitude != null) {
			coords.add(0, latitude);
			coords.add(1, longitude);
		} else {
			return null;
		}

		saveCoords(coords.get(0), coords.get(1));

		return coords;
	}

	private void saveCoords(Double latitude, Double longitude) {
		if (changed == true && latitude == null || longitude == null) {
			SharedPreferences.Editor edit = prefs.edit();

			edit.putFloat("anylatitude", new Float(latitude));
			edit.putFloat("anylongitude", new Float(longitude));

			edit.commit();
		} else {
			SharedPreferences.Editor edit = prefs.edit();

			edit.remove("anylatitude");
			edit.remove("anylongitude");

			edit.commit();
		}
	}

	public void goHome(View view) {
		base.goHome(activity);
	}

	public void goManual(View view) {
		try {
			AppManualReaderClient.openManual(
				"c-geo",
				"c:geo-navigate-any",
				activity,
				"http://cgeo.carnero.cc/manual/"
			);
		} catch (Exception e) {
			// nothing
		}
	}
}