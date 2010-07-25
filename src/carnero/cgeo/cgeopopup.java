package carnero.cgeo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.HashMap;
import java.util.Locale;

public class cgeopopup extends Activity {
	private Activity activity = null;
    private cgeoapplication app = null;
	private cgSettings settings = null;
	private cgBase base = null;
	private cgWarning warning = null;
    private Boolean fromDetail = false;
	private LayoutInflater inflater = null;
    private String geocode = null;
    private cgCache cache = null;
	private cgGeo geo = null;
	private cgUpdateLoc geoUpdate = new update();
    private ProgressDialog storeDialog = null;
    private ProgressDialog dropDialog = null;
	private TextView cacheDistance = null;
	private HashMap<String, Integer> gcIcons = new HashMap<String, Integer>();

	private Handler storeCacheHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				if (storeDialog != null) storeDialog.dismiss();

				finish();
				return;
			} catch (Exception e) {
				warning.showToast("Sorry, c:geo can\'t store geocache.");

				Log.e(cgSettings.tag, "cgeopopup.storeCacheHandler: " + e.toString());
			}

            if (storeDialog != null) storeDialog.dismiss();
			init();
        }
    };

	private Handler dropCacheHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				if (dropDialog != null) dropDialog.dismiss();

				finish();
				return;
			} catch (Exception e) {
				warning.showToast("Sorry, c:geo can\'t drop geocache.");

				Log.e(cgSettings.tag, "cgeopopup.dropCacheHandler: " + e.toString());
			}

            if (dropDialog != null) dropDialog.dismiss();
            init();
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
		setTitle("detail");
		if (settings.skin == 1) setContentView(R.layout.popup_light);
		else setContentView(R.layout.popup_dark);

		// get parameters
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
            fromDetail = extras.getBoolean("fromdetail");
			geocode = extras.getString("geocode");
        }

        if (geocode == null || geocode.length() == 0) {
            warning.showToast("Sorry, c:geo can\'t find that geocache.");

            finish();
            return;
        }

        init();
	}

   private void init() {
        if (geo == null) geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);

		app.setAction(geocode);

		cache = app.getCacheByGeocode(geocode);

		if (cache == null) {
			warning.showToast("Sorry, c:geo can\'t find that geocache.");

			finish();
			return;
		}

		try {
			RelativeLayout itemLayout;
			TextView itemName;
			TextView itemValue;
			LinearLayout itemStars;

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
			if (settings.skin == 1) itemLayout = (RelativeLayout)inflater.inflate(R.layout.cacheitem_light, null);
			else itemLayout = (RelativeLayout) inflater.inflate(R.layout.cacheitem_dark, null);
			itemName = (TextView)itemLayout.findViewById(R.id.name);
			itemValue = (TextView)itemLayout.findViewById(R.id.value);

			itemName.setText("type");
			if (base.cacheTypesInv.containsKey(cache.type) == true) { // cache icon
				if (cache.size != null && cache.size.length() > 0) itemValue.setText(base.cacheTypesInv.get(cache.type) + " (" + cache.size + ")");
				else itemValue.setText(base.cacheTypesInv.get(cache.type));
			} else {
				if (cache.size != null && cache.size.length() > 0) itemValue.setText(base.cacheTypesInv.get("mystery") + " (" + cache.size + ")");
				else itemValue.setText(base.cacheTypesInv.get("mystery"));
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
			if (cache.difficulty > 0f) {
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
			}

			// terrain
			if (cache.terrain > 0f) {
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
			}

			// more details
			if (fromDetail == false) {
				((LinearLayout)findViewById(R.id.more_details_box)).setVisibility(View.VISIBLE);

				Button buttonMore = (Button)findViewById(R.id.more_details);
				buttonMore.setClickable(true);
				buttonMore.setOnTouchListener(new cgViewTouch(settings, buttonMore, 0));
				buttonMore.setOnClickListener(new OnClickListener() {
					public void onClick(View arg0) {
						Intent cachesIntent = new Intent(activity, cgeodetail.class);
						cachesIntent.putExtra("geocode", geocode.toUpperCase());
						activity.startActivity(cachesIntent);

						activity.finish();
						return;
					}
				});
			} else {
				((LinearLayout)findViewById(R.id.more_details_box)).setVisibility(View.GONE);
			}

			if (fromDetail == false) {
				((LinearLayout)findViewById(R.id.offline_box)).setVisibility(View.VISIBLE);
				
				// offline use
				final TextView offlineText = (TextView)findViewById(R.id.offline_text);
				final Button offlineRefresh = (Button)findViewById(R.id.offline_refresh);
				final Button offlineStore = (Button)findViewById(R.id.offline_store);

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

					offlineText.setText("stored in device\n" + ago);

					offlineRefresh.setVisibility(View.VISIBLE);
					offlineRefresh.setClickable(true);
					offlineRefresh.setOnTouchListener(new cgViewTouch(settings, offlineRefresh, 0));
					offlineRefresh.setOnClickListener(new storeCache());

					offlineStore.setText("drop");
					offlineStore.setClickable(true);
					offlineStore.setOnTouchListener(new cgViewTouch(settings, offlineStore, 0));
					offlineStore.setOnClickListener(new dropCache());
				} else {
					offlineText.setText("not ready\nfor offline use");

					offlineRefresh.setVisibility(View.GONE);
					offlineRefresh.setClickable(false);
					offlineRefresh.setOnTouchListener(null);
					offlineRefresh.setOnClickListener(null);

					offlineStore.setText("store");
					offlineStore.setClickable(true);
					offlineStore.setOnTouchListener(new cgViewTouch(settings, offlineStore, 0));
					offlineStore.setOnClickListener(new storeCache());
				}
			} else {
				((LinearLayout)findViewById(R.id.offline_box)).setVisibility(View.GONE);
			}
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgeopopup.init: " + e.toString());
		}

		if (cache.latitude != null && cache.longitude != null) {
			((LinearLayout)findViewById(R.id.navigation_part)).setVisibility(View.VISIBLE);

			Button buttonCompass = (Button)findViewById(R.id.compass);
			buttonCompass.setClickable(true);
			buttonCompass.setOnTouchListener(new cgViewTouch(settings, buttonCompass, 0));
			buttonCompass.setOnClickListener(new navigateToListener(cache.latitude, cache.longitude, cache.name, ""));

			Button buttonRadar = (Button)findViewById(R.id.radar);
			if (base.isIntentAvailable(activity, "com.google.android.radar.SHOW_RADAR") == true) {
				buttonRadar.setClickable(true);
				buttonRadar.setOnTouchListener(new cgViewTouch(settings, buttonRadar, 0));
				buttonRadar.setOnClickListener(new radarToListener(cache.latitude, cache.longitude));
			} else {
				buttonRadar.setBackgroundResource(settings.buttonInactive);
			}

			Button buttonTurn = (Button)findViewById(R.id.turn);
			buttonTurn.setClickable(true);
			buttonTurn.setOnTouchListener(new cgViewTouch(settings, buttonTurn, 0));
			buttonTurn.setOnClickListener(new turnToListener(cache.latitude, cache.longitude));
		} else {
			((LinearLayout)findViewById(R.id.navigation_part)).setVisibility(View.GONE);
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
			}
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
			cgeonavigate navigateActivity = new cgeonavigate();

			Intent navigateIntent = new Intent(activity, navigateActivity.getClass());

			navigateIntent.putExtra("latitude", latitude);
			navigateIntent.putExtra("longitude", longitude);
			navigateIntent.putExtra("geocode", geocode.toUpperCase());
			navigateIntent.putExtra("name", name);
            
			activity.startActivity(navigateIntent);
            
            activity.finish();
            return;
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

                activity.finish();
                return;
			} catch (Exception e) {
				warning.showToast("c:geo can\'t use Radar because this application isn't installed.");
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
			if (settings.useGNavigation == 1) {
				try {
					// turn-by-turn navigation
					activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q="+ latitude + "," + longitude)));
				} catch (Exception e) {
					try {
						// google maps directions
						if (geo != null && geo.latitudeNow != null && geo.longitudeNow != null) {
							activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?f=d&saddr="+ geo.latitudeNow + "," + geo.longitudeNow + "&daddr="+ latitude + "," + longitude)));
						} else {
							activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?f=d&daddr="+ latitude + "," + longitude)));
						}

                        activity.finish();
                        return;
					} catch (Exception e2) {
						Log.d(cgSettings.tag, "cgeopopup.turnTo: No navigation application available.");
						warning.showToast("c:geo can\'t find any supported navigation.");
					}
				}
			} else if (settings.useGNavigation == 0) {
				try {
					// google maps directions
					if (geo != null && geo.latitudeNow != null && geo.longitudeNow != null) {
						activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?f=d&saddr="+ geo.latitudeNow + "," + geo.longitudeNow + "&daddr="+ latitude + "," + longitude)));
					} else {
						activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?f=d&daddr="+ latitude + "," + longitude)));
					}

                    activity.finish();
                    return;
				} catch (Exception e) {
					Log.d(cgSettings.tag, "cgeopopup.turnTo: No navigation application available.");
					warning.showToast("c:geo can\'t find any suitable application.");
				}
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
}
