package carnero.cgeo;

import gnu.android.app.appmanualclient.*;

import java.util.ArrayList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.app.Activity;
import android.view.Menu;
import android.view.SubMenu;
import android.view.MenuItem;
import android.widget.TextView;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.view.View;
import android.view.WindowManager;
import java.util.HashMap;
import java.util.Locale;

import carnero.cgeo.mapcommon.cgeomap;

public class cgeonavigate extends Activity {
	public static ArrayList<cgCoord> coordinates = new ArrayList<cgCoord>();

	private Resources res = null;
	private cgeoapplication app = null;
	private Activity activity = null;
	private cgSettings settings = null;
	private cgBase base = null;
	private cgWarning warning = null;
	private PowerManager pm = null;
	private cgGeo geo = null;
	private cgDirection dir = null;
	private cgUpdateLoc geoUpdate = new update();
	private cgUpdateDir dirUpdate = new updateDir();
	private Double dstLatitude = null;
	private Double dstLongitude = null;
	private Double cacheHeading = new Double(0);
	private Double northHeading = new Double(0);
	private String title = null;
	private String name = null;
	private TextView navType = null;
	private TextView navAccuracy = null;
	private TextView navSatellites = null;
	private TextView navLocation = null;
	private TextView distanceView = null;
	private TextView headingView = null;
	private cgCompass compassView = null;
	private updaterThread updater = null;

