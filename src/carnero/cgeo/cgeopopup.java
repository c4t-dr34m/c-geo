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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
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
    private String geocode = null;
    private cgCache cache = null;
	private cgGeo geo = null;
	private cgUpdateLoc geoUpdate = new update();
    private ProgressDialog storeDialog = null;
    private ProgressDialog dropDialog = null;

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

		if (cache.name != null && cache.name.length() > 0) setTitle(cache.name);
		else setTitle(cache.geocode.toUpperCase());

		HashMap<String, Integer> gcIcons = new HashMap<String, Integer>();
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

		if (cache.name != null && cache.name.length() > 0) setTitle(cache.name);
		else setTitle(cache.geocode);

		StringBuilder cacheInfo = new StringBuilder();
		cacheInfo.append("type: " + cache.type + "\n");
		cacheInfo.append("gc-code: " + cache.geocode + "\n");
		if (cache.size != null && cache.size.length() > 0) {
			cacheInfo.append("size: " + cache.size + "\n");
		}
		if (cache.difficulty != null && cache.difficulty > 0) {
			cacheInfo.append("difficulty: " + String.format(Locale.getDefault(), "%.1f", cache.difficulty) + " of 5\n");
		}
		if (cache.terrain != null && cache.terrain > 0) {
			cacheInfo.append("terrain: " + String.format(Locale.getDefault(), "%.1f", cache.terrain) + " of 5\n");
		}

		if (cache.archived == true || cache.disabled == true || cache.members == true || cache.found == true) {
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

			cacheInfo.append("status: " + state.toString() + "\n");

			state = null;
		}

		if (cacheInfo.length() > 0) {
			((LinearLayout)findViewById(R.id.details_part)).setVisibility(View.VISIBLE);
			((TextView)findViewById(R.id.details)).setText(cacheInfo.toString());
		} else {
			((LinearLayout)findViewById(R.id.details_part)).setVisibility(View.GONE);
		}

		if (cache.latitude != null && cache.longitude != null) {
			((LinearLayout)findViewById(R.id.navigation_part)).setVisibility(View.VISIBLE);

			Button buttonCompass = (Button)findViewById(R.id.compass);
			buttonCompass.setClickable(true);
			buttonCompass.setOnTouchListener(new cgViewTouch(settings, buttonCompass));
			buttonCompass.setOnClickListener(new navigateToListener(cache.latitude, cache.longitude, cache.name, ""));

			Button buttonRadar = (Button)findViewById(R.id.radar);
			if (base.isIntentAvailable(activity, "com.google.android.radar.SHOW_RADAR") == true) {
				buttonRadar.setClickable(true);
				buttonRadar.setOnTouchListener(new cgViewTouch(settings, buttonRadar));
				buttonRadar.setOnClickListener(new radarToListener(cache.latitude, cache.longitude));
			} else {
				buttonRadar.setBackgroundResource(settings.buttonInactive);
			}

			Button buttonTurn = (Button)findViewById(R.id.turn);
			buttonTurn.setClickable(true);
			buttonTurn.setOnTouchListener(new cgViewTouch(settings, buttonTurn));
			buttonTurn.setOnClickListener(new turnToListener(cache.latitude, cache.longitude));
		} else {
			((LinearLayout)findViewById(R.id.navigation_part)).setVisibility(View.GONE);
		}

		if (fromDetail == false) {
			((LinearLayout)findViewById(R.id.more_part)).setVisibility(View.VISIBLE);

			Button buttonA = (Button)findViewById(R.id.more);
			buttonA.setVisibility(View.VISIBLE);
			buttonA.setClickable(true);
			buttonA.setOnTouchListener(new cgViewTouch(settings, buttonA));
			buttonA.setOnClickListener(new OnClickListener() {
				public void onClick(View arg0) {
					Intent cachesIntent = new Intent(activity, cgeodetail.class);
					cachesIntent.putExtra("geocode", geocode.toUpperCase());
					activity.startActivity(cachesIntent);

					activity.finish();
					return;
				}
			});
		} else {
			((LinearLayout)findViewById(R.id.more_part)).setVisibility(View.GONE);
		}

		((LinearLayout)findViewById(R.id.offline_part)).setVisibility(View.VISIBLE);
		Button buttonStore = (Button)findViewById(R.id.offline);
		buttonStore.setVisibility(View.VISIBLE);
		buttonStore.setClickable(true);
		buttonStore.setOnTouchListener(new cgViewTouch(settings, buttonStore));
		if (cache.reason == 1) {
			buttonStore.setText("drop");
			buttonStore.setOnClickListener(new dropCache());
		} else {
			buttonStore.setText("store");
			buttonStore.setOnClickListener(new storeCache());
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
            // nothing
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
