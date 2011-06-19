package carnero.cgeo.mapcommon;

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
import carnero.cgeo.R;
import carnero.cgeo.cgBase;
import carnero.cgeo.cgCache;
import carnero.cgeo.cgCoord;
import carnero.cgeo.cgDirection;
import carnero.cgeo.cgGeo;
import carnero.cgeo.cgSettings;
import carnero.cgeo.cgUpdateDir;
import carnero.cgeo.cgUpdateLoc;
import carnero.cgeo.cgUser;
import carnero.cgeo.cgWarning;
import carnero.cgeo.cgWaypoint;
import carnero.cgeo.cgeoapplication;
import carnero.cgeo.mapinterfaces.ActivityImpl;
import carnero.cgeo.mapinterfaces.CacheOverlayItemImpl;
import carnero.cgeo.mapinterfaces.GeoPointImpl;
import carnero.cgeo.mapinterfaces.MapControllerImpl;
import carnero.cgeo.mapinterfaces.MapFactory;
import carnero.cgeo.mapinterfaces.MapViewImpl;
import carnero.cgeo.mapinterfaces.UserOverlayItemImpl;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Locale;

public class cgeomap extends MapBase {

	private Resources res = null;
	private Activity activity = null;
	private MapViewImpl mapView = null;
	private MapControllerImpl mapController = null;
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
	private String waypointTypeIntent = null;
	// status data
	private Long searchId = null;
	private String token = null;
	private boolean noMapTokenShowed = false;
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
	private UsersTimer usersTimer = null;
	private LoadThread loadThread = null;
	private DownloadThread downloadThread = null;
	private DisplayThread displayThread = null;
	private UsersThread usersThread = null;
	private DisplayUsersThread displayUsersThread = null;
	private LoadDetails loadDetailsThread = null;
	private volatile long loadThreadRun = 0l;
	private volatile long downloadThreadRun = 0l;
	private volatile long usersThreadRun = 0l;
	private volatile boolean downloaded = false;
	// overlays
	private cgMapOverlay overlayCaches = null;
	private cgUsersOverlay overlayUsers = null;
	private cgOverlayScale overlayScale = null;
	private cgMapMyOverlay overlayMyLoc = null;
	// data for overlays
	private int cachesCnt = 0;
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
	private boolean liveChanged = false; // previous state for loadTimer
	private boolean centered = false; // if map is already centered
	private boolean alreadyCentered = false; // -""- for setting my location
	// handlers
	final private Handler displayHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			final int what = msg.what;

			if (what == 0) {
				// set title
				final StringBuilder title = new StringBuilder();

				if (live == true) {
					title.append(res.getString(R.string.map_live));
				} else {
					title.append(res.getString(R.string.map_map));
				}

				if (caches != null && cachesCnt > 0) {
					title.append(" ");
					title.append("[");
					title.append(caches.size());
					title.append("]");
				}

				base.setTitle(activity, title.toString());
			} else if (what == 1 && mapView != null) {
				mapView.invalidate();
			}
		}
	};
	final private Handler showProgressHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			final int what = msg.what;

			if (what == 0) {
				base.showProgress(activity, false);
			} else if (what == 1) {
				base.showProgress(activity, true);
			}
		}
	};
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

				if (geo == null) {
					geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);
				}
				if (settings.useCompass == 1 && dir == null) {
					dir = app.startDir(activity, dirUpdate, warning);
				}
			}
		}
	};
	final private Handler noMapTokenHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (!noMapTokenShowed) {
				warning.showToast(res.getString(R.string.map_token_err));

				noMapTokenShowed = true;
			}
		}
	};

	public cgeomap(ActivityImpl activity) {
		super(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// class init
		res = this.getResources();
		activity = this.getActivity();
		app = (cgeoapplication) activity.getApplication();
		app.setAction(null);
		settings = new cgSettings(activity, activity.getSharedPreferences(cgSettings.preferences, 0));
		base = new cgBase(app, settings, activity.getSharedPreferences(cgSettings.preferences, 0));
		warning = new cgWarning(activity);
		prefsEdit = activity.getSharedPreferences(cgSettings.preferences, 0).edit();
		MapFactory mapFactory = settings.getMapFactory();

		// reset status
		noMapTokenShowed = false;

		// set layout
		activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// set layout
		if (settings.skin == 1) {
			activity.setTheme(R.style.light);
		} else {
			activity.setTheme(R.style.dark);
		}
		activity.setContentView(settings.getMapFactory().getMapLayoutId());
		base.setTitle(activity, res.getString(R.string.map_map));

		if (geo == null) {
			geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);
		}
		if (settings.useCompass == 1 && dir == null) {
			dir = app.startDir(activity, dirUpdate, warning);
		}

		mapView = (MapViewImpl) activity.findViewById(mapFactory.getMapViewId());
		mapView.setMapSource(settings);
		if (!mapView.needsScaleOverlay()) {
			mapView.setBuiltinScale(true);
		}

		// initialize map
		if (settings.maptype == cgSettings.mapSatellite) {
			mapView.setSatellite(true);
		} else {
			mapView.setSatellite(false);
		}
		mapView.setBuiltInZoomControls(true);
		mapView.displayZoomControls(true);
		mapView.preLoad();

		// initialize overlays
		mapView.clearOverlays();

		if (overlayMyLoc == null) {
			overlayMyLoc = new cgMapMyOverlay(settings);
			mapView.addOverlay(mapFactory.getOverlayBaseWrapper(overlayMyLoc));
		}

		if (settings.publicLoc > 0 && overlayUsers == null) {
			overlayUsers = mapView.createAddUsersOverlay(activity, getResources().getDrawable(R.drawable.user_location));
		}

		if (overlayCaches == null) {
			overlayCaches = mapView.createAddMapOverlay(settings, mapView.getContext(), getResources().getDrawable(R.drawable.marker), fromDetailIntent);
		}

		if (overlayScale == null && mapView.needsScaleOverlay()) {
			overlayScale = new cgOverlayScale(activity, settings);
			mapView.addOverlay(mapFactory.getOverlayBaseWrapper(overlayScale));
		}

		mapView.invalidate();

		mapController = mapView.getMapController();
		mapController.setZoom(settings.mapzoom);

		// start location and directory services
		if (geo != null) {
			geoUpdate.updateLoc(geo);
		}
		if (dir != null) {
			dirUpdate.updateDir(dir);
		}

		// get parameters
		Bundle extras = activity.getIntent().getExtras();
		if (extras != null) {
			fromDetailIntent = extras.getBoolean("detail");
			searchIdIntent = extras.getLong("searchid");
			geocodeIntent = extras.getString("geocode");
			latitudeIntent = extras.getDouble("latitude");
			longitudeIntent = extras.getDouble("longitude");
			waypointTypeIntent = extras.getString("wpttype");

			if (searchIdIntent == 0l) {
				searchIdIntent = null;
			}
			if (latitudeIntent == 0.0) {
				latitudeIntent = null;
			}
			if (longitudeIntent == 0.0) {
				longitudeIntent = null;
			}
		}

		// live or death
		if (searchIdIntent == null && geocodeIntent == null && (latitudeIntent == null || longitudeIntent == null)) {
			live = true;
		} else {
			live = false;
		}

		// google analytics
		if (live) {
			base.sendAnal(activity, "/map/live");

			followMyLocation = true;
		} else {
			base.sendAnal(activity, "/map/normal");

			followMyLocation = false;

			if (geocodeIntent != null || searchIdIntent != null || (latitudeIntent != null && longitudeIntent != null)) {
				centerMap(geocodeIntent, searchIdIntent, latitudeIntent, longitudeIntent);
			}
		}
		setMyLoc(null);
		startTimer();
	}

	@Override
	public void onResume() {
		super.onResume();
		
		settings.load();

		app.setAction(null);
		if (geo == null) {
			geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);
		}
		if (settings.useCompass == 1 && dir == null) {
			dir = app.startDir(activity, dirUpdate, warning);
		}

		if (geo != null) {
			geoUpdate.updateLoc(geo);
		}
		if (dir != null) {
			dirUpdate.updateDir(dir);
		}

		startTimer();
	}

	@Override
	public void onStop() {
		if (loadTimer != null) {
			loadTimer.stopIt();
			loadTimer = null;
		}

		if (usersTimer != null) {
			usersTimer.stopIt();
			usersTimer = null;
		}

		if (dir != null) {
			dir = app.removeDir();
		}
		if (geo != null) {
			geo = app.removeGeo();
		}

		savePrefs();

		if (mapView != null) {
			mapView.destroyDrawingCache();
		}

		super.onStop();
	}

	@Override
	public void onPause() {
		if (loadTimer != null) {
			loadTimer.stopIt();
			loadTimer = null;
		}

		if (usersTimer != null) {
			usersTimer.stopIt();
			usersTimer = null;
		}

		if (dir != null) {
			dir = app.removeDir();
		}
		if (geo != null) {
			geo = app.removeGeo();
		}

		savePrefs();

		if (mapView != null) {
			mapView.destroyDrawingCache();
		}

		super.onPause();
	}

	@Override
	public void onDestroy() {
		if (loadTimer != null) {
			loadTimer.stopIt();
			loadTimer = null;
		}

		if (usersTimer != null) {
			usersTimer.stopIt();
			usersTimer = null;
		}

		if (dir != null) {
			dir = app.removeDir();
		}
		if (geo != null) {
			geo = app.removeGeo();
		}

		savePrefs();

		if (mapView != null) {
			mapView.destroyDrawingCache();
		}

		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 0, res.getString(R.string.caches_on_map)).setIcon(android.R.drawable.ic_menu_mapmode);
		menu.add(0, 3, 0, res.getString(R.string.map_live_disable)).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		menu.add(0, 4, 0, res.getString(R.string.caches_store_offline)).setIcon(android.R.drawable.ic_menu_set_as).setEnabled(false);
		menu.add(0, 2, 0, res.getString(R.string.map_trail_hide)).setIcon(android.R.drawable.ic_menu_recent_history);
		menu.add(0, 5, 0, res.getString(R.string.map_circles_hide)).setIcon(android.R.drawable.ic_menu_view);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		MenuItem item;
		try {
			item = menu.findItem(1); // view
			if (mapView != null && mapView.isSatellite() == false) {
				item.setTitle(res.getString(R.string.map_view_satellite));
			} else {
				item.setTitle(res.getString(R.string.map_view_map));
			}

			item = menu.findItem(2); // show trail
			if (settings.maptrail == 1) {
				item.setTitle(res.getString(R.string.map_trail_hide));
			} else {
				item.setTitle(res.getString(R.string.map_trail_show));
			}

			item = menu.findItem(3); // live map
			if (live == false) {
				item.setEnabled(false);
				item.setTitle(res.getString(R.string.map_live_enable));
			} else {
				if (settings.maplive == 1) {
					item.setTitle(res.getString(R.string.map_live_disable));
				} else {
					item.setTitle(res.getString(R.string.map_live_enable));
				}
			}

			item = menu.findItem(4); // store loaded
			if (live && !isLoading() && app.getNotOfflineCount(searchId) > 0 && caches != null && caches.size() > 0) {
				item.setEnabled(true);
			} else {
				item.setEnabled(false);
			}

			item = menu.findItem(5); // show circles
			if (overlayCaches != null && overlayCaches.getCircles()) {
				item.setTitle(res.getString(R.string.map_circles_hide));
			} else {
				item.setTitle(res.getString(R.string.map_circles_show));
			}
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
			if (settings.maplive == 1) {
				settings.liveMapDisable();
			} else {
				settings.liveMapEnable();
			}
			liveChanged = true;
			searchId = null;
			searchIdIntent = null;
		} else if (id == 4) {
			if (live && !isLoading() && caches != null && !caches.isEmpty()) {
				final ArrayList<String> geocodes = new ArrayList<String>();

				ArrayList<cgCache> cachesProtected = (ArrayList<cgCache>) caches.clone();
				try {
					if (cachesProtected != null && cachesProtected.size() > 0) {
						final GeoPointImpl mapCenter = mapView.getMapViewCenter();
						final int mapCenterLat = mapCenter.getLatitudeE6();
						final int mapCenterLon = mapCenter.getLongitudeE6();
						final int mapSpanLat = mapView.getLatitudeSpan();
						final int mapSpanLon = mapView.getLongitudeSpan();

						for (cgCache oneCache : cachesProtected) {
							if (oneCache != null && oneCache.latitude != null && oneCache.longitude != null) {
								if (base.isCacheInViewPort(mapCenterLat, mapCenterLon, mapSpanLat, mapSpanLon, oneCache.latitude, oneCache.longitude) && app.isOffline(oneCache.geocode, null) == false) {
									geocodes.add(oneCache.geocode);
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

				waitDialog = new ProgressDialog(activity);
				waitDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				waitDialog.setCancelable(true);
				waitDialog.setMax(detailTotal);
				waitDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

					public void onCancel(DialogInterface arg0) {
						try {
							if (loadDetailsThread != null) {
								loadDetailsThread.stopIt();
							}

							if (geo == null) {
								geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);
							}
							if (settings.useCompass == 1 && dir == null) {
								dir = app.startDir(activity, dirUpdate, warning);
							}
						} catch (Exception e) {
							Log.e(cgSettings.tag, "cgeocaches.onPrepareOptionsMenu.onCancel: " + e.toString());
						}
					}
				});

				Float etaTime = new Float((detailTotal * (float) 7) / 60);
				if (etaTime < 0.4) {
					waitDialog.setMessage(res.getString(R.string.caches_downloading) + " " + res.getString(R.string.caches_eta_ltm));
				} else if (etaTime < 1.5) {
					waitDialog.setMessage(res.getString(R.string.caches_downloading) + " " + String.format(Locale.getDefault(), "%.0f", etaTime) + " " + res.getString(R.string.caches_eta_min));
				} else {
					waitDialog.setMessage(res.getString(R.string.caches_downloading) + " " + String.format(Locale.getDefault(), "%.0f", etaTime) + " " + res.getString(R.string.caches_eta_mins));
				}
				waitDialog.show();

				detailProgressTime = System.currentTimeMillis();

				loadDetailsThread = new LoadDetails(loadDetailsHandler, geocodes);
				loadDetailsThread.start();

				return true;
			}
		} else if (id == 5) {
			if (overlayCaches == null) {
				return false;
			}

			overlayCaches.switchCircles();
		}

		return false;
	}

	private void savePrefs() {
		if (mapView == null) {
			return;
		}

		if (mapView.isSatellite()) {
			prefsEdit.putInt("maptype", cgSettings.mapSatellite);
			settings.maptype = cgSettings.mapSatellite;
		} else {
			prefsEdit.putInt("maptype", cgSettings.mapClassic);
			settings.maptype = cgSettings.mapClassic;
		}

		if (prefsEdit == null) {
			prefsEdit = activity.getSharedPreferences(cgSettings.preferences, 0).edit();
		}
		prefsEdit.putInt("mapzoom", mapView.getMapZoomLevel());
		prefsEdit.commit();
	}

	// set center of map to my location
	private void myLocationInMiddle() {
		if (geo == null) {
			return;
		}
		if (!followMyLocation) {
			return;
		}

		centerMap(geo.latitudeNow, geo.longitudeNow);
	}

	// class: update location
	private class UpdateLoc extends cgUpdateLoc {

		@Override
		public void updateLoc(cgGeo geo) {
			if (geo == null) {
				return;
			}

			try {
				if (overlayMyLoc == null && mapView != null) {
					overlayMyLoc = new cgMapMyOverlay(settings);
					mapView.addOverlay(settings.getMapFactory().getOverlayBaseWrapper(overlayMyLoc));
				}

				if (overlayMyLoc != null && geo.location != null) {
					overlayMyLoc.setCoordinates(geo.location);
				}

				if (geo.latitudeNow != null && geo.longitudeNow != null) {
					if (followMyLocation == true) {
						myLocationInMiddle();
					}
				}

				if (settings.useCompass == 0 || (geo.speedNow != null && geo.speedNow > 5)) { // use GPS when speed is higher than 18 km/h
					if (geo.bearingNow != null) {
						overlayMyLoc.setHeading(geo.bearingNow);
					} else {
						overlayMyLoc.setHeading(new Double(0));
					}
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

	public void startTimer() {
		if (latitudeIntent != null && longitudeIntent != null) {
			// display just one point
			(new DisplayPointThread()).start();
		} else {
			// start timer
			if (loadTimer != null) {
				loadTimer.stopIt();
				loadTimer = null;
			}
			loadTimer = new LoadTimer();
			loadTimer.start();
		}

		if (settings.publicLoc > 0) {
			if (usersTimer != null) {
				usersTimer.stopIt();
				usersTimer = null;
			}
			usersTimer = new UsersTimer();
			usersTimer.start();
		}
	}

	// loading timer
	private class LoadTimer extends Thread {

		private volatile boolean stop = false;

		public void stopIt() {
			stop = true;

			if (loadThread != null) {
				loadThread.stopIt();
				loadThread = null;
			}

			if (downloadThread != null) {
				downloadThread.stopIt();
				downloadThread = null;
			}

			if (displayThread != null) {
				displayThread.stopIt();
				displayThread = null;
			}
		}

		@Override
		public void run() {
			GeoPointImpl mapCenterNow;
			int centerLatitudeNow;
			int centerLongitudeNow;
			int spanLatitudeNow;
			int spanLongitudeNow;
			boolean moved = false;
			boolean force = false;
			long currentTime = 0;

			while (!stop) {
				try {
					sleep(250);

					if (mapView != null) {
						// get current viewport
						mapCenterNow = mapView.getMapViewCenter();
						centerLatitudeNow = mapCenterNow.getLatitudeE6();
						centerLongitudeNow = mapCenterNow.getLongitudeE6();
						spanLatitudeNow = mapView.getLatitudeSpan();
						spanLongitudeNow = mapView.getLongitudeSpan();

						// check if map moved or zoomed
						moved = false;
						force = false;

						if (liveChanged) {
							moved = true;
							force = true;
						} else if (live && settings.maplive == 1 && downloaded == false) {
							moved = true;
						} else if (centerLatitude == null || centerLongitude == null) {
							moved = true;
						} else if (spanLatitude == null || spanLongitude == null) {
							moved = true;
						} else if (((Math.abs(spanLatitudeNow - spanLatitude) > 50) || (Math.abs(spanLongitudeNow - spanLongitude) > 50) || // changed zoom
								(Math.abs(centerLatitudeNow - centerLatitude) > (spanLatitudeNow / 4)) || (Math.abs(centerLongitudeNow - centerLongitude) > (spanLongitudeNow / 4)) // map moved
								) && (cachesCnt <= 0 || caches == null || caches.isEmpty()
								|| !base.isInViewPort(centerLatitude, centerLongitude, centerLatitudeNow, centerLongitudeNow, spanLatitude, spanLongitude, spanLatitudeNow, spanLongitudeNow))) {
							moved = true;
						}

						if (moved && caches != null && centerLatitude != null && centerLongitude != null && ((Math.abs(centerLatitudeNow - centerLatitude) > (spanLatitudeNow * 1.2)) || (Math.abs(centerLongitudeNow - centerLongitude) > (spanLongitudeNow * 1.2)))) {
							force = true;
						}

						// save new values
						if (moved) {
							liveChanged = false;

							currentTime = System.currentTimeMillis();
							if (live && settings.maplive == 1) {
								if (1000 < (currentTime - downloadThreadRun)) {
									// from web
									if (20000 < (currentTime - downloadThreadRun)) {
										force = true; // probably stucked thread
									}

									if (force && downloadThread != null && downloadThread.isWorking()) {
										downloadThread.stopIt();

										try {
											sleep(100);
										} catch (Exception e) {
											// nothing
										}
									}

									if (downloadThread != null && downloadThread.isWorking()) {
										continue;
									}

									centerLatitude = centerLatitudeNow;
									centerLongitude = centerLongitudeNow;
									spanLatitude = spanLatitudeNow;
									spanLongitude = spanLongitudeNow;

									showProgressHandler.sendEmptyMessage(1); // show progress
									downloadThread = new DownloadThread(centerLatitude, centerLongitude, spanLatitude, spanLongitude);
									downloadThread.start();
								}
							} else {
								if (250 < (currentTime - loadThreadRun)) {
									// from database
									if (force && loadThread != null && loadThread.isWorking()) {
										loadThread.stopIt();

										try {
											sleep(100);
										} catch (Exception e) {
											// nothing
										}
									}

									if (loadThread != null && loadThread.isWorking()) {
										continue;
									}

									centerLatitude = centerLatitudeNow;
									centerLongitude = centerLongitudeNow;
									spanLatitude = spanLatitudeNow;
									spanLongitude = spanLongitudeNow;

									showProgressHandler.sendEmptyMessage(1); // show progress
									loadThread = new LoadThread(centerLatitude, centerLongitude, spanLatitude, spanLongitude);
									loadThread.start();
								}
							}
						}
					}

					if (!isLoading()) {
						showProgressHandler.sendEmptyMessage(0); // hide progress
					}

					yield();
				} catch (Exception e) {
					Log.w(cgSettings.tag, "cgeomap.LoadTimer.run: " + e.toString());
				}
			};
		}
	}

	// loading timer
	private class UsersTimer extends Thread {

		private volatile boolean stop = false;

		public void stopIt() {
			stop = true;

			if (usersThread != null) {
				usersThread.stopIt();
				usersThread = null;
			}

			if (displayUsersThread != null) {
				displayUsersThread.stopIt();
				displayUsersThread = null;
			}
		}

		@Override
		public void run() {
			GeoPointImpl mapCenterNow;
			int centerLatitudeNow;
			int centerLongitudeNow;
			int spanLatitudeNow;
			int spanLongitudeNow;
			boolean moved = false;
			long currentTime = 0;

			while (!stop) {
				try {
					sleep(250);

					if (mapView != null) {
						// get current viewport
						mapCenterNow = mapView.getMapViewCenter();
						centerLatitudeNow = mapCenterNow.getLatitudeE6();
						centerLongitudeNow = mapCenterNow.getLongitudeE6();
						spanLatitudeNow = mapView.getLatitudeSpan();
						spanLongitudeNow = mapView.getLongitudeSpan();

						// check if map moved or zoomed
						moved = false;

						currentTime = System.currentTimeMillis();

						if (60000 < (currentTime - usersThreadRun)) {
							moved = true;
						} else if (centerLatitudeUsers == null || centerLongitudeUsers == null) {
							moved = true;
						} else if (spanLatitudeUsers == null || spanLongitudeUsers == null) {
							moved = true;
						} else if (((Math.abs(spanLatitudeNow - spanLatitudeUsers) > 50) || (Math.abs(spanLongitudeNow - spanLongitudeUsers) > 50) || // changed zoom
								(Math.abs(centerLatitudeNow - centerLatitudeUsers) > (spanLatitudeNow / 4)) || (Math.abs(centerLongitudeNow - centerLongitudeUsers) > (spanLongitudeNow / 4)) // map moved
								) && !base.isInViewPort(centerLatitudeUsers, centerLongitudeUsers, centerLatitudeNow, centerLongitudeNow, spanLatitudeUsers, spanLongitudeUsers, spanLatitudeNow, spanLongitudeNow)) {
							moved = true;
						}

						// save new values
						if (moved && (1000 < (currentTime - usersThreadRun))) {
							if (usersThread != null && usersThread.isWorking()) {
								continue;
							}

							centerLatitudeUsers = centerLatitudeNow;
							centerLongitudeUsers = centerLongitudeNow;
							spanLatitudeUsers = spanLatitudeNow;
							spanLongitudeUsers = spanLongitudeNow;

							usersThread = new UsersThread(centerLatitude, centerLongitude, spanLatitude, spanLongitude);
							usersThread.start();
						}
					}

					yield();
				} catch (Exception e) {
					Log.w(cgSettings.tag, "cgeomap.LoadUsersTimer.run: " + e.toString());
				}
			};
		}
	}

	// load caches from database
	private class LoadThread extends DoThread {

		public LoadThread(long centerLatIn, long centerLonIn, long spanLatIn, long spanLonIn) {
			super(centerLatIn, centerLonIn, spanLatIn, spanLonIn);
		}

		@Override
		public void run() {
			try {
				stop = false;
				working = true;
				loadThreadRun = System.currentTimeMillis();

				if (geocodeIntent == null) {
					if (searchIdIntent != null && searchIdIntent > 0) {
						searchId = searchIdIntent;
					} else {
						searchId = app.getOfflineAll(settings.cacheType);
					}

					caches = app.getCaches(searchId, centerLat, centerLon, spanLat, spanLon);
				} else {
					cgCache cache = app.getCacheByGeocode(geocodeIntent);

					caches = new ArrayList<cgCache>();
					caches.add(cache);
				}

				if (stop) {
					displayHandler.sendEmptyMessage(0);
					working = false;

					return;
				}

				if (displayThread != null && displayThread.isWorking()) {
					displayThread.stopIt();
				}

				displayThread = new DisplayThread(centerLat, centerLon, spanLat, spanLon);
				displayThread.start();

			} finally {
				working = false;
			}
		}
	}

	// load caches from internet
	private class DownloadThread extends DoThread {

		public DownloadThread(long centerLatIn, long centerLonIn, long spanLatIn, long spanLonIn) {
			super(centerLatIn, centerLonIn, spanLatIn, spanLonIn);
		}

		@Override
		public void run() {
			try {
				stop = false;
				working = true;
				downloadThreadRun = System.currentTimeMillis();

				if (token == null) {
					token = base.getMapUserToken(noMapTokenHandler);
				}

				if (stop) {
					displayHandler.sendEmptyMessage(0);
					working = false;

					return;
				}

				double latMin = (centerLat / 1e6) - ((spanLat / 1e6) / 2) - ((spanLat / 1e6) / 4);
				double latMax = (centerLat / 1e6) + ((spanLat / 1e6) / 2) + ((spanLat / 1e6) / 4);
				double lonMin = (centerLon / 1e6) - ((spanLon / 1e6) / 2) - ((spanLon / 1e6) / 4);
				double lonMax = (centerLon / 1e6) + ((spanLon / 1e6) / 2) + ((spanLon / 1e6) / 4);
				double llCache;

				if (latMin > latMax) {
					llCache = latMax;
					latMax = latMin;
					latMin = llCache;
				}
				if (lonMin > lonMax) {
					llCache = lonMax;
					lonMax = lonMin;
					lonMin = llCache;
				}

				HashMap<String, String> params = new HashMap<String, String>();
				params.put("usertoken", token);
				params.put("latitude-min", String.format((Locale) null, "%.6f", latMin));
				params.put("latitude-max", String.format((Locale) null, "%.6f", latMax));
				params.put("longitude-min", String.format((Locale) null, "%.6f", lonMin));
				params.put("longitude-max", String.format((Locale) null, "%.6f", lonMax));

				searchId = base.searchByViewport(params, 0);
				if (searchId != null) {
					downloaded = true;
				}

				caches = app.getCaches(searchId, centerLat, centerLon, spanLat, spanLon);

				if (stop) {
					displayHandler.sendEmptyMessage(0);
					working = false;

					return;
				}

				if (displayThread != null && displayThread.isWorking()) {
					displayThread.stopIt();
				}

				displayThread = new DisplayThread(centerLat, centerLon, spanLat, spanLon);
				displayThread.start();
			} finally {
				working = false;
			}
		}
	}

	// display (down)loaded caches
	private class DisplayThread extends DoThread {

		public DisplayThread(long centerLatIn, long centerLonIn, long spanLatIn, long spanLonIn) {
			super(centerLatIn, centerLonIn, spanLatIn, spanLonIn);
		}

		@Override
		public void run() {
			try {
				stop = false;
				working = true;

				if (mapView == null || caches == null) {
					displayHandler.sendEmptyMessage(0);
					working = false;

					return;
				}

				// display caches
				final ArrayList<cgCache> cachesProtected = (ArrayList<cgCache>) caches.clone();
				final ArrayList<CacheOverlayItemImpl> items = new ArrayList<CacheOverlayItemImpl>();

				if (cachesProtected != null && !cachesProtected.isEmpty()) {
					int counter = 0;
					int icon = 0;
					Drawable pin = null;
					CacheOverlayItemImpl item = null;

					for (cgCache cacheOne : cachesProtected) {
						if (stop) {
							displayHandler.sendEmptyMessage(0);
							working = false;

							return;
						}

						if (cacheOne.latitude == null && cacheOne.longitude == null) {
							continue;
						}

						final cgCoord coord = new cgCoord(cacheOne);
						coordinates.add(coord);

						item = settings.getMapFactory().getCacheOverlayItem(coord, cacheOne.type);
						icon = base.getIcon(true, cacheOne.type, cacheOne.own, cacheOne.found, cacheOne.disabled || cacheOne.archived);
						pin = null;

						if (iconsCache.containsKey(icon)) {
							pin = iconsCache.get(icon);
						} else {
							pin = getResources().getDrawable(icon);
							pin.setBounds(0, 0, pin.getIntrinsicWidth(), pin.getIntrinsicHeight());

							iconsCache.put(icon, pin);
						}
						item.setMarker(pin);

						items.add(item);

						counter++;
						if ((counter % 10) == 0) {
							overlayCaches.updateItems(items);
							displayHandler.sendEmptyMessage(1);
						}
					}

					overlayCaches.updateItems(items);
					displayHandler.sendEmptyMessage(1);

					cachesCnt = cachesProtected.size();

					if (stop) {
						displayHandler.sendEmptyMessage(0);
						working = false;

						return;
					}

					// display cache waypoints
					if (cachesCnt == 1 && (geocodeIntent != null || searchIdIntent != null) && !live) {
						if (cachesCnt == 1 && live == false) {
							cgCache oneCache = cachesProtected.get(0);

							if (oneCache != null && oneCache.waypoints != null && !oneCache.waypoints.isEmpty()) {
								for (cgWaypoint oneWaypoint : oneCache.waypoints) {
									if (oneWaypoint.latitude == null && oneWaypoint.longitude == null) {
										continue;
									}

									cgCoord coord = new cgCoord(oneWaypoint);

									coordinates.add(coord);
									item = settings.getMapFactory().getCacheOverlayItem(coord, null);

									icon = base.getIcon(false, oneWaypoint.type, false, false, false);
									if (iconsCache.containsKey(icon)) {
										pin = iconsCache.get(icon);
									} else {
										pin = getResources().getDrawable(icon);
										pin.setBounds(0, 0, pin.getIntrinsicWidth(), pin.getIntrinsicHeight());
										iconsCache.put(icon, pin);
									}
									item.setMarker(pin);

									items.add(item);
								}

								overlayCaches.updateItems(items);
								displayHandler.sendEmptyMessage(1);
							}
						}
					}
				} else {
					overlayCaches.updateItems(items);
					displayHandler.sendEmptyMessage(1);
				}

				cachesProtected.clear();

				displayHandler.sendEmptyMessage(0);
			} finally {
				working = false;
			}
		}
	}

	// load users from Go 4 Cache
	private class UsersThread extends DoThread {

		public UsersThread(long centerLatIn, long centerLonIn, long spanLatIn, long spanLonIn) {
			super(centerLatIn, centerLonIn, spanLatIn, spanLonIn);
		}

		@Override
		public void run() {
			try {
				stop = false;
				working = true;
				usersThreadRun = System.currentTimeMillis();

				if (stop) {
					return;
				}

				double latMin = (centerLat / 1e6) - ((spanLat / 1e6) / 2) - ((spanLat / 1e6) / 4);
				double latMax = (centerLat / 1e6) + ((spanLat / 1e6) / 2) + ((spanLat / 1e6) / 4);
				double lonMin = (centerLon / 1e6) - ((spanLon / 1e6) / 2) - ((spanLon / 1e6) / 4);
				double lonMax = (centerLon / 1e6) + ((spanLon / 1e6) / 2) + ((spanLon / 1e6) / 4);
				double llCache;

				if (latMin > latMax) {
					llCache = latMax;
					latMax = latMin;
					latMin = llCache;
				}
				if (lonMin > lonMax) {
					llCache = lonMax;
					lonMax = lonMin;
					lonMin = llCache;
				}

				users = base.getGeocachersInViewport(settings.getUsername(), latMin, latMax, lonMin, lonMax);

				if (stop) {
					return;
				}

				if (displayUsersThread != null && displayUsersThread.isWorking()) {
					displayUsersThread.stopIt();
				}
				displayUsersThread = new DisplayUsersThread(users, centerLat, centerLon, spanLat, spanLon);
				displayUsersThread.start();
			} finally {
				working = false;
			}
		}
	}

	// display users of Go 4 Cache
	private class DisplayUsersThread extends DoThread {

		private ArrayList<cgUser> users = null;

		public DisplayUsersThread(ArrayList<cgUser> usersIn, long centerLatIn, long centerLonIn, long spanLatIn, long spanLonIn) {
			super(centerLatIn, centerLonIn, spanLatIn, spanLonIn);

			users = usersIn;
		}

		@Override
		public void run() {
			try {
				stop = false;
				working = true;

				if (mapView == null || users == null || users.isEmpty()) {
					return;
				}

				// display users
				ArrayList<UserOverlayItemImpl> items = new ArrayList<UserOverlayItemImpl>();

				int counter = 0;
				UserOverlayItemImpl item = null;

				for (cgUser userOne : users) {
					if (stop) {
						return;
					}

					if (userOne.latitude == null && userOne.longitude == null) {
						continue;
					}

					item = settings.getMapFactory().getUserOverlayItemBase(activity, userOne);
					items.add(item);

					counter++;
					if ((counter % 10) == 0) {
						overlayUsers.updateItems(items);
						displayHandler.sendEmptyMessage(1);
					}
				}

				overlayUsers.updateItems(items);
			} finally {
				working = false;
			}
		}
	}

	// display one point
	private class DisplayPointThread extends Thread {

		@Override
		public void run() {
			if (mapView == null || caches == null) {
				return;
			}

			if (latitudeIntent != null && longitudeIntent != null) {
				cgCoord coord = new cgCoord();
				coord.type = "waypoint";
				coord.latitude = latitudeIntent;
				coord.longitude = longitudeIntent;
				coord.name = "some place";

				coordinates.add(coord);
				CacheOverlayItemImpl item = settings.getMapFactory().getCacheOverlayItem(coord, null);

				final int icon = base.getIcon(false, waypointTypeIntent, false, false, false);
				Drawable pin = null;
				if (iconsCache.containsKey(icon)) {
					pin = iconsCache.get(icon);
				} else {
					pin = getResources().getDrawable(icon);
					pin.setBounds(0, 0, pin.getIntrinsicWidth(), pin.getIntrinsicHeight());
					iconsCache.put(icon, pin);
				}
				item.setMarker(pin);

				overlayCaches.updateItems(item);
				displayHandler.sendEmptyMessage(1);

				cachesCnt = 1;
			} else {
				cachesCnt = 0;
			}

			displayHandler.sendEmptyMessage(0);
		}
	}

	// parent for those above :)
	private class DoThread extends Thread {

		protected boolean working = true;
		protected boolean stop = false;
		protected long centerLat = 0l;
		protected long centerLon = 0l;
		protected long spanLat = 0l;
		protected long spanLon = 0l;

		public DoThread(long centerLatIn, long centerLonIn, long spanLatIn, long spanLonIn) {
			centerLat = centerLatIn;
			centerLon = centerLonIn;
			spanLat = spanLatIn;
			spanLon = spanLonIn;
		}

		public synchronized boolean isWorking() {
			return working;
		}

		public synchronized void stopIt() {
			stop = true;
		}
	}

	// get if map is loading something
	private synchronized boolean isLoading() {
		boolean loading = false;

		if (loadThread != null && loadThread.isWorking()) {
			loading = true;
		} else if (downloadThread != null && downloadThread.isWorking()) {
			loading = true;
		} else if (displayThread != null && displayThread.isWorking()) {
			loading = true;
		}

		return loading;
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
			if (geocodes == null || geocodes.isEmpty()) {
				return;
			}

			if (dir != null) {
				dir = app.removeDir();
			}
			if (geo != null) {
				geo = app.removeGeo();
			}

			for (String geocode : geocodes) {
				try {
					if (stop == true) {
						break;
					}

					if (!app.isOffline(geocode, null)) {
						if ((System.currentTimeMillis() - last) < 1500) {
							try {
								int delay = 1000 + ((Double) (Math.random() * 1000)).intValue() - (int) (System.currentTimeMillis() - last);
								if (delay < 0) {
									delay = 500;
								}

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

	// center map to desired location
	private void centerMap(Double latitude, Double longitude) {
		if (latitude == null || longitude == null) {
			return;
		}
		if (mapView == null) {
			return;
		}

		if (!alreadyCentered) {
			alreadyCentered = true;

			mapController.setCenter(makeGeoPoint(latitude, longitude));
		} else {
			mapController.animateTo(makeGeoPoint(latitude, longitude));
		}
	}

	// move map to view results of searchIdIntent
	private void centerMap(String geocodeCenter, Long searchIdCenter, Double latitudeCenter, Double longitudeCenter) {
		if (!centered && (geocodeCenter != null || searchIdIntent != null)) {
			try {
				ArrayList<Object> viewport;

				if (geocodeCenter != null) {
					viewport = app.getBounds(geocodeCenter);
				} else {
					viewport = app.getBounds(searchIdCenter);
				}

				Integer cnt = (Integer) viewport.get(0);
				Integer minLat = null;
				Integer maxLat = null;
				Integer minLon = null;
				Integer maxLon = null;

				if (viewport.get(1) != null) {
					minLat = new Double((Double) viewport.get(1) * 1e6).intValue();
				}
				if (viewport.get(2) != null) {
					maxLat = new Double((Double) viewport.get(2) * 1e6).intValue();
				}
				if (viewport.get(3) != null) {
					maxLon = new Double((Double) viewport.get(3) * 1e6).intValue();
				}
				if (viewport.get(4) != null) {
					minLon = new Double((Double) viewport.get(4) * 1e6).intValue();
				}

				if (cnt == null || cnt <= 0 || minLat == null || maxLat == null || minLon == null || maxLon == null) {
					return;
				}

				int centerLat = 0;
				int centerLon = 0;

				if ((Math.abs(maxLat) - Math.abs(minLat)) != 0) {
					centerLat = minLat + ((maxLat - minLat) / 2);
				} else {
					centerLat = maxLat;
				}
				if ((Math.abs(maxLon) - Math.abs(minLon)) != 0) {
					centerLon = minLon + ((maxLon - minLon) / 2);
				} else {
					centerLon = maxLon;
				}

				if (cnt != null && cnt > 0) {
					mapController.setCenter(settings.getMapFactory().getGeoPointBase(centerLat, centerLon));
					if (Math.abs(maxLat - minLat) != 0 && Math.abs(maxLon - minLon) != 0) {
						mapController.zoomToSpan(Math.abs(maxLat - minLat), Math.abs(maxLon - minLon));
					}
				}
			} catch (Exception e) {
				// nothing at all
			}

			centered = true;
			alreadyCentered = true;
		} else if (!centered && latitudeCenter != null && longitudeCenter != null) {
			try {
				mapController.setCenter(makeGeoPoint(latitudeCenter, longitudeCenter));
			} catch (Exception e) {
				// nothing at all
			}

			centered = true;
			alreadyCentered = true;
		}
	}

	// switch My Location button image
	private void setMyLoc(Boolean status) {
		if (myLocSwitch == null) {
			myLocSwitch = (ImageView) activity.findViewById(R.id.my_position);
		}

		if (status == null) {
			if (followMyLocation == true) {
				myLocSwitch.setImageResource(R.drawable.my_location_on);
			} else {
				myLocSwitch.setImageResource(R.drawable.my_location_off);
			}
		} else {
			if (status == true) {
				myLocSwitch.setImageResource(R.drawable.my_location_on);
			} else {
				myLocSwitch.setImageResource(R.drawable.my_location_off);
			}
		}

		myLocSwitch.setOnClickListener(new MyLocationListener());
	}

	// set my location listener
	private class MyLocationListener implements View.OnClickListener {

		public void onClick(View view) {
			if (myLocSwitch == null) {
				myLocSwitch = (ImageView) activity.findViewById(R.id.my_position);
			}

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
	private GeoPointImpl makeGeoPoint(Double latitude, Double longitude) {
		return settings.getMapFactory().getGeoPointBase((int) (latitude * 1e6), (int) (longitude * 1e6));
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
