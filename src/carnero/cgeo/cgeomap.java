package carnero.cgeo;

import gnu.android.app.appmanualclient.*;

import android.app.Activity;
import android.app.ProgressDialog;
import java.util.ArrayList;
import android.os.Bundle;
import android.view.Menu;
import android.view.SubMenu;
import android.view.MenuItem;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.widget.TextView;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.lang.reflect.Field;
import java.util.Date;
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
	private cgUpdateLoc geoUpdate = new update();
	private cgUpdateDir dirUpdate = new updateDir();
	private boolean followLocation = false;
	private boolean initLocation = true;
	private cgMapOverlay overlay = null;
	private cgUsersOverlay overlayUsers = null;
	private cgOverlayScale overlayScale = null;
	private cgMapMyOverlay overlayMyLoc = null;
	private Drawable pin = null;
	private boolean fromDetail = false;
	private Double oneLatitude = null;
	private Double oneLongitude = null;
	private Long searchId = null;
	private String geocode = null;
	private Integer centerLatitude = null;
	private Integer centerLongitude = null;
	private Integer spanLatitude = null;
	private Integer spanLongitude = null;
	private Integer centerLatitudeUsers = null;
	private Integer centerLongitudeUsers = null;
	private Integer spanLatitudeUsers = null;
	private Integer spanLongitudeUsers = null;
	private ArrayList<cgCache> caches = new ArrayList<cgCache>();
	private ArrayList<cgCoord> coordinates = new ArrayList<cgCoord>();
	private ArrayList<cgUser> users = new ArrayList<cgUser>();
	private loadCaches loadingThread = null;
	private loadUsers usersThread = null;
	private long closeShowed = 0l;
	private int numberType = 0; // 0: altitude, 1: traveled distance
	private TextView numberView = null;
	private LinearLayout close = null;
	private TextView closeGC = null;
	private TextView closeDst = null;
	private ImageView myLocation = null;
	private ProgressDialog waitDialog = null;
	private int detailTotal = 0;
	private int detailProgress = 0;
	private Long detailProgressTime = 0l;
	private boolean firstRun = true;
	private geocachesLoadDetails threadD = null;
	private int closeCounter = 0;
	private String usertoken = null;
	protected boolean searching = false;
	protected boolean searchingUsers = false;
	protected boolean searchingForClose = false;
	protected boolean live = false;
	final private static HashMap<String, Integer> gcIconsClear = new HashMap<String, Integer>();
	final private static HashMap<String, Integer> gcIcons = new HashMap<String, Integer>();
	final private static HashMap<String, Integer> wpIcons = new HashMap<String, Integer>();

	final private Handler startLoading = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			changeTitle(true);
		}
	};

	final private Handler loadCacheFromDbHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				if (app != null && searchId != null && app.getError(searchId) != null && app.getError(searchId).length() > 0) {
					warning.showToast(res.getString(R.string.err_no_chaches));

					changeTitle(false);
					return;
				}

				addOverlays(true, true);
			} catch (Exception e) {
				Log.e(cgSettings.tag, "cgeomap.loadCacheFromDbHandler: " + e.toString());

				changeTitle(false);
			}
		}
	};

	final private Handler loadCachesHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				if (app != null && app.getError(searchId) != null && app.getError(searchId).length() > 0) {
					warning.showToast(res.getString(R.string.err_download_fail) + app.getError(searchId) + ".");

					searching = false;
					changeTitle(false);
					return;
				}

				addOverlays(true, false);
			} catch (Exception e) {
				Log.e(cgSettings.tag, "cgeomap.loadCachesHandler: " + e.toString());

				searching = false;
				changeTitle(false);
				return;
			} finally {
				searching = false;
			}
		}
	};

	final private Handler loadUsersHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				addOverlays(false, false);
			} catch (Exception e) {
				Log.e(cgSettings.tag, "cgeomap.loadUsersHandler: " + e.toString());

				searchingUsers = false;
				return;
			} finally {
				searchingUsers = false;
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

				if (geo == null) geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);
				if (settings.useCompass == 1 && dir == null) dir = app.startDir(activity, dirUpdate, warning);
            }
		}
	};

	final private Handler setCloseHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				if (close == null) close = (LinearLayout)findViewById(R.id.close);
				if (closeGC == null) closeGC = (TextView)findViewById(R.id.close_gc);
				if (closeDst == null) closeDst = (TextView)findViewById(R.id.close_dst);

				final int index = msg.what;
				if (geo == null || caches == null || caches.isEmpty() == true || index == -1 || caches.size() <= index) {
					if ((System.currentTimeMillis() - 5000) < closeShowed) {
						close.setVisibility(View.GONE);
						searchingForClose = false;
						return;
					}
				}

				cgCache cache = null;
				try { // probably trying to get cache that doesn't exist in list
					cache = caches.get(index);
				} catch (Exception e) {
					if ((System.currentTimeMillis() - 5000) < closeShowed) close.setVisibility(View.GONE);
					searchingForClose = false;
					return;
				}

				if (cache == null) {
					if ((System.currentTimeMillis() - 5000) < closeShowed) close.setVisibility(View.GONE);
					searchingForClose = false;

					return;
				}

				final Double distance = base.getDistance(geo.latitudeNow, geo.longitudeNow, cache.latitude, cache.longitude);

				close.setClickable(false);
				close.setOnClickListener(null);

				if (cache != null && geo != null && followLocation == true && geo.speedNow != null && geo.speedNow > 9) { // more than 9 m/s
					if (closeCounter < 5) {
						closeCounter ++;
					} else {
						closeShowed = System.currentTimeMillis();
						close.setVisibility(View.VISIBLE);

						if (geo != null) {
							closeDst.setText(base.getHumanDistance(distance));
						} else {
							closeDst.setText("---");
						}
						if (cache.name != null && cache.name.length() > 0) {
							closeGC.setText(cache.name);
						} else {
							closeGC.setText(cache.geocode);
						}
						if (cache.type != null && gcIcons.containsKey(cache.type) == true) { // cache icon
							closeGC.setCompoundDrawablesWithIntrinsicBounds((Drawable)activity.getResources().getDrawable(gcIconsClear.get(cache.type)), null, null, null);
						} else { // unknown cache type, "mystery" icon
							closeGC.setCompoundDrawablesWithIntrinsicBounds((Drawable)activity.getResources().getDrawable(gcIconsClear.get("mystery")), null, null, null);
						}
						close.setClickable(true);
						close.setOnClickListener(new closeClickListener(cache));

						close.bringToFront();

						closeCounter = 5;
					}
				} else {
					if (closeCounter > 0) {
						closeCounter --;
					} else {
						if (closeShowed < (System.currentTimeMillis() - (30 * 1000))) close.setVisibility(View.GONE);

						closeCounter = 0;
					}
				}
			} catch (Exception e) {
				Log.e(cgSettings.tag, "cgeomap.setCloseHandler.handleMessage: " + e.toString());
			}

			searchingForClose = false;
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// class init
		res = this.getResources();
		activity = this;
		app = (cgeoapplication)activity.getApplication();
		app.setAction(null);
		settings = new cgSettings(activity, getSharedPreferences(cgSettings.preferences, 0));
		base = new cgBase(app, settings, getSharedPreferences(cgSettings.preferences, 0));
		warning = new cgWarning(activity);
		prefsEdit = getSharedPreferences(cgSettings.preferences, 0).edit();

		// set layout
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		// set layout
		if (settings.skin == 1) {
			setTheme(R.style.light);
		} else {
			setTheme(R.style.dark);
		}
		setContentView(R.layout.map);
		base.setTitle(activity, res.getString(R.string.map_map));

		if (geo == null) {
			geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);
		}
		if (settings.useCompass == 1 && dir == null) {
			dir = app.startDir(activity, dirUpdate, warning);
		}

		mapView = (MapView)findViewById(R.id.map);
		mapController = mapView.getController();
		mapView.getOverlays().clear();

		if (overlayMyLoc == null) {
			overlayMyLoc = new cgMapMyOverlay(settings);
			mapView.getOverlays().add(overlayMyLoc);
		}

		// get parameters
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			fromDetail = extras.getBoolean("detail");
			searchId = extras.getLong("searchid");
			geocode = extras.getString("geocode");
			oneLatitude = extras.getDouble("latitude");
			oneLongitude = extras.getDouble("longitude");
		}

		if (settings.maptype == settings.mapSatellite) {
			mapView.setSatellite(true);
		} else {
			mapView.setSatellite(false);
		}
		mapView.setBuiltInZoomControls(true);
		mapView.displayZoomControls(true);

		mapController.setZoom(settings.mapzoom);

		if ((searchId == null || searchId <= 0) && (oneLatitude == null || oneLongitude == null)) {
			base.setTitle(activity, res.getString(R.string.map_live));
			searchId = null;
			live = true;
			initLocation = false;
			followLocation = true;

			loadingThread = new loadCaches(loadCachesHandler, mapView);
			loadingThread.enable();
			loadingThread.start();

			myLocationInMiddle();
		} else if (searchId != null && searchId > 0) {
			base.setTitle(activity, res.getString(R.string.map_map));
			live = false;
			initLocation = true;
			followLocation = false;

			(new loadCacheFromDb(loadCacheFromDbHandler)).start();
		} else if (geocode != null && geocode.length() > 0) {
			base.setTitle(activity, res.getString(R.string.map_map));
			live = false;
			initLocation = true;
			followLocation = false;

			(new loadCacheFromDb(loadCacheFromDbHandler)).start();
		} else if (oneLatitude != null && oneLongitude != null) {
			base.setTitle(activity, res.getString(R.string.map_map));
			searchId = null;
			live = false;
			initLocation = true;
			followLocation = false;

			addOverlays(true, true);
		}

		// google analytics
		if (live == false) {
			base.sendAnal(activity, "/map/normal");
		} else {
			base.sendAnal(activity, "/map/live");
		}

		if (myLocation == null) {
			myLocation = (ImageView)findViewById(R.id.my_position);
			if (followLocation == true) myLocation.setImageResource(R.drawable.my_location_on);
			else myLocation.setImageResource(R.drawable.my_location_off);
			myLocation.setOnClickListener(new myLocationListener());
		}

		usersThread = new loadUsers(loadUsersHandler, mapView);
		if (settings.publicLoc == 1) {
			usersThread.enable();
		} else {
			usersThread.disable();
		}
		usersThread.start();

		if (geo != null) {
			geoUpdate.updateLoc(geo);
		}
		if (dir != null) {
			dirUpdate.updateDir(dir);
		}

		if (numberView == null) {
			numberView = (TextView)findViewById(R.id.number);
		}
		numberView.setClickable(true);
		numberView.setOnClickListener(new changeNumber());
		numberView.setOnLongClickListener(new resetNumber());
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onResume() {
		super.onResume();

		app.setAction(null);
		if (geo == null) geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);
		if (settings.useCompass == 1 && dir == null) dir = app.startDir(activity, dirUpdate, warning);

		// restart loading threads
		if (loadingThread != null) loadingThread.kill();
		if (usersThread != null) usersThread.kill();

		if (live == true) {
			loadingThread = new loadCaches(loadCachesHandler, mapView);
			loadingThread.enable();
			loadingThread.start();
		}

		usersThread = new loadUsers(loadUsersHandler, mapView);
		if (settings.publicLoc == 1) {
			usersThread.enable();
		} else {
			usersThread.disable();
		}
		usersThread.start();

		if (geo != null) geoUpdate.updateLoc(geo);
		if (dir != null) dirUpdate.updateDir(dir);
	}

	@Override
	public void onStop() {
		if (dir != null) dir = app.removeDir();
		if (geo != null) geo = app.removeGeo();

		savePrefs();

		if (mapView != null) mapView.destroyDrawingCache();

		if (loadingThread != null) loadingThread.kill();
		if (usersThread != null) usersThread.kill();

		super.onStop();
	}

	@Override
	public void onPause() {
		if (loadingThread != null) loadingThread.kill();
		if (usersThread != null) usersThread.kill();

		if (dir != null) dir = app.removeDir();
		if (geo != null) geo = app.removeGeo();

		savePrefs();

		if (mapView != null) mapView.destroyDrawingCache();

		super.onPause();
	}

	@Override
	public void onDestroy() {
		if (loadingThread != null) loadingThread.kill();
		if (usersThread != null) usersThread.kill();

		if (dir != null) dir = app.removeDir();
		if (geo != null) geo = app.removeGeo();

		savePrefs();

		if (mapView != null) {
			mapView.destroyDrawingCache();
			mapView = null;
		}

		try {
			// clean up tiles from memory
			Class<?> tileClass = Class.forName("com.google.googlenav.map.Tile");
			Field fTileCache = tileClass.getDeclaredField("tileObjectCache");
			fTileCache.setAccessible(true);
			Object[] tileObjectCache = (Object[]) fTileCache.get(null);
			for (int i = 0; i < tileObjectCache.length; i++) {
				tileObjectCache[i] = null;
			}
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgeomap.onDestroy: " + e.toString());
		}

		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 2, 0, res.getString(R.string.map_trail_hide)).setIcon(android.R.drawable.ic_menu_recent_history);
		menu.add(0, 3, 0, res.getString(R.string.map_live_disable)).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		menu.add(0, 4, 0, res.getString(R.string.caches_store_offline)).setIcon(android.R.drawable.ic_menu_set_as).setEnabled(false);
		menu.add(0, 0, 0, res.getString(R.string.caches_on_map)).setIcon(android.R.drawable.ic_menu_mapmode);

		SubMenu subMenu = menu.addSubMenu(0, 5, 0, res.getString(R.string.caches_select)).setIcon(android.R.drawable.ic_menu_myplaces);
		if (coordinates.size() > 0) {
			int cnt = 6;
			for (cgCoord coordinate : coordinates) {
				subMenu.add(0, cnt, 0, Html.fromHtml(coordinate.name) + " (" + coordinate.type + ")");
				cnt ++;
			}
		}

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		MenuItem item;
		try {
			item = menu.findItem(0); // view
			if (mapView != null && mapView.isSatellite() == false) item.setTitle(res.getString(R.string.map_view_satellite));
			else item.setTitle(res.getString(R.string.map_view_map));

			item = menu.findItem(2); // show trail
			if (settings.maptrail == 1) item.setTitle(res.getString(R.string.map_trail_hide));
			else item.setTitle(res.getString(R.string.map_trail_show));

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
			if ((searchId != null && app.getNotOfflineCount(searchId) > 0) && caches != null && caches.size() > 0 && searching == false) {
				item.setEnabled(true);
			} else {
				item.setEnabled(false);
			}

			item = menu.findItem(5);
			item.setEnabled(false);

			SubMenu subMenu = item.getSubMenu();
			subMenu.clear();
			if (coordinates.size() > 0) {
				int cnt = 6;
				for (cgCoord coordinate : coordinates) {
					subMenu.add(0, cnt, 0, Html.fromHtml(coordinate.name) + " (" + coordinate.type + ")");
					cnt ++;
				}
				item.setEnabled(true);
			}
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgeomap.onPrepareOptionsMenu: " + e.toString());
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == 0) {
			if (mapView != null && mapView.isSatellite() == false) {
				mapView.setSatellite(true);

				prefsEdit.putInt("maptype", settings.mapSatellite);
				prefsEdit.commit();
			} else {
				mapView.setSatellite(false);

				prefsEdit.putInt("maptype", settings.mapClassic);
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
				dismissClose();

				settings.liveMapDisable();
			} else {
				dismissClose();

				settings.liveMapEnable();
			}

			// reset last viewport to force load caches imediatelly
			centerLatitude = null;
			centerLongitude = null;
			spanLatitude = null;
			spanLongitude = null;
		} else if (id == 4) {
			ArrayList<String> geocodes = new ArrayList<String>();

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
			waitDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface arg0) {
					try {
						if (threadD != null) threadD.kill();

						if (geo == null) geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);
						if (settings.useCompass == 1 && dir == null) dir = app.startDir(activity, dirUpdate, warning);
					} catch (Exception e) {
						Log.e(cgSettings.tag, "cgeocaches.onPrepareOptionsMenu.onCancel: " + e.toString());
					}
				}
			});
			waitDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			Float etaTime = new Float((detailTotal * (float)7) / 60);
			if (etaTime < 0.4) {
				waitDialog.setMessage(res.getString(R.string.caches_downloading) + " " + res.getString(R.string.caches_eta_ltm));
			} else if (etaTime < 1.5){
				waitDialog.setMessage(res.getString(R.string.caches_downloading) + " " + String.format(Locale.getDefault(), "%.0f", etaTime) + " " + res.getString(R.string.caches_eta_min));
			} else {
				waitDialog.setMessage(res.getString(R.string.caches_downloading) + " " + String.format(Locale.getDefault(), "%.0f", etaTime) + " " + res.getString(R.string.caches_eta_mins));
			}
			waitDialog.setCancelable(true);
			waitDialog.setMax(detailTotal);
			waitDialog.show();

			detailProgressTime = System.currentTimeMillis();

			threadD = new geocachesLoadDetails(loadDetailsHandler, geocodes);
			threadD.start();

			return true;
		} else if (id > 5 && coordinates.get(id - 6) != null) {
			try {
				cgCoord coordinate = coordinates.get(id - 6);

				followLocation = false;
				centerMap(coordinate.latitude, coordinate.longitude);
			} catch (Exception e) {
				Log.e(cgSettings.tag, "cgeomap.onOptionsItemSelected: " + e.toString());
			}

			return true;
		}

		return false;
	}

	private void savePrefs() {
		if (mapView == null) return;
		if (prefsEdit == null) prefsEdit = getSharedPreferences(cgSettings.preferences, 0).edit();

		if (mapView.isSatellite()) prefsEdit.putInt("maptype", settings.mapSatellite);
		else prefsEdit.putInt("maptype", settings.mapClassic);

		prefsEdit.putInt("mapzoom", mapView.getZoomLevel());
		prefsEdit.commit();

		if (mapView.isSatellite()) settings.maptype = settings.mapSatellite;
		else settings.maptype = settings.mapClassic;
	}

	private void addOverlays(boolean canChangeTitle, boolean canInit) {
		// scale bar
		if (overlayScale == null && mapView != null) {
			overlayScale = new cgOverlayScale(activity, base, settings);
			mapView.getOverlays().add(overlayScale);
		}
		if (mapView.getOverlays().contains(overlayScale) == false) {
			mapView.getOverlays().add(overlayScale);
		}

		mapView.invalidate();

		// users
		if (mapView == null) return;

		// init images
		if (gcIconsClear.isEmpty() == true) {
			gcIconsClear.put("ape", R.drawable.type_ape);
			gcIconsClear.put("cito", R.drawable.type_cito);
			gcIconsClear.put("earth", R.drawable.type_earth);
			gcIconsClear.put("event", R.drawable.type_event);
			gcIconsClear.put("letterbox", R.drawable.type_letterbox);
			gcIconsClear.put("locationless", R.drawable.type_locationless);
			gcIconsClear.put("mega", R.drawable.type_mega);
			gcIconsClear.put("multi", R.drawable.type_multi);
			gcIconsClear.put("traditional", R.drawable.type_traditional);
			gcIconsClear.put("virtual", R.drawable.type_virtual);
			gcIconsClear.put("webcam", R.drawable.type_webcam);
			gcIconsClear.put("wherigo", R.drawable.type_wherigo);
			gcIconsClear.put("mystery", R.drawable.type_mystery);
			gcIconsClear.put("gchq", R.drawable.type_hq);
		}

		if (gcIcons.isEmpty() == true) {
			gcIcons.put("ape", R.drawable.marker_cache_ape);
			gcIcons.put("cito", R.drawable.marker_cache_cito);
			gcIcons.put("earth", R.drawable.marker_cache_earth);
			gcIcons.put("event", R.drawable.marker_cache_event);
			gcIcons.put("letterbox", R.drawable.marker_cache_letterbox);
			gcIcons.put("locationless", R.drawable.marker_cache_locationless);
			gcIcons.put("mega", R.drawable.marker_cache_mega);
			gcIcons.put("multi", R.drawable.marker_cache_multi);
			gcIcons.put("traditional", R.drawable.marker_cache_traditional);
			gcIcons.put("virtual", R.drawable.marker_cache_virtual);
			gcIcons.put("webcam", R.drawable.marker_cache_webcam);
			gcIcons.put("wherigo", R.drawable.marker_cache_wherigo);
			gcIcons.put("mystery", R.drawable.marker_cache_mystery);
			gcIcons.put("gchq", R.drawable.marker_cache_gchq);
			gcIcons.put("ape-found", R.drawable.marker_cache_ape_found);
			gcIcons.put("cito-found", R.drawable.marker_cache_cito_found);
			gcIcons.put("earth-found", R.drawable.marker_cache_earth_found);
			gcIcons.put("event-found", R.drawable.marker_cache_event_found);
			gcIcons.put("letterbox-found", R.drawable.marker_cache_letterbox_found);
			gcIcons.put("locationless-found", R.drawable.marker_cache_locationless_found);
			gcIcons.put("mega-found", R.drawable.marker_cache_mega_found);
			gcIcons.put("multi-found", R.drawable.marker_cache_multi_found);
			gcIcons.put("traditional-found", R.drawable.marker_cache_traditional_found);
			gcIcons.put("virtual-found", R.drawable.marker_cache_virtual_found);
			gcIcons.put("webcam-found", R.drawable.marker_cache_webcam_found);
			gcIcons.put("wherigo-found", R.drawable.marker_cache_wherigo_found);
			gcIcons.put("mystery-found", R.drawable.marker_cache_mystery_found);
			gcIcons.put("gchq-found", R.drawable.marker_cache_gchq_found);
			gcIcons.put("ape-disabled", R.drawable.marker_cache_ape_disabled);
			gcIcons.put("cito-disabled", R.drawable.marker_cache_cito_disabled);
			gcIcons.put("earth-disabled", R.drawable.marker_cache_earth_disabled);
			gcIcons.put("event-disabled", R.drawable.marker_cache_event_disabled);
			gcIcons.put("letterbox-disabled", R.drawable.marker_cache_letterbox_disabled);
			gcIcons.put("locationless-disabled", R.drawable.marker_cache_locationless_disabled);
			gcIcons.put("mega-disabled", R.drawable.marker_cache_mega_disabled);
			gcIcons.put("multi-disabled", R.drawable.marker_cache_multi_disabled);
			gcIcons.put("traditional-disabled", R.drawable.marker_cache_traditional_disabled);
			gcIcons.put("virtual-disabled", R.drawable.marker_cache_virtual_disabled);
			gcIcons.put("webcam-disabled", R.drawable.marker_cache_webcam_disabled);
			gcIcons.put("wherigo-disabled", R.drawable.marker_cache_wherigo_disabled);
			gcIcons.put("mystery-disabled", R.drawable.marker_cache_mystery_disabled);
			gcIcons.put("gchq-disabled", R.drawable.marker_cache_gchq_disabled);
		}

		if (wpIcons.isEmpty() == true) {
			wpIcons.put("waypoint", R.drawable.marker_waypoint_waypoint);
			wpIcons.put("flag", R.drawable.marker_waypoint_flag);
			wpIcons.put("pkg", R.drawable.marker_waypoint_pkg);
			wpIcons.put("puzzle", R.drawable.marker_waypoint_puzzle);
			wpIcons.put("stage", R.drawable.marker_waypoint_stage);
			wpIcons.put("trailhead", R.drawable.marker_waypoint_trailhead);
		}

		if (settings.publicLoc == 1 && users != null && users.isEmpty() == false) {
			if (overlayUsers == null) {
				overlayUsers = new cgUsersOverlay(app, (Context)this, base, getResources().getDrawable(R.drawable.user_location));
			} else {
				overlayUsers.disableTap();
				overlayUsers.clearItems();
			}

			for (cgUser user : users) {
				if (user.latitude == null && user.longitude == null) continue;

				final cgOverlayUser item = new cgOverlayUser(activity, user);

				pin = getResources().getDrawable(R.drawable.user_location);
				pin.setBounds(0, 0, pin.getIntrinsicWidth(), pin.getIntrinsicHeight());
				item.setMarker(pin);

				overlayUsers.addItem(item);
			}

			if (mapView.getOverlays().contains(overlayUsers) == false) {
				mapView.getOverlays().add(overlayUsers);
			}

			mapView.invalidate();
			overlayUsers.enableTap();
		}

		searchingUsers = false;

		// geocaches
		if (overlay == null) {
			overlay = new cgMapOverlay(app, (Context)this, base, getResources().getDrawable(R.drawable.marker), fromDetail);
		} else {
			overlay.disableTap();
			overlay.clearItems();
		}

		Integer maxLat = Integer.MIN_VALUE;
		Integer minLat = Integer.MAX_VALUE;
		Integer maxLon = Integer.MIN_VALUE;
		Integer minLon = Integer.MAX_VALUE;

		GeoPoint geopoint = null;
		int cachesWithCoords = 0;

		coordinates.clear();
		if (caches != null && caches.size() > 0) {
			for (cgCache cache : caches) {
				if (cache.latitude == null && cache.longitude == null) continue;
				else cachesWithCoords ++;

				String type = null;

				if (cache.found == true) {
					type = cache.type + "-found";
				} else if (cache.disabled == true) {
					type = cache.type + "-disabled";
				} else {
					type = cache.type;
				}

				if (type != null && gcIcons.containsKey(type) == true) {
					pin = getResources().getDrawable(gcIcons.get(type));
				} else {
					pin = getResources().getDrawable(gcIcons.get("mystery"));
				}

				final cgCoord coord = new cgCoord(cache);

				coordinates.add(coord);
				final cgOverlayItem item = new cgOverlayItem(coord);

				pin.setBounds(0, 0, pin.getIntrinsicWidth(), pin.getIntrinsicHeight());
				item.setMarker(pin);

				overlay.addItem(item);

				final int latitudeE6 = (int)(cache.latitude * 1e6);
				final int longitudeE6 = (int)(cache.longitude * 1e6);

				if (latitudeE6 > maxLat) maxLat = latitudeE6;
				if (latitudeE6 < minLat) minLat = latitudeE6;
				if (longitudeE6 > maxLon) maxLon = longitudeE6;
				if (longitudeE6 < minLon) minLon = longitudeE6;
			}

			if (cachesWithCoords == 0) {
				warning.showToast(res.getString(R.string.warn_no_cache_coord));
				myLocationInMiddleForce();
			}

			if (live == false) {
				// there is only one cache
				if (caches != null && caches.size() == 1 && cachesWithCoords > 0) {
					cgCache oneCache = caches.get(0);

					maxLat = (int)(oneCache.latitude * 1e6);
					minLat = (int)(oneCache.latitude * 1e6);
					maxLon = (int)(oneCache.longitude * 1e6);
					minLon = (int)(oneCache.longitude * 1e6);

					// waypoints
					if (oneCache != null && oneCache.waypoints != null && oneCache.waypoints.size() > 0) {
						for (cgWaypoint waypoint : oneCache.waypoints) {
							if (waypoint.latitude == null && waypoint.longitude == null) continue;

							if (waypoint.type != null && wpIcons.containsKey(waypoint.type) == true) {
								pin = getResources().getDrawable(wpIcons.get(waypoint.type));
							} else {
								pin = getResources().getDrawable(wpIcons.get("waypoint"));
							}

							cgCoord coord = new cgCoord(waypoint);

							coordinates.add(coord);
							cgOverlayItem item = new cgOverlayItem(coord);

							pin.setBounds(0, 0, pin.getIntrinsicWidth(), pin.getIntrinsicHeight());
							item.setMarker(pin);

							overlay.addItem(item);

							int latitudeE6 = (int)(waypoint.latitude * 1e6);
							int longitudeE6 = (int)(waypoint.longitude * 1e6);

							if (latitudeE6 > maxLat) maxLat = latitudeE6;
							if (latitudeE6 < minLat) minLat = latitudeE6;
							if (longitudeE6 > maxLon) maxLon = longitudeE6;
							if (longitudeE6 < minLon) minLon = longitudeE6;

							coord = null;
						}
					}

					int centerLat = 0;
					int centerLon = 0;
					if (coordinates.size() > 1) {
						if ((Math.abs(maxLat) - Math.abs(minLat)) != 0) centerLat = minLat + ((maxLat - minLat) / 2);
						if ((Math.abs(maxLon) - Math.abs(minLon)) != 0) centerLon = minLon + ((maxLon - minLon) / 2);
					} else {
						centerLat = (int)(oneCache.latitude * 1e6);
						centerLon = (int)(oneCache.longitude * 1e6);
					}

					if (canInit == true && initLocation == true) {
						mapController.animateTo(new GeoPoint(centerLat, centerLon));
						if (Math.abs(maxLat - minLat) != 0 && Math.abs(maxLon - minLon) != 0) mapController.zoomToSpan(Math.abs(maxLat - minLat), Math.abs(maxLon - minLon));
						initLocation = false;
					}
				} else {
					int centerLat = 0;
					int centerLon = 0;
					if ((Math.abs(maxLat) - Math.abs(minLat)) != 0) centerLat = minLat + ((maxLat - minLat) / 2);
					if ((Math.abs(maxLon) - Math.abs(minLon)) != 0) centerLon = minLon + ((maxLon - minLon) / 2);

					if ((canInit == true || initLocation == true) && cachesWithCoords > 0) {
						mapController.animateTo(new GeoPoint(centerLat, centerLon));
						if (Math.abs(maxLat - minLat) != 0 && Math.abs(maxLon - minLon) != 0) mapController.zoomToSpan(Math.abs(maxLat - minLat), Math.abs(maxLon - minLon));
						initLocation = false;
					}
				}
			}
		} else if (oneLatitude != null && oneLongitude != null) {
			pin = getResources().getDrawable(wpIcons.get("waypoint"));

			cgCoord coord = new cgCoord();
			coord.type = "waypoint";
			coord.latitude = oneLatitude;
			coord.longitude = oneLongitude;
			coord.name = "some place";

			coordinates.add(coord);
			cgOverlayItem item = new cgOverlayItem(coord);

			pin.setBounds(0, 0, pin.getIntrinsicWidth(), pin.getIntrinsicHeight());
			item.setMarker(pin);

			overlay.addItem(item);

			geopoint = new GeoPoint((int)(oneLatitude * 1e6), (int)(oneLongitude * 1e6));

			if (canInit == true || initLocation == true) {
				mapController.animateTo(geopoint);
				initLocation = false;
			}
		}

		if (mapView.getOverlays().contains(overlay) == false) {
			mapView.getOverlays().add(overlay);
		}

		mapView.invalidate();
		overlay.enableTap();

		searching = false;
		if (canChangeTitle == true) changeTitle(false);
	}

	private void myLocationInMiddle() {
		if (followLocation == false && initLocation == false) return;
		if (geo == null) return;

		centerMap(geo.latitudeNow, geo.longitudeNow);

		if (initLocation == true) initLocation = false;
	}

	private void myLocationInMiddleForce() {
		if (geo == null) return;

		centerMap(geo.latitudeNow, geo.longitudeNow);
	}

	private void centerMap(Double latitude, Double longitude) {
		if (latitude == null || longitude == null) return;
		if (mapView == null) return;

		mapController.animateTo(new GeoPoint((int)(latitude * 1e6), (int)(longitude * 1e6)));
	}

	private class update extends cgUpdateLoc {
		@Override
		public void updateLoc(cgGeo geo) {
			if (geo == null) return;

			try {
				if (overlayMyLoc == null && mapView != null) {
					overlayMyLoc = new cgMapMyOverlay(settings);
					mapView.getOverlays().add(overlayMyLoc);
				}

				if (overlayMyLoc != null && geo.location != null) overlayMyLoc.setCoordinates(geo.location);

				(new findClose()).start();

				if (geo.latitudeNow != null && geo.longitudeNow != null) {
					if (followLocation == true) myLocationInMiddle();
				}

				if (settings.useCompass == 0 || (geo.speedNow != null && geo.speedNow > 5)) { // use GPS when speed is higher than 18 km/h
					if (geo.bearingNow != null) overlayMyLoc.setHeading(geo.bearingNow);
					else overlayMyLoc.setHeading(new Double(0));
				}
			} catch (Exception e) {
				Log.w(cgSettings.tag, "Failed to update location.");
			}

			setNumber();
		}
	}

	private class updateDir extends cgUpdateDir {
		@Override
		public void updateDir(cgDirection dir) {
			if (dir == null || dir.directionNow == null) return;

			if (overlayMyLoc != null && mapView != null && (geo == null || geo.speedNow == null || geo.speedNow <= 5)) { // use compass when speed is lower than 18 km/h
				overlayMyLoc.setHeading(dir.directionNow);
				mapView.invalidate();
			}
		}
	}

	private class loadCacheFromDb extends Thread {
		private Handler handler = null;

		private loadCacheFromDb(Handler handlerIn) {
			handler = handlerIn;
		}

		@Override
		public void run() {
			startLoading.sendEmptyMessage(0);

			if (searchId != null) {
				caches = app.getCaches(searchId, false, true, false, false, false, false);
			}

			if (geocode != null && geocode.length() > 0) {
				caches = new ArrayList<cgCache>();
				caches.add(app.getCacheByGeocode(geocode, false, true, false, false, false, false));
			}
			handler.sendMessage(new Message());
		}
	}

	// thread used just like timer
	private class loadCaches extends Thread {
		private boolean requestedKill = false;
		private boolean enabled = true;
		private Handler handler = null;
		private String viewstate = null;
		private MapView mapView = null;

		private loadCaches(Handler handlerIn, MapView mapViewIn) {
			handler = handlerIn;
			mapView = mapViewIn;
		}

		protected void kill() {
			requestedKill = true;
			usertoken = null;
		}

		protected void enable() {
			enabled = true;
			usertoken = null;
		}

		protected void disable() {
			enabled = false;
			usertoken = null;
		}

		public boolean state() {
			return enabled;
		}

		protected void setViewstate(String viewstateIn) {
			viewstate = viewstateIn;
		}

		@Override
		public void run() {
			while (requestedKill == false) {
				try {
					if (firstRun == false) {
						sleep(700);
					} else {
						firstRun = false;
					}

					if (enabled == true && mapView != null && searching == false) {
						loadCachesReal realThread = new loadCachesReal(handler, mapView, viewstate);
						realThread.start();
					}
				} catch (Exception e) {
					Log.e(cgSettings.tag, "cgeomap.loadCaches: " + e.toString());
				}
			}
		}
	}

	// thread that is downloading caches
	private class loadCachesReal extends Thread {
		private Handler handler = null;
		private String viewstate = null;
		private MapView mapView = null;
		private Double latitudeT = null;
		private Double latitudeB = null;
		private Double longitudeL = null;
		private Double longitudeR = null;

		private loadCachesReal(Handler handlerIn, MapView mapViewIn, String viewstateIn) {
			handler = handlerIn;
			viewstate = viewstateIn;
			mapView = mapViewIn;
		}

		@Override
		public void run() {
			GeoPoint center = mapView.getMapCenter();
			int latitudeCenter = center.getLatitudeE6();
			int longitudeCenter = center.getLongitudeE6();
			int latitudeSpan = mapView.getLatitudeSpan();
			int longitudeSpan = mapView.getLongitudeSpan();

			if (
					(centerLatitude == null || centerLongitude == null || spanLatitude == null || spanLongitude == null) || // first run
					((
						(Math.abs(latitudeSpan - spanLatitude) > 50) || // changed zoom
						(Math.abs(longitudeSpan - spanLongitude) > 50) || // changed zoom
						(Math.abs(latitudeCenter - centerLatitude) > (latitudeSpan / 6)) || // map moved
						(Math.abs(longitudeCenter - centerLongitude) > (longitudeSpan / 6)) // map moved
					) && (
						base.isInViewPort(centerLatitude, centerLongitude, latitudeCenter, longitudeCenter, spanLatitude, spanLongitude, latitudeSpan, longitudeSpan) == false ||
						caches.isEmpty() == true
					))
				) {

				latitudeT = (latitudeCenter + (latitudeSpan / 2) + (latitudeSpan / 10)) / 1e6;
				latitudeB = (latitudeCenter - (latitudeSpan / 2) - (latitudeSpan / 10)) / 1e6;
				longitudeL = (longitudeCenter + (longitudeSpan / 2) + (longitudeSpan / 10)) / 1e6;
				longitudeR = (longitudeCenter - (longitudeSpan / 2) - (longitudeSpan / 10)) / 1e6;

				centerLatitude = latitudeCenter;
				centerLongitude = longitudeCenter;
				spanLatitude = latitudeSpan;
				spanLongitude = longitudeSpan;

				if (searching == false) {
					searching = true;
					startLoading.sendEmptyMessage(0);

					if (settings.maplive == 1) { // live map - downloads caches from gc.com
						if (usertoken == null) {
							usertoken = base.getMapUserToken();
						}

						HashMap<String, String> params = new HashMap<String, String>();
						params.put("usertoken", usertoken);
						params.put("latitude-t", String.format((Locale)null, "%.6f", latitudeT));
						params.put("latitude-b", String.format((Locale)null, "%.6f", latitudeB));
						params.put("longitude-l", String.format((Locale)null, "%.6f", longitudeL));
						params.put("longitude-r", String.format((Locale)null, "%.6f", longitudeR));

						Log.i(cgSettings.tag, "Starting download caches for: " + String.format((Locale)null, "%.6f", latitudeT) + "," + String.format((Locale)null, "%.6f", longitudeL) + " | " + String.format((Locale)null, "%.6f", latitudeB) + "," + String.format((Locale)null, "%.6f", longitudeR));

						searchId = base.searchByViewport(params, 0);

						if (searchId != null && searchId > 0) {
							if (loadingThread != null && app.getViewstate(searchId) != null) {
								loadingThread.setViewstate(app.getViewstate(searchId));
							}

							caches.clear();
							if (app.getCount(searchId) > 0) {
								caches.addAll(app.getCaches(searchId, false, false, false, false, false, false));
							}
						}
					} else { // dead map - uses stored caches
						Log.i(cgSettings.tag, "Starting load offline caches for: " + String.format((Locale)null, "%.6f", latitudeT) + "," + String.format((Locale)null, "%.6f", longitudeL) + " | " + String.format((Locale)null, "%.6f", latitudeB) + "," + String.format((Locale)null, "%.6f", longitudeR));

						searchId = app.getOfflineInViewport(latitudeT, longitudeL, latitudeB, longitudeR, settings.cacheType);

						if (searchId != null && searchId > 0) {
							caches.clear();
							if (app.getCount(searchId) > 0) {
								caches.addAll(app.getCaches(searchId, false, false, false, false, false, false));
							}
						}
					}

					Log.i(cgSettings.tag, "Caches found: " + caches.size());

					handler.sendEmptyMessage(0);
				}
			}
		}
	}

	private class loadUsers extends Thread {
		private boolean requestedKill = false;
		private boolean enabled = true;
		private Handler handler = null;
		private MapView mapView = null;
		private Double latitudeT = null;
		private Double latitudeB = null;
		private Double longitudeL = null;
		private Double longitudeR = null;

		protected void kill() {
			requestedKill = true;
		}

		protected void enable() {
			enabled = true;
		}

		protected void disable() {
			enabled = false;
		}

		public boolean state() {
			return enabled;
		}

		private loadUsers(Handler handlerIn, MapView mapViewIn) {
			setPriority(Thread.MIN_PRIORITY);

			handler = handlerIn;
			mapView = mapViewIn;
		}

		@Override
		public void run() {
			while (requestedKill == false) {
				try {
					sleep(500);
				} catch (Exception e) {
					// nothing
				}

				if (enabled == true && mapView != null) {
					GeoPoint center = mapView.getMapCenter();
					int latitudeCenter = center.getLatitudeE6();
					int longitudeCenter = center.getLongitudeE6();
					int latitudeSpan = mapView.getLatitudeSpan();
					int longitudeSpan = mapView.getLongitudeSpan();

					if (
							(centerLatitudeUsers == null || centerLongitudeUsers == null || spanLatitudeUsers == null || spanLongitudeUsers == null) || // first run
							((
								(Math.abs(latitudeSpan - spanLatitudeUsers) > 50) || // changed zoom
								(Math.abs(longitudeSpan - spanLongitudeUsers) > 50) || // changed zoom
								(Math.abs(latitudeCenter - centerLatitudeUsers) > (latitudeSpan / 6)) || // map moved
								(Math.abs(longitudeCenter - centerLongitudeUsers) > (longitudeSpan / 6)) // map moved
							) && (
								base.isInViewPort(centerLatitudeUsers, centerLongitudeUsers, latitudeCenter, longitudeCenter, spanLatitudeUsers, spanLongitudeUsers, latitudeSpan, longitudeSpan) == false ||
								users == null || users.isEmpty() == true
							))
						) {

						latitudeT = (latitudeCenter + (latitudeSpan / 2)) / 1e6;
						latitudeB = (latitudeCenter - (latitudeSpan / 2)) / 1e6;
						longitudeL = (longitudeCenter + (longitudeSpan / 2)) / 1e6;
						longitudeR = (longitudeCenter - (longitudeSpan / 2)) / 1e6;

						centerLatitudeUsers = latitudeCenter;
						centerLongitudeUsers = longitudeCenter;
						spanLatitudeUsers = latitudeSpan;
						spanLongitudeUsers = longitudeSpan;

						if (searchingUsers == false) {
							Log.i(cgSettings.tag, "Starting download other users for: " + String.format((Locale)null, "%.6f", latitudeT) + "," + String.format((Locale)null, "%.6f", longitudeL) + " | " + String.format((Locale)null, "%.6f", latitudeB) + "," + String.format((Locale)null, "%.6f", longitudeR));

							searchingUsers = true;
							users = base.usersInViewport(settings.getUsername(), latitudeB, latitudeT, longitudeR, longitudeL);
						}

						handler.sendEmptyMessage(0);
					}
				}
			}
		}
	}

	private class findClose extends Thread {
		public findClose() {
			setPriority(Thread.MIN_PRIORITY);
		}

		@Override
		public void run() {
			if (searchingForClose == true) return;

			searchingForClose = true;
			try {
				double distance = 0d;
				double closestDistance = Double.POSITIVE_INFINITY;
				int closestCache = -1;

				if (geo != null && caches != null && caches.isEmpty() == false) {
					for (cgCache oneCache : caches) {
						distance = base.getDistance(geo.latitudeNow, geo.longitudeNow, oneCache.latitude, oneCache.longitude);
						if (live == true && geo != null && distance < closestDistance) {
							closestDistance = distance;
							closestCache = caches.indexOf(oneCache);
						}
					}
				}

				setCloseHandler.sendEmptyMessage(closestCache);
			} catch (Exception e) {
				Log.e(cgSettings.tag, "cgeocaches.findClose.run: " + e.toString());
			}
		}
	}

	private class closeClickListener implements View.OnClickListener {
		private cgCache cache = null;

		public closeClickListener(cgCache cacheIn) {
			cache = cacheIn;
		}

		public void onClick(View arg0) {
			if (cache == null) return;

			Intent cacheIntent = new Intent(activity, cgeodetail.class);
			cacheIntent.putExtra("geocode", cache.geocode.toUpperCase());
			activity.startActivity(cacheIntent);
		}
	}

	public void dismissClose() {
		if (close == null) close = (LinearLayout)findViewById(R.id.close);
		close.setVisibility(View.GONE);
	}

	private class geocachesLoadDetails extends Thread {
		private Handler handler = null;
		private ArrayList<String> geocodes = null;
        private volatile Boolean needToStop = false;

		public geocachesLoadDetails(Handler handlerIn, ArrayList<String> geocodesIn) {
			handler = handlerIn;
			geocodes = geocodesIn;
		}

        public void kill() {
            this.needToStop = true;
        }

		@Override
		public void run() {
			if (geocodes == null || geocodes.isEmpty()) return;
			if (dir != null) dir = app.removeDir();
			if (geo != null) geo = app.removeGeo();

			Message msg = null;

			for (String geocode : geocodes) {
				try {
					if (needToStop == true) {
						Log.i(cgSettings.tag, "Stopped storing process.");
						break;
					}

					try {
						sleep(3000 + ((Double)(Math.random() * 3000)).intValue());
					} catch (Exception e) {
						Log.e(cgSettings.tag, "cgeomap.geocachesLoadDetails.sleep: " + e.toString());
					}

					if (needToStop == true) {
						Log.i(cgSettings.tag, "Stopped storing process.");
						break;
					}

					detailProgress ++;
					base.storeCache(app, activity, null, geocode, handler);

					msg = new Message();
					msg.what = 0;
					handler.sendMessage(msg);
				} catch (Exception e) {
					Log.e(cgSettings.tag, "cgeocaches.geocachesLoadDetails: " + e.toString());
				}

				yield();
			}

			msg = new Message();
			msg.what = 1;
			handler.sendMessage(msg);
		}
	}

	protected void changeTitle(boolean loading) {
		String title = null;
		if (live == true) title = res.getString(R.string.map_live);
		else title = res.getString(R.string.map_map);

		if (loading == true) {
			base.showProgress(activity, true);
			base.setTitle(activity, title);
		} else if (caches != null) {
			base.showProgress(activity, false);
			base.setTitle(activity, title + " (" + caches.size() + ")");
		} else {
			base.showProgress(activity, false);
			base.setTitle(activity, title + " " + res.getString(R.string.caches_no_caches));
		}
	}

	private class myLocationListener implements View.OnClickListener {
		public void onClick(View view) {
			if (myLocation == null) myLocation = (ImageView)findViewById(R.id.my_position);

			if (followLocation == true) {
				followLocation = false;

				myLocation.setImageResource(R.drawable.my_location_off);
			} else {
				followLocation = true;
				myLocationInMiddle();

				myLocation.setImageResource(R.drawable.my_location_on);
			}
		}
	}

	public void setNumber() {
		try {
			if (numberView == null) numberView = (TextView)findViewById(R.id.number);

			if (numberType >= 0 && numberView.getVisibility() != View.VISIBLE) numberView.setVisibility(View.VISIBLE);
			else if (numberType < 0 && numberView.getVisibility() != View.GONE) numberView.setVisibility(View.GONE);

			if (numberType == 0 && geo != null) { // altitude
				String humanAlt;
				if (geo.altitudeNow != null) {
					if (settings.units == settings.unitsImperial) {
						humanAlt = String.format("%.0f", (geo.altitudeNow * 3.2808399)) + " ft";
					} else {
						humanAlt = String.format("%.0f", geo.altitudeNow) + " m";
					}
				} else {
					humanAlt = "N/A";
				}
				numberView.setText(humanAlt);
				numberView.bringToFront();
			} else if (numberType == 1 && geo != null) { // travaled distance
				numberView.setText(base.getHumanDistance(geo.distanceNow));
				numberView.bringToFront();
			}
		} catch (Exception e) {
			Log.w(cgSettings.tag, "Failed to update traveled distance.");
		}
	}

	private class changeNumber implements View.OnClickListener {
		public void onClick(View view) {
			if (numberType < 1) numberType ++;
			else numberType = 0;

			setNumber();

			if (numberType == 0) {
				warning.showShortToast(res.getString(R.string.info_altitude));
			} else if (numberType == 1) {
				long dstSince = 0l;
				String dstString = null;
				dstSince = activity.getSharedPreferences(cgSettings.preferences, 0).getLong("dst-since", 0l);
				if (dstSince > 0) {
					Date dstDate = new Date(dstSince);
					dstString = res.getString(R.string.info_since) + " " + base.dateOut.format(dstDate) + ", " + base.timeOut.format(dstDate) +  ")";
				} else {
					dstString = "";
				}

				warning.showShortToast(res.getString(R.string.info_distance) + dstString);
			}
		}
	}

	private class resetNumber implements View.OnLongClickListener {
		public boolean onLongClick(View view) {
			if (numberType == 1) {
				geo.distanceNow = 0f;

				final SharedPreferences.Editor prefsEdit = activity.getSharedPreferences(cgSettings.preferences, 0).edit();
				if (prefsEdit != null) {
					prefsEdit.putFloat("dst", 0f);
					prefsEdit.putLong("dst-since", System.currentTimeMillis());
					prefsEdit.commit();
				}

				setNumber();
				warning.showToast(res.getString(R.string.info_distance_cleared));

				return true;
			}

			return false;
		}
	}

	public void goHome(View view) {
		base.goHome(activity);
	}

	public void goManual(View view) {
		try {
			AppManualReaderClient.openManual(
				"c-geo",
				"c:geo-live-map",
				activity,
				"http://cgeo.carnero.cc/manual/"
			);
		} catch (Exception e) {
			// nothing
		}
	}
}
