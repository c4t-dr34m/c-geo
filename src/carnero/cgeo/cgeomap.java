package carnero.cgeo;

import gnu.android.app.appmanualclient.*;

import android.app.Activity;
import android.app.ProgressDialog;
import java.util.ArrayList;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import java.util.HashMap;
import java.util.Locale;

public class cgeomap extends MapActivity {

	private Resources res = null;
	private Activity activity = null;
	private MapView mapView = null;
	private MapController mapController = null;
	private cgSettings settings = null;
	private cgBase base = null;
	private cgWarning warning = null;
	private cgeoapplication app = null;
	private SharedPreferences.Editor prefsEdit = null;
	private cgGeo geo = null;
	private cgDirection dir = null;
	private cgUpdateLoc geoUpdate = new UpdateLoc();
	private cgUpdateDir dirUpdate = new UpdateDir();
	// from intent
	private boolean fromDetailIntent = false;
	private Long searchIdIntent = null;
	private String geocodeIntent = null;
	private Double latitudeIntent = null;
	private Double longitudeIntent = null;
	// status data
	private Long searchId = null;
	// map status data
	private boolean followMyLocation = false;
	private Integer centerLatitude = null;
	private Integer centerLongitude = null;
	private Integer spanLatitude = null;
	private Integer spanLongitude = null;
	private Integer centerLatitudeUsers = null;
	private Integer centerLongitudeUsers = null;
	private Integer spanLatitudeUsers = null;
	private Integer spanLongitudeUsers = null;
	// thread
	private LoadTimer loadTimer = null;
	private LoadDetails loadDetailsThread = null;
	// overlays
	private cgMapOverlay overlayCaches = null;
	private cgUsersOverlay overlayUsers = null;
	private cgOverlayScale overlayScale = null;
	private cgMapMyOverlay overlayMyLoc = null;
	// data for overlays
	private HashMap<Integer, Drawable> iconsCache = new HashMap<Integer, Drawable>();
	private ArrayList<cgCache> caches = new ArrayList<cgCache>();
	private ArrayList<cgUser> users = new ArrayList<cgUser>();
	private ArrayList<cgCoord> coordinates = new ArrayList<cgCoord>();
	// storing for offline
	private ProgressDialog waitDialog = null;
	private int detailTotal = 0;
	private int detailProgress = 0;
	private Long detailProgressTime = 0l;
	// views
	private ImageView myLocSwitch = null;
	// other things
	private boolean live = true; // live map (live, dead) or rest (displaying caches on map)

	final private Handler loadDetailsHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				if (waitDialog != null) {
					Float diffTime = new Float((System.currentTimeMillis() - detailProgressTime) / 1000); // seconds left
					Float oneCache = diffTime / detailProgress; // left time per cache
					Float etaTime = (detailTotal - detailProgress) * oneCache; // seconds remaining

					waitDialog.setProgress(detailProgress);
					if (etaTime < 40) {
						waitDialog.setMessage(res.getString(R.string.caches_downloading) + " " + res.getString(R.string.caches_eta_ltm));
					} else if (etaTime < 90) {
						waitDialog.setMessage(res.getString(R.string.caches_downloading) + " " + String.format(Locale.getDefault(), "%.0f", (etaTime / 60)) + " " + res.getString(R.string.caches_eta_min));
					} else {
						waitDialog.setMessage(res.getString(R.string.caches_downloading) + " " + String.format(Locale.getDefault(), "%.0f", (etaTime / 60)) + " " + res.getString(R.string.caches_eta_mins));
					}
				}
			} else {
				if (waitDialog != null) {
					waitDialog.dismiss();
					waitDialog.setOnCancelListener(null);
				}

				if (geo == null)
					geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);
				if (settings.useCompass == 1 && dir == null)
					dir = app.startDir(activity, dirUpdate, warning);
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// class init
		res = this.getResources();
		activity = this;
		app = (cgeoapplication) activity.getApplication();
		app.setAction(null);
		settings = new cgSettings(activity, getSharedPreferences(cgSettings.preferences, 0));
		base = new cgBase(app, settings, getSharedPreferences(cgSettings.preferences, 0));
		warning = new cgWarning(activity);
		prefsEdit = getSharedPreferences(cgSettings.preferences, 0).edit();

		// set layout
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// set layout
		if (settings.skin == 1)
			setTheme(R.style.light);
		else
			setTheme(R.style.dark);
		setContentView(R.layout.map);
		base.setTitle(activity, res.getString(R.string.map_map));

		if (geo == null)
			geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);
		if (settings.useCompass == 1 && dir == null)
			dir = app.startDir(activity, dirUpdate, warning);

		mapView = (MapView) findViewById(R.id.map);
		mapController = mapView.getController();
		mapView.getOverlays().clear();

		if (overlayMyLoc == null) {
			overlayMyLoc = new cgMapMyOverlay(settings);
			mapView.getOverlays().add(overlayMyLoc);
		}

		// get parameters
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			fromDetailIntent = extras.getBoolean("detail");
			searchIdIntent = extras.getLong("searchid");
			geocodeIntent = extras.getString("geocode");
			latitudeIntent = extras.getDouble("latitude");
			longitudeIntent = extras.getDouble("longitude");
		}

		// initialize map
		if (settings.maptype == cgSettings.mapSatellite)
			mapView.setSatellite(true);
		else
			mapView.setSatellite(false);
		mapView.setBuiltInZoomControls(true);
		mapView.displayZoomControls(true);
		mapController.setZoom(settings.mapzoom);
		setMyLoc(null);

		// live or death
		if (searchIdIntent == null && geocodeIntent == null && (latitudeIntent == null || longitudeIntent == null))
			live = true;
		
		// google analytics
		if (live) {
			base.sendAnal(activity, "/map/live");
			
			followMyLocation = true;
		} else {
			base.sendAnal(activity, "/map/normal");
			
			followMyLocation = false;
			
			// TODO: get center of map and span, center map
		}

		if (geo != null)
			geoUpdate.updateLoc(geo);
		if (dir != null)
			dirUpdate.updateDir(dir);
		
		loadTimer = new LoadTimer();
		loadTimer.start();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onResume() {
		super.onResume();

		app.setAction(null);
		if (geo == null)
			geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);
		if (settings.useCompass == 1 && dir == null)
			dir = app.startDir(activity, dirUpdate, warning);

		if (geo != null)
			geoUpdate.updateLoc(geo);
		if (dir != null)
			dirUpdate.updateDir(dir);
	}

	@Override
	public void onStop() {
		if (dir != null)
			dir = app.removeDir();
		if (geo != null)
			geo = app.removeGeo();

		savePrefs();

		if (mapView != null)
			mapView.destroyDrawingCache();

		super.onStop();
	}

	@Override
	public void onPause() {
		if (dir != null)
			dir = app.removeDir();
		if (geo != null)
			geo = app.removeGeo();

		savePrefs();

		if (mapView != null)
			mapView.destroyDrawingCache();

		super.onPause();
	}

	@Override
	public void onDestroy() {
		if (dir != null)
			dir = app.removeDir();
		if (geo != null)
			geo = app.removeGeo();

		savePrefs();

		if (mapView != null)
			mapView.destroyDrawingCache();

		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 0, res.getString(R.string.caches_on_map)).setIcon(android.R.drawable.ic_menu_mapmode);
		menu.add(0, 2, 0, res.getString(R.string.map_trail_hide)).setIcon(android.R.drawable.ic_menu_recent_history);
		menu.add(0, 3, 0, res.getString(R.string.map_live_disable)).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		menu.add(0, 4, 0, res.getString(R.string.caches_store_offline)).setIcon(android.R.drawable.ic_menu_set_as).setEnabled(false);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		MenuItem item;
		try {
			item = menu.findItem(1); // view
			if (mapView != null && mapView.isSatellite() == false)
				item.setTitle(res.getString(R.string.map_view_satellite));
			else
				item.setTitle(res.getString(R.string.map_view_map));

			item = menu.findItem(2); // show trail
			if (settings.maptrail == 1)
				item.setTitle(res.getString(R.string.map_trail_hide));
			else
				item.setTitle(res.getString(R.string.map_trail_show));

			item = menu.findItem(3); // live map
			if (live == false) {
				item.setEnabled(false);
				item.setTitle(res.getString(R.string.map_live_enable));
			} else {
				if (settings.maplive == 1)
					item.setTitle(res.getString(R.string.map_live_disable));
				else
					item.setTitle(res.getString(R.string.map_live_enable));
			}

			item = menu.findItem(4); // store loaded
			if (live && !isLoading() && app.getNotOfflineCount(searchId) > 0 && caches != null && caches.size() > 0)
				item.setEnabled(true);
			else
				item.setEnabled(false);
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgeomap.onPrepareOptionsMenu: " + e.toString());
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int id = item.getItemId();

		if (id == 1) {
			if (mapView != null && mapView.isSatellite() == false) {
				mapView.setSatellite(true);

				prefsEdit.putInt("maptype", cgSettings.mapSatellite);
				prefsEdit.commit();
			} else {
				mapView.setSatellite(false);

				prefsEdit.putInt("maptype", cgSettings.mapClassic);
				prefsEdit.commit();
			}

			return true;
		} else if (id == 2) {
			if (settings.maptrail == 1) {
				prefsEdit.putInt("maptrail", 0);
				prefsEdit.commit();

				settings.maptrail = 0;
			} else {
				prefsEdit.putInt("maptrail", 1);
				prefsEdit.commit();

				settings.maptrail = 1;
			}
		} else if (id == 3) {
			if (settings.maplive == 1)
				settings.liveMapDisable();
			else
				settings.liveMapEnable();
		} else if (id == 4) {
			if (live && !isLoading() && caches != null && caches.size() > 0) {
				final ArrayList<String> geocodes = new ArrayList<String>();

				try {
					if (coordinates != null && coordinates.size() > 0) {
						final GeoPoint mapCenter = mapView.getMapCenter();
						final int mapCenterLat = mapCenter.getLatitudeE6();
						final int mapCenterLon = mapCenter.getLongitudeE6();
						final int mapSpanLat = mapView.getLatitudeSpan();
						final int mapSpanLon = mapView.getLongitudeSpan();

						for (cgCoord coord : coordinates) {
							if (coord.geocode != null && coord.geocode.length() > 0) {
								if (base.isCacheInViewPort(mapCenterLat, mapCenterLon, mapSpanLat, mapSpanLon, coord.latitude, coord.longitude) && app.isOffline(coord.geocode, null) == false) {
									geocodes.add(coord.geocode);
								}
							}
						}
					}
				} catch (Exception e) {
					Log.e(cgSettings.tag, "cgeomap.onOptionsItemSelected.#4: " + e.toString());
				}

				detailTotal = geocodes.size();

				if (detailTotal == 0) {
					warning.showToast(res.getString(R.string.warn_save_nothing));

					return true;
				}

				waitDialog = new ProgressDialog(this);
				waitDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				waitDialog.setCancelable(true);
				waitDialog.setMax(detailTotal);
				waitDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

					public void onCancel(DialogInterface arg0) {
						try {
							if (loadDetailsThread != null)
								loadDetailsThread.stopIt();

							if (geo == null)
								geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);
							if (settings.useCompass == 1 && dir == null)
								dir = app.startDir(activity, dirUpdate, warning);
						} catch (Exception e) {
							Log.e(cgSettings.tag, "cgeocaches.onPrepareOptionsMenu.onCancel: " + e.toString());
						}
					}
				});
				
				Float etaTime = new Float((detailTotal * (float) 7) / 60);
				if (etaTime < 0.4)
					waitDialog.setMessage(res.getString(R.string.caches_downloading) + " " + res.getString(R.string.caches_eta_ltm));
				else if (etaTime < 1.5)
					waitDialog.setMessage(res.getString(R.string.caches_downloading) + " " + String.format(Locale.getDefault(), "%.0f", etaTime) + " " + res.getString(R.string.caches_eta_min));
				else
					waitDialog.setMessage(res.getString(R.string.caches_downloading) + " " + String.format(Locale.getDefault(), "%.0f", etaTime) + " " + res.getString(R.string.caches_eta_mins));
				waitDialog.show();

				detailProgressTime = System.currentTimeMillis();

				loadDetailsThread = new LoadDetails(loadDetailsHandler, geocodes);
				loadDetailsThread.start();

				return true;
			}
		}

		return false;
	}

	private void savePrefs() {
		if (mapView == null)
			return;
		
		if (mapView.isSatellite()) {
			prefsEdit.putInt("maptype", cgSettings.mapSatellite);
			settings.maptype = cgSettings.mapSatellite;
		} else {
			prefsEdit.putInt("maptype", cgSettings.mapClassic);
			settings.maptype = cgSettings.mapClassic;
		}
		
		if (prefsEdit == null)
			prefsEdit = getSharedPreferences(cgSettings.preferences, 0).edit();
		prefsEdit.putInt("mapzoom", mapView.getZoomLevel());
		prefsEdit.commit();
	}

	// set center of map to my location
	private void myLocationInMiddle() {
		if (geo == null)
			return;
		if (!followMyLocation)
			return;

		centerMap(geo.latitudeNow, geo.longitudeNow);
	}

	// center map to desired location
	private void centerMap(Double latitude, Double longitude) {
		if (latitude == null || longitude == null)
			return;
		if (mapView == null)
			return;

		mapController.animateTo(makeGeoPoint(latitude, longitude));
	}

	// class: update location
	private class UpdateLoc extends cgUpdateLoc {

		@Override
		public void updateLoc(cgGeo geo) {
			if (geo == null)
				return;

			try {
				if (overlayMyLoc == null && mapView != null) {
					overlayMyLoc = new cgMapMyOverlay(settings);
					mapView.getOverlays().add(overlayMyLoc);
				}

				if (overlayMyLoc != null && geo.location != null)
					overlayMyLoc.setCoordinates(geo.location);

				if (geo.latitudeNow != null && geo.longitudeNow != null) {
					if (followMyLocation == true)
						myLocationInMiddle();
				}

				if (settings.useCompass == 0 || (geo.speedNow != null && geo.speedNow > 5)) { // use GPS when speed is higher than 18 km/h
					if (geo.bearingNow != null)
						overlayMyLoc.setHeading(geo.bearingNow);
					else
						overlayMyLoc.setHeading(new Double(0));
				}
			} catch (Exception e) {
				Log.w(cgSettings.tag, "Failed to update location.");
			}
		}
	}

	// class: update direction
	private class UpdateDir extends cgUpdateDir {

		@Override
		public void updateDir(cgDirection dir) {
			if (dir == null || dir.directionNow == null) {
				return;
			}

			if (overlayMyLoc != null && mapView != null && (geo == null || geo.speedNow == null || geo.speedNow <= 5)) { // use compass when speed is lower than 18 km/h
				overlayMyLoc.setHeading(dir.directionNow);
				mapView.invalidate();
			}
		}
	}
	
	// loading timer
	private class LoadTimer extends Thread {
		
		private volatile boolean stop = false;
		
		public void stopIt() {
			stop = true;
		}

		@Override
		public void run() {
			while (!stop) {
				try {
					// TODO: timer body
					
					
					yield();
					sleep(200);
				} catch (Exception e) {
					Log.w(cgSettings.tag, "cgeomap.LoadTimer.run: " + e.toString());
				}
			};
		}
	}

	// store caches
	private class LoadDetails extends Thread {

		private Handler handler = null;
		private ArrayList<String> geocodes = null;
		private volatile boolean stop = false;
		private long last = 0l;

		public LoadDetails(Handler handlerIn, ArrayList<String> geocodesIn) {
			handler = handlerIn;
			geocodes = geocodesIn;
		}

		public void stopIt() {
			stop = true;
		}

		@Override
		public void run() {
			if (geocodes == null || geocodes.isEmpty())
				return;
			
			if (dir != null)
				dir = app.removeDir();
			if (geo != null)
				geo = app.removeGeo();

			for (String geocode : geocodes) {
				try {
					if (stop == true)
						break;
					
					if (!app.isOffline(geocode, null)) {
						if ((System.currentTimeMillis() - last) < 1500) {
							try {
								int delay = 1000 + ((Double) (Math.random() * 1000)).intValue() - (int) (System.currentTimeMillis() - last);
								if (delay < 0)
									delay = 500;

								sleep(delay);
							} catch (Exception e) {
								// nothing
							}
						}

						if (stop == true) {
							Log.i(cgSettings.tag, "Stopped storing process.");

							break;
						}

						base.storeCache(app, activity, null, geocode, 1, handler);
					}
				} catch (Exception e) {
					Log.e(cgSettings.tag, "cgeocaches.LoadDetails.run: " + e.toString());
				} finally {
					// one more cache over
					detailProgress++;
					handler.sendEmptyMessage(0);
				}

				yield();

				last = System.currentTimeMillis();
			}

			// we're done
			handler.sendEmptyMessage(1);
		}
	}
	
	// get if map is loading something
	protected boolean isLoading() {
		boolean loading = false;
		
		// TODO: returns true if some loading/displaying thread still runs
		
		return loading;
	}

	// change actionbar title
	protected void changeTitle(boolean loading) {
		String title = null;
		
		if (live == true)
			title = res.getString(R.string.map_live);
		else
			title = res.getString(R.string.map_map);

		if (loading == true) {
			base.showProgress(activity, true);
			base.setTitle(activity, title);
		} else if (caches != null) {
			base.showProgress(activity, false);
			base.setTitle(activity, title + " [" + caches.size() + "]");
		} else {
			base.showProgress(activity, false);
			base.setTitle(activity, title + " " + res.getString(R.string.caches_no_caches));
		}
	}

	// switch My Location button image
	private void setMyLoc(Boolean status) {
		if (myLocSwitch == null)
			myLocSwitch = (ImageView) findViewById(R.id.my_position);
		
		if (status == null) {
			if (followMyLocation == true)
				myLocSwitch.setImageResource(R.drawable.my_location_on);
			else
				myLocSwitch.setImageResource(R.drawable.my_location_off);
		} else {
			if (status == true)
				myLocSwitch.setImageResource(R.drawable.my_location_on);
			else
				myLocSwitch.setImageResource(R.drawable.my_location_off);
		}
		
		myLocSwitch.setOnClickListener(new MyLocationListener());
	}

	// set my location listener
	private class MyLocationListener implements View.OnClickListener {

		public void onClick(View view) {
			if (myLocSwitch == null)
				myLocSwitch = (ImageView) findViewById(R.id.my_position);

			if (followMyLocation == true) {
				followMyLocation = false;

				myLocSwitch.setImageResource(R.drawable.my_location_off);
			} else {
				followMyLocation = true;
				myLocationInMiddle();

				myLocSwitch.setImageResource(R.drawable.my_location_on);
			}
		}
	}
	
	// make geopoint
	private GeoPoint makeGeoPoint(Double latitude, Double longitude) {
		return new GeoPoint((int) (latitude * 1e6), (int) (longitude * 1e6));
	}

	// close activity and open homescreen
	public void goHome(View view) {
		base.goHome(activity);
	}

	// open manual entry
	public void goManual(View view) {
		try {
			AppManualReaderClient.openManual(
					"c-geo",
					"c:geo-live-map",
					activity,
					"http://cgeo.carnero.cc/manual/");
		} catch (Exception e) {
			// nothing
		}
	}
}
