package carnero.cgeo;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
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
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;
import java.util.Locale;
import org.openintents.intents.AbstractWikitudeARIntent;
import org.openintents.intents.WikitudeARIntent;
import org.openintents.intents.WikitudePOI;

public class cgeocaches extends ListActivity {
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
    private Float northHeading = 0.0f;
	private cgGeo geo = null;
	private cgDirection dir = null;
	private cgUpdateLoc geoUpdate = new update();
	private cgUpdateDir dirUpdate = new updateDir();
	private String title = "";
    private int detailTotal = 0;
    private int detailProgress = 0;
    private Long detailProgressTime = 0l;
    private geocachesLoadDetails threadD = null;
	private boolean offline = false;
	private boolean progressBar = false;

	private Handler loadCachesHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				setAdapter();

				if (cacheList == null) {
                    warning.showToast("Sorry, c:geo failed to load cache list.");
					setMoreCaches(false);
				} else {
                    final Integer count = app.getTotal(searchId);
					final int size = cacheList.size();
                    if (count != null && count > 0) {
                        setTitle(title + " (" + size + "/" + count + ")");
						if (cacheList.size() < app.getTotal(searchId)) setMoreCaches(true);
						else setMoreCaches(false);
                    } else {
                        setTitle(title);
						setMoreCaches(false);
                    }
                }

