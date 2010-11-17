package carnero.cgeo;

import java.util.ArrayList;
import java.util.HashMap;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.widget.ScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.view.SubMenu;
import android.widget.Button;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import java.util.Date;
import java.util.Locale;

public class cgeodetail extends Activity {
	private GoogleAnalyticsTracker tracker = null;
	public Long searchId = null;
	public cgCache cache = null;
	public String geocode = null;
	public String name = null;
	public String guid = null;
	private Resources res = null;
	private Activity activity = null;
	private LayoutInflater inflater = null;
	private cgeoapplication app = null;
	private cgSettings settings = null;
	private cgBase base = null;
	private cgWarning warning = null;
	private cgGeo geo = null;
	private cgUpdateLoc geoUpdate = new update();
	private TextView cacheDistance = null;
	private ProgressDialog waitDialog = null;
	private ProgressDialog descDialog = null;
	private Spanned longDesc = null;
	private Boolean longDescDisplayed = false;
	private loadCache threadCache = null;
	private loadLongDesc threadLongDesc = null;
	private HashMap<String, Integer> gcIcons = new HashMap<String, Integer>();
	private ProgressDialog storeDialog = null;
	private ProgressDialog refreshDialog = null;
	private ProgressDialog dropDialog = null;
	private HashMap<Integer, String> calendars = new HashMap<Integer, String>();
	private Handler storeCacheHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				cache = app.getCache(searchId); // reload cache details
			} catch (Exception e) {
				warning.showToast("Sorry, c:geo can\'t store geocache.");

				Log.e(cgSettings.tag, "cgeodetail.storeCacheHandler: " + e.toString());
			}

			setView();
		}
	};

	private Handler refreshCacheHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				cache = app.getCache(searchId); // reload cache details
			} catch (Exception e) {
				warning.showToast("Sorry, c:geo can\'t refresh geocache.");

				Log.e(cgSettings.tag, "cgeodetail.refreshCacheHandler: " + e.toString());
			}

			setView();
		}
	};

	private Handler dropCacheHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				cache = app.getCache(searchId); // reload cache details
			} catch (Exception e) {
				warning.showToast("Sorry, c:geo can\'t drop geocache.");

				Log.e(cgSettings.tag, "cgeodetail.dropCacheHandler: " + e.toString());
			}

			setView();
		}
	};

	private Handler loadCacheHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (searchId == null || searchId <= 0) {
				warning.showToast("Sorry, c:geo failed to download cache details.");

				finish();
				return;
			}

			if (app.getError(searchId) != null) {
				warning.showToast("Sorry, c:geo failed to download cache details because of " + app.getError(searchId) + ".");

				finish();
				return;
			}

			setView();

			if (settings.autoLoadDesc == 1) {
				try {
					loadLongDesc();
				} catch (Exception e) {
					// activity is not visible
				}
			}
		}
	};

	private Handler loadDescriptionHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (longDesc == null && cache != null && cache.description != null) {
				longDesc = Html.fromHtml(cache.description.trim(), new cgHtmlImg(activity, settings, geocode, true, cache.reason, false), null);
			}

			if (longDesc != null) {
				((LinearLayout) findViewById(R.id.desc_box)).setVisibility(View.VISIBLE);

				TextView descView = (TextView) findViewById(R.id.description);
				descView.setVisibility(View.VISIBLE);
				descView.setText(longDesc, TextView.BufferType.SPANNABLE);
				descView.setMovementMethod(LinkMovementMethod.getInstance());

				Button showDesc = (Button) findViewById(R.id.show_description);
				showDesc.setVisibility(View.GONE);
				showDesc.setOnTouchListener(null);
				showDesc.setOnClickListener(null);
			} else {
				warning.showToast("Sorry, c:geo can't load description.");
			}

			if (descDialog != null && descDialog.isShowing()) {
				descDialog.dismiss();
			}

			longDescDisplayed = true;
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// google analytics
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start(cgSettings.analytics, this);
		tracker.dispatch();
		tracker.trackPageView("/cache/detail");

		// init
		activity = this;
		res = this.getResources();
		app = (cgeoapplication) this.getApplication();
		settings = new cgSettings(this, getSharedPreferences(cgSettings.preferences, 0));
		base = new cgBase(app, settings, getSharedPreferences(cgSettings.preferences, 0));
		warning = new cgWarning(this);

		// set layout
		setTitle(res.getString(R.string.cache));
		if (settings.skin == 1) {
			setContentView(R.layout.cachedetail_light);
		} else {
			setContentView(R.layout.cachedetail_dark);
		}

		init();

		// get parameters
		Bundle extras = getIntent().getExtras();
		Uri uri = getIntent().getData();

		// try to get data from extras
		if (geocode == null && extras != null) {
			geocode = extras.getString("geocode");
			name = extras.getString("name");
			guid = extras.getString("guid");
		}

		// try to get data from URI
		if (geocode == null && guid == null && uri != null) {
			String uriHost = uri.getHost().toLowerCase();
			String uriPath = uri.getPath().toLowerCase();
			String uriQuery = uri.getQuery();

			if (uriQuery != null) {
				Log.i(cgSettings.tag, "Opening URI: " + uriHost + uriPath + "?" + uriQuery);
			} else {
				Log.i(cgSettings.tag, "Opening URI: " + uriHost + uriPath);
			}

			if (uriHost.contains("geocaching.com") == true) {
				geocode = uri.getQueryParameter("wp");
				guid = uri.getQueryParameter("guid");

				if (geocode != null && geocode.length() > 0) {
					geocode = geocode.toUpperCase();
					guid = null;
				} else if (guid != null && guid.length() > 0) {
					geocode = null;
					guid = guid.toLowerCase();
				} else {
					warning.showToast(res.getString(R.string.err_detail_open));
					finish();
					return;
				}
			} else if (uriHost.contains("coord.info") == true) {
				if (uriPath != null && uriPath.startsWith("/gc") == true) {
					geocode = uriPath.substring(1).toUpperCase();
				} else {
					warning.showToast(res.getString(R.string.err_detail_open));
					finish();
					return;
				}
			}
		}

		// no given data
		if (geocode == null && guid == null) {
			warning.showToast(res.getString(R.string.err_detail_cache));
			finish();
			return;
		}

		app.setAction(geocode);

		try {
			if (name != null && name.length() > 0) {
				waitDialog = ProgressDialog.show(this, name, res.getString(R.string.cache_dialog_loading_details), true);
			} else if (geocode != null && geocode.length() > 0) {
				waitDialog = ProgressDialog.show(this, geocode.toUpperCase(), res.getString(R.string.cache_dialog_loading_details), true);
			} else {
				waitDialog = ProgressDialog.show(this, res.getString(R.string.cache), res.getString(R.string.cache_dialog_loading_details), true);
			}
			waitDialog.setCancelable(true);
		} catch (Exception e) {
			// nothing, we lost the window
		}

		threadCache = new loadCache(loadCacheHandler, geocode, guid);
		threadCache.start();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		setView();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (geo == null) {
			geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);
		}
		setView();
	}

	@Override
	public void onDestroy() {
		if (geo != null) {
			geo = app.removeGeo();
		}
		if (tracker != null) tracker.stop();

		super.onDestroy();
	}

	@Override
	public void onStop() {
		if (geo != null) {
			geo = app.removeGeo();
		}

		super.onStop();
	}

	@Override
	public void onPause() {
		if (geo != null) {
			geo = app.removeGeo();
		}

		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (cache.latitude != null && cache.longitude != null) {
			menu.add(0, 2, 0, res.getString(R.string.cache_menu_compass)).setIcon(android.R.drawable.ic_menu_compass); // compass

			SubMenu subMenu = menu.addSubMenu(1, 0, 0, res.getString(R.string.cache_menu_navigate)).setIcon(android.R.drawable.ic_menu_more);
			subMenu.add(0, 8, 0, res.getString(R.string.cache_menu_radar)); // radar
			subMenu.add(0, 1, 0, res.getString(R.string.cache_menu_map)); // google maps
			subMenu.add(0, 10, 0, res.getString(R.string.cache_menu_map_ext)); // external map
			if (cache != null && cache.reason == 1) {
				subMenu.add(1, 6, 0, res.getString(R.string.cache_menu_map_static)); // static maps
			}
			subMenu.add(0, 9, 0, res.getString(R.string.cache_menu_tbt)); // turn-by-turn
		}

		menu.add(1, 7, 0, res.getString(R.string.cache_menu_browser)).setIcon(android.R.drawable.ic_menu_info_details); // browser
		if (cache != null && cache.hidden != null && (cache.type.equalsIgnoreCase("event") == true || cache.type.equalsIgnoreCase("mega") == true || cache.type.equalsIgnoreCase("cito") == true)) {
			menu.add(1, 12, 0, res.getString(R.string.cache_menu_event)).setIcon(android.R.drawable.ic_menu_agenda); // add event to calendar
		}
		if (settings.isLogin() == true) {
			menu.add(1, 3, 0, res.getString(R.string.cache_menu_visit)).setIcon(android.R.drawable.ic_menu_agenda); // log visit
		}

		if (cache != null && cache.spoilers != null && cache.spoilers.size() > 0) {
			menu.add(1, 5, 0, res.getString(R.string.cache_menu_spoilers)).setIcon(android.R.drawable.ic_menu_gallery); // spoiler images
		}

		if (cache.latitude != null && cache.longitude != null) {
			menu.add(0, 11, 0, res.getString(R.string.cache_menu_around)).setIcon(android.R.drawable.ic_menu_rotate); // caches around
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case 1:
				showOnMap();
				return true;
			case 2:
				navigateTo();
				return true;
			case 3:
				logVisit();
				return true;
			case 5:
				showSpoilers();
				return true;
			case 6:
				showSmaps();
				return true;
			case 7:
				activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.geocaching.com/seek/cache_details.aspx?wp=" + cache.geocode)));
				return true;
			case 8:
				radarTo();
				return true;
			case 9:
				if (geo != null) {
					base.runNavigation(activity, res, settings, warning, cache.latitude, cache.longitude, geo.latitudeNow, geo.longitudeNow);
				} else {
					base.runNavigation(activity, res, settings, warning, cache.latitude, cache.longitude);
				}
				
				return true;
			case 10:
				base.runExternalMap(activity, res, warning, cache.latitude, cache.longitude, cache.geocode, cache.name);
				
				return true;
			case 11:
				cachesAround();
				return true;
			case 12:
				addToCalendar();
				return true;
		}

		return false;
	}

	private void init() {
		if (inflater == null) {
			inflater = activity.getLayoutInflater();
		}
		if (geo == null) {
			geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);
		}

		if (searchId != null && searchId > 0) {
			cache = app.getCache(searchId);
			if (cache != null && cache.geocode != null) {
				geocode = cache.geocode;
			}
		}

		if (geocode != null && geocode.length() > 0) {
			app.setAction(geocode);
		}
	}

	private void setView() {
		RelativeLayout itemLayout;
		TextView itemName;
		TextView itemValue;
		LinearLayout itemStars;

		if (searchId == null) return;

		cache = app.getCache(searchId);

		if (cache == null) {
			if (waitDialog != null && waitDialog.isShowing()) waitDialog.dismiss();

			if (geocode != null && geocode.length() > 0) {
				warning.showToast(res.getString(R.string.err_detail_cache_find) + " " + geocode + ".");
			} else {
				geocode = null;
				warning.showToast(res.getString(R.string.err_detail_cache_find_some));
			}

			finish();
			return;
		}

		if (app.warnedLanguage == false && cache.reason == 0 && (cache.owner == null || cache.owner.length() == 0 || cache.hidden == null)) {
			warning.helpDialog(res.getString(R.string.err_title_problem), res.getString(R.string.err_detail_cache_language));
			app.warnedLanguage = true;
		}

		try {
			if (gcIcons == null || gcIcons.isEmpty()) {
				gcIcons.put("ape", R.drawable.type_ape);
				gcIcons.put("cito", R.drawable.type_cito);
				gcIcons.put("earth", R.drawable.type_earth);
				gcIcons.put("event", R.drawable.type_event);
				gcIcons.put("letterbox", R.drawable.type_letterbox);
				gcIcons.put("locationless", R.drawable.type_locationless);
				gcIcons.put("mega", R.drawable.type_mega);
				gcIcons.put("multi", R.drawable.type_multi);
				gcIcons.put("traditional", R.drawable.type_traditional);
				gcIcons.put("virtual", R.drawable.type_virtual);
				gcIcons.put("webcam", R.drawable.type_webcam);
				gcIcons.put("wherigo", R.drawable.type_wherigo);
				gcIcons.put("mystery", R.drawable.type_mystery);
			}

			if (cache.name != null && cache.name.length() > 0) {
				setTitle(cache.name);
			} else {
				setTitle(geocode.toUpperCase());
			}

			inflater = activity.getLayoutInflater();
			geocode = cache.geocode.toUpperCase();

			((ScrollView) findViewById(R.id.details_list_box)).setVisibility(View.VISIBLE);
			LinearLayout detailsList = (LinearLayout) findViewById(R.id.details_list);
			detailsList.removeAllViews();

			// cache type
			if (settings.skin == 1) {
				itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_light, null);
			} else {
				itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_dark, null);
			}
			itemName = (TextView) itemLayout.findViewById(R.id.name);
			itemValue = (TextView) itemLayout.findViewById(R.id.value);

			itemName.setText(res.getString(R.string.cache_type));

			String size = null;
			if (cache.size != null && cache.size.length() > 0) size = " (" + cache.size + ")";
			else size = "";
			
			if (base.cacheTypesInv.containsKey(cache.type) == true) { // cache icon
				itemValue.setText(base.cacheTypesInv.get(cache.type) + size);
			} else {
				itemValue.setText(base.cacheTypesInv.get("mystery") + size);
			}
			if (cache.type != null && gcIcons.containsKey(cache.type) == true) { // cache icon
				itemValue.setCompoundDrawablesWithIntrinsicBounds((Drawable) activity.getResources().getDrawable(gcIcons.get(cache.type)), null, null, null);
			} else { // unknown cache type, "mystery" icon
				itemValue.setCompoundDrawablesWithIntrinsicBounds((Drawable) activity.getResources().getDrawable(gcIcons.get("mystery")), null, null, null);
			}
			detailsList.addView(itemLayout);

			// gc-code
			if (settings.skin == 1) {
				itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_light, null);
			} else {
				itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_dark, null);
			}
			itemName = (TextView) itemLayout.findViewById(R.id.name);
			itemValue = (TextView) itemLayout.findViewById(R.id.value);

			itemName.setText(res.getString(R.string.cache_geocode));
			itemValue.setText(cache.geocode.toUpperCase());
			detailsList.addView(itemLayout);

			// cache state
			if (cache.logOffline == true || cache.archived == true || cache.disabled == true || cache.members == true || cache.found == true) {
				if (settings.skin == 1) {
					itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_light, null);
				} else {
					itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_dark, null);
				}
				itemName = (TextView) itemLayout.findViewById(R.id.name);
				itemValue = (TextView) itemLayout.findViewById(R.id.value);

				itemName.setText(res.getString(R.string.cache_status));

				StringBuilder state = new StringBuilder();
				if (cache.logOffline == true) {
					if (state.length() > 0) {
						state.append(", ");
					}
					state.append(res.getString(R.string.cache_status_offline_log));
				}
				if (cache.found == true) {
					if (state.length() > 0) {
						state.append(", ");
					}
					state.append(res.getString(R.string.cache_status_found));
				}
				if (cache.archived == true) {
					if (state.length() > 0) {
						state.append(", ");
					}
					state.append(res.getString(R.string.cache_status_archived));
				}
				if (cache.disabled == true) {
					if (state.length() > 0) {
						state.append(", ");
					}
					state.append(res.getString(R.string.cache_status_disabled));
				}
				if (cache.members == true) {
					if (state.length() > 0) {
						state.append(", ");
					}
					state.append(res.getString(R.string.cache_status_premium));
				}

				itemValue.setText(state.toString());
				detailsList.addView(itemLayout);

				state = null;
			}

			// distance
			if (settings.skin == 1) {
				itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_light, null);
			} else {
				itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_dark, null);
			}
			itemName = (TextView) itemLayout.findViewById(R.id.name);
			itemValue = (TextView) itemLayout.findViewById(R.id.value);

			itemName.setText(res.getString(R.string.cache_distance));
			if (cache.distance != null) {
				itemValue.setText("~" + base.getHumanDistance(cache.distance));
			} else {
				itemValue.setText("--");
			}
			detailsList.addView(itemLayout);
			cacheDistance = itemValue;

			// difficulty
			if (cache.difficulty != null && cache.difficulty > 0) {
				if (settings.skin == 1) {
					itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_light_layout, null);
				} else {
					itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_dark_layout, null);
				}
				itemName = (TextView) itemLayout.findViewById(R.id.name);
				itemValue = (TextView) itemLayout.findViewById(R.id.value);
				itemStars = (LinearLayout) itemLayout.findViewById(R.id.stars);

				itemName.setText(res.getString(R.string.cache_difficulty));
				itemValue.setText(String.format(Locale.getDefault(), "%.1f", cache.difficulty) + " of 5");
				for (int i = 0; i <= 4; i++) {
					ImageView star = (ImageView) inflater.inflate(R.layout.star, null);
					if ((cache.difficulty - i) >= 1.0) {
						star.setImageResource(R.drawable.star_on);
					} else if ((cache.difficulty - i) > 0.0) {
						star.setImageResource(R.drawable.star_half);
					} else {
						star.setImageResource(R.drawable.star_off);
					}
					itemStars.addView(star, (1 + i));
				}
				detailsList.addView(itemLayout);
			}

			// terrain
			if (cache.terrain != null && cache.terrain > 0) {
				if (settings.skin == 1) {
					itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_light_layout, null);
				} else {
					itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_dark_layout, null);
				}
				itemName = (TextView) itemLayout.findViewById(R.id.name);
				itemValue = (TextView) itemLayout.findViewById(R.id.value);
				itemStars = (LinearLayout) itemLayout.findViewById(R.id.stars);

				itemName.setText(res.getString(R.string.cache_terrain));
				itemValue.setText(String.format(Locale.getDefault(), "%.1f", cache.terrain) + " of 5");
				for (int i = 0; i <= 4; i++) {
					ImageView star = (ImageView) inflater.inflate(R.layout.star, null);
					if ((cache.terrain - i) >= 1.0) {
						star.setImageResource(R.drawable.star_on);
					} else if ((cache.terrain - i) > 0.0) {
						star.setImageResource(R.drawable.star_half);
					} else {
						star.setImageResource(R.drawable.star_off);
					}
					itemStars.addView(star, (1 + i));
				}
				detailsList.addView(itemLayout);
			}

			// rating
			if (cache.rating != null && cache.rating > 0) {
				if (settings.skin == 1) {
					itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_light_layout, null);
				} else {
					itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_dark_layout, null);
				}
				itemName = (TextView) itemLayout.findViewById(R.id.name);
				itemValue = (TextView) itemLayout.findViewById(R.id.value);
				itemStars = (LinearLayout) itemLayout.findViewById(R.id.stars);

				itemName.setText(res.getString(R.string.cache_rating));
				itemValue.setText(String.format(Locale.getDefault(), "%.1f", cache.rating) + " of 5");
				for (int i = 0; i <= 4; i++) {
					ImageView star = (ImageView) inflater.inflate(R.layout.star, null);
					if ((cache.rating - i) >= 1.0) {
						star.setImageResource(R.drawable.star_on);
					} else if ((cache.rating - i) > 0.0) {
						star.setImageResource(R.drawable.star_half);
					} else {
						star.setImageResource(R.drawable.star_off);
					}
					itemStars.addView(star, (1 + i));
				}
				if (cache.votes != null) {
					final TextView itemAddition = (TextView)itemLayout.findViewById(R.id.addition);
					itemAddition.setText("(" + cache.votes + ")");
					itemAddition.setVisibility(View.VISIBLE);
				}
				detailsList.addView(itemLayout);
			}

			itemStars = null;

			// cache author
			if (cache.owner != null && cache.owner.length() > 0) {
				if (settings.skin == 1) {
					itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_light, null);
				} else {
					itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_dark, null);
				}
				itemName = (TextView) itemLayout.findViewById(R.id.name);
				itemValue = (TextView) itemLayout.findViewById(R.id.value);

				itemName.setText(res.getString(R.string.cache_owner));
				itemValue.setText(Html.fromHtml(cache.owner), TextView.BufferType.SPANNABLE);
				detailsList.addView(itemLayout);
			}

			// cache hidden
			if (cache.hidden != null && cache.hidden.getTime() > 0) {
				if (settings.skin == 1) {
					itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_light, null);
				} else {
					itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_dark, null);
				}
				itemName = (TextView) itemLayout.findViewById(R.id.name);
				itemValue = (TextView) itemLayout.findViewById(R.id.value);

				if (cache.type.equalsIgnoreCase("event") == true || cache.type.equalsIgnoreCase("mega") == true || cache.type.equalsIgnoreCase("cito") == true) {
					itemName.setText(res.getString(R.string.cache_event));
				} else {
					itemName.setText(res.getString(R.string.cache_hidden));
				}
				itemValue.setText(base.dateOut.format(cache.hidden));
				detailsList.addView(itemLayout);
			}

			// cache location
			if (cache.location != null && cache.location.length() > 0) {
				if (settings.skin == 1) {
					itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_light, null);
				} else {
					itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_dark, null);
				}
				itemName = (TextView) itemLayout.findViewById(R.id.name);
				itemValue = (TextView) itemLayout.findViewById(R.id.value);

				itemName.setText(res.getString(R.string.cache_location));
				itemValue.setText(cache.location);
				detailsList.addView(itemLayout);
			}

			// cache coordinates
			if (cache.latitude != null && cache.longitude != null) {
				if (settings.skin == 1) {
					itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_light, null);
				} else {
					itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_dark, null);
				}
				itemName = (TextView) itemLayout.findViewById(R.id.name);
				itemValue = (TextView) itemLayout.findViewById(R.id.value);

				itemName.setText(res.getString(R.string.cache_coordinates));
				itemValue.setText(cache.latitudeString + " | " + cache.longitudeString);
				detailsList.addView(itemLayout);
			}

			// cache attributes
			if (cache.attributes != null && cache.attributes.size() > 0) {
				final LinearLayout attribBox = (LinearLayout) findViewById(R.id.attributes_box);
				final TextView attribView = (TextView) findViewById(R.id.attributes);

				attribView.setText(base.implode("\n", cache.attributes.toArray()));
				attribBox.setVisibility(View.VISIBLE);
			}

			// cache inventory
			if (cache.inventory != null && cache.inventory.size() > 0) {
				final LinearLayout inventBox = (LinearLayout) findViewById(R.id.inventory_box);
				final TextView inventView = (TextView) findViewById(R.id.inventory);

				StringBuilder inventoryString = new StringBuilder();
				for (cgTrackable inventoryItem : cache.inventory) {
					if (inventoryString.length() > 0) {
						inventoryString.append("\n");
					}
					inventoryString.append(Html.fromHtml(inventoryItem.name).toString());
				}
				inventView.setText(inventoryString, TextView.BufferType.SPANNABLE);
				inventBox.setClickable(true);
				inventBox.setOnClickListener(new selectTrackable());
				inventBox.setVisibility(View.VISIBLE);
			}

			// offline use
			final TextView offlineText = (TextView) findViewById(R.id.offline_text);
			final Button offlineRefresh = (Button) findViewById(R.id.offline_refresh);
			final Button offlineStore = (Button) findViewById(R.id.offline_store);

			if (cache.reason == 1) {
				Long diff = (System.currentTimeMillis() / (60 * 1000)) - (cache.detailedUpdate / (60 * 1000)); // minutes

				String ago = "";
				if (diff < 15) {
					ago = res.getString(R.string.cache_offline_time_mins_few);
				} else if (diff < 50) {
					ago = res.getString(R.string.cache_offline_time_about) + " " + diff + " " + res.getString(R.string.cache_offline_time_mins);
				} else if (diff < 90) {
					ago = res.getString(R.string.cache_offline_time_about) + " " + res.getString(R.string.cache_offline_time_hour);
				} else if (diff < (48 * 60)) {
					ago = res.getString(R.string.cache_offline_time_about) + " " + (diff / 60) + " " + res.getString(R.string.cache_offline_time_hours);
				} else {
					ago = res.getString(R.string.cache_offline_time_about) + " " + (diff / (24 * 60)) + " " + res.getString(R.string.cache_offline_time_days);
				}

				offlineText.setText(res.getString(R.string.cache_offline_stored) + "\n" + ago);

				offlineRefresh.setVisibility(View.VISIBLE);
				offlineRefresh.setClickable(true);
				offlineRefresh.setOnClickListener(new storeCache());

				offlineStore.setText(res.getString(R.string.cache_offline_drop));
				offlineStore.setClickable(true);
				offlineStore.setOnClickListener(new dropCache());
			} else {
				offlineText.setText(res.getString(R.string.cache_offline_not_ready));

				offlineRefresh.setVisibility(View.VISIBLE);
				offlineRefresh.setClickable(true);
				offlineRefresh.setOnClickListener(new refreshCache());

				offlineStore.setText(res.getString(R.string.cache_offline_store));
				offlineStore.setClickable(true);
				offlineStore.setOnClickListener(new storeCache());
			}

			// cache short desc
			if (cache.shortdesc != null && cache.shortdesc.length() > 0) {
				((LinearLayout) findViewById(R.id.desc_box)).setVisibility(View.VISIBLE);

				TextView descView = (TextView) findViewById(R.id.shortdesc);
				descView.setVisibility(View.VISIBLE);
				descView.setText(Html.fromHtml(cache.shortdesc.trim(), new cgHtmlImg(activity, settings, geocode, true, cache.reason, false), null), TextView.BufferType.SPANNABLE);
				descView.setMovementMethod(LinkMovementMethod.getInstance());
			}

			// cache long desc
			if (longDescDisplayed == true) {
				if (longDesc == null && cache != null && cache.description != null) {
					longDesc = Html.fromHtml(cache.description.trim(), new cgHtmlImg(activity, settings, geocode, true, cache.reason, false), null);
				}

				if (longDesc != null && longDesc.length() > 0) {
					((LinearLayout) findViewById(R.id.desc_box)).setVisibility(View.VISIBLE);

					TextView descView = (TextView) findViewById(R.id.description);
					descView.setVisibility(View.VISIBLE);
					descView.setText(longDesc, TextView.BufferType.SPANNABLE);
					descView.setMovementMethod(LinkMovementMethod.getInstance());

					Button showDesc = (Button) findViewById(R.id.show_description);
					showDesc.setVisibility(View.GONE);
					showDesc.setOnTouchListener(null);
					showDesc.setOnClickListener(null);
				}
			} else if (longDescDisplayed == false && cache.description != null && cache.description.length() > 0) {
				((LinearLayout) findViewById(R.id.desc_box)).setVisibility(View.VISIBLE);

				Button showDesc = (Button) findViewById(R.id.show_description);
				showDesc.setVisibility(View.VISIBLE);
				showDesc.setOnClickListener(new View.OnClickListener() {
					public void onClick(View arg0) {
						loadLongDesc();
					}
				});
			}

			// waypoints
			LinearLayout waypoints = (LinearLayout) findViewById(R.id.waypoints);
			waypoints.removeAllViews();

			if (cache.waypoints != null && cache.waypoints.size() > 0) {
				LinearLayout waypointView;

				for (cgWaypoint wpt : cache.waypoints) {
					if (settings.skin == 1) {
						waypointView = (LinearLayout) inflater.inflate(R.layout.waypointitem_light, null);
					} else {
						waypointView = (LinearLayout) inflater.inflate(R.layout.waypointitem_dark, null);
					}
					final TextView identification = (TextView) waypointView.findViewById(R.id.identification);

					((TextView) waypointView.findViewById(R.id.type)).setText(base.waypointTypes.get(wpt.type));
					if (wpt.prefix.equalsIgnoreCase("OWN") == false) {
						identification.setText(wpt.prefix.trim() + "/" + wpt.lookup.trim());
					} else {
						identification.setText(res.getString(R.string.waypoint_custom));
					}

					if (wpt.name.trim().length() == 0) {
						((TextView) waypointView.findViewById(R.id.name)).setText(base.formatCoordinate(wpt.latitude, "lat", true) + " | " + base.formatCoordinate(wpt.longitude, "lon", true));
					} else {
						((TextView) waypointView.findViewById(R.id.name)).setText(Html.fromHtml(wpt.name.trim()), TextView.BufferType.SPANNABLE);
					}
					((TextView) waypointView.findViewById(R.id.note)).setText(Html.fromHtml(wpt.note.trim()), TextView.BufferType.SPANNABLE);

					waypointView.setOnClickListener(new waypointInfo(wpt.id));

					waypoints.addView(waypointView, 0);
				}
			}

			Button addWaypoint = (Button) findViewById(R.id.add_waypoint);
			addWaypoint.setClickable(true);
			addWaypoint.setOnClickListener(new addWaypoint());

			// cache hint
			if (cache.hint != null && cache.hint.length() > 0) {
				((LinearLayout) findViewById(R.id.hint_box)).setVisibility(View.VISIBLE);
				TextView hintView = ((TextView) findViewById(R.id.hint));
				hintView.setText(base.rot13(cache.hint.trim()));
				hintView.setClickable(true);
				hintView.setOnClickListener(new codeHint());
			} else {
				((LinearLayout) findViewById(R.id.hint_box)).setVisibility(View.GONE);
				TextView hintView = ((TextView) findViewById(R.id.hint));
				hintView.setClickable(false);
				hintView.setOnClickListener(null);
			}

			if (geo != null && geo.latitudeNow != null && geo.longitudeNow != null && cache != null && cache.latitude != null && cache.longitude != null) {
				cacheDistance.setText(base.getHumanDistance(base.getDistance(geo.latitudeNow, geo.longitudeNow, cache.latitude, cache.longitude)));
				cacheDistance.bringToFront();
			}
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgeodetail.setView: " + e.toString());
		}

		if (waitDialog != null && waitDialog.isShowing()) waitDialog.dismiss();
		if (storeDialog != null && storeDialog.isShowing()) storeDialog.dismiss();
		if (dropDialog != null && dropDialog.isShowing()) dropDialog.dismiss();
		if (refreshDialog != null && refreshDialog.isShowing()) refreshDialog.dismiss();

		displayLogs();

		if (geo != null) geoUpdate.updateLoc(geo);
	}

	private void displayLogs() {
		// cache logs
		LinearLayout listView = (LinearLayout) findViewById(R.id.log_list);
		listView.removeAllViews();

		RelativeLayout rowView;

		if (cache != null && cache.logs != null) {
			for (cgLog log : cache.logs) {
				if (settings.skin == 1) {
					rowView = (RelativeLayout) inflater.inflate(R.layout.logitem_light, null);
				} else {
					rowView = (RelativeLayout) inflater.inflate(R.layout.logitem_dark, null);
				}

				if (log.date > 0) {
					final Date logDate = new Date(log.date);
					((TextView) rowView.findViewById(R.id.added)).setText(base.dateOutShort.format(logDate));
				}

				if (base.logTypes1.containsKey(log.type) == true) {
					((TextView) rowView.findViewById(R.id.type)).setText(base.logTypes1.get(log.type));
				} else {
					((TextView) rowView.findViewById(R.id.type)).setText(base.logTypes1.get(4)); // note if type is unknown
				}
				((TextView) rowView.findViewById(R.id.author)).setText(Html.fromHtml(log.author), TextView.BufferType.SPANNABLE);

				if (log.found == -1) {
					((TextView) rowView.findViewById(R.id.count)).setVisibility(View.GONE);
				} else if (log.found == 0) {
					((TextView) rowView.findViewById(R.id.count)).setText(res.getString(R.string.cache_count_no));
				} else if (log.found == 1) {
					((TextView) rowView.findViewById(R.id.count)).setText(res.getString(R.string.cache_count_one));
				} else {
					((TextView) rowView.findViewById(R.id.count)).setText(log.found + " " + res.getString(R.string.cache_count_more));
				}
				((TextView) rowView.findViewById(R.id.log)).setText(Html.fromHtml(log.log, new cgHtmlImg(activity, settings, null, false, cache.reason, false), null), TextView.BufferType.SPANNABLE);

				final ImageView markFound = (ImageView) rowView.findViewById(R.id.found_mark);
				final ImageView markDNF = (ImageView) rowView.findViewById(R.id.dnf_mark);
				final ImageView markDisabled = (ImageView) rowView.findViewById(R.id.disabled_mark);
				if (log.type == 2) { // found
					markFound.setVisibility(View.VISIBLE);
					markDNF.setVisibility(View.GONE);
					markDisabled.setVisibility(View.GONE);
				} else if (log.type == 3) { // did not find
					markFound.setVisibility(View.GONE);
					markDNF.setVisibility(View.VISIBLE);
					markDisabled.setVisibility(View.GONE);
				} else if (log.type == 7 || log.type == 8) { // disabled, archived
					markFound.setVisibility(View.GONE);
					markDNF.setVisibility(View.GONE);
					markDisabled.setVisibility(View.VISIBLE);
				} else {
					markFound.setVisibility(View.GONE);
					markDNF.setVisibility(View.GONE);
					markDisabled.setVisibility(View.GONE);
				}

				listView.addView(rowView);
			}

			if (cache.logs.size() > 0) {
				((LinearLayout) findViewById(R.id.log_box)).setVisibility(View.VISIBLE);
			}
		}
	}

	private class loadCache extends Thread {

		private Handler handler = null;
		private String geocode = null;
		private String guid = null;

		public loadCache(Handler handlerIn, String geocodeIn, String guidIn) {
			handler = handlerIn;
			geocode = geocodeIn;
			guid = guidIn;

			if (geocode == null && guid == null) {
				warning.showToast(res.getString(R.string.err_detail_cache_forgot));

				finish();
				return;
			}
		}

		@Override
		public void run() {
			HashMap<String, String> params = new HashMap<String, String>();
			if (geocode != null && geocode.length() > 0) {
				params.put("geocode", geocode);
			} else if (guid != null && guid.length() > 0) {
				params.put("guid", guid);
			} else {
				return;
			}

			searchId = base.searchByGeocode(params, 0, false);

			handler.sendMessage(new Message());
		}
	}

	public void loadLongDesc() {
		if (activity != null && (waitDialog == null || waitDialog.isShowing() == false)) {
			descDialog = ProgressDialog.show(activity, null, res.getString(R.string.cache_dialog_loading_description), true);
			descDialog.setCancelable(true);
		}

		threadLongDesc = new loadLongDesc(loadDescriptionHandler);
		threadLongDesc.start();
	}

	private class loadLongDesc extends Thread {

		private Handler handler = null;

		public loadLongDesc(Handler handlerIn) {
			handler = handlerIn;
		}

		@Override
		public void run() {
			longDesc = Html.fromHtml(cache.description.trim(), new cgHtmlImg(activity, settings, geocode, true, cache.reason, false), null);
			handler.sendMessage(new Message());
		}
	}

	public ArrayList<cgCoord> getCoordinates() {
		cgCoord coords = null;
		ArrayList<cgCoord> coordinates = new ArrayList<cgCoord>();

		try {
			// cache
			coords = new cgCoord();
			coords.type = "cache";
			if (name != null && name.length() > 0) {
				coords.name = name;
			} else {
				coords.name = geocode.toUpperCase();
			}
			coords.latitude = cache.latitude;
			coords.longitude = cache.longitude;
			coordinates.add(coords);
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgeodetail.getCoordinates (cache): " + e.toString());
		}

		try {
			// waypoints
			for (cgWaypoint waypoint : cache.waypoints) {
				if (waypoint.latitude == null || waypoint.longitude == null) {
					continue;
				}

				coords = new cgCoord();
				coords.type = "waypoint";
				coords.name = waypoint.name;
				coords.latitude = waypoint.latitude;
				coords.longitude = waypoint.longitude;
				coordinates.add(coords);
			}
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgeodetail.getCoordinates (waypoint): " + e.toString());
		}

		return coordinates;
	}

	private void showOnMap() {
		cgeomap mapActivity = new cgeomap();

		Intent mapIntent = new Intent(activity, mapActivity.getClass());
		mapIntent.putExtra("detail", true);
		mapIntent.putExtra("searchid", searchId);

		activity.startActivity(mapIntent);
	}

	private void cachesAround() {
		cgeocaches cachesActivity = new cgeocaches();

		Intent cachesIntent = new Intent(activity, cachesActivity.getClass());
		cachesIntent.putExtra("type", "coordinate");
		cachesIntent.putExtra("latitude", cache.latitude);
		cachesIntent.putExtra("longitude", cache.longitude);
		cachesIntent.putExtra("cachetype", settings.cacheType);

		activity.startActivity(cachesIntent);

		finish();
	}

	private void addToCalendar() {
		String[] projection = new String[] { "_id", "displayName" };
		Uri calendarProvider = null;
		final int sdk = new Integer(Build.VERSION.SDK).intValue();
		if (sdk >= 8) {
			calendarProvider = Uri.parse("content://com.android.calendar/calendars");
		} else {
			calendarProvider = Uri.parse("content://calendar/calendars");
		}

		Cursor cursor = managedQuery(calendarProvider, projection, "selected=1", null, null);

		calendars.clear();
		int cnt = 0;
		if (cursor != null) {
			cnt = cursor.getCount();

			if (cnt > 0) {
				cursor.moveToFirst();

				int calId = 0;
				String calIdPre = null;
				String calName = null;
				int calIdIn = cursor.getColumnIndex("_id");
				int calNameIn = cursor.getColumnIndex("displayName");

				do {
					calIdPre = cursor.getString(calIdIn);
					if (calIdPre != null) {
						calId = new Integer(calIdPre);
					}
					calName = cursor.getString(calNameIn);

					if (calId > 0 && calName != null) {
						calendars.put(calId, calName);
					}
				} while (cursor.moveToNext() == true);
			}
		}

		final CharSequence[] items = calendars.values().toArray(new CharSequence[calendars.size()]);

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(R.string.cache_calendars);
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				addToCalendarFn(item);
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void addToCalendarFn(int index) {
		if (calendars == null || calendars.isEmpty() == true) {
			return;
		}

		try {
			Uri calendarProvider = null;
			final int sdk = new Integer(Build.VERSION.SDK).intValue();
			if (sdk >= 8) {
				calendarProvider = Uri.parse("content://com.android.calendar/events");
			} else {
				calendarProvider = Uri.parse("content://calendar/events");
			}

			final Integer[] keys = calendars.keySet().toArray(new Integer[calendars.size()]);
			final Integer calId = keys[index];

			final Date eventDate = cache.hidden;
			eventDate.setHours(0);
			eventDate.setMinutes(0);
			eventDate.setSeconds(0);

			StringBuilder description = new StringBuilder();
			description.append("http://coord.info/");
			description.append(cache.geocode.toUpperCase());
			description.append("\n\n");
			if (cache.shortdesc != null && cache.shortdesc.length() > 0) {
				description.append(Html.fromHtml(cache.shortdesc).toString());
			}

			ContentValues event = new ContentValues();
			event.put("calendar_id", calId);
			event.put("dtstart", eventDate.getTime() + 43200000); // noon
			event.put("dtend", eventDate.getTime() + 43200000 + 3600000); // + one hour
			event.put("eventTimezone", "UTC");
			event.put("title", Html.fromHtml(cache.name).toString());
			event.put("description", description.toString());
			if (cache.location != null && cache.location.length() > 0) {
				event.put("eventLocation", Html.fromHtml(cache.location).toString());
			}
			event.put("allDay", 1);
			event.put("hasAlarm", 0);

			getContentResolver().insert(calendarProvider, event);
			
			warning.showToast(res.getString(R.string.event_success));
		} catch (Exception e) {
			warning.showToast(res.getString(R.string.event_fail));

			Log.e(cgSettings.tag, "cgeodetail.addToCalendarFn: " + e.toString());
		}
	}

	private void navigateTo() {
		if (cache == null || cache.latitude == null || cache.longitude == null) {
			warning.showToast("c:geo doesn't know location of cache.");
		}

		cgeonavigate navigateActivity = new cgeonavigate();

		Intent navigateIntent = new Intent(activity, navigateActivity.getClass());
		navigateIntent.putExtra("latitude", cache.latitude);
		navigateIntent.putExtra("longitude", cache.longitude);
		navigateIntent.putExtra("geocode", cache.geocode.toUpperCase());
		navigateIntent.putExtra("name", cache.name);

		if (navigateActivity.coordinates != null) {
			navigateActivity.coordinates.clear();
		}
		navigateActivity.coordinates = getCoordinates();
		activity.startActivity(navigateIntent);
	}

	private void radarTo() {
		try {
			if (base.isIntentAvailable(activity, "com.google.android.radar.SHOW_RADAR") == true) {
				Intent radarIntent = new Intent("com.google.android.radar.SHOW_RADAR");
				radarIntent.putExtra("latitude", new Float(cache.latitude));
				radarIntent.putExtra("longitude", new Float(cache.longitude));
				activity.startActivity(radarIntent);
			} else {
				AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
				dialog.setTitle(res.getString(R.string.err_radar_title));
				dialog.setMessage(res.getString(R.string.err_radar_message));
				dialog.setCancelable(true);
				dialog.setPositiveButton("yes", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						try {
							activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:com.eclipsim.gpsstatus2")));
							dialog.cancel();
						} catch (Exception e) {
							warning.showToast(res.getString(R.string.err_radar_market));
							Log.e(cgSettings.tag, "cgeodetail.radarTo.onClick: " + e.toString());
						}
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
		} catch (Exception e) {
			warning.showToast(res.getString(R.string.err_radar_generic));
			Log.e(cgSettings.tag, "cgeodetail.radarTo: " + e.toString());
		}
	}

	private class waypointInfo implements View.OnClickListener {

		private int id = -1;

		public waypointInfo(int idIn) {
			id = idIn;
		}

		public void onClick(View arg0) {
			Intent waypointIntent = new Intent(activity, cgeowaypoint.class);
			waypointIntent.putExtra("waypoint", id);
			waypointIntent.putExtra("geocode", cache.geocode);
			activity.startActivity(waypointIntent);
		}
	}

	private void logVisit() {
		Intent logVisitIntent = new Intent(activity, cgeovisit.class);
		logVisitIntent.putExtra("id", cache.cacheid);
		logVisitIntent.putExtra("geocode", cache.geocode.toUpperCase());
		logVisitIntent.putExtra("type", cache.type.toLowerCase());
		activity.startActivity(logVisitIntent);
	}

	private void showSpoilers() {
		if (cache == null || cache.spoilers == null || cache.spoilers.isEmpty() == true) {
			warning.showToast(res.getString(R.string.err_detail_no_spoiler));
		}

		Intent spoilersIntent = new Intent(activity, cgeospoilers.class);
		spoilersIntent.putExtra("geocode", geocode.toUpperCase());
		activity.startActivity(spoilersIntent);
	}

	private void showSmaps() {
		if (cache == null || cache.reason == 0) {
			warning.showToast(res.getString(R.string.err_detail_no_map_static));
		}

		Intent smapsIntent = new Intent(activity, cgeosmaps.class);
		smapsIntent.putExtra("geocode", geocode.toUpperCase());
		activity.startActivity(smapsIntent);
	}

	public class codeHint implements View.OnClickListener {

		public void onClick(View arg0) {
			// code hint
			TextView hintView = ((TextView) findViewById(R.id.hint));
			hintView.setText(base.rot13(hintView.getText().toString()));

		}
	}

	private class update extends cgUpdateLoc {

		@Override
		public void updateLoc(cgGeo geo) {
			if (geo == null) {
				return;
			}

			try {
				if (geo.latitudeNow != null && geo.longitudeNow != null && cache != null && cache.latitude != null && cache.longitude != null) {
					cacheDistance.setText(base.getHumanDistance(base.getDistance(geo.latitudeNow, geo.longitudeNow, cache.latitude, cache.longitude)));
					cacheDistance.bringToFront();
				}
			} catch (Exception e) {
				Log.w(cgSettings.tag, "Failed to update location.");
			}
		}
	}

	private class selectTrackable implements View.OnClickListener {
		public void onClick(View arg0) {
			// show list of trackables
			try {
				Intent trackablesIntent = new Intent(activity, cgeotrackables.class);
				trackablesIntent.putExtra("geocode", geocode.toUpperCase());
				activity.startActivity(trackablesIntent);
			} catch (Exception e) {
				Log.e(cgSettings.tag, "cgeodetail.selectTrackable: " + e.toString());
			}
		}
	}

	private class storeCache implements View.OnClickListener {
		public void onClick(View arg0) {
			if (dropDialog != null && dropDialog.isShowing() == true) {
				warning.showToast(res.getString(R.string.err_detail_still_removing));
				return;
			}
			if (refreshDialog != null && refreshDialog.isShowing() == true) {
				warning.showToast(res.getString(R.string.err_detail_still_refreshing));
				return;
			}

			storeDialog = ProgressDialog.show(activity, res.getString(R.string.cache_dialog_offline_save_title), res.getString(R.string.cache_dialog_offline_save_message), true);
			storeDialog.setCancelable(false);
			Thread thread = new storeCacheThread(storeCacheHandler);
			thread.start();
		}
	}

	private class refreshCache implements View.OnClickListener {
		public void onClick(View arg0) {
			if (dropDialog != null && dropDialog.isShowing() == true) {
				warning.showToast(res.getString(R.string.err_detail_still_removing));
				return;
			}
			if (storeDialog != null && storeDialog.isShowing() == true) {
				warning.showToast(res.getString(R.string.err_detail_still_saving));
				return;
			}

			refreshDialog = ProgressDialog.show(activity, res.getString(R.string.cache_dialog_refresh_title), res.getString(R.string.cache_dialog_refresh_message), true);
			refreshDialog.setCancelable(false);
			Thread thread = new refreshCacheThread(refreshCacheHandler);
			thread.start();
		}
	}

	private class storeCacheThread extends Thread {
		private Handler handler = null;

		public storeCacheThread(Handler handlerIn) {
			handler = handlerIn;
		}

		@Override
		public void run() {
			base.storeCache(app, activity, cache, null, handler);
		}
	}

	private class refreshCacheThread extends Thread {
		private Handler handler = null;

		public refreshCacheThread(Handler handlerIn) {
			handler = handlerIn;
		}

		@Override
		public void run() {
			app.removeCacheFromCache(geocode);

			final HashMap<String, String> params = new HashMap<String, String>();
			params.put("geocode", cache.geocode);
			searchId = base.searchByGeocode(params, 0, true);

			handler.sendEmptyMessage(0);
		}
	}

	private class dropCache implements View.OnClickListener {
		public void onClick(View arg0) {
			if (storeDialog != null && storeDialog.isShowing() == true) {
				warning.showToast(res.getString(R.string.err_detail_still_saving));
				return;
			}
			if (refreshDialog != null && refreshDialog.isShowing() == true) {
				warning.showToast(res.getString(R.string.err_detail_still_refreshing));
				return;
			}

			dropDialog = ProgressDialog.show(activity, res.getString(R.string.cache_dialog_offline_drop_title), res.getString(R.string.cache_dialog_offline_drop_message), true);
			dropDialog.setCancelable(false);
			Thread thread = new dropCacheThread(dropCacheHandler);
			thread.start();
		}
	}

	private class dropCacheThread extends Thread {

		private Handler handler = null;

		public dropCacheThread(Handler handlerIn) {
			handler = handlerIn;
		}

		@Override
		public void run() {
			base.dropCache(app, activity, cache, handler);
		}
	}

	private class addWaypoint implements View.OnClickListener {

		public void onClick(View arg0) {
			Intent addWptIntent = new Intent(activity, cgeowaypointadd.class);

			addWptIntent.putExtra("geocode", geocode);

			activity.startActivity(addWptIntent);
		}
	}
}
