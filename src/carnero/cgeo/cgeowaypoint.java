package carnero.cgeo;

import gnu.android.app.appmanualclient.*;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import android.content.Intent;
import android.content.res.Resources;
import android.widget.Button;
import android.widget.LinearLayout;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class cgeowaypoint extends Activity {
	private GoogleAnalyticsTracker tracker = null;
	private cgWaypoint waypoint = null;
	private String geocode = null;
	private int id = -1;
	private cgeoapplication app = null;
	private Resources res = null;
	private Activity activity = null;
	private cgSettings settings = null;
	private cgBase base = null;
	private cgWarning warning = null;
	private ProgressDialog waitDialog = null;
	private cgGeo geo = null;
	private cgUpdateLoc geoUpdate = new update();

	private Handler loadWaypointHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				if (waypoint == null) {
					if (waitDialog != null) {
						waitDialog.dismiss();
						waitDialog = null;
					}

					warning.showToast(res.getString(R.string.err_waypoint_load_failed));

					finish();
					return;
				} else {
					final TextView identification = (TextView)findViewById(R.id.identification);
					final TextView coords = (TextView)findViewById(R.id.coordinates);
					final TextView note = (TextView)findViewById(R.id.note);
					final LinearLayout navigationPart = (LinearLayout)findViewById(R.id.navigation_part);

					if (waypoint.name != null && waypoint.name.length() > 0) {
						base.setTitle(activity, Html.fromHtml(waypoint.name.trim()).toString());
					} else {
						base.setTitle(activity, res.getString(R.string.waypoint_title));
					}

					if (waypoint.prefix.equalsIgnoreCase("OWN") == false) {
						identification.setText(waypoint.prefix.trim() + "/" + waypoint.lookup.trim());
					}
					else {
						identification.setText(res.getString(R.string.waypoint_custom));
					}

					if (waypoint.latitude != null && waypoint.longitude != null) {
						coords.setText(Html.fromHtml(base.formatCoordinate(waypoint.latitude, "lat", true) + " | " + base.formatCoordinate(waypoint.longitude, "lon", true)), TextView.BufferType.SPANNABLE);
					}
					else {
						coords.setText(res.getString(R.string.waypoint_unknown_coordinates));
					}

					if (waypoint.note != null && waypoint.note.length() > 0) {
						note.setText(Html.fromHtml(waypoint.note.trim()), TextView.BufferType.SPANNABLE);
					}

					if (waypoint.latitude != null && waypoint.longitude != null) {
						Button buttonCompass = (Button)findViewById(R.id.compass);
						buttonCompass.setOnClickListener(new navigateToListener(waypoint.latitude, waypoint.longitude, waypoint.name, ""));

						Button buttonRadar = (Button)findViewById(R.id.radar);
						if (base.isIntentAvailable(activity, "com.google.android.radar.SHOW_RADAR") == true) {
							buttonRadar.setEnabled(true);
							buttonRadar.setOnClickListener(new radarToListener(waypoint.latitude, waypoint.longitude));
						} else {
							buttonRadar.setEnabled(false);
						}

						Button buttonMap = (Button)findViewById(R.id.map);
						buttonMap.setOnClickListener(new mapToListener(waypoint.latitude, waypoint.longitude));

						Button buttonMapExt = (Button)findViewById(R.id.map_ext);
						buttonMapExt.setOnClickListener(new mapExtToListener(waypoint));

						Button buttonTurn = (Button)findViewById(R.id.turn);
						buttonTurn.setOnClickListener(new turnToListener(waypoint.latitude, waypoint.longitude));

						navigationPart.setVisibility(View.VISIBLE);
					}

					Button buttonEdit = (Button)findViewById(R.id.edit);
					buttonEdit.setOnClickListener(new editWaypointListener(waypoint.id));

					Button buttonDelete = (Button)findViewById(R.id.delete);
					if (waypoint.type != null && waypoint.type.equalsIgnoreCase("own") == true) {
						buttonDelete.setOnClickListener(new deleteWaypointListener(waypoint.id));
						buttonDelete.setVisibility(View.VISIBLE);
					}

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
				Log.e(cgSettings.tag, "cgeowaypoint.loadWaypointHandler: " + e.toString());
			}
		}
	};

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// init
		activity = this;
		res = this.getResources();
		app = (cgeoapplication)this.getApplication();
		settings = new cgSettings(this, getSharedPreferences(cgSettings.preferences, 0));
		base = new cgBase(app, settings, getSharedPreferences(cgSettings.preferences, 0));
		warning = new cgWarning(this);

		// set layout
		if (settings.skin == 1) {
			setTheme(R.style.light);
		} else {
			setTheme(R.style.dark);
		}
		setContentView(R.layout.waypoint);
		base.setTitle(activity, "waypoint");

		// google analytics
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start(cgSettings.analytics, this);
		tracker.dispatch();
		base.sendAnal(activity, tracker, "/waypoint/detail");

		// get parameters
		Bundle extras = getIntent().getExtras();

		// try to get data from extras
		if (extras != null) {
			id = extras.getInt("waypoint");
			geocode = extras.getString("geocode");
		}

		if (id <= 0) {
			warning.showToast(res.getString(R.string.err_waypoint_unknown));
			finish();
			return;
		}

		if (geo == null) geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);

		waitDialog = ProgressDialog.show(this, null, res.getString(R.string.waypoint_loading), true);
		waitDialog.setCancelable(true);

		(new loadWaypoint()).start();
	}

	@Override
	public void onResume() {
		super.onResume();

        if (geo == null) geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);

		if (waitDialog == null) {
			waitDialog = ProgressDialog.show(this, null, res.getString(R.string.waypoint_loading), true);
			waitDialog.setCancelable(true);

			(new loadWaypoint()).start();
		}
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

	private class update extends cgUpdateLoc {
		@Override
		public void updateLoc(cgGeo geo) {
			// nothing
		}
	}

	private class mapToListener implements View.OnClickListener {
		private Double latitude = null;
		private Double longitude = null;

		public mapToListener(Double latitudeIn, Double longitudeIn) {
			latitude = latitudeIn;
			longitude = longitudeIn;
		}

		public void onClick(View arg0) {
			Intent mapIntent = new Intent(activity, cgeomap.class);
			mapIntent.putExtra("latitude", latitude);
			mapIntent.putExtra("longitude", longitude);
			activity.startActivity(mapIntent);
		}
	}

	private class mapExtToListener implements View.OnClickListener {
		private cgWaypoint waypoint = null;

		public mapExtToListener(cgWaypoint waypointIn) {
			waypoint = waypointIn;
		}

		public void onClick(View arg0) {
			base.runExternalMap(activity, res, warning, tracker, waypoint);
		}
	}

	private class navigateToListener implements View.OnClickListener {
		private Double latitude = null;
		private Double longitude = null;
		private String geocode = null;
		private String name = null;

		public navigateToListener(Double latitudeIn, Double longitudeIn, String nameIn, String geocodeIn) {
			latitude = latitudeIn;
			longitude = longitudeIn;
			geocode = geocodeIn;
			name = nameIn;
		}

		public void onClick(View arg0) {
			final cgeonavigate navigateActivity = new cgeonavigate();

			Intent navigateIntent = new Intent(activity, navigateActivity.getClass());
			navigateIntent.putExtra("latitude", latitude);
			navigateIntent.putExtra("longitude", longitude);
			navigateIntent.putExtra("geocode", geocode.toUpperCase());
			navigateIntent.putExtra("name", name);

			activity.startActivity(navigateIntent);
		}
	}

	private class radarToListener implements View.OnClickListener {
		private Double latitude = null;
		private Double longitude = null;

		public radarToListener(Double latitudeIn, Double longitudeIn) {
			latitude = latitudeIn;
			longitude = longitudeIn;
		}

		public void onClick(View arg0) {
			try {
				Intent radarIntent = new Intent("com.google.android.radar.SHOW_RADAR");
				radarIntent.putExtra("latitude", new Float(latitude));
				radarIntent.putExtra("longitude", new Float(longitude));
				activity.startActivity(radarIntent);
			} catch (Exception e) {
				warning.showToast(res.getString(R.string.err_radar_generic));
				Log.w(cgSettings.tag, "Radar not installed");
			}
		}
	}

	private class turnToListener implements View.OnClickListener {
		private Double latitude = null;
		private Double longitude = null;

		public turnToListener(Double latitudeIn, Double longitudeIn) {
			latitude = latitudeIn;
			longitude = longitudeIn;
		}

		public void onClick(View arg0) {
			if (geo != null) {
				base.runNavigation(activity, res, settings, warning, tracker, latitude, longitude, geo.latitudeNow, geo.longitudeNow);
			} else {
				base.runNavigation(activity, res, settings, warning, tracker, latitude, longitude);
			}
		}
	}

	private class editWaypointListener implements View.OnClickListener {
		private int id = -1;

		public editWaypointListener(int idIn) {
			id = idIn;
		}

		public void onClick(View arg0) {
			Intent editIntent = new Intent(activity, cgeowaypointadd.class);
			editIntent.putExtra("waypoint", id);
			activity.startActivity(editIntent);
		}
	}

	private class deleteWaypointListener implements View.OnClickListener {
		private Integer id = null;

		public deleteWaypointListener(int idIn) {
			id = idIn;
		}

		public void onClick(View arg0) {
            if (app.deleteWaypoint(id) == false) {
                warning.showToast(res.getString(R.string.err_waypoint_delete_failed));
            } else {
				app.removeCacheFromCache(geocode);

                finish();
				return;
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
				"c:geo-waypoint-details",
				activity,
				"http://cgeo.carnero.cc/manual/"
			);
		} catch (Exception e) {
			// nothing
		}
	}
}