				if (cacheList != null && app.getError(searchId) != null && app.getError(searchId).equalsIgnoreCase(base.errorRetrieve.get(-7)) == true) {
					AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
					dialog.setTitle("license");
					dialog.setMessage("You have not agreed with Geocaching.com license agreement, so c:geo can't load caches coordinates.");
					dialog.setCancelable(true);
					dialog.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
					   public void onClick(DialogInterface dialog, int id) {
						    settings.deleteCookies();
							dialog.cancel();
					   }
					});
					dialog.setPositiveButton("Show License", new DialogInterface.OnClickListener() {
					   public void onClick(DialogInterface dialog, int id) {
						    settings.deleteCookies();
							activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.geocaching.com/waypoints/agreement.aspx?ID=0")));
					   }
					});

				   AlertDialog alert = dialog.create();
				   alert.show();
				} else if (app != null && app.getError(searchId) != null && app.getError(searchId).length() > 0) {
					warning.showToast("Sorry, c:geo failed to download caches because of " + app.getError(searchId) + ".");

					if (progressBar == true) setProgressBarIndeterminateVisibility(false);
					if (waitDialog != null) {
						waitDialog.dismiss();
                        waitDialog.setOnCancelListener(null);
					}
					finish();
					return;
				}

				if (geo != null && geo.latitudeNow != null && geo.longitudeNow != null) {
					adapter.setActualCoordinates(geo.latitudeNow, geo.longitudeNow);
					adapter.setActualHeading(northHeading);
				}
			} catch (Exception e) {
				warning.showToast("Sorry, c:geo can\'t find any geocache.");
				Log.e(cgSettings.tag, "cgeocaches.loadCachesHandler: " + e.toString());

				if (progressBar == true) setProgressBarIndeterminateVisibility(false);
				if (waitDialog != null) {
					waitDialog.dismiss();
                    waitDialog.setOnCancelListener(null);
				}
				finish();
				return;
			}

			try {
				if (progressBar == true) setProgressBarIndeterminateVisibility(false);
				if (waitDialog != null) {
					waitDialog.dismiss();
                    waitDialog.setOnCancelListener(null);
				}
			} catch (Exception e2) {
				Log.e(cgSettings.tag, "cgeocaches.loadCachesHandler.2: " + e2.toString());
			}
		}
	};

	private Handler loadNextPageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				setAdapter();

				if (cacheList == null) {
                    warning.showToast("Sorry, c:geo failed to load cache list.");
					setMoreCaches(false);
				} else {
                    final Integer count = app.getTotal(searchId);
					final int size = cacheList.size();
                    if (count != null && count > 0) {
                        setTitle(title + " (" + size + "/" + count + ")");
						if (cacheList.size() < app.getTotal(searchId)) setMoreCaches(true);
						else setMoreCaches(false);
                    } else {
                        setTitle(title);
						setMoreCaches(false);
                    }
                }

				if (app.getError(searchId) != null && app.getError(searchId).length() > 0) {
					warning.showToast("Sorry, c:geo failed to download caches because of " + app.getError(searchId) + ".");

					if (progressBar == true) setProgressBarIndeterminateVisibility(false);
					if (waitDialog != null) {
						waitDialog.dismiss();
                        waitDialog.setOnCancelListener(null);
					}
					finish();
					return;
				}

				if (geo != null && geo.latitudeNow != null && geo.longitudeNow != null) {
					adapter.setActualCoordinates(geo.latitudeNow, geo.longitudeNow);
					adapter.setActualHeading(northHeading);
				}
			} catch (Exception e) {
				warning.showToast("c:geo can\'t find next geocaches.");
				Log.e(cgSettings.tag, "cgeocaches.loadNextPageHandler: " + e.toString());
			}

			if (progressBar == true) setProgressBarIndeterminateVisibility(false);
			if (waitDialog != null) {
				waitDialog.dismiss();
                waitDialog.setOnCancelListener(null);
			}
		}
	};

	private Handler loadDetailsHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			setAdapter();

            if (msg.what == 0) {
                if (waitDialog != null) {
                    Float diffTime = new Float((System.currentTimeMillis() - detailProgressTime) / 1000); // seconds left
                    Float oneCache = diffTime / detailProgress; // left time per cache
                    int etaTime = (int)((detailTotal - detailProgress) * oneCache / 60); // seconds remaining


                    waitDialog.setProgress(detailProgress);
					if (etaTime < 1) {
						waitDialog.setMessage("downloading caches...\neta: less than minute");
					} else if (etaTime == 1) {
						waitDialog.setMessage("downloading caches...\neta: " + etaTime + " min");
					} else {
						waitDialog.setMessage("downloading caches...\neta: " + etaTime + " mins");
					}
                }
            } else {
                if (cacheList != null && searchId != null) {
                    cacheList.clear();
                    cacheList.addAll(app.getCaches(searchId));
                }
                
				if (geo != null && geo.latitudeNow != null && geo.longitudeNow != null) {
					adapter.setActualCoordinates(geo.latitudeNow, geo.longitudeNow);
					adapter.setActualHeading(northHeading);
				}

				if (progressBar == true) setProgressBarIndeterminateVisibility(false);
                if (waitDialog != null) {
                    waitDialog.dismiss();
                    waitDialog.setOnCancelListener(null);
                }

				if (geo == null) geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);
				if (settings.livelist == 1 && settings.useCompass == 1 && dir == null) dir = app.startDir(activity, dirUpdate, warning);
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
		app.setAction(action);
        settings = new cgSettings(this, getSharedPreferences(cgSettings.preferences, 0));
        base = new cgBase(app, settings, getSharedPreferences(cgSettings.preferences, 0));
        warning = new cgWarning(this);

		// set layout
		progressBar = requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setTitle("caches");
		if (settings.skin == 1) setContentView(R.layout.caches_light);
		else setContentView(R.layout.caches_dark);

		init();

		String typeText;
		if (cachetype != null && base.cacheTypesInv.containsKey(cachetype) == true) typeText = "\ntype: " + base.cacheTypesInv.get(cachetype);
		else typeText = "\ntype: all cache types";

		Thread thread;
		if (type.equals("nearest")) {
			action = "pending";
			title = "nearby";
			setTitle(title);
			if (progressBar == true) setProgressBarIndeterminateVisibility(true);
			waitDialog = ProgressDialog.show(this, "searching for caches", "caches nearby" + typeText, true);
			waitDialog.setCancelable(true);
			thread = new geocachesLoadByCoords(loadCachesHandler,  latitude, longitude, cachetype);
			thread.start();
		} else if (type.equals("offline")) {
			title = "stored";
			offline = true;
			setTitle(title);
			if (progressBar == true) setProgressBarIndeterminateVisibility(true);
			waitDialog = ProgressDialog.show(this, "loading caches", "caches stored in device", true);
			waitDialog.setCancelable(true);
			thread = new geocachesLoadByOffline(loadCachesHandler, latitude, longitude);
			thread.start();
		} else if (type.equals("coordinate")) {
			action = "planning";
			title = base.formatCoordinate(latitude, "lat", true) + " | " + base.formatCoordinate(longitude, "lon", true);
			setTitle(title);
			if (progressBar == true) setProgressBarIndeterminateVisibility(true);
			waitDialog = ProgressDialog.show(this, "searching for caches", "caches near\n" + base.formatCoordinate(latitude, "lat", true) + " | " + base.formatCoordinate(longitude, "lon", true) + typeText, true);
			waitDialog.setCancelable(true);
			thread = new geocachesLoadByCoords(loadCachesHandler,  latitude, longitude, cachetype);
			thread.start();
		} else if (type.equals("keyword")) {
			title = keyword;
			setTitle(title);
			if (progressBar == true) setProgressBarIndeterminateVisibility(true);
			waitDialog = ProgressDialog.show(this, "searching for caches", "caches by keyword " + keyword + typeText, true);
			waitDialog.setCancelable(true);
			thread = new geocachesLoadByKeyword(loadCachesHandler,  keyword, cachetype);
			thread.start();
		} else if (type.equals("address")) {
			action = "planning";
			if (address != null && address.length() > 0) {
				title = address;
				setTitle(title);
				if (progressBar == true) setProgressBarIndeterminateVisibility(true);
				waitDialog = ProgressDialog.show(this, "searching for caches", "caches near\n" + address + typeText, true);
			} else {
				title = base.formatCoordinate(latitude, "lat", true) + " | " + base.formatCoordinate(longitude, "lon", true);
				setTitle(title);
				if (progressBar == true) setProgressBarIndeterminateVisibility(true);
				waitDialog = ProgressDialog.show(this, "searching for caches", "caches near\n" + base.formatCoordinate(latitude, "lat", true) + " | " + base.formatCoordinate(longitude, "lon", true) + typeText, true);
			}
			waitDialog.setCancelable(true);
			thread = new geocachesLoadByCoords(loadCachesHandler,  latitude, longitude, cachetype);
			thread.start();
		} else if (type.equals("username")) {
			title = username;
			setTitle(title);
			if (progressBar == true) setProgressBarIndeterminateVisibility(true);
			waitDialog = ProgressDialog.show(this, "searching for caches", "caches found by " + username + typeText, true);
			waitDialog.setCancelable(true);
			thread = new geocachesLoadByUserName(loadCachesHandler,  username, cachetype);
			thread.start();
		} else if (type.equals("owner")) {
			title = username;
			setTitle(title);
			if (progressBar == true) setProgressBarIndeterminateVisibility(true);
			waitDialog = ProgressDialog.show(this, "searching for caches", "caches hidden by " + username + typeText, true);
			waitDialog.setCancelable(true);
			thread = new geocachesLoadByOwner(loadCachesHandler,  username, cachetype);
			thread.start();
		} else {
			title = "caches";
			setTitle(title);
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

		setAdapter();

        if (geo == null) geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);
		if (settings.livelist == 1 && settings.useCompass == 1 && dir == null) dir = app.startDir(activity, dirUpdate, warning);

        if (searchId != null && searchId > 0) {
            cacheList.clear();
            cacheList.addAll(app.getCaches(searchId));
            
			if (geo != null && geo.latitudeNow != null && geo.longitudeNow != null) {
				adapter.setActualCoordinates(geo.latitudeNow, geo.longitudeNow);
				adapter.setActualHeading(northHeading);
			}
        }
	}

	@Override
	public void onDestroy() {
		if (adapter != null) adapter = null;
		if (geo != null) geo = app.removeGeo(geo);
		if (dir != null) dir = app.removeDir(dir);

		super.onDestroy();
	}

	@Override
	public void onStop() {
		if (geo != null) geo = app.removeGeo(geo);
		if (dir != null) dir = app.removeDir(dir);

		super.onStop();
	}

	@Override
	public void onPause() {
		if (geo != null) geo = app.removeGeo(geo);
		if (dir != null) dir = app.removeDir(dir);

		super.onPause();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        if (type.equals("offline") == true) {
            menu.add(0, 4, 0, "drop all").setIcon(android.R.drawable.ic_menu_delete); // delete saved caches
            menu.add(0, 1, 0, "refresh listed").setIcon(android.R.drawable.ic_menu_set_as); // download details for all caches
        } else {
            menu.add(0, 1, 0, "store for offline").setIcon(android.R.drawable.ic_menu_set_as); // download details for all caches
        }
		menu.add(0, 2, 0, "show on map").setIcon(android.R.drawable.ic_menu_mapmode); // show all caches on map
		menu.add(0, 3, 0, "show on wikitude").setIcon(android.R.drawable.ic_menu_search); // show all caches on wikitude
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
            case 1:
				if (offline == false) {
					detailTotal = app.getNotOfflineCount(searchId);

					if (detailTotal == 0) {
						warning.showToast("There is nothing to be saved.");

						return true;
					}
				} else {
					detailTotal = app.getCount(searchId);
				}

				if (progressBar == true) setProgressBarIndeterminateVisibility(true);
                waitDialog = new ProgressDialog(this);
                waitDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface arg0) {
                        try {
                            if (threadD != null) threadD.kill();

							if (geo == null) geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);
							if (settings.livelist == 1 && settings.useCompass == 1 && dir == null) dir = app.startDir(activity, dirUpdate, warning);
                        } catch (Exception e) {
                            Log.e(cgSettings.tag, "cgeocaches.onOptionsItemSelected.onCancel: " + e.toString());
                        }
                    }
                });
                waitDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                int etaTime = (int)((detailTotal * 7) / 60);
                if (etaTime < 1) {
                    waitDialog.setMessage("downloading caches...\neta: less than minute");
				} else if (etaTime == 1) {
                    waitDialog.setMessage("downloading caches...\neta: " + etaTime + " min");
                } else {
                    waitDialog.setMessage("downloading caches...\neta: " + etaTime + " mins");
                }
                waitDialog.setCancelable(true);
                waitDialog.setMax(detailTotal);
                waitDialog.show();

                detailProgressTime = System.currentTimeMillis();

                threadD = new geocachesLoadDetails(loadDetailsHandler);
                threadD.start();

                return true;
			case 2:
				showOnMap();
				return false;
			case 3:
				showOnWikitude();
				return false;
            case 4:
                dropStored();
                return false;
		}

		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		try {
			if (type.equals("offline") == false && (cacheList != null && app != null && cacheList.size() >= app.getTotal(searchId)) ) { // there are no more caches
				menu.findItem(0).setEnabled(false);
			}
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgeocaches.onPrepareOptionsMenu: " + e.toString());
		}

		return true;
	}

	private void setAdapter() {
		if (listFooter == null) {
			if (inflater == null) inflater = activity.getLayoutInflater();
			if (settings.skin == 1) listFooter = inflater.inflate(R.layout.caches_footer_light, null);
			else listFooter = inflater.inflate(R.layout.caches_footer_dark, null);
			listFooter.setClickable(true);
			listFooter.setOnClickListener(new moreCachesListener());
		}
		if (listFooterText == null) {
			listFooterText = (TextView)listFooter.findViewById(R.id.more_caches);
		}

		if (adapter == null) {
			getListView().addFooterView(listFooter);
			getListView().setOnLongClickListener(new longTapListener());
			adapter = new cgCacheListAdapter(activity, settings, cacheList, base);
			setListAdapter(adapter);
		}
	}

	private void setMoreCaches(boolean more) {
		if (listFooter == null) {
			if (inflater == null) inflater = activity.getLayoutInflater();
			if (settings.skin == 1) listFooter = inflater.inflate(R.layout.caches_footer_light, null);
			else listFooter = inflater.inflate(R.layout.caches_footer_dark, null);
			listFooter.setClickable(true);
			listFooter.setOnClickListener(new moreCachesListener());
		}
		if (listFooterText == null) {
			listFooterText = (TextView)listFooter.findViewById(R.id.more_caches);
		}

		if (more == false) {
			if (cacheList == null || cacheList.size() == 0) {
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
		// get parameters
		Bundle extras = getIntent().getExtras();
		if (extras !=null) {
			type = extras.getString("type");
			latitude = extras.getDouble("latitude");
			longitude = extras.getDouble("longitude");
			cachetype = extras.getString("cachetype");
			keyword = extras.getString("keyword");
			address = extras.getString("address");
			username = extras.getString("username");
		}

		setAdapter();

		// sensor & geolocation manager
        if (geo == null) geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);
		if (settings.livelist == 1 && settings.useCompass == 1 && dir == null) dir = app.startDir(activity, dirUpdate, warning);
		if (cacheList != null) setTitle(title + " (" + cacheList.size() + "/" + app.getTotal(searchId) + ")");
	}

	private void showOnMap() {
		cgeomap mapActivity = new cgeomap();

		Intent mapIntent = new Intent(activity, mapActivity.getClass());
		mapIntent.putExtra("detail", false);
        mapIntent.putExtra("searchid", searchId);

		activity.startActivity(mapIntent);
	}
	
	private void showOnWikitude() {
		try {
			WikitudeARIntent wikitudeIntent = new WikitudeARIntent(activity.getApplication(), "c:geo", "83D847F0BC8ECC72A2B21B868DF9B15A", "carnero", false);
			
			HashMap<String, Integer> poiIcons = new HashMap<String, Integer>();
			List<WikitudePOI> pois = new ArrayList<WikitudePOI>();

			if (poiIcons.isEmpty()) {
				poiIcons.put("ape", R.drawable.marker_cache_ape);
				poiIcons.put("cito", R.drawable.marker_cache_cito);
				poiIcons.put("earth", R.drawable.marker_cache_earth);
				poiIcons.put("event", R.drawable.marker_cache_event);
				poiIcons.put("letterbox", R.drawable.marker_cache_letterbox);
				poiIcons.put("locationless", R.drawable.marker_cache_locationless);
				poiIcons.put("mega", R.drawable.marker_cache_mega);
				poiIcons.put("multi", R.drawable.marker_cache_multi);
				poiIcons.put("traditional", R.drawable.marker_cache_traditional);
				poiIcons.put("virtual", R.drawable.marker_cache_virtual);
				poiIcons.put("webcam", R.drawable.marker_cache_webcam);
				poiIcons.put("wherigo", R.drawable.marker_cache_wherigo);
				poiIcons.put("mystery", R.drawable.marker_cache_mystery);
				poiIcons.put("ape-found", R.drawable.marker_cache_ape_found);
				poiIcons.put("cito-found", R.drawable.marker_cache_cito_found);
				poiIcons.put("earth-found", R.drawable.marker_cache_earth_found);
				poiIcons.put("event-found", R.drawable.marker_cache_event_found);
				poiIcons.put("letterbox-found", R.drawable.marker_cache_letterbox_found);
				poiIcons.put("locationless-found", R.drawable.marker_cache_locationless_found);
				poiIcons.put("mega-found", R.drawable.marker_cache_mega_found);
				poiIcons.put("multi-found", R.drawable.marker_cache_multi_found);
				poiIcons.put("traditional-found", R.drawable.marker_cache_traditional_found);
				poiIcons.put("virtual-found", R.drawable.marker_cache_virtual_found);
				poiIcons.put("webcam-found", R.drawable.marker_cache_webcam_found);
				poiIcons.put("wherigo-found", R.drawable.marker_cache_wherigo_found);
				poiIcons.put("mystery-found", R.drawable.marker_cache_mystery_found);
				poiIcons.put("ape-disabled", R.drawable.marker_cache_ape_disabled);
				poiIcons.put("cito-disabled", R.drawable.marker_cache_cito_disabled);
				poiIcons.put("earth-disabled", R.drawable.marker_cache_earth_disabled);
				poiIcons.put("event-disabled", R.drawable.marker_cache_event_disabled);
				poiIcons.put("letterbox-disabled", R.drawable.marker_cache_letterbox_disabled);
				poiIcons.put("locationless-disabled", R.drawable.marker_cache_locationless_disabled);
				poiIcons.put("mega-disabled", R.drawable.marker_cache_mega_disabled);
				poiIcons.put("multi-disabled", R.drawable.marker_cache_multi_disabled);
				poiIcons.put("traditional-disabled", R.drawable.marker_cache_traditional_disabled);
				poiIcons.put("virtual-disabled", R.drawable.marker_cache_virtual_disabled);
				poiIcons.put("webcam-disabled", R.drawable.marker_cache_webcam_disabled);
				poiIcons.put("wherigo-disabled", R.drawable.marker_cache_wherigo_disabled);
				poiIcons.put("mystery-disabled", R.drawable.marker_cache_mystery_disabled);
			}

			WikitudePOI poi = null;
			for (cgCache cache : cacheList) {
				if (cache.latitude == null || cache.longitude == null) {
					continue;
				}

				Resources res = getResources();
				String iconresource = null;
				String cacheType = null;

				if (cache.found == true) {
					cacheType = cache.type + "-found";
				} else if (cache.disabled == true) {
					cacheType = cache.type + "-disabled";
				} else {
					cacheType = cache.type;
				}

				if (poiIcons.containsKey(cache.type) == true) { // cache icon
					iconresource = res.getResourceName(poiIcons.get(cacheType));
				} else { // unknown cache type, "mystery" icon
					iconresource = res.getResourceName(poiIcons.get("mystery"));
				}

				poi = new WikitudePOI(cache.latitude, cache.longitude, 0.0, cache.geocode.toUpperCase(), cache.name);
				
				poi.setIconresource(iconresource);
				poi.setDetailAction("wikitudeapi.arcallback");
				poi.setLink("http://www.geocaching.com/seek/cache_details.aspx?wp=" + cache.geocode);

				pois.add(poi);
			}

			if (title == null || title.length() == 0) {
				title = "c:geo";
			} else {
				title = "c:geo ~ " + title;
			}
			
			wikitudeIntent.addTitleText(title);
			wikitudeIntent.setPrintMarkerSubText(false);
			wikitudeIntent.addPOIs(pois);

			((cgeoapplication)activity.getApplication()).setPois(pois);

			try {
				wikitudeIntent.startIntent(activity);
			} catch (Exception e) {
				AbstractWikitudeARIntent.handleWikitudeNotFound(activity, "Wikitude", "c:geo can\'t find proper version of Wikitude. Install it?");
			}
		} catch (Exception e) {
		 	Log.e(cgSettings.tag, "cgeodetail.showOnWikitude (cache): " + e.toString());
		}
	}

    public void dropStored() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        dialog.setTitle("Drop stored");
        dialog.setMessage("Do you want to delete all caches stored for offline use?");
        dialog.setCancelable(true);
        dialog.setPositiveButton("yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				(new Thread() {
					@Override
					public void run() {
						app.dropStored();
					}
				}).start();
				dialog.cancel();
				activity.finish();
			}
        });
        dialog.setNegativeButton("no", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
           }
        });

       AlertDialog alert = dialog.create();
       alert.show();
    }

	private class update extends cgUpdateLoc {
		@Override
		public void updateLoc(cgGeo geo) {
			if (geo == null) return;
			if (adapter == null) return;

			try {
				if (settings.useCompass == 0) {
					if (settings.useCompass == 0) {
						if (geo.bearingNow != null) adapter.setActualHeading(geo.bearingNow);
						else adapter.setActualHeading(0.0f);
					}
					if (northHeading != null) adapter.setActualHeading(northHeading);
				}
			} catch (Exception e) {
				Log.w(cgSettings.tag, "Failed to update location.");
			}
		}
	}
	
	private class updateDir extends cgUpdateDir {
		@Override
		public void updateDir(cgDirection dir) {
			if (settings.livelist == 0) return;
			if (dir == null) return;

			northHeading = dir.directionNow;
			if (northHeading != null && adapter != null) adapter.setActualHeading(northHeading);
		}
	}

	private class geocachesLoadNextPage extends Thread {
		private Handler handler = null;

		public geocachesLoadNextPage(Handler handlerIn) {
			handler = handlerIn;
		}

		@Override
		public void run() {
			searchId = base.searchByNextPage(searchId, 0);
			
			if (searchId != null && searchId > 0 && app.getCount(searchId) > 0) {
				cacheList.clear();
				cacheList.addAll(app.getCaches(searchId));
			}

			handler.sendMessage(new Message());
		}
	}

	private class geocachesLoadByOffline extends Thread {
		private Handler handler = null;
		private Double latitude = null;
		private Double longitude = null;

		public geocachesLoadByOffline(Handler handlerIn, Double latitudeIn, Double longitudeIn) {
			handler = handlerIn;
			latitude = latitudeIn;
			longitude = longitudeIn;
		}

		@Override
		public void run() {
			HashMap<String, Object> params = new HashMap<String, Object>();
			if (latitude != null && longitude != null) {
				params.put("latitude", latitude);
				params.put("longitude", longitude);
				params.put("cachetype", settings.cacheType);
			}

			searchId = base.searchByOffline(params);

			if (searchId != null && searchId > 0 && app.getCount(searchId) > 0) {
				cacheList.clear();
				cacheList.addAll(app.getCaches(searchId));
			}

			handler.sendMessage(new Message());
		}
	}

	private class geocachesLoadByCoords extends Thread {
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
				warning.showToast("No coordinates given.");
				
				finish();
				return;
			}
		}

		@Override
		public void run() {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("latitude", String.format((Locale)null, "%.6f", latitude));
			params.put("longitude", String.format((Locale)null, "%.6f", longitude));
			params.put("cachetype", cachetype);

			searchId = base.searchByCoords(params, 0);

			if (searchId != null && searchId > 0 && app.getCount(searchId) > 0) {
				cacheList.clear();
				cacheList.addAll(app.getCaches(searchId));
			}

			handler.sendMessage(new Message());
		}
	}
	
	private class geocachesLoadByKeyword extends Thread {
		private Handler handler = null;
		private String keyword = null;
		private String cachetype = null;

		public geocachesLoadByKeyword(Handler handlerIn, String keywordIn, String cachetypeIn) {
			setPriority(Thread.MIN_PRIORITY);

			handler = handlerIn;
			keyword = keywordIn;
			cachetype = cachetypeIn;

			if (keyword == null) {
				warning.showToast("No keyword given.");

				finish();
				return;
			}
		}

		@Override
		public void run() {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("keyword", keyword);
			params.put("cachetype", cachetype);

			searchId = base.searchByKeyword(params, 0);

			if (searchId != null && searchId > 0 && app.getCount(searchId) > 0) {
				cacheList.clear();
				cacheList.addAll(app.getCaches(searchId));
			}

			handler.sendMessage(new Message());
		}
	}

	private class geocachesLoadByUserName extends Thread {
		private Handler handler = null;
		private String username = null;
		private String cachetype = null;

		public geocachesLoadByUserName(Handler handlerIn, String usernameIn, String cachetypeIn) {
			setPriority(Thread.MIN_PRIORITY);

			handler = handlerIn;
			username = usernameIn;
			cachetype = cachetypeIn;

			if (username == null || username.length() == 0) {
				warning.showToast("No username given.");

				finish();
				return;
			}
		}

		@Override
		public void run() {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("username", username);
			params.put("cachetype", cachetype);

			searchId = base.searchByUsername(params, 0);

			if (searchId != null && searchId > 0 && app.getCount(searchId) > 0) {
				cacheList.clear();
				cacheList.addAll(app.getCaches(searchId));
			}

			handler.sendMessage(new Message());
		}
	}

	private class geocachesLoadByOwner extends Thread {
		private Handler handler = null;
		private String username = null;
		private String cachetype = null;

		public geocachesLoadByOwner(Handler handlerIn, String usernameIn, String cachetypeIn) {
			setPriority(Thread.MIN_PRIORITY);

			handler = handlerIn;
			username = usernameIn;
			cachetype = cachetypeIn;

			if (username == null || username.length() == 0) {
				warning.showToast("No username given.");

				finish();
				return;
			}
		}

		@Override
		public void run() {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("username", username);
			params.put("cachetype", cachetype);

			searchId = base.searchByOwner(params, 0);
			
			if (searchId != null && searchId > 0 && app.getCount(searchId) > 0) {
				cacheList.clear();
				cacheList.addAll(app.getCaches(searchId));
			}

			handler.sendMessage(new Message());
		}
	}

	private class geocachesLoadDetails extends Thread {
		private Handler handler = null;
        private volatile Boolean needToStop = false;

		public geocachesLoadDetails(Handler handlerIn) {
			setPriority(Thread.MIN_PRIORITY);

			handler = handlerIn;
		}

        public void kill() {
            this.needToStop = true;
        }

		@Override
		public void run() {
			if (geo != null) geo = app.removeGeo(geo);
			if (dir != null) dir = app.removeDir(dir);
			
            Message msg;
            HashMap<String, String> params;

            ArrayList<cgCache> caches = app.getCaches(searchId);

            for (cgCache cache : caches) {
                try {
                    if (needToStop == true) {
                        Log.i(cgSettings.tag, "Stopped storing process.");
                        break;
                    }

					try {
						sleep(3000 + ((Double)(Math.random() * 3000)).intValue());
					} catch (Exception e) {
						Log.e(cgSettings.tag, "cgeocaches.geocachesLoadDetails.sleep: " + e.toString());
					}

                    if (needToStop == true) {
                        Log.i(cgSettings.tag, "Stopped storing process.");
                        break;
                    }

                    detailProgress ++;
					base.storeCache(app, activity, cache, null, handler);

                    msg = new Message();
                    msg.what = 0;
                    handler.sendMessage(msg);

                    this.yield();
                } catch (Exception e) {
                    Log.e(cgSettings.tag, "cgeocaches.geocachesLoadDetails: " + e.toString());
                }
            }

            msg = new Message();
            msg.what = 1;
			handler.sendMessage(msg);
		}
	}

	private class moreCachesListener implements View.OnClickListener {
		@Override
		public void onClick(View arg0) {
			if (progressBar == true) setProgressBarIndeterminateVisibility(true);

			geocachesLoadNextPage thread;
			thread = new geocachesLoadNextPage(loadNextPageHandler);
			thread.start();
		}
	}

	private class longTapListener implements View.OnLongClickListener {
		@Override
		public boolean onLongClick(View view) {
			getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			
			return true;
		}
	}
}
