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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.Display;
import android.view.SubMenu;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import java.util.List;
import java.util.Locale;
import org.openintents.intents.WikitudeARIntentHelper;
import org.openintents.intents.WikitudePOI;

public class cgeodetail extends Activity {
    public Long searchId = null;
	public cgCache cache = null;
	public String geocode = null;
	public String name = null;
	public String guid = null;
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
	private int poiId = -1;
	private List<WikitudePOI> pois = null;
	private HashMap<String, Integer> gcIcons = new HashMap<String, Integer>();
    private ProgressDialog storeDialog = null;
    private ProgressDialog dropDialog = null;
	private BitmapFactory factory = null;

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
				longDesc = Html.fromHtml(cache.description.trim(), new cgHtmlImg(activity, settings, geocode, cache.reason, false), null);
			}

			if (longDesc != null) {
				((LinearLayout)findViewById(R.id.desc_box)).setVisibility(View.VISIBLE);
				
				TextView descView = (TextView)findViewById(R.id.description);
				descView.setVisibility(View.VISIBLE);
				descView.setText(longDesc, TextView.BufferType.SPANNABLE);
				descView.setMovementMethod(LinkMovementMethod.getInstance());
                
                Button showDesc = (Button)findViewById(R.id.show_description);
                showDesc.setVisibility(View.GONE);
                showDesc.setOnTouchListener(null);
                showDesc.setOnClickListener(null);
			} else {
				warning.showToast("Sorry, c:geo can't load description.");
			}

			if (descDialog != null && descDialog.isShowing()) descDialog.dismiss();

			longDescDisplayed = true;
		}
	};
	
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// init
		activity = this;
        app = (cgeoapplication)this.getApplication();
        settings = new cgSettings(this, getSharedPreferences(cgSettings.preferences, 0));
        base = new cgBase(app, settings, getSharedPreferences(cgSettings.preferences, 0));
        warning = new cgWarning(this);

		// set layout
		setTitle("cache");
		if (settings.skin == 1) setContentView(R.layout.cachedetail_light);
		else setContentView(R.layout.cachedetail_dark);
		cacheDistance = (TextView)findViewById(R.id.distance);

		init();

		// get parameters
		Bundle extras = getIntent().getExtras();
		Uri uri = getIntent().getData();

		// try to get data from Wikitude
		pois = ((cgeoapplication)activity.getApplication()).getPois();
		poiId = this.getIntent().getIntExtra(WikitudeARIntentHelper.EXTRA_INDEX_SELECTED_POI, -1);

		if (pois != null && poiId > -1) {
			Log.d(cgSettings.tag, "pois: " + poiId + " / " + pois.size());

			geocode = pois.get(poiId).getName().toUpperCase();
			name = pois.get(poiId).getDescription();
			if (geocode == null || geocode.indexOf("GC") == -1) {
				geocode = null;
			}
		}
		
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

			if (uriQuery != null) Log.i(cgSettings.tag, "Opening URI: " + uriHost + uriPath + "?" + uriQuery);
			else Log.i(cgSettings.tag, "Opening URI: " + uriHost + uriPath);

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
					warning.showToast("Sorry, c:geo can\'t open geocache details.");
					finish();
					return;
				}
			} else if (uriHost.contains("coord.info") == true) {
				if (uriPath != null && uriPath.startsWith("/gc") == true) {
					geocode = uriPath.substring(1).toUpperCase();
				} else {
					warning.showToast("Sorry, c:geo can\'t open geocache details.");
					finish();
					return;
				}
			}
		}

		// no given data
		if (geocode == null && guid == null) {
			warning.showToast("Sorry, c:geo can\'t display geocache you want. Is it really geocache?");
			finish();
			return;
		}

		app.setAction(geocode);

		if (name != null && name.length() > 0) {
			waitDialog = ProgressDialog.show(this, name, "loading cache details...", true);
		} else if (geocode != null && geocode.length() > 0) {
			waitDialog = ProgressDialog.show(this, geocode.toUpperCase(), "loading cache details...", true);
		} else {
			waitDialog = ProgressDialog.show(this, "cache", "loading cache details...", true);
		}
		waitDialog.setCancelable(true);

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

        if (geo == null) geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);
		setView();
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
		SubMenu subMenu = menu.addSubMenu(1, 0, 0, "navigate").setIcon(android.R.drawable.ic_menu_compass);
		subMenu.add(0, 2, 0, "compass"); // compass
		subMenu.add(0, 8, 0, "radar"); // radar
		subMenu.add(0, 9, 0, "turn-by-turn"); // turn-by-turn

		menu.add(0, 1, 0, "show on map").setIcon(android.R.drawable.ic_menu_mapmode); // google maps
		menu.add(1, 7, 0, "open in browser").setIcon(android.R.drawable.ic_menu_info_details); // browser
		// ---- next row
		menu.add(1, 3, 0, "log visit").setIcon(android.R.drawable.ic_menu_agenda); // log visit

		if (cache != null && cache.spoilers != null && cache.spoilers.size() > 0) {
			menu.add(1, 5, 0, "spoiler images").setIcon(android.R.drawable.ic_menu_gallery); // spoiler images
		}

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		try {
			if (cache.latitude == null || cache.longitude == null) { // cache has no coordinates (really?)
				MenuItem item;
				item = menu.findItem(1); // show on map
				item = menu.findItem(2); // compass
				item = menu.findItem(8); // radar
				item = menu.findItem(9); // turn-by-turn
				item.setEnabled(false);
			}
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgeodetail.onPrepareOptionsMenu: " + e.toString());
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
			case 7:
				activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.geocaching.com/seek/cache_details.aspx?wp=" + cache.geocode)));
				return true;
			case 8:
				radarTo();
				return true;
			case 9:
				turnTo();
				return true;
		}
		
		return false;
	}

	private void init() {
		if (inflater == null) inflater = activity.getLayoutInflater();
        if (geo == null) geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);

        if (searchId != null && searchId > 0) {
            cache = app.getCache(searchId);
            if (cache != null && cache.geocode != null) {
                geocode = cache.geocode;
            }
        }

		if (geocode != null && geocode.length() > 0) app.setAction(geocode);
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
				geocode = cache.geocode;
				warning.showToast("Sorry, c:geo can\'t find geocache " + geocode + ".");
			} else {
				geocode = null;
				warning.showToast("Sorry, c:geo can\'t find that geocache.");
			}

			finish();
			return;
		}

		if (app.warnedLanguage == false && (cache.owner == null || cache.owner.length() == 0 || cache.hidden == null)) {
			warning.helpDialog("problem", "c:geo can\'t read some cache details. Please check if you have geocaching.com website set to English. Unfortunately, c:geo doesn\'t understand other localizations.");
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

			((ScrollView)findViewById(R.id.details_list_box)).setVisibility(View.VISIBLE);
			LinearLayout detailsList = (LinearLayout)findViewById(R.id.details_list);
			detailsList.removeAllViews();

			// cache type
			if (settings.skin == 1) itemLayout = (RelativeLayout)inflater.inflate(R.layout.cacheitem_light_first, null);
			else itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_dark_first, null);
			itemName = (TextView)itemLayout.findViewById(R.id.name);
			itemValue = (TextView)itemLayout.findViewById(R.id.value);

			itemName.setText("type");
			if (base.cacheTypesInv.containsKey(cache.type) == true) { // cache icon
				itemValue.setText(base.cacheTypesInv.get(cache.type) + " (" + cache.size + ")");
			} else {
				itemValue.setText(base.cacheTypesInv.get("mystery") + " (" + cache.size + ")");
			}
			if (cache.type != null && gcIcons.containsKey(cache.type) == true) { // cache icon
				itemValue.setCompoundDrawablesWithIntrinsicBounds((Drawable)activity.getResources().getDrawable(gcIcons.get(cache.type)), null, null, null);
			} else { // unknown cache type, "mystery" icon
				itemValue.setCompoundDrawablesWithIntrinsicBounds((Drawable)activity.getResources().getDrawable(gcIcons.get("mystery")), null, null, null);
			}
			detailsList.addView(itemLayout);

			// gc-code
			if (settings.skin == 1) itemLayout = (RelativeLayout)inflater.inflate(R.layout.cacheitem_light, null);
			else itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_dark, null);
			itemName = (TextView)itemLayout.findViewById(R.id.name);
			itemValue = (TextView)itemLayout.findViewById(R.id.value);

			itemName.setText("gc-code");
			itemValue.setText(cache.geocode.toUpperCase());
			detailsList.addView(itemLayout);

			// cache state
			if (cache.archived == true || cache.disabled == true || cache.members == true || cache.found == true) {
				if (settings.skin == 1) itemLayout = (RelativeLayout)inflater.inflate(R.layout.cacheitem_light, null);
				else itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_dark, null);
				itemName = (TextView)itemLayout.findViewById(R.id.name);
				itemValue = (TextView)itemLayout.findViewById(R.id.value);

				itemName.setText("status");

                StringBuilder state = new StringBuilder();
				if (cache.found == true) {
                    if (state.length() > 0) { state.append(", "); }
					state.append("found");
				}
				if (cache.archived == true) {
                    if (state.length() > 0) { state.append(", "); }
					state.append("archived");
				}
                if (cache.disabled == true) {
                    if (state.length() > 0) { state.append(", "); }
					state.append("disabled");
				}
                if (cache.members == true) {
                    if (state.length() > 0) { state.append(", "); }
					state.append("premium members only");
				}

                itemValue.setText(state.toString());
				detailsList.addView(itemLayout);

                state = null;
			}

			// distance
			if (settings.skin == 1) itemLayout = (RelativeLayout)inflater.inflate(R.layout.cacheitem_light, null);
			else itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_dark, null);
			itemName = (TextView)itemLayout.findViewById(R.id.name);
			itemValue = (TextView)itemLayout.findViewById(R.id.value);

			itemName.setText("distance");
			itemValue.setText("--");
			detailsList.addView(itemLayout);
			cacheDistance = itemValue;

			// difficulty
			if (settings.skin == 1) itemLayout = (RelativeLayout)inflater.inflate(R.layout.cacheitem_light_layout, null);
			else itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_dark_layout, null);
			itemName = (TextView)itemLayout.findViewById(R.id.name);
			itemValue = (TextView)itemLayout.findViewById(R.id.value);
			itemStars = (LinearLayout)itemLayout.findViewById(R.id.stars);

			itemName.setText("difficulty");
			itemValue.setText(String.format(Locale.getDefault(), "%.1f", cache.difficulty) + " of 5");
			for (int i = 0; i <= 4; i ++) {
				ImageView star = (ImageView)inflater.inflate(R.layout.star, null);
				if ((cache.difficulty - i) >= 1.0) {
					star.setImageResource(R.drawable.star_on);
				} else if ((cache.difficulty - i) > 0.0) {
					star.setImageResource(R.drawable.star_half);
				} else {
					star.setImageResource(R.drawable.star_off);
				}
				itemStars.addView(star);
			}
			detailsList.addView(itemLayout);

			// terrain
			if (settings.skin == 1) itemLayout = (RelativeLayout)inflater.inflate(R.layout.cacheitem_light_layout, null);
			else itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_dark_layout, null);
			itemName = (TextView)itemLayout.findViewById(R.id.name);
			itemValue = (TextView)itemLayout.findViewById(R.id.value);
			itemStars = (LinearLayout)itemLayout.findViewById(R.id.stars);

			itemName.setText("terrain");
			itemValue.setText(String.format(Locale.getDefault(), "%.1f", cache.terrain) + " of 5");
			for (int i = 0; i <= 4; i ++) {
				ImageView star = (ImageView)inflater.inflate(R.layout.star, null);
				if ((cache.terrain - i) >= 1.0) {
					star.setImageResource(R.drawable.star_on);
				} else if ((cache.terrain - i) > 0.0) {
					star.setImageResource(R.drawable.star_half);
				} else {
					star.setImageResource(R.drawable.star_off);
				}
				itemStars.addView(star);
			}
			detailsList.addView(itemLayout);

			itemStars = null;

			// cache author
			if (settings.skin == 1) itemLayout = (RelativeLayout)inflater.inflate(R.layout.cacheitem_light, null);
			else itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_dark, null);
			itemName = (TextView)itemLayout.findViewById(R.id.name);
			itemValue = (TextView)itemLayout.findViewById(R.id.value);

			itemName.setText("owner");
			itemValue.setText(Html.fromHtml(cache.owner), TextView.BufferType.SPANNABLE);
			detailsList.addView(itemLayout);

			// cache hidden
			if (cache.hidden != null) {
				if (settings.skin == 1) itemLayout = (RelativeLayout)inflater.inflate(R.layout.cacheitem_light, null);
				else itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_dark, null);
				itemName = (TextView)itemLayout.findViewById(R.id.name);
				itemValue = (TextView)itemLayout.findViewById(R.id.value);

				itemName.setText("hidden");
				itemValue.setText(base.dateOut.format(cache.hidden));
				detailsList.addView(itemLayout);
			}

			// cache location
			if (cache.location != null && cache.location.length() > 0) {
				if (settings.skin == 1) itemLayout = (RelativeLayout)inflater.inflate(R.layout.cacheitem_light, null);
				else itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_dark, null);
				itemName = (TextView)itemLayout.findViewById(R.id.name);
				itemValue = (TextView)itemLayout.findViewById(R.id.value);

				itemName.setText("location");
				itemValue.setText(cache.location);
				detailsList.addView(itemLayout);
			}

			// cache coordinates
			if (settings.skin == 1) itemLayout = (RelativeLayout)inflater.inflate(R.layout.cacheitem_light, null);
			else itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_dark, null);
			itemName = (TextView)itemLayout.findViewById(R.id.name);
			itemValue = (TextView)itemLayout.findViewById(R.id.value);

			itemName.setText("coordinates");
			itemValue.setText(cache.latitudeString + " | " + cache.longitudeString);
			detailsList.addView(itemLayout);

			// cache attributes
			if (cache.attributes != null && cache.attributes.size() > 0) {
				if (settings.skin == 1) itemLayout = (RelativeLayout)inflater.inflate(R.layout.cacheitem_light, null);
				else itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_dark, null);
				itemName = (TextView)itemLayout.findViewById(R.id.name);
				itemValue = (TextView)itemLayout.findViewById(R.id.value);

				itemName.setText("attributes");
				itemValue.setText(base.implode("\n", cache.attributes.toArray()));
				detailsList.addView(itemLayout);
			}

			// cache inventory
			if (cache.inventory != null && cache.inventory.size() > 0) {
				if (settings.skin == 1) itemLayout = (RelativeLayout)inflater.inflate(R.layout.cacheitem_light, null);
				else itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_dark, null);
				itemName = (TextView)itemLayout.findViewById(R.id.name);
				itemValue = (TextView)itemLayout.findViewById(R.id.value);

				itemName.setText("inventory");
				StringBuilder inventoryString = new StringBuilder();
				for (cgTrackable inventoryItem : cache.inventory) {
					if (inventoryString.length() > 0) {
						inventoryString.append("\n");
					}
					inventoryString.append(Html.fromHtml(inventoryItem.name).toString());
				}
				itemValue.setText(inventoryString, TextView.BufferType.SPANNABLE);
				itemLayout.setClickable(true);
				itemLayout.setOnClickListener(new selectTrackable());
				detailsList.addView(itemLayout);
			}

            // offline use
			if (settings.skin == 1) itemLayout = (RelativeLayout)inflater.inflate(R.layout.cacheitem_light_offline, null);
			else itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_dark_offline, null);
			itemName = (TextView)itemLayout.findViewById(R.id.name);
			itemValue = (TextView)itemLayout.findViewById(R.id.value);
            Button itemRefresh = (Button)itemLayout.findViewById(R.id.refresh);
            Button itemStore = (Button)itemLayout.findViewById(R.id.store);

			itemName.setText("offline");
            if (cache.reason == 1) {
                Long diff = (System.currentTimeMillis() / (60 * 1000)) - (cache.detailedUpdate / (60 * 1000)); // minutes

                String ago = "";
                if (diff < 15) {
                    ago = "few minutes ago";
                } else if (diff < 50) {
                    ago = "about " + diff + " minutes ago";
                } else if (diff < 90) {
                    ago = "about one hour ago";
                } else if (diff < (48 * 60)) {
                    ago = "about " + (diff / 60) + " hours ago";
                } else {
                    ago = "about " + (diff / (24 * 60)) + " days ago";
                }

                itemValue.setText("stored in device\n" + ago);

				itemRefresh.setVisibility(View.VISIBLE);
                itemRefresh.setClickable(true);
                itemRefresh.setOnTouchListener(new cgViewTouch(settings, itemRefresh));
                itemRefresh.setOnClickListener(new storeCache());

                itemStore.setText("drop");
                itemStore.setClickable(true);
                itemStore.setOnTouchListener(new cgViewTouch(settings, itemStore));
                itemStore.setOnClickListener(new dropCache());
            } else {
    			itemValue.setText("not ready\nfor offline use");

				itemRefresh.setVisibility(View.GONE);
                itemRefresh.setClickable(false);
                itemRefresh.setOnTouchListener(null);
                itemRefresh.setOnClickListener(null);

                itemStore.setText("store");
                itemStore.setClickable(true);
                itemStore.setOnTouchListener(new cgViewTouch(settings, itemStore));
                itemStore.setOnClickListener(new storeCache());
            }
			detailsList.addView(itemLayout);
            itemStore = null;

			// cache short desc
			if (cache.shortdesc != null && cache.shortdesc.length() > 0) {
				((LinearLayout)findViewById(R.id.desc_box)).setVisibility(View.VISIBLE);
                
				TextView descView = (TextView)findViewById(R.id.shortdesc);
				descView.setVisibility(View.VISIBLE);
				descView.setText(Html.fromHtml(cache.shortdesc.trim(), new cgHtmlImg(activity, settings, geocode, cache.reason, false), null), TextView.BufferType.SPANNABLE);
				descView.setMovementMethod(LinkMovementMethod.getInstance());
			}

			// cache long desc
			if (longDescDisplayed == true) {
				if (longDesc == null && cache != null && cache.description != null) {
					longDesc = Html.fromHtml(cache.description.trim(), new cgHtmlImg(activity, settings, geocode, cache.reason, false), null);
				}

				if (longDesc != null && longDesc.length() > 0) {
					((LinearLayout)findViewById(R.id.desc_box)).setVisibility(View.VISIBLE);

					TextView descView = (TextView)findViewById(R.id.description);
					descView.setVisibility(View.VISIBLE);
					descView.setText(longDesc, TextView.BufferType.SPANNABLE);
					descView.setMovementMethod(LinkMovementMethod.getInstance());

                    Button showDesc = (Button)findViewById(R.id.show_description);
                    showDesc.setVisibility(View.GONE);
                    showDesc.setOnTouchListener(null);
                    showDesc.setOnClickListener(null);
				}
			} else if (longDescDisplayed == false && cache.description != null) {
				((LinearLayout)findViewById(R.id.desc_box)).setVisibility(View.VISIBLE);
                
                Button showDesc = (Button)findViewById(R.id.show_description);
                showDesc.setVisibility(View.VISIBLE);
                showDesc.setOnTouchListener(new cgViewTouch(settings, showDesc));
                showDesc.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View arg0) {
                        loadLongDesc();
                    }
                });
            }

			// waypoints
			LinearLayout waypoints = (LinearLayout)findViewById(R.id.waypoints);
			waypoints.removeAllViews();
			
			if (cache.waypoints != null && cache.waypoints.size() > 0) {
				RelativeLayout waypointView;

				for (cgWaypoint wpt : cache.waypoints) {
					if (settings.skin == 1) waypointView = (RelativeLayout)inflater.inflate(R.layout.waypointitem_light, null);
					else waypointView = (RelativeLayout)inflater.inflate(R.layout.waypointitem_dark, null);
					final TextView identification = (TextView)waypointView.findViewById(R.id.identification);

					((TextView)waypointView.findViewById(R.id.type)).setText(base.waypointTypes.get(wpt.type));
					if (wpt.prefix.equalsIgnoreCase("OWN") == false) identification.setText(wpt.prefix.trim() + "/" + wpt.lookup.trim());
					else identification.setText("custom");
					((TextView)waypointView.findViewById(R.id.name)).setText(Html.fromHtml(wpt.name.trim()), TextView.BufferType.SPANNABLE);
					((TextView)waypointView.findViewById(R.id.note)).setText(Html.fromHtml(wpt.note.trim()), TextView.BufferType.SPANNABLE);

					waypointView.setOnClickListener(new waypointInfo(wpt.id));

					waypoints.addView(waypointView, 0);
                }
			}

            Button addWaypoint = (Button)findViewById(R.id.add_waypoint);
            addWaypoint.setClickable(true);
            addWaypoint.setOnTouchListener(new cgViewTouch(settings, addWaypoint));
            addWaypoint.setOnClickListener(new addWaypoint());

			// cache hint
			if (cache.hint != null && cache.hint.length() > 0) {
				((LinearLayout)findViewById(R.id.hint_box)).setVisibility(View.VISIBLE);
				TextView hintView = ((TextView)findViewById(R.id.hint));
				hintView.setText(base.rot13(cache.hint.trim()));
				hintView.setClickable(true);
				hintView.setOnClickListener(new codeHint());
			} else {
				((LinearLayout)findViewById(R.id.hint_box)).setVisibility(View.GONE);
				TextView hintView = ((TextView)findViewById(R.id.hint));
				hintView.setClickable(false);
				hintView.setOnClickListener(null);
			}

			// cache logs
			LinearLayout listView = (LinearLayout)findViewById(R.id.log_list);
			listView.removeAllViews();

			RelativeLayout rowView;

            if (cache.logs != null) {
                for (cgLog log : cache.logs) {
                    if (settings.skin == 1) rowView = (RelativeLayout)inflater.inflate(R.layout.logitem_light, null);
					else rowView = (RelativeLayout)inflater.inflate(R.layout.logitem_dark, null);

                    ((TextView)rowView.findViewById(R.id.type)).setText(log.type);
                    ((TextView)rowView.findViewById(R.id.author)).setText(Html.fromHtml(log.author), TextView.BufferType.SPANNABLE);
                    ((TextView)rowView.findViewById(R.id.added)).setText(Html.fromHtml(log.date + "; " + log.found), TextView.BufferType.SPANNABLE);
                    ((TextView)rowView.findViewById(R.id.log)).setText(Html.fromHtml(log.log, new cgHtmlImg(activity, settings, null, cache.reason, false), null), TextView.BufferType.SPANNABLE);

					final ImageView markFound = (ImageView)rowView.findViewById(R.id.found_mark);
					final ImageView markDNF = (ImageView)rowView.findViewById(R.id.dnf_mark);
					final ImageView markDisabled = (ImageView)rowView.findViewById(R.id.disabled_mark);
					if (log.type.equalsIgnoreCase("found") == true) {
						markFound.setVisibility(View.VISIBLE);
						markDNF.setVisibility(View.GONE);
						markDisabled.setVisibility(View.GONE);
					} else if (log.type.equalsIgnoreCase("did not find") == true) {
						markFound.setVisibility(View.GONE);
						markDNF.setVisibility(View.VISIBLE);
						markDisabled.setVisibility(View.GONE);
					} else if (log.type.equalsIgnoreCase("disabled") == true) {
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

                if (cache.logs.size() > 0) ((LinearLayout)findViewById(R.id.log_box)).setVisibility(View.VISIBLE);
            }

			// cache maps
			if (cache.reason == 1) {
				if (factory == null) factory = new BitmapFactory();
				Bitmap imagePre = null;
				BitmapDrawable image = null;

				Display display = ((WindowManager)activity.getSystemService(activity.WINDOW_SERVICE)).getDefaultDisplay();
				LinearLayout mapList = (LinearLayout)findViewById(R.id.map_list);
				mapList.removeAllViews();

				int imgWidth = 0;
				int imgHeight = 0;

				for (int level = 1; level <= 5; level ++) {
					try {
						imagePre = factory.decodeFile(settings.getStorage() + cache.geocode + "/map_" + level);

						imgWidth = imagePre.getWidth();
						imgHeight = imagePre.getHeight();

						image = new BitmapDrawable(imagePre);
						image.setBounds(new Rect(0, 0, imgWidth, imgHeight));
						imagePre = null;
					} catch (Exception e) {
						Log.e(cgSettings.tag, "cgeodetail.setView.map.run: " + e.toString());
					}

					ImageView mapImage = null;
					if (settings.skin == 1) mapImage = (ImageView)inflater.inflate(R.layout.imgitem_light, null);
					else mapImage = (ImageView)inflater.inflate(R.layout.imgitem_dark, null);

					if (image != null) {
						int maxWidth = display.getWidth() - 25;

						mapImage.setImageDrawable(image);
						mapImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
						mapImage.setLayoutParams(new LayoutParams(maxWidth, imgHeight));
						mapImage.setPadding(0, 5, 0, 5);
						
						mapList.addView(mapImage);

						image = null;
					}
				}

				if (mapList.getChildCount() > 0) ((LinearLayout)findViewById(R.id.map_box)).setVisibility(View.VISIBLE);
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
				warning.showToast("Sorry, c:geo forgot which geocache you want.");

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

			searchId = base.searchByGeocode(params, 0);

			handler.sendMessage(new Message());
		}
	}

	public void loadLongDesc() {
		if (activity != null && (waitDialog == null || waitDialog.isShowing() == false)) {
			descDialog = ProgressDialog.show(activity, null, "loading cache description...", true);
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
			longDesc = Html.fromHtml(cache.description.trim(), new cgHtmlImg(activity, settings, geocode, cache.reason, false), null);
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
				if (waypoint.latitude == null || waypoint.longitude == null) continue;

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

	private void navigateTo() {
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
				dialog.setTitle("Radar isn\'t installed");
				dialog.setMessage("This function requires Radar application. Would you install it?");
				dialog.setCancelable(true);
				dialog.setPositiveButton("yes", new DialogInterface.OnClickListener() {
				   public void onClick(DialogInterface dialog, int id) {
						try {
							activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:com.google.android.radar")));
							dialog.cancel();
						} catch (Exception e) {
							warning.showToast("c:geo can\'t start Android Market to search for Radar application.");
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
			warning.showToast("Sorry, c:geo can\'t start Radar application. Is installed?");
			Log.e(cgSettings.tag, "cgeodetail.radarTo: " + e.toString());
		}
	}

	private void turnTo() {
		if (settings.useGNavigation == 1) {
			try {
				// turn-by-turn navigation
				activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q="+ cache.latitude + "," + cache.longitude)));
			} catch (Exception e) {
				try {
					// google maps directions
					if (geo != null && geo.latitudeNow != null && geo.longitudeNow != null) {
						activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?f=d&saddr="+ geo.latitudeNow + "," + geo.longitudeNow + "&daddr="+ cache.latitude + "," + cache.longitude)));
					} else {
						activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?f=d&daddr="+ cache.latitude + "," + cache.longitude)));
					}
				} catch (Exception e2) {
					Log.d(cgSettings.tag, "cgeodetail.turnTo: No navigation application available.");
					warning.showToast("c:geo can\'t find any supported navigation.");
				}
			}
		} else if (settings.useGNavigation == 0) {
			try {
				// google maps directions
				if (geo != null && geo.latitudeNow != null && geo.longitudeNow != null) {
					activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?f=d&saddr="+ geo.latitudeNow + "," + geo.longitudeNow + "&daddr="+ cache.latitude + "," + cache.longitude)));
				} else {
					activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?f=d&daddr="+ cache.latitude + "," + cache.longitude)));
				}
			} catch (Exception e) {
				Log.d(cgSettings.tag, "cgeodetail.turnTo: No navigation application available.");
				warning.showToast("c:geo can\'t find any suitable application.");
			}
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
		if (cache == null || cache.spoilers == null || cache.spoilers.size() == 0) {
			warning.showToast("c:geo found no spoiler images for this cache");
		}

		Intent spoilersIntent = new Intent(activity, cgeospoilers.class);
		spoilersIntent.putExtra("geocode", geocode.toUpperCase());
		activity.startActivity(spoilersIntent);
	}

	public class codeHint implements View.OnClickListener {
		public void onClick(View arg0) {
			// code hint
			TextView hintView = ((TextView)findViewById(R.id.hint));
			hintView.setText(base.rot13(hintView.getText().toString()));

		}
	}

	private class update extends cgUpdateLoc {
		@Override
		public void updateLoc(cgGeo geo) {
			if (geo == null) return;

			try {
				if (geo.latitudeNow != null && geo.longitudeNow != null && cache != null && cache.latitude != null && cache.longitude != null) {
					cacheDistance.setText(base.getHumanDistance(base.getDistance(geo.latitudeNow, geo.longitudeNow, cache.latitude, cache.longitude)));
					cacheDistance.bringToFront();
				}
			} catch (Exception e) {
				Log.w(cgSettings.tag, "Failed to update location.");
				e.printStackTrace();
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
                warning.showToast("Still removing this cache.");
                return;
            }

			storeDialog = ProgressDialog.show(activity, "offline", "Saving cache for offline use...", true);
			storeDialog.setCancelable(false);
			Thread thread = new storeCacheThread(storeCacheHandler);
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

	private class dropCache implements View.OnClickListener {
		public void onClick(View arg0) {
            if (storeDialog != null && storeDialog.isShowing() == true) {
                warning.showToast("Still saving this cache.");
                return;
            }

			dropDialog = ProgressDialog.show(activity, "offline", "Removing cache from device...", true);
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