	private Handler updaterHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				if (compassView != null) compassView.updateNorth(northHeading, cacheHeading);
			} catch (Exception e) {
				Log.e(cgSettings.tag, "cgeonavigate.updaterHandler: " + e.toString());
			}
		}
	};

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// class init
		activity = this;
		res = this.getResources();
		app = (cgeoapplication)this.getApplication();
		settings = new cgSettings(this, getSharedPreferences(cgSettings.preferences, 0));
		base = new cgBase(app, settings, getSharedPreferences(cgSettings.preferences, 0));
		warning = new cgWarning(this);

		// set layout
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		if (settings.skin == 1) {
			setTheme(R.style.light);
		} else {
			setTheme(R.style.dark);
		}
		setContentView(R.layout.navigate);
		base.setTitle(activity, res.getString(R.string.compass_title));

		// google analytics
		base.sendAnal(activity, "/navigate");

		// sensor & geolocation manager
		if (geo == null) geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);
		if (settings.useCompass == 1 && dir == null) dir = app.startDir(activity, dirUpdate, warning);

		// get parameters
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			title = extras.getString("geocode");
			name = extras.getString("name");
			dstLatitude = extras.getDouble("latitude");
			dstLongitude = extras.getDouble("longitude");

			if (name != null && name.length() > 0) {
                if (title != null && title.length() > 0) title = title + ": " + name;
                else title = name;
			}
		} else {
			Intent pointIntent = new Intent(activity, cgeopoint.class);
			activity.startActivity(pointIntent);

			finish();
			return;
		}

		if (title != null && title.length() > 0) app.setAction(title);
		else if (name != null && name.length() > 0) app.setAction(name);

		// set header
		setTitle();
		setDestCoords();

		// get textviews once
		compassView = (cgCompass)findViewById(R.id.rose);

		// start updater thread
		updater = new updaterThread(updaterHandler);
		updater.start();

		if (geo != null) geoUpdate.updateLoc(geo);
		if (dir != null) dirUpdate.updateDir(dir);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (title != null && title.length() > 0) app.setAction(title);
		else if (name != null && name.length() > 0) app.setAction(name);

		// sensor & geolocation manager
		if (geo == null) geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);
		if (settings.useCompass == 1 && dir == null) dir = app.startDir(activity, dirUpdate, warning);

		// keep backlight on
        if (pm == null) {
            pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        }

        // updater thread
		if (updater == null) {
			updater = new updaterThread(updaterHandler);
			updater.start();
		}
	}

	@Override
	public void onStop() {
		if (geo != null) geo = app.removeGeo();
		if (dir != null) dir = app.removeDir();

        super.onStop();
	}

	@Override
	public void onPause() {
		if (geo != null) geo = app.removeGeo();
		if (dir != null) dir = app.removeDir();

		super.onPause();
	}

	@Override
	public void onDestroy() {
		if (geo != null) geo = app.removeGeo();
		if (dir != null) dir = app.removeDir();

		compassView.destroyDrawingCache();
		compassView = null;

		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (settings.useCompass == 1) {
			menu.add(0, 1, 0, res.getString(R.string.use_gps)).setIcon(android.R.drawable.ic_menu_compass);
		} else {
			menu.add(0, 1, 0, res.getString(R.string.use_compass)).setIcon(android.R.drawable.ic_menu_compass);
		}
		menu.add(0, 0, 0, res.getString(R.string.caches_on_map)).setIcon(android.R.drawable.ic_menu_mapmode);
		menu.add(0, 2, 0, res.getString(R.string.destination_set)).setIcon(android.R.drawable.ic_menu_edit);
		if (coordinates != null && coordinates.size() > 1) {
			SubMenu subMenu = menu.addSubMenu(0, 3, 0, res.getString(R.string.destination_select)).setIcon(android.R.drawable.ic_menu_myplaces);

			int cnt = 4;
			for (cgCoord coordinate : coordinates) {
				subMenu.add(0, cnt, 0, coordinate.name + " (" + coordinate.type + ")");
				cnt ++;
			}

			return true;
		} else {
			menu.add(0, 3, 0, res.getString(R.string.destination_select)).setIcon(android.R.drawable.ic_menu_myplaces).setEnabled(false);

			return true;
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		MenuItem item;
		item = menu.findItem(1);
		if (settings.useCompass == 1) {
			item.setTitle(res.getString(R.string.use_gps));
		} else {
			item.setTitle(res.getString(R.string.use_compass));
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == 0) {
			Intent mapIntent = new Intent(activity, settings.getMapFactory().getMapClass());
			mapIntent.putExtra("detail", false);
			mapIntent.putExtra("latitude", dstLatitude);
			mapIntent.putExtra("longitude", dstLongitude);

			activity.startActivity(mapIntent);
		} else if (id == 1) {
			if (settings.useCompass == 1) {
				settings.useCompass = 0;

				if (dir != null) dir = app.removeDir();

				SharedPreferences.Editor prefsEdit = getSharedPreferences(cgSettings.preferences, 0).edit();
				prefsEdit.putInt("usecompass", settings.useCompass);
				prefsEdit.commit();
			} else {
				settings.useCompass = 1;

				if (dir == null) dir = app.startDir(activity, dirUpdate, warning);

				SharedPreferences.Editor prefsEdit = getSharedPreferences(cgSettings.preferences, 0).edit();
				prefsEdit.putInt("usecompass", settings.useCompass);
				prefsEdit.commit();
			}
		} else if (id == 2) {
			Intent pointIntent = new Intent(activity, cgeopoint.class);
			activity.startActivity(pointIntent);

			finish();
			return true;
		} else if (id > 3 && coordinates != null && coordinates.get(id - 4) != null) {
			cgCoord coordinate = coordinates.get(id - 4);

			title = coordinate.name;
			dstLatitude = coordinate.latitude;
			dstLongitude = coordinate.longitude;
			setTitle();
			setDestCoords();
			updateDistanceInfo();

			Log.d(cgSettings.tag, "destination set: " + title + " (" + String.format(Locale.getDefault(), "%.8f", dstLatitude) + " | " + String.format(Locale.getDefault(), "%.8f", dstLatitude) + ")");
			return true;
		}

		return false;
	}

	private void setTitle() {
		if (title != null && title.length() > 0) {
			base.setTitle(activity, title);
		} else {
			base.setTitle(activity, res.getString(R.string.navigation));
		}
	}

	private void setDestCoords() {
		if (dstLatitude == null || dstLatitude == null) {
			return;
		}

		((TextView)findViewById(R.id.destination)).setText(base.formatCoordinate(dstLatitude, "lat", true) + " | " + base.formatCoordinate(dstLongitude, "lon", true));
	}

	public void setDest(Double lat, Double lon) {
		if (lat == null || lon == null) {
			return;
		}

		title = "some place";
		setTitle();
		setDestCoords();

		dstLatitude = lat;
		dstLongitude = lon;
		updateDistanceInfo();
	}

	public HashMap<String, Double> getCoordinatesNow() {
		HashMap<String, Double> coordsNow = new HashMap<String, Double>();
		if (geo != null) {
			coordsNow.put("latitude", geo.latitudeNow);
			coordsNow.put("longitude", geo.longitudeNow);
		}
		return coordsNow;
	}

	private void updateDistanceInfo() {
		if (geo == null || geo.latitudeNow == null || geo.longitudeNow == null || dstLatitude == null || dstLongitude == null) return;

		if (distanceView == null) distanceView = (TextView)findViewById(R.id.distance);
		if (headingView == null) headingView = (TextView)findViewById(R.id.heading);

		cacheHeading = cgBase.getHeading(geo.latitudeNow, geo.longitudeNow, dstLatitude, dstLongitude);
		distanceView.setText(base.getHumanDistance(cgBase.getDistance(geo.latitudeNow, geo.longitudeNow, dstLatitude, dstLongitude)));
		headingView.setText(String.format(Locale.getDefault(), "%.0f", cacheHeading) + "°");
	}

	private class update extends cgUpdateLoc {
		@Override
		public void updateLoc(cgGeo geo) {
			if (geo == null) return;

			try {
				if (navType == null || navLocation == null || navAccuracy == null) {
					navType = (TextView)findViewById(R.id.nav_type);
					navAccuracy = (TextView)findViewById(R.id.nav_accuracy);
					navSatellites = (TextView)findViewById(R.id.nav_satellites);
					navLocation = (TextView)findViewById(R.id.nav_location);
				}

				if (geo.latitudeNow != null && geo.longitudeNow != null) {
					String satellites = null;
					if (geo.satellitesVisible != null && geo.satellitesFixed != null && geo.satellitesFixed > 0) {
						satellites = res.getString(R.string.loc_sat) + ": " + geo.satellitesFixed + "/" + geo.satellitesVisible;
					} else if (geo.satellitesVisible != null) {
						satellites = res.getString(R.string.loc_sat) + ": 0/" + geo.satellitesVisible;
					} else {
						satellites = "";
					}
					navSatellites.setText(satellites);

					if (geo.gps == -1) {
						navType.setText(res.getString(R.string.loc_last));
					} else if (geo.gps == 0) {
						navType.setText(res.getString(R.string.loc_net));
					} else {
						navType.setText(res.getString(R.string.loc_gps));
					}

					if (geo.accuracyNow != null) {
						if (settings.units == cgSettings.unitsImperial) {
							navAccuracy.setText("±" + String.format(Locale.getDefault(), "%.0f", (geo.accuracyNow * 3.2808399)) + " ft");
						} else {
							navAccuracy.setText("±" + String.format(Locale.getDefault(), "%.0f", geo.accuracyNow) + " m");
						}
					} else {
						navAccuracy.setText(null);
					}

					if (geo.altitudeNow != null) {
						String humanAlt;
						if (settings.units == cgSettings.unitsImperial) {
							humanAlt = String.format("%.0f", (geo.altitudeNow * 3.2808399)) + " ft";
						} else {
							humanAlt = String.format("%.0f", geo.altitudeNow) + " m";
						}
						navLocation.setText(base.formatCoordinate(geo.latitudeNow, "lat", true) + " | " + base.formatCoordinate(geo.longitudeNow, "lon", true) + " | " + humanAlt);
					} else {
						navLocation.setText(base.formatCoordinate(geo.latitudeNow, "lat", true) + " | " + base.formatCoordinate(geo.longitudeNow, "lon", true));
					}

					updateDistanceInfo();
				} else {
					navType.setText(null);
					navAccuracy.setText(null);
					navLocation.setText(res.getString(R.string.loc_trying));
				}

				if (settings.useCompass == 0 || (geo.speedNow != null && geo.speedNow > 5)) { // use GPS when speed is higher than 18 km/h
					if (geo != null && geo.bearingNow != null) {
						northHeading = geo.bearingNow;
					} else {
						northHeading = new Double(0);
					}
				}
			} catch (Exception e) {
				Log.w(cgSettings.tag, "Failed to update location.");
			}
		}
	}

	private class updateDir extends cgUpdateDir {
		@Override
		public void updateDir(cgDirection dir) {
			if (dir == null || dir.directionNow == null) return;

			if (geo == null || geo.speedNow == null || geo.speedNow <= 5) { // use compass when speed is lower than 18 km/h
				northHeading = dir.directionNow;
			}
		}
	}

	private class updaterThread extends Thread {
		private Handler handler = null;

		public updaterThread(Handler handlerIn) {
			handler = handlerIn;
		}

		@Override
		public void run() {
			while(!Thread.currentThread().isInterrupted()){
				if (handler != null) handler.sendMessage(new Message());

				try {
					Thread.sleep(20);
				} catch (Exception e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	public void goHome(View view) {
		base.goHome(activity);
	}

	public void goManual(View view) {
		try {
			AppManualReaderClient.openManual(
				"c-geo",
				"c:geo-compass",
				activity,
				"http://cgeo.carnero.cc/manual/"
			);
		} catch (Exception e) {
			// nothing
		}
	}
}