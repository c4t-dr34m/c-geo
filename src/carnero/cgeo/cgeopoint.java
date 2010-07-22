package carnero.cgeo;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class cgeopoint extends Activity {
    private cgeoapplication app = null;
	private cgSettings settings = null;
	private cgBase base = null;
	private cgWarning warning = null;
    private Context activity = null;
	private cgGeo geo = null;
	private cgUpdateLoc geoUpdate = new update();
	private EditText latEdit = null;
	private EditText lonEdit = null;

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
		setTitle("");
		if (settings.skin == 1) setContentView(R.layout.point_light);
		else setContentView(R.layout.point_dark);

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

	private void init() {
        if (geo == null) geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);

		Button buttonCurrent = (Button)findViewById(R.id.current);
		buttonCurrent.setClickable(true);
		buttonCurrent.setOnTouchListener(new cgViewTouch(settings, buttonCurrent));
		buttonCurrent.setOnClickListener(new currentListener());

		Button buttonCompass = (Button)findViewById(R.id.compass);
		buttonCompass.setClickable(true);
		buttonCompass.setOnTouchListener(new cgViewTouch(settings, buttonCompass));
		buttonCompass.setOnClickListener(new compassListener());

		Button buttonRadar = (Button)findViewById(R.id.radar);
		buttonRadar.setClickable(true);
		buttonRadar.setOnTouchListener(new cgViewTouch(settings, buttonRadar));
		buttonRadar.setOnClickListener(new radarListener());

		Button buttonTurn = (Button)findViewById(R.id.turn);
		buttonTurn.setClickable(true);
		buttonTurn.setOnTouchListener(new cgViewTouch(settings, buttonTurn));
		buttonTurn.setOnClickListener(new turnListener());
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
				warning.showToast("c:geo can\'t use Radar because this application isn't installed.");
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
						warning.showToast("c:geo can\'t find any supported navigation.");
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
					warning.showToast("c:geo can\'t find any suitable application.");
				}
			}
		}
	}

	private ArrayList<Double> getDestination() {
		ArrayList<Double> coords = new ArrayList<Double>();

		String bearingText = ((EditText)findViewById(R.id.bearing)).getText().toString();
		String distanceText = ((EditText)findViewById(R.id.distance)).getText().toString();
		String latText = ((EditText)findViewById(R.id.latitude)).getText().toString();
		String lonText = ((EditText)findViewById(R.id.longitude)).getText().toString();

		if (bearingText != null && bearingText.length() > 0 && distanceText != null && distanceText.length() > 0) {
			// bearing & distance
            Double bearing = null;
            try {
                bearing = new Double(bearingText);
            } catch (Exception e) {
                // probably not a number
            }
			if (bearing == null) {
				warning.helpDialog("need some help?", "Fill both bearing and distance. Bearing is angle 0 to 360 degrees relative to north. Distance with or without units.");
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
				warning.showToast("Sorry, c:geo can\'t parse bearing.");
				return null;
			}

			if (distance == null) {
				warning.showToast("Sorry, c:geo can\'t parse distance.");
				return null;
			}

			Double latParsed = null;
			Double lonParsed = null;

			if (geo == null || geo.latitudeNow == null || geo.longitudeNow == null) {
				warning.showToast("c:geo still doesn\'t have current coordinates. Please, wait a while.");
				return null;
			}

			HashMap<String, Double> coordsDst = base.getRadialDistance(geo.latitudeNow, geo.longitudeNow, bearing, distance);

			latParsed = coordsDst.get("latitude");
			lonParsed = coordsDst.get("longitude");

			if (latParsed == null || lonParsed == null) {
				warning.showToast("Sorry, c:geo can\'t get direction to place you want.");
				return null;
			}

			coords.add(0, (Double)latParsed);
			coords.add(1, (Double)lonParsed);
		} else if (latText != null && latText.length() > 0 && lonText != null && lonText.length() > 0) {
			// latitude & longitude
			HashMap latParsed = base.parseCoordinate(latText, "lat");
			HashMap lonParsed = base.parseCoordinate(lonText, "lat");

			if (latParsed == null || latParsed.get("coordinate") == null || latParsed.get("string") == null) {
				warning.showToast("Sorry, c:geo can\'t parse latitude.");
				return null;
			}

			if (lonParsed == null || lonParsed.get("coordinate") == null || lonParsed.get("string") == null) {
				warning.showToast("Sorry, c:geo can\'t parse longitude.");
				return null;
			}

			coords.add(0, (Double)latParsed.get("coordinate"));
			coords.add(1, (Double)lonParsed.get("coordinate"));
		} else {
			return null;
		}

		return coords;
	}
}