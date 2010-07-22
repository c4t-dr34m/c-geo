package carnero.cgeo;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;
import android.view.SubMenu;
import java.util.Locale;

public class cgeo extends Activity {
	private Resources res = null;
    private cgeoapplication app = null;
	private Context context = null;
	private cgSettings settings = null;
	private cgBase base = null;
	private cgWarning warning = null;
	private cgGeo geo = null;
	private cgUpdateLoc geoUpdate = new update();
	private TextView navTypeName = null;
	private TextView navTypeNameDetail = null;
	private TextView navTypeValue = null;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = this;
		res = this.getResources();
        app = (cgeoapplication)this.getApplication();
		app.setAction(null);
        settings = new cgSettings(this, getSharedPreferences(cgSettings.preferences, 0));
        base = new cgBase(app, settings, getSharedPreferences(cgSettings.preferences, 0));
        warning = new cgWarning(this);

		setTitle("c:geo");
		if (settings.skin == 1) setContentView(R.layout.main_light);
		else setContentView(R.layout.main_dark);

        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            
            Log.i(cgSettings.tag, "Starting " + info.packageName + " " + info.versionCode + " a.k.a " + info.versionName + "...");

            info = null;
            manager = null;
        } catch(Exception e) {
            Log.i(cgSettings.tag, "No info.");
        }

		(base.new loginThread()).start();

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, res.getString(R.string.menu_any_destination)).setIcon(android.R.drawable.ic_menu_compass);
		SubMenu subMenu = menu.addSubMenu(0, 1, 0, res.getString(R.string.type) + ": " + res.getString(R.string.all)).setIcon(android.R.drawable.ic_menu_search);

		subMenu.add(0, 2, 0, res.getString(R.string.all));
		int cnt = 3;
		for (String choice : base.cacheTypes.keySet()) {
			subMenu.add(0, cnt, 0, choice);
			cnt ++;
		}

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		try {
			MenuItem item = menu.findItem(1);
			if (settings.cacheType == null) {
				item.setTitle(res.getString(R.string.type) + ": " + res.getString(R.string.all));
			} else {
				item.setTitle(res.getString(R.string.type) + ": " + base.cacheTypesInv.get(settings.cacheType));
			}
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgeo.onPrepareOptionsMenu: " + e.toString());
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == 0) {
			Intent pointIntent = new Intent(context, cgeopoint.class);
			context.startActivity(pointIntent);

			return true;
		} else if (id == 2) {
			settings.setCacheType(null);
			warning.showToast(res.getString(R.string.now_searching) + ": " + res.getString(R.string.all));

			return true;
		} else if (id > 2) {
			String choice = item.getTitle().toString();

			String cachetype = settings.setCacheType(base.cacheTypes.get(choice));
			if (cachetype != null) {
				warning.showToast(res.getString(R.string.now_searching) + ": " + choice);
			} else {
				warning.showToast(res.getString(R.string.now_searching) + ": " + res.getString(R.string.all));
			}
				
			return true;
		}

		return false;
	}

	private void init() {
		settings.getLogin();

		if (settings.cacheType != null && base.cacheTypesInv.containsKey(settings.cacheType) == false) settings.setCacheType(null);

		if (geo == null) geo = app.startGeo(context, geoUpdate, base, settings, warning, 0, 0);

		navTypeName = (TextView)findViewById(R.id.nav_type_name);
		navTypeNameDetail = (TextView)findViewById(R.id.nav_type_name_detail);
		navTypeValue = (TextView)findViewById(R.id.nav_type_value);

		final Button findOnMap = (Button)findViewById(R.id.map);
		findOnMap.setClickable(true);
		findOnMap.setOnTouchListener(new cgViewTouch(settings, findOnMap));
		findOnMap.setOnClickListener(new cgeoFindOnMapListener());

		final Button findByOffline = (Button)findViewById(R.id.search_offline);
		findByOffline.setClickable(true);
		findByOffline.setOnTouchListener(new cgViewTouch(settings, findByOffline));
		findByOffline.setOnClickListener(new cgeoFindByOfflineListener());

		final Button advanced = (Button)findViewById(R.id.advanced_button);
		advanced.setClickable(true);
		advanced.setOnTouchListener(new cgViewTouch(settings, advanced));
		advanced.setOnClickListener(new cgeoSearchListener());

		final Button setup = (Button)findViewById(R.id.settings_button);
		setup.setClickable(true);
		setup.setOnTouchListener(new cgViewTouch(settings, setup));
		setup.setOnClickListener(new cgeoSettingsListener());

		final Button about = (Button)findViewById(R.id.about_button);
		about.setClickable(true);
		about.setOnTouchListener(new cgViewTouch(settings, about));
		about.setOnClickListener(new cgeoAboutListener());
	}

	private class update extends cgUpdateLoc {
		@Override
		public void updateLoc(cgGeo geo) {
			if (geo == null) return;

			try {
				if (navTypeName == null || navTypeValue == null) {
					navTypeName = (TextView)findViewById(R.id.nav_type_name);
					navTypeNameDetail = (TextView)findViewById(R.id.nav_type_name_detail);
					navTypeValue = (TextView)findViewById(R.id.nav_type_value);
				}

				if (geo.latitudeNow != null && geo.longitudeNow != null) {
					Button findNearest = (Button)findViewById(R.id.nearest);
					findNearest.setBackgroundResource(settings.buttonActive);
					findNearest.setClickable(true);
					findNearest.setOnTouchListener(new cgViewTouch(settings, findNearest));
					findNearest.setOnClickListener(new cgeoFindNearestListener());

					String satellites = null;
					if (geo.satellitesVisible != null && geo.satellitesFixed != null && geo.satellitesFixed > 0) {
						satellites = " (" + res.getString(R.string.loc_sat) + ": " + geo.satellitesFixed + "/" + geo.satellitesVisible + ")";
					} else if (geo.satellitesVisible != null) {
						satellites = " (" + res.getString(R.string.loc_sat) + ": --/" + geo.satellitesVisible + ")";
					} else {
						satellites = "";
					}

					if (geo.gps == -1) {
						navTypeName.setText(res.getString(R.string.loc_last) + satellites);
					} else if (geo.gps == 0) {
						navTypeName.setText(res.getString(R.string.loc_net) + satellites);
					} else {
						navTypeName.setText(res.getString(R.string.loc_gps) + satellites);
					}

					if (geo.accuracyNow != null) {
						if (settings.units == settings.unitsImperial) {
							navTypeNameDetail.setText("±" + String.format(Locale.getDefault(), "%.0f", (geo.accuracyNow * 3.2808399)) + " ft");
						} else {
							navTypeNameDetail.setText("±" + String.format(Locale.getDefault(), "%.0f", geo.accuracyNow) + " m");
						}
					} else {
						navTypeNameDetail.setText(null);
					}

					if (geo.altitudeNow != null) {
						String humanAlt;
						if (settings.units == settings.unitsImperial) {
							humanAlt = String.format("%.0f", (geo.altitudeNow * 3.2808399)) + " ft";
						} else {
							humanAlt = String.format("%.0f", geo.altitudeNow) + " m";
						}
						navTypeValue.setText(base.formatCoordinate(geo.latitudeNow, "lat", true) + " | " + base.formatCoordinate(geo.longitudeNow, "lon", true) + " | " + humanAlt);
					} else {
						navTypeValue.setText(base.formatCoordinate(geo.latitudeNow, "lat", true) + " | " + base.formatCoordinate(geo.longitudeNow, "lon", true));
					}
				} else {
					Button findNearest = (Button)findViewById(R.id.nearest);
					findNearest.setBackgroundResource(settings.buttonInactive);
					findNearest.setClickable(false);
					findNearest.setOnTouchListener(null);
					findNearest.setOnClickListener(null);

					navTypeName.setText(null);
					navTypeNameDetail.setText(null);
					navTypeValue.setText(res.getString(R.string.loc_trying));
				}
			} catch (Exception e) {
				Log.w(cgSettings.tag, "Failed to update location.");
			}
		}
	}

	private class cgeoFindNearestListener implements View.OnClickListener {
		public void onClick(View arg0) {
			if (geo == null) return;

			final Intent cachesIntent = new Intent(context, cgeocaches.class);
			cachesIntent.putExtra("type", "nearest");
			cachesIntent.putExtra("latitude", geo.latitudeNow);
			cachesIntent.putExtra("longitude", geo.longitudeNow);
			cachesIntent.putExtra("cachetype", settings.cacheType);
			context.startActivity(cachesIntent);
		}
	}

	private class cgeoFindOnMapListener implements View.OnClickListener {
		public void onClick(View arg0) {
			context.startActivity(new Intent(context, cgeomap.class));
		}
	}

	private class cgeoFindByOfflineListener implements View.OnClickListener {
		public void onClick(View arg0) {
			final Intent cachesIntent = new Intent(context, cgeocaches.class);
			cachesIntent.putExtra("type", "offline");
			context.startActivity(cachesIntent);
		}
	}

	private class cgeoSearchListener implements View.OnClickListener {
		public void onClick(View arg0) {
			context.startActivity(new Intent(context, cgeoadvsearch.class));
		}
	}

	private class cgeoSettingsListener implements View.OnClickListener {
		public void onClick(View arg0) {
			context.startActivity(new Intent(context, cgeoinit.class));
		}
	}

	private class cgeoAboutListener implements View.OnClickListener {
		public void onClick(View arg0) {
			context.startActivity(new Intent(context, cgeoabout.class));
		}
	}
}
