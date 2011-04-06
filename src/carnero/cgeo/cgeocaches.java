package carnero.cgeo;

import gnu.android.app.appmanualclient.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SubMenu;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Locale;

public class cgeocaches extends ListActivity {

	private GoogleAnalyticsTracker tracker = null;
	private String action = null;
	private String type = null;
	private Double latitude = null;
	private Double longitude = null;
	private String cachetype = null;
	private String keyword = null;
	private String address = null;
	private String username = null;
	private Long searchId = null;
	private ArrayList<cgCache> cacheList = new ArrayList<cgCache>();
	private cgeoapplication app = null;
	private Resources res = null;
	private static Activity activity = null;
	private cgCacheListAdapter adapter = null;
	private LayoutInflater inflater = null;
	private View listFooter = null;
	private TextView listFooterText = null;
	private cgSettings settings = null;
	private cgBase base = null;
	private cgWarning warning = null;
	private ProgressDialog waitDialog = null;
	private Double northHeading = new Double(0);
	private cgGeo geo = null;
	private cgDirection dir = null;
	private cgUpdateLoc geoUpdate = new update();
	private cgUpdateDir dirUpdate = new updateDir();
	private String title = "";
	private int detailTotal = 0;
	private int detailProgress = 0;
	private Long detailProgressTime = 0l;
	private geocachesLoadDetails threadD = null;
	private geocachesDropDetails threadR = null;
	private int listId = 0;
	private ArrayList<cgList> lists = null;
	private cgCacheGeocodeComparator gcComparator = new cgCacheGeocodeComparator();
	private Handler loadCachesHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			try {
				if (searchId != null && searchId > 0) {
					base.setTitle(activity, title + " [" + app.getCount(searchId) + "]");
					cacheList.clear();
					
					final ArrayList<cgCache> cacheListTmp = app.getCaches(searchId);
					if (cacheListTmp != null && cacheListTmp.isEmpty() == false) {
						cacheList.addAll(cacheListTmp);
						cacheListTmp.clear();
						
						Collections.sort((List<cgCache>)cacheList, gcComparator);
					}
				} else {
					base.setTitle(activity, title);
				}

				setAdapter();

				if (cacheList == null) {
					warning.showToast(res.getString(R.string.err_list_load_fail));
					setMoreCaches(false);
				} else {
					final Integer count = app.getTotal(searchId);

					if (count != null && count > 0) {
						if (cacheList.size() < app.getTotal(searchId) && cacheList.size() < 1000) {
							setMoreCaches(true);
						} else {
							setMoreCaches(false);
						}
					} else {
						setMoreCaches(false);
					}
				}

				if (cacheList != null && app.getError(searchId) != null && app.getError(searchId).equalsIgnoreCase(cgBase.errorRetrieve.get(-7)) == true) {
					AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
					dialog.setTitle(res.getString(R.string.license));
					dialog.setMessage(res.getString(R.string.err_license));
					dialog.setCancelable(true);
					dialog.setNegativeButton(res.getString(R.string.license_dismiss), new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int id) {
							settings.deleteCookies();
							dialog.cancel();
						}
					});
					dialog.setPositiveButton(res.getString(R.string.license_show), new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int id) {
							settings.deleteCookies();
							activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.geocaching.com/software/agreement.aspx?ID=0")));
						}
					});

					AlertDialog alert = dialog.create();
					alert.show();
				} else if (app != null && app.getError(searchId) != null && app.getError(searchId).length() > 0) {
					warning.showToast(res.getString(R.string.err_download_fail) + app.getError(searchId) + ".");

					hideLoading();
					base.showProgress(activity, false);

					finish();
					return;
				}

				if (geo != null && geo.latitudeNow != null && geo.longitudeNow != null) {
					adapter.setActualCoordinates(geo.latitudeNow, geo.longitudeNow);
					adapter.setActualHeading(northHeading);
				}
			} catch (Exception e) {
				warning.showToast(res.getString(R.string.err_detail_cache_find_any));
				Log.e(cgSettings.tag, "cgeocaches.loadCachesHandler: " + e.toString());

				hideLoading();
				base.showProgress(activity, false);
					
				finish();
				return;
			}

			try {
				hideLoading();
				base.showProgress(activity, false);
			} catch (Exception e2) {
				Log.e(cgSettings.tag, "cgeocaches.loadCachesHandler.2: " + e2.toString());
			}
			
			if (adapter != null) {
				adapter.setSelectMode(false, true);
			}
		}
	};
	private Handler loadNextPageHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			try {
				if (searchId != null && searchId > 0) {
					base.setTitle(activity, title + " [" + app.getCount(searchId) + "]");
					cacheList.clear();
					
					final ArrayList<cgCache> cacheListTmp = app.getCaches(searchId);
					if (cacheListTmp != null && cacheListTmp.isEmpty() == false) {
						cacheList.addAll(cacheListTmp);
						cacheListTmp.clear();
						Collections.sort((List<cgCache>)cacheList, gcComparator);
					}
				} else {
					base.setTitle(activity, title);
				}

				setAdapter();

				if (cacheList == null) {
					warning.showToast(res.getString(R.string.err_list_load_fail));
					setMoreCaches(false);
				} else {
					final Integer count = app.getTotal(searchId);
					if (count != null && count > 0) {
						if (cacheList.size() < app.getTotal(searchId) && cacheList.size() < 1000) {
							setMoreCaches(true);
						} else {
							setMoreCaches(false);
						}
					} else {
						setMoreCaches(false);
					}
				}

				if (app.getError(searchId) != null && app.getError(searchId).length() > 0) {
					warning.showToast(res.getString(R.string.err_download_fail) + app.getError(searchId) + ".");

					listFooter.setOnClickListener(new moreCachesListener());
					hideLoading();
					base.showProgress(activity, false);

					finish();
					return;
				}

				if (geo != null && geo.latitudeNow != null && geo.longitudeNow != null) {
					adapter.setActualCoordinates(geo.latitudeNow, geo.longitudeNow);
					adapter.setActualHeading(northHeading);
				}
			} catch (Exception e) {
				warning.showToast(res.getString(R.string.err_detail_cache_find_next));
				Log.e(cgSettings.tag, "cgeocaches.loadNextPageHandler: " + e.toString());
			}

			listFooter.setOnClickListener(new moreCachesListener());
			
			hideLoading();
			base.showProgress(activity, false);
			
			if (adapter != null) {
				adapter.setSelectMode(false, true);
			}
		}
	};
	private Handler loadDetailsHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			setAdapter();

			if (msg.what > -1) {
				if (waitDialog != null) {
					cacheList.get(msg.what).statusChecked = false;

					if (adapter != null) {
						adapter.notifyDataSetChanged();
					}

					Float diffTime = new Float((System.currentTimeMillis() - detailProgressTime) / 1000); // seconds left
					Float oneCache = diffTime / detailProgress; // left time per cache
					int etaTime = (int) ((detailTotal - detailProgress) * oneCache / 60); // seconds remaining

					waitDialog.setProgress(detailProgress);
					if (etaTime < 1) {
						waitDialog.setMessage(res.getString(R.string.caches_downloading) + " " + res.getString(R.string.caches_eta_ltm));
					} else if (etaTime == 1) {
						waitDialog.setMessage(res.getString(R.string.caches_downloading) + " " + etaTime + " " + res.getString(R.string.caches_eta_min));
					} else {
						waitDialog.setMessage(res.getString(R.string.caches_downloading) + " " + etaTime + " " + res.getString(R.string.caches_eta_mins));
					}
				}
			} else {
				if (cacheList != null && searchId != null) {
					final ArrayList<cgCache> cacheListTmp = app.getCaches(searchId);
					if (cacheListTmp != null && cacheListTmp.isEmpty() == false) {
						cacheList.clear();
						cacheList.addAll(cacheListTmp);
						cacheListTmp.clear();
						Collections.sort((List<cgCache>)cacheList, gcComparator);
					}
				}

				if (geo != null && geo.latitudeNow != null && geo.longitudeNow != null) {
					adapter.setActualCoordinates(geo.latitudeNow, geo.longitudeNow);
					adapter.setActualHeading(northHeading);
				}

				base.showProgress(activity, false);
				if (waitDialog != null) {
					waitDialog.dismiss();
					waitDialog.setOnCancelListener(null);
				}

				if (geo == null) {
					geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);
				}
				if (settings.livelist == 1 && settings.useCompass == 1 && dir == null) {
					dir = app.startDir(activity, dirUpdate, warning);
				}
			}
		}
	};
	private Handler dropDetailsHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			setAdapter();

			if (msg.what > -1) {
				cacheList.get(msg.what).statusChecked = false;
			} else {
				if (adapter != null) {
					adapter.setSelectMode(false, true);
				}
				
				cacheList.clear();

				final ArrayList<cgCache> cacheListTmp = app.getCaches(searchId);
				if (cacheListTmp != null && cacheListTmp.isEmpty() == false) {
					cacheList.addAll(cacheListTmp);
					cacheListTmp.clear();

					Collections.sort((List<cgCache>)cacheList, gcComparator);
				}
				
				if (waitDialog != null) {
					waitDialog.dismiss();
					waitDialog.setOnCancelListener(null);
				}
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
		app.setAction(action);
		settings = new cgSettings(this, getSharedPreferences(cgSettings.preferences, 0));
		base = new cgBase(app, settings, getSharedPreferences(cgSettings.preferences, 0));
		warning = new cgWarning(this);

		// set layout
		if (settings.skin == 1) {
			setTheme(R.style.light);
		} else {
			setTheme(R.style.dark);
		}
		setContentView(R.layout.caches);
		base.setTitle(activity, "caches");

		// google analytics
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start(cgSettings.analytics, this);
		tracker.dispatch();
		base.sendAnal(activity, tracker, "/cache/list");

		// get parameters
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			type = extras.getString("type");
			latitude = extras.getDouble("latitude");
			longitude = extras.getDouble("longitude");
			cachetype = extras.getString("cachetype");
			keyword = extras.getString("keyword");
			address = extras.getString("address");
			username = extras.getString("username");
		}

		init();

		Thread threadPure;
		cgSearchThread thread;

		if (type.equals("offline") == true) {
			listId = settings.getLastList();
			if (listId <= 0) {
				listId = 1;
				title = res.getString(R.string.caches_stored);
			} else {
				final cgList list = app.getList(listId);
				title = list.title;
			}
			
			base.setTitle(activity, title);
			base.showProgress(activity, true);
			setLoadingCaches();
			
			threadPure = new geocachesLoadByOffline(loadCachesHandler, latitude, longitude, listId);
			threadPure.start();
		} else if (type.equals("history") == true) {
			if (adapter != null) {
				adapter.setHistoric(true);
			}

			title = res.getString(R.string.caches_history);
			base.setTitle(activity, title);
			base.showProgress(activity, true);
			setLoadingCaches();

			threadPure = new geocachesLoadByHistory(loadCachesHandler);
			threadPure.start();
		} else if (type.equals("nearest") == true) {
			action = "pending";
			title = res.getString(R.string.caches_nearby);
			base.setTitle(activity, title);
			base.showProgress(activity, true);
			setLoadingCaches();

			thread = new geocachesLoadByCoords(loadCachesHandler, latitude, longitude, cachetype);
			thread.setRecaptchaHandler(new cgSearchHandler(activity, res, thread));
			thread.start();
		} else if (type.equals("coordinate") == true) {
			action = "planning";
			title = base.formatCoordinate(latitude, res.getString(R.string.search_lat), true) + " | " + base.formatCoordinate(longitude, res.getString(R.string.search_lon), true);
			base.setTitle(activity, title);
			base.showProgress(activity, true);
			setLoadingCaches();

			thread = new geocachesLoadByCoords(loadCachesHandler, latitude, longitude, cachetype);
			thread.setRecaptchaHandler(new cgSearchHandler(activity, res, thread));
			thread.start();
		} else if (type.equals("keyword") == true) {
			title = keyword;
			base.setTitle(activity, title);
			base.showProgress(activity, true);
			setLoadingCaches();

			thread = new geocachesLoadByKeyword(loadCachesHandler, keyword, cachetype);
			thread.setRecaptchaHandler(new cgSearchHandler(activity, res, thread));
			thread.start();
		} else if (type.equals("address") == true) {
			action = "planning";
			if (address != null && address.length() > 0) {
				title = address;
				base.setTitle(activity, title);
				base.showProgress(activity, true);
				setLoadingCaches();
			} else {
				title = base.formatCoordinate(latitude, res.getString(R.string.search_lat), true) + " | " + base.formatCoordinate(longitude, res.getString(R.string.search_lon), true);
				base.setTitle(activity, title);
				base.showProgress(activity, true);
				setLoadingCaches();
			}

			thread = new geocachesLoadByCoords(loadCachesHandler, latitude, longitude, cachetype);
			thread.setRecaptchaHandler(new cgSearchHandler(activity, res, thread));
			thread.start();
		} else if (type.equals("username") == true) {
			title = username;
			base.setTitle(activity, title);
			base.showProgress(activity, true);
			setLoadingCaches();

			thread = new geocachesLoadByUserName(loadCachesHandler, username, cachetype);
			thread.setRecaptchaHandler(new cgSearchHandler(activity, res, thread));
			thread.start();
		} else if (type.equals("owner") == true) {
			title = username;
			base.setTitle(activity, title);
			base.showProgress(activity, true);
			setLoadingCaches();

			thread = new geocachesLoadByOwner(loadCachesHandler, username, cachetype);
			thread.setRecaptchaHandler(new cgSearchHandler(activity, res, thread));
			thread.start();
		} else {
			title = "caches";
			base.setTitle(activity, title);
			Log.e(cgSettings.tag, "cgeocaches.onCreate: No action or unknown action specified");
		}
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

		if (adapter != null && geo != null && geo.latitudeNow != null && geo.longitudeNow != null) {
			adapter.setActualCoordinates(geo.latitudeNow, geo.longitudeNow);
			adapter.setActualHeading(northHeading);
		}

		if (adapter != null) {
			adapter.setSelectMode(false, true);
			if (geo != null && geo.latitudeNow != null && geo.longitudeNow != null) {
				adapter.forceSort(geo.latitudeNow, geo.longitudeNow);
			}
		}
	}

	@Override
	public void onDestroy() {
		if (adapter != null) {
			adapter = null;
		}

		if (dir != null) {
			dir = app.removeDir();
		}
		if (geo != null) {
			geo = app.removeGeo();
		}
		if (tracker != null) {
			tracker.stop();
		}

		super.onDestroy();
	}

	@Override
	public void onStop() {
		if (dir != null) {
			dir = app.removeDir();
		}
		if (geo != null) {
			geo = app.removeGeo();
		}

		super.onStop();
	}

	@Override
	public void onPause() {
		if (dir != null) {
			dir = app.removeDir();
		}
		if (geo != null) {
			geo = app.removeGeo();
		}

		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		SubMenu subMenuSort = menu.addSubMenu(0, 104, 0, res.getString(R.string.caches_sort)).setIcon(android.R.drawable.ic_menu_sort_alphabetically);
		subMenuSort.setHeaderTitle(res.getString(R.string.caches_sort_title));
		subMenuSort.add(0, 10, 0, res.getString(R.string.caches_sort_distance));
		subMenuSort.add(0, 11, 0, res.getString(R.string.caches_sort_difficulty));
		subMenuSort.add(0, 12, 0, res.getString(R.string.caches_sort_terrain));
		subMenuSort.add(0, 13, 0, res.getString(R.string.caches_sort_size));
		subMenuSort.add(0, 14, 0, res.getString(R.string.caches_sort_favorites));
		
		menu.add(0, 0, 0, res.getString(R.string.caches_select_mode)).setIcon(android.R.drawable.ic_menu_agenda);
		menu.add(0, 9, 0, res.getString(R.string.caches_select_invert)).setIcon(android.R.drawable.ic_menu_agenda);
		if (type.equals("offline") == true) {
			SubMenu subMenu = menu.addSubMenu(0, 103, 0, res.getString(R.string.caches_manage)).setIcon(android.R.drawable.ic_menu_save);
			subMenu.add(0, 5, 0, res.getString(R.string.caches_drop_all)); // delete saved caches
			subMenu.add(0, 1, 0, res.getString(R.string.cache_offline_refresh)); // download details for all caches
			menu.add(0, 6, 0, res.getString(R.string.gpx_import_title)).setIcon(android.R.drawable.ic_menu_upload); // import gpx file
		} else {
			menu.add(0, 1, 0, res.getString(R.string.caches_store_offline)).setIcon(android.R.drawable.ic_menu_set_as); // download details for all caches
		}

		final Intent intentTest = new Intent(Intent.ACTION_VIEW);
		intentTest.setData(Uri.parse("menion.points:x"));
		if (cgBase.isIntentAvailable(activity, intentTest) == true) {
			SubMenu subMenu = menu.addSubMenu(0, 101, 0, res.getString(R.string.caches_on_map)).setIcon(android.R.drawable.ic_menu_mapmode);
			subMenu.add(0, 2, 0, res.getString(R.string.caches_map_cgeo)); // show all caches on map using c:geo
			subMenu.add(0, 3, 0, res.getString(R.string.caches_map_locus)); // show all caches on map using Locus
		} else {
			menu.add(0, 2, 0, res.getString(R.string.caches_on_map)).setIcon(android.R.drawable.ic_menu_mapmode); // show all caches on map
		}

		if (type.equals("offline") == true) {
			SubMenu subMenu = menu.addSubMenu(0, 102, 0, res.getString(R.string.list_menu)).setIcon(android.R.drawable.ic_menu_more);
			subMenu.add(0, 7, 0, res.getString(R.string.list_menu_create));
			subMenu.add(0, 8, 0, res.getString(R.string.list_menu_drop));
		}
		
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		try {
			if (adapter != null && adapter.getSelectMode() == true) {
				menu.findItem(0).setTitle(res.getString(R.string.caches_select_mode_exit));
				menu.findItem(9).setVisible(true);
			} else {
				menu.findItem(0).setTitle(res.getString(R.string.caches_select_mode));
				menu.findItem(9).setVisible(false);
			}
			
			if (type.equals("offline") == true) {
				if (adapter != null && adapter.getChecked() > 0) {
					menu.findItem(5).setTitle(res.getString(R.string.caches_drop_selected) + " (" + adapter.getChecked() + ")");
				} else {
					menu.findItem(5).setTitle(res.getString(R.string.caches_drop_all));
				}

				if (adapter != null && adapter.getChecked() > 0) {
					menu.findItem(1).setTitle(res.getString(R.string.caches_refresh_selected) + " (" + adapter.getChecked() + ")");
				} else {
					menu.findItem(1).setTitle(res.getString(R.string.caches_refresh_all));
				}
			} else {
				if (adapter == null) {
					Log.i(cgSettings.tag, "No adapter");
				} else {
					Log.i(cgSettings.tag, "Checked: " + adapter.getChecked());
				}
				if (adapter != null && adapter.getChecked() > 0) {
					menu.findItem(1).setTitle(res.getString(R.string.caches_store_selected) + " (" + adapter.getChecked() + ")");
				} else {
					menu.findItem(1).setTitle(res.getString(R.string.caches_store_offline));
				}
			}

			if (type.equals("offline") == false && (cacheList != null && app != null && cacheList.size() >= app.getTotal(searchId))) { // there are no more caches
				menu.findItem(0).setEnabled(false);
			} else {
				menu.findItem(0).setEnabled(true);
			}
			
			if (listId == 1) {
				menu.findItem(8).setVisible(false);
			} else {
				menu.findItem(8).setVisible(true);
			}
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgeocaches.onPrepareOptionsMenu: " + e.toString());
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case 0:
				if (adapter != null) {
					adapter.switchSelectMode();
				}
				return true;
			case 1:
				refreshStored();
				return true;
			case 2:
				showOnMap();
				return false;
			case 3:
				showOnLocus();
				return false;
			case 5:
				dropStored();
				return false;
			case 6:
				importGpx();
				return false;
			case 7:
				createList();
				return false;
			case 8:
				removeList();
				return false;
			case 9:
				if (adapter != null) {
					adapter.invertSelection();
				}
				return false;
			case 10:
				if (adapter != null) {
					adapter.setComparator(null);
				}
				return false;
			case 11:
				if (adapter != null) {
					adapter.setComparator(new cgCacheDifficultyComparator());
				}
				return false;
			case 12:
				if (adapter != null) {
					adapter.setComparator(new cgCacheTerrainComparator());
				}
				return false;
			case 13:
				if (adapter != null) {
					adapter.setComparator(new cgCacheSizeComparator());
				}
				return false;
			case 14:
				if (adapter != null) {
					adapter.setComparator(new cgCachePopularityComparator());
				}
				return false;
		}

		return false;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo info) {
		super.onCreateContextMenu(menu, view, info);

		if (adapter == null) {
			return;
		}

		AdapterContextMenuInfo adapterInfo = null;
		try {
			adapterInfo = (AdapterContextMenuInfo) info;
		} catch (Exception e) {
			Log.w(cgSettings.tag, "cgeocaches.onCreateContextMenu: " + e.toString());
		}

		final cgCache cache = adapter.getItem(adapterInfo.position);

		if (cache.name != null && cache.name.length() > 0) {
			menu.setHeaderTitle(cache.name);
		} else {
			menu.setHeaderTitle(cache.geocode);
		}
		if (cache.latitude != null && cache.longitude != null) {
			menu.add(0, 1, 0, res.getString(R.string.cache_menu_compass));
			menu.add(0, 2, 0, res.getString(R.string.cache_menu_radar));
			menu.add(0, 3, 0, res.getString(R.string.cache_menu_map));
			menu.add(0, 4, 0, res.getString(R.string.cache_menu_map_ext));
			menu.add(0, 5, 0, res.getString(R.string.cache_menu_tbt));
			menu.add(0, 6, 0, res.getString(R.string.cache_menu_visit));
			menu.add(0, 7, 0, res.getString(R.string.cache_menu_details));
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int id = item.getItemId();
		ContextMenu.ContextMenuInfo info = item.getMenuInfo();

		if (info == null) {
			return false;
		}

		AdapterContextMenuInfo adapterInfo = null;
		try {
			adapterInfo = (AdapterContextMenuInfo) info;
		} catch (Exception e) {
			Log.w(cgSettings.tag, "cgeocaches.onContextItemSelected: " + e.toString());
		}

		final int touchedPos = adapterInfo.position;
		final cgCache cache = adapter.getItem(touchedPos);

		if (id == 1) { // compass
			Intent navigateIntent = new Intent(activity, cgeonavigate.class);
			navigateIntent.putExtra("latitude", cache.latitude);
			navigateIntent.putExtra("longitude", cache.longitude);
			navigateIntent.putExtra("geocode", cache.geocode.toUpperCase());
			navigateIntent.putExtra("name", cache.name);

			activity.startActivity(navigateIntent);

			return true;
		} else if (id == 2) { // radar
			try {
				if (cgBase.isIntentAvailable(activity, "com.google.android.radar.SHOW_RADAR") == true) {
					Intent radarIntent = new Intent("com.google.android.radar.SHOW_RADAR");
					radarIntent.putExtra("latitude", new Float(cache.latitude));
					radarIntent.putExtra("longitude", new Float(cache.longitude));
					activity.startActivity(radarIntent);
				} else {
					AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
					dialog.setTitle(res.getString(R.string.err_radar_title));
					dialog.setMessage(res.getString(R.string.err_radar_message));
					dialog.setCancelable(true);
					dialog.setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int id) {
							try {
								activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:com.eclipsim.gpsstatus2")));
								dialog.cancel();
							} catch (Exception e) {
								warning.showToast(res.getString(R.string.err_radar_market));
								Log.e(cgSettings.tag, "cgeocaches.onContextItemSelected.radar.onClick: " + e.toString());
							}
						}
					});
					dialog.setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});

					AlertDialog alert = dialog.create();
					alert.show();
				}
			} catch (Exception e) {
				warning.showToast(res.getString(R.string.err_radar_generic));
				Log.e(cgSettings.tag, "cgeocaches.onContextItemSelected.radar: " + e.toString());
			}

			return true;
		} else if (id == 3) { // show on map
			Intent mapIntent = new Intent(activity, cgeomap.class);
			mapIntent.putExtra("detail", false);
			mapIntent.putExtra("geocode", cache.geocode);

			activity.startActivity(mapIntent);

			return true;
		} else if (id == 4) { // show on external map
			base.runExternalMap(0, activity, res, warning, tracker, cache);

			return true;
		} else if (id == 5) { // turn-by-turn
			if (geo != null) {
				base.runNavigation(activity, res, settings, warning, tracker, cache.latitude, cache.longitude, geo.latitudeNow, geo.longitudeNow);
			} else {
				base.runNavigation(activity, res, settings, warning, tracker, cache.latitude, cache.longitude);
			}

			return true;
		} else if (id == 6) { // log visit
			if (cache.cacheid == null || cache.cacheid.length() == 0) {
				warning.showToast(res.getString(R.string.err_cannot_log_visit));
				return true;
			}

			Intent logVisitIntent = new Intent(activity, cgeovisit.class);
			logVisitIntent.putExtra("id", cache.cacheid);
			logVisitIntent.putExtra("geocode", cache.geocode.toUpperCase());
			logVisitIntent.putExtra("type", cache.type.toLowerCase());

			activity.startActivity(logVisitIntent);

			return true;
		} else if (id == 7) { // cache details
			Intent cachesIntent = new Intent(activity, cgeodetail.class);
			cachesIntent.putExtra("geocode", cache.geocode.toUpperCase());
			cachesIntent.putExtra("name", cache.name);
			activity.startActivity(cachesIntent);

			return true;
		}

		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (adapter != null) {
				if (adapter.resetChecks() == true) {
					return true;
				} else if (adapter.getSelectMode() == true) {
					adapter.setSelectMode(false, true);

					return true;
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void setAdapter() {
		if (listFooter == null) {
			if (inflater == null) {
				inflater = activity.getLayoutInflater();
			}
			listFooter = inflater.inflate(R.layout.caches_footer, null);

			listFooter.setClickable(true);
			listFooter.setOnClickListener(new moreCachesListener());
		}
		if (listFooterText == null) {
			listFooterText = (TextView) listFooter.findViewById(R.id.more_caches);
		}

		if (adapter == null) {
			final ListView list = getListView();

			registerForContextMenu(list);
			list.setLongClickable(true);
			list.addFooterView(listFooter);

			adapter = new cgCacheListAdapter(activity, settings, cacheList, base);
			setListAdapter(adapter);
		} else {
			adapter.notifyDataSetChanged();
		}

		if (adapter != null && geo != null) {
			adapter.setActualCoordinates(geo.latitudeNow, geo.longitudeNow);
		}
		if (adapter != null && dir != null) {
			adapter.setActualHeading(dir.directionNow);
		}
	}

	private void setLoadingCaches() {
		if (listFooter == null) {
			return;
		}
		if (listFooterText == null) {
			return;
		}

		listFooterText.setText(res.getString(R.string.caches_more_caches_loading));
		listFooter.setClickable(false);
		listFooter.setOnClickListener(null);
	}
	
	private void setMoreCaches(boolean more) {
		if (listFooter == null) {
			return;
		}
		if (listFooterText == null) {
			return;
		}

		if (more == false) {
			if (cacheList == null || cacheList.isEmpty()) {
				listFooterText.setText(res.getString(R.string.caches_no_cache));
			} else {
				listFooterText.setText(res.getString(R.string.caches_more_caches_no));
			}
			listFooter.setClickable(false);
			listFooter.setOnClickListener(null);
		} else {
			listFooterText.setText(res.getString(R.string.caches_more_caches));
			listFooter.setClickable(true);
			listFooter.setOnClickListener(new moreCachesListener());
		}
	}

	private void init() {
		// sensor & geolocation manager
		if (geo == null) {
			geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);
		}
		if (settings.livelist == 1 && settings.useCompass == 1 && dir == null) {
			dir = app.startDir(activity, dirUpdate, warning);
		}

		if (cacheList != null) {
			base.setTitle(activity, title);
		}

		if (cacheList != null && cacheList.isEmpty() == false) {
			final Integer count = app.getTotal(searchId);
			if (count != null && count > 0) {
				base.setTitle(activity, title);
				if (cacheList.size() < app.getTotal(searchId) && cacheList.size() < 1000) {
					setMoreCaches(true);
				} else {
					setMoreCaches(false);
				}
			} else {
				base.setTitle(activity, title);
				setMoreCaches(false);
			}
		} else {
			base.setTitle(activity, title);
		}

		setAdapter();

		if (geo != null) {
			geoUpdate.updateLoc(geo);
		}
		if (dir != null) {
			dirUpdate.updateDir(dir);
		}
	}

	private void showOnMap() {
		if (searchId == null || searchId == 0 || cacheList == null || cacheList.isEmpty() == true) {
			warning.showToast(res.getString(R.string.warn_no_cache_coord));

			return;
		}

		cgeomap mapActivity = new cgeomap();

		Intent mapIntent = new Intent(activity, mapActivity.getClass());
		mapIntent.putExtra("detail", false);
		mapIntent.putExtra("searchid", searchId);

		activity.startActivity(mapIntent);
	}

	private void showOnLocus() {
		if (cacheList == null || cacheList.isEmpty() == true) {
			return;
		}

		try {
			final Intent intentTest = new Intent(Intent.ACTION_VIEW);
			intentTest.setData(Uri.parse("menion.points:x"));

			if (cgBase.isIntentAvailable(activity, intentTest) == false) {
				return;
			}

			final ArrayList<cgCache> cacheListTemp = (ArrayList<cgCache>) cacheList.clone();
			final ArrayList<cgCache> cacheListCoord = new ArrayList<cgCache>();
			for (cgCache cache : cacheListTemp) {
				if (cache.latitude != null && cache.longitude != null) {
					cacheListCoord.add(cache);
				}
			}
			cacheListTemp.clear();

			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final DataOutputStream dos = new DataOutputStream(baos);

			dos.writeInt(1); // not used
			dos.writeInt(cacheListCoord.size()); // cache and waypoints

			// cache waypoints
			if (cacheListCoord != null && cacheListCoord.isEmpty() == false) {
				for (cgCache cache : cacheListCoord) {
					final int wpIcon = base.getIcon(true, cache.type, cache.own, cache.found, cache.disabled);

					if (wpIcon > 0) {
						// load icon
						Bitmap bitmap = BitmapFactory.decodeResource(res, wpIcon);
						ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
						bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos2);
						byte[] image = baos2.toByteArray();

						dos.writeInt(image.length);
						dos.write(image);
					} else {
						// no icon
						dos.writeInt(0); // no image
					}

					// name
					if (cache != null && cache.geocode != null && cache.geocode.length() > 0) {
						dos.writeUTF(cache.geocode.toUpperCase());
					} else {
						dos.writeUTF("");
					}

					// description
					if (cache != null && cache.name != null && cache.name.length() > 0) {
						dos.writeUTF(cache.name);
					} else {
						dos.writeUTF("");
					}

					// additional data :: keyword, button title, package, activity, data name, data content
					if (cache != null && cache.geocode != null && cache.geocode.length() > 0) {
						dos.writeUTF("intent;c:geo;carnero.cgeo;carnero.cgeo.cgeodetail;geocode;" + cache.geocode);
					} else {
						dos.writeUTF("");
					}

					dos.writeDouble(cache.latitude); // latitude
					dos.writeDouble(cache.longitude); // longitude
				}
			}

			final Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("menion.points:data"));
			intent.putExtra("data", baos.toByteArray());

			activity.startActivity(intent);

			base.sendAnal(activity, tracker, "/external/locus");
		} catch (Exception e) {
			// nothing
		}
	}

	private void importGpx() {
		final Intent intent = new Intent(activity, cgeogpxes.class);
		intent.putExtra("list", listId);
		activity.startActivity(intent);

		finish();
	}

	public void refreshStored() {
		if (adapter != null && adapter.getChecked() > 0) {
			// there are some checked caches
			detailTotal = adapter.getChecked();
		} else {
			// no checked caches, download everything (when already stored - refresh them)
			detailTotal = cacheList.size();
		}
		detailProgress = 0;

		base.showProgress(activity, false);
		waitDialog = new ProgressDialog(this);
		waitDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

			public void onCancel(DialogInterface arg0) {
				try {
					if (threadD != null) {
						threadD.kill();
					}

					if (geo == null) {
						geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);
					}
					if (settings.livelist == 1 && settings.useCompass == 1 && dir == null) {
						dir = app.startDir(activity, dirUpdate, warning);
					}
				} catch (Exception e) {
					Log.e(cgSettings.tag, "cgeocaches.onOptionsItemSelected.onCancel: " + e.toString());
				}
			}
		});

		waitDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		int etaTime = (int) ((detailTotal * 25) / 60);
		if (etaTime < 1) {
			waitDialog.setMessage(res.getString(R.string.caches_downloading) + " " + res.getString(R.string.caches_eta_ltm));
		} else if (etaTime == 1) {
			waitDialog.setMessage(res.getString(R.string.caches_downloading) + " " + etaTime + " " + res.getString(R.string.caches_eta_min));
		} else {
			waitDialog.setMessage(res.getString(R.string.caches_downloading) + " " + etaTime + " " + res.getString(R.string.caches_eta_mins));
		}
		waitDialog.setCancelable(true);
		waitDialog.setMax(detailTotal);
		waitDialog.show();

		detailProgressTime = System.currentTimeMillis();

		threadD = new geocachesLoadDetails(loadDetailsHandler, listId);
		threadD.start();
	}

	public void dropStored() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
		dialog.setCancelable(true);
		dialog.setTitle(res.getString(R.string.caches_drop_stored));

		if (adapter != null && adapter.getChecked() > 0) {
			dialog.setMessage(res.getString(R.string.caches_drop_selected_ask));
			dialog.setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int id) {
					dropSelected();
					dialog.cancel();
				}
			});
		} else {
			dialog.setMessage(res.getString(R.string.caches_drop_all_ask));
			dialog.setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int id) {
					dropSelected();
					dialog.cancel();
				}
			});
		}
		dialog.setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});

		AlertDialog alert = dialog.create();
		alert.show();
	}

	public void dropSelected() {
		waitDialog = new ProgressDialog(this);
		waitDialog.setMessage(res.getString(R.string.caches_drop_progress));
		waitDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

			public void onCancel(DialogInterface arg0) {
				try {
					if (threadR != null) {
						threadR.kill();
					}
				} catch (Exception e) {
					Log.e(cgSettings.tag, "cgeocaches.onOptionsItemSelected.onCancel: " + e.toString());
				}
			}
		});

		waitDialog.setCancelable(true);
		waitDialog.show();

		threadR = new geocachesDropDetails(dropDetailsHandler);
		threadR.start();
	}

	private class update extends cgUpdateLoc {

		@Override
		public void updateLoc(cgGeo geo) {
			if (geo == null) {
				return;
			}
			if (adapter == null) {
				return;
			}

			try {
				if (cacheList != null && geo.latitudeNow != null && geo.longitudeNow != null) {
					adapter.setActualCoordinates(geo.latitudeNow, geo.longitudeNow);
				}

				if (settings.useCompass == 0 || (geo.speedNow != null && geo.speedNow > 5)) { // use GPS when speed is higher than 18 km/h
					if (settings.useCompass == 0) {
						if (geo.bearingNow != null) {
							adapter.setActualHeading(geo.bearingNow);
						} else {
							adapter.setActualHeading(new Double(0));
						}
					}
					if (northHeading != null) {
						adapter.setActualHeading(northHeading);
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
			if (settings.livelist == 0) {
				return;
			}
			if (dir == null || dir.directionNow == null) {
				return;
			}

			northHeading = dir.directionNow;
			if (northHeading != null && adapter != null && (geo == null || geo.speedNow == null || geo.speedNow <= 5)) { // use compass when speed is lower than 18 km/h) {
				adapter.setActualHeading(northHeading);
			}
		}
	}

	private class geocachesLoadByOffline extends Thread {

		private Handler handler = null;
		private Double latitude = null;
		private Double longitude = null;
		private int listId = 1;

		public geocachesLoadByOffline(Handler handlerIn, Double latitudeIn, Double longitudeIn, int listIdIn) {
			handler = handlerIn;
			latitude = latitudeIn;
			longitude = longitudeIn;
			listId = listIdIn;
		}

		@Override
		public void run() {
			HashMap<String, Object> params = new HashMap<String, Object>();
			if (latitude != null && longitude != null) {
				params.put("latitude", latitude);
				params.put("longitude", longitude);
				params.put("cachetype", settings.cacheType);
				params.put("list", listId);
			}

			searchId = base.searchByOffline(params);

			handler.sendMessage(new Message());
		}
	}

	private class geocachesLoadByHistory extends Thread {

		private Handler handler = null;

		public geocachesLoadByHistory(Handler handlerIn) {
			handler = handlerIn;
		}

		@Override
		public void run() {
			HashMap<String, Object> params = new HashMap<String, Object>();
			if (latitude != null && longitude != null) {
				params.put("cachetype", settings.cacheType);
			}

			searchId = base.searchByHistory(params);

			handler.sendMessage(new Message());
		}
	}

	private class geocachesLoadNextPage extends cgSearchThread {

		private Handler handler = null;

		public geocachesLoadNextPage(Handler handlerIn) {
			handler = handlerIn;
		}

		@Override
		public void run() {
			searchId = base.searchByNextPage(this, searchId, 0, settings.showCaptcha);

			handler.sendMessage(new Message());
		}
	}

	private class geocachesLoadByCoords extends cgSearchThread {

		private Handler handler = null;
		private Double latitude = null;
		private Double longitude = null;
		private String cachetype = null;

		public geocachesLoadByCoords(Handler handlerIn, Double latitudeIn, Double longitudeIn, String cachetypeIn) {
			setPriority(Thread.MIN_PRIORITY);

			handler = handlerIn;
			latitude = latitudeIn;
			longitude = longitudeIn;
			cachetype = cachetypeIn;

			if (latitude == null || longitude == null) {
				warning.showToast(res.getString(R.string.warn_no_coordinates));

				finish();
				return;
			}
		}

		@Override
		public void run() {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("latitude", String.format((Locale) null, "%.6f", latitude));
			params.put("longitude", String.format((Locale) null, "%.6f", longitude));
			params.put("cachetype", cachetype);

			searchId = base.searchByCoords(this, params, 0, settings.showCaptcha);

			handler.sendMessage(new Message());
		}
	}

	private class geocachesLoadByKeyword extends cgSearchThread {

		private Handler handler = null;
		private String keyword = null;
		private String cachetype = null;

		public geocachesLoadByKeyword(Handler handlerIn, String keywordIn, String cachetypeIn) {
			setPriority(Thread.MIN_PRIORITY);

			handler = handlerIn;
			keyword = keywordIn;
			cachetype = cachetypeIn;

			if (keyword == null) {
				warning.showToast(res.getString(R.string.warn_no_keyword));

				finish();
				return;
			}
		}

		@Override
		public void run() {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("keyword", keyword);
			params.put("cachetype", cachetype);

			searchId = base.searchByKeyword(this, params, 0, settings.showCaptcha);

			handler.sendMessage(new Message());
		}
	}

	private class geocachesLoadByUserName extends cgSearchThread {

		private Handler handler = null;
		private String username = null;
		private String cachetype = null;

		public geocachesLoadByUserName(Handler handlerIn, String usernameIn, String cachetypeIn) {
			setPriority(Thread.MIN_PRIORITY);

			handler = handlerIn;
			username = usernameIn;
			cachetype = cachetypeIn;

			if (username == null || username.length() == 0) {
				warning.showToast(res.getString(R.string.warn_no_username));

				finish();
				return;
			}
		}

		@Override
		public void run() {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("username", username);
			params.put("cachetype", cachetype);

			searchId = base.searchByUsername(this, params, 0, settings.showCaptcha);

			handler.sendMessage(new Message());
		}
	}

	private class geocachesLoadByOwner extends cgSearchThread {

		private Handler handler = null;
		private String username = null;
		private String cachetype = null;

		public geocachesLoadByOwner(Handler handlerIn, String usernameIn, String cachetypeIn) {
			setPriority(Thread.MIN_PRIORITY);

			handler = handlerIn;
			username = usernameIn;
			cachetype = cachetypeIn;

			if (username == null || username.length() == 0) {
				warning.showToast(res.getString(R.string.warn_no_username));

				finish();
				return;
			}
		}

		@Override
		public void run() {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("username", username);
			params.put("cachetype", cachetype);

			searchId = base.searchByOwner(this, params, 0, settings.showCaptcha);

			handler.sendMessage(new Message());
		}
	}

	private class geocachesLoadDetails extends Thread {

		private Handler handler = null;
		private int reason = 1;
		private volatile boolean needToStop = false;
		private int checked = 0;
		private long last = 0l;

		public geocachesLoadDetails(Handler handlerIn, int reasonIn) {
			setPriority(Thread.MIN_PRIORITY);

			handler = handlerIn;
			reason = reasonIn;

			if (adapter != null) {
				checked = adapter.getChecked();
			}
		}

		public void kill() {
			needToStop = true;
		}

		@Override
		public void run() {
			if (dir != null) {
				dir = app.removeDir();
			}
			if (geo != null) {
				geo = app.removeGeo();
			}

			final ArrayList<cgCache> cacheListTemp = (ArrayList<cgCache>) cacheList.clone();
			for (cgCache cache : cacheListTemp) {
				if (checked > 0 && cache.statusChecked == false) {
					handler.sendEmptyMessage(0);

					yield();
					continue;
				}

				try {
					if (needToStop == true) {
						Log.i(cgSettings.tag, "Stopped storing process.");
						break;
					}

					if ((System.currentTimeMillis() - last) < 1500) {
						try {
							int delay = 1000 + ((Double) (Math.random() * 1000)).intValue() - (int) (System.currentTimeMillis() - last);
							if (delay < 0) {
								delay = 500;
							}

							Log.i(cgSettings.tag, "Waiting for next cache " + delay + " ms");
							sleep(delay);
						} catch (Exception e) {
							Log.e(cgSettings.tag, "cgeocaches.geocachesLoadDetails.sleep: " + e.toString());
						}
					}

					if (needToStop == true) {
						Log.i(cgSettings.tag, "Stopped storing process.");
						break;
					}

					detailProgress++;
					base.storeCache(app, activity, cache, null, reason, handler);

					handler.sendEmptyMessage(cacheList.indexOf(cache));

					yield();
				} catch (Exception e) {
					Log.e(cgSettings.tag, "cgeocaches.geocachesLoadDetails: " + e.toString());
				}

				last = System.currentTimeMillis();
			}
			cacheListTemp.clear();

			handler.sendEmptyMessage(-1);
		}
	}

	private class geocachesDropDetails extends Thread {

		private Handler handler = null;
		private volatile boolean needToStop = false;
		private int checked = 0;

		public geocachesDropDetails(Handler handlerIn) {
			setPriority(Thread.MIN_PRIORITY);

			handler = handlerIn;

			if (adapter != null) {
				checked = adapter.getChecked();
			}
		}

		public void kill() {
			needToStop = true;
		}

		@Override
		public void run() {
			if (dir != null) {
				dir = app.removeDir();
			}
			if (geo != null) {
				geo = app.removeGeo();
			}

			final ArrayList<cgCache> cacheListTemp = (ArrayList<cgCache>) cacheList.clone();
			for (cgCache cache : cacheListTemp) {
				if (checked > 0 && cache.statusChecked == false) {
					handler.sendEmptyMessage(0);

					yield();
					continue;
				}

				try {
					if (needToStop == true) {
						Log.i(cgSettings.tag, "Stopped dropping process.");
						break;
					}

					app.markDropped(cache.geocode);

					handler.sendEmptyMessage(cacheList.indexOf(cache));

					yield();
				} catch (Exception e) {
					Log.e(cgSettings.tag, "cgeocaches.geocachesDropDetails: " + e.toString());
				}
			}
			cacheListTemp.clear();

			handler.sendEmptyMessage(-1);
		}
	}

	private class moreCachesListener implements View.OnClickListener {

		@Override
		public void onClick(View arg0) {
			base.showProgress(activity, true);
			setLoadingCaches();
			listFooter.setOnClickListener(null);

			geocachesLoadNextPage thread;
			thread = new geocachesLoadNextPage(loadNextPageHandler);
			thread.setRecaptchaHandler(new cgSearchHandler(activity, res, thread));
			thread.start();
		}
	}
	
	private void hideLoading() {
		final ListView list = getListView();
		final RelativeLayout loading = (RelativeLayout) findViewById(R.id.loading);
		
		if (list.getVisibility() == View.GONE) {
			list.setVisibility(View.VISIBLE);
			loading.setVisibility(View.GONE);
		}
	}
	
	public void selectList(View view) {
		if (type.equals("offline") == false) {
			return;
		}

		lists = app.getLists();
		
		if (lists == null) {
			return;
		}
		
		final ArrayList<CharSequence> listsTitle = new ArrayList<CharSequence>();
		for (cgList list : lists) {
			listsTitle.add(list.title);
		}
		
		final CharSequence[] items = new CharSequence[listsTitle.size()];
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(res.getString(R.string.list_title));
		builder.setItems(listsTitle.toArray(items), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int item) {
				switchListByOrder(item);
				
				return;
			}
		});
		builder.create().show();
	}
	
	public void switchListByOrder(int order) {
		switchList(-1, order);
	}
	
	public void switchListById(int id) {
		switchList(id, -1);
	}
	
	public void switchList(int id, int order) {
		cgList list = null;
		
		if (id >= 0) {
			list = app.getList(id);
		} else if (order >= 0) {
			lists = app.getLists();
			list = lists.get(order);
		} else {
			return;
		}
		
		if (list == null) {
			return;
		}
		
		listId = list.id;
		title = list.title;
		
		settings.saveLastList(listId);
		
		base.showProgress(activity, true);
		setLoadingCaches();
		
		Handler handlerMove = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				Thread threadPure = new geocachesLoadByOffline(loadCachesHandler, latitude, longitude, msg.what);
				threadPure.start();
			}
		};
		
		(new moveCachesToList(listId, handlerMove)).start();
	}
	
	private class moveCachesToList extends Thread {
		int listId = -1;
		Handler handler = null;
		
		public moveCachesToList(int listIdIn, Handler handlerIn) {
			listId = listIdIn;
			handler = handlerIn;
		}
		
		@Override
		public void run() {
			int checked = adapter.getChecked();
			if (checked > 0) {
				final ArrayList<cgCache> cacheListTemp = (ArrayList<cgCache>) cacheList.clone();
				for (cgCache cache : cacheListTemp) {
					if (cache.statusChecked != false) {
						app.moveToList(cache.geocode, listId);
					}
				}
			}
			
			handler.sendEmptyMessage(listId);
		}
	}
	
	private void createList() {
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		final View view = inflater.inflate(R.layout.list_create_dialog, null);
		final EditText input = (EditText) view.findViewById(R.id.text);

		alert.setTitle(R.string.list_dialog_create_title);
		alert.setView(view);
		alert.setPositiveButton(R.string.list_dialog_create, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				
				if (value != null && value.length() > 0) {
					int newId = app.createList(value);
					
					if (newId >= 10) {
						warning.showToast(res.getString(R.string.list_dialog_create_ok));
					} else {
						warning.showToast(res.getString(R.string.list_dialog_create_err));
					}
				}
			}
		});
		alert.setNegativeButton(res.getString(R.string.list_dialog_cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.dismiss();
			}
		});

		alert.show();
	}
	
	private void removeList() {
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(R.string.list_dialog_remove_title);
		alert.setMessage(R.string.list_dialog_remove_description);
		alert.setPositiveButton(R.string.list_dialog_remove, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				boolean status = app.removeList(listId);
				
				if (status) {
					warning.showToast(res.getString(R.string.list_dialog_remove_ok));
					switchListById(1);
				} else {
					warning.showToast(res.getString(R.string.list_dialog_remove_err));
				}
			}
		});
		alert.setNegativeButton(res.getString(R.string.list_dialog_cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.dismiss();
			}
		});

		alert.show();
	}

	public void goMap(View view) {
		showOnMap();
	}

	public void goHome(View view) {
		base.goHome(activity);
	}

	public void goManual(View view) {
		try {
			if (type != null && type.equals("offline") == true) {
				AppManualReaderClient.openManual(
						"c-geo",
						"c:geo-stored",
						activity,
						"http://cgeo.carnero.cc/manual/");
			} else if (type != null && type.equals("history") == true) {
				AppManualReaderClient.openManual(
						"c-geo",
						"c:geo-history",
						activity,
						"http://cgeo.carnero.cc/manual/");
			} else {
				AppManualReaderClient.openManual(
						"c-geo",
						"c:geo-nearby",
						activity,
						"http://cgeo.carnero.cc/manual/");
			}
		} catch (Exception e) {
			// nothing
		}
	}
}
