package carnero.cgeo;

import java.util.HashMap;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.content.Intent;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class cgeoadvsearch extends Activity {
	private GoogleAnalyticsTracker tracker = null;
	private Resources res = null;
	private cgeoapplication app = null;
	private Context context = null;
	private cgSettings settings = null;
	private cgBase base = null;
	private cgWarning warning = null;
	private cgGeo geo = null;
	private cgUpdateLoc geoUpdate = new update();
	private EditText latEdit = null;
	private EditText lonEdit = null;
	private String[] geocodesInCache = null;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// google analytics
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start(cgSettings.analytics, this);
		tracker.dispatch();
		tracker.trackPageView("/advanced-search");

		// init
		context = this;
		res = this.getResources();
		app = (cgeoapplication)this.getApplication();
		app.setAction(null);
		settings = new cgSettings(this, getSharedPreferences(cgSettings.preferences, 0));
		base = new cgBase(app, settings, getSharedPreferences(cgSettings.preferences, 0));
		warning = new cgWarning(this);

		// set layout
		setTitle(res.getString(R.string.search));
		if (settings.skin == 1) setContentView(R.layout.advsearch_light);
		else setContentView(R.layout.advsearch_dark);

		init();
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

	private void init() {
		settings.getLogin();
		settings.reloadCacheType();

		if (settings.cacheType != null && base.cacheTypesInv.containsKey(settings.cacheType) == false) settings.setCacheType(null);

		if (geo == null) geo = app.startGeo(context, geoUpdate, base, settings, warning, 0, 0);

		((EditText)findViewById(R.id.latitude)).setOnEditorActionListener(new findByCoordsAction());
		((EditText)findViewById(R.id.longitude)).setOnEditorActionListener(new findByCoordsAction());

		final Button findByCoords = (Button)findViewById(R.id.search_coordinates);
		findByCoords.setClickable(true);
		findByCoords.setOnClickListener(new findByCoordsListener());

		((EditText)findViewById(R.id.address)).setOnEditorActionListener(new findByAddressAction());

		final Button findByAddress = (Button)findViewById(R.id.search_address);
		findByAddress.setClickable(true);
		findByAddress.setOnClickListener(new findByAddressListener());

		final AutoCompleteTextView geocodeEdit = (AutoCompleteTextView)findViewById(R.id.geocode);
		geocodeEdit.setOnEditorActionListener(new findByGeocodeAction());
		geocodesInCache = app.geocodesInCache();
		if (geocodesInCache != null) {
			final ArrayAdapter<String> geocodesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, geocodesInCache);
			geocodeEdit.setAdapter(geocodesAdapter);
		}

		final Button displayByGeocode = (Button)findViewById(R.id.display_geocode);
		displayByGeocode.setClickable(true);
		displayByGeocode.setOnClickListener(new findByGeocodeListener());


		((EditText)findViewById(R.id.keyword)).setOnEditorActionListener(new findByKeywordAction());

		final Button findByKeyword = (Button)findViewById(R.id.search_keyword);
		findByKeyword.setClickable(true);
		findByKeyword.setOnClickListener(new findByKeywordListener());

		((EditText)findViewById(R.id.username)).setOnEditorActionListener(new findByUsernameAction());

		final Button findByUserName = (Button)findViewById(R.id.search_username);
		findByUserName.setClickable(true);
		findByUserName.setOnClickListener(new findByUsernameListener());

		((EditText)findViewById(R.id.owner)).setOnEditorActionListener(new findByOwnerAction());

		final Button findByOwner = (Button)findViewById(R.id.search_owner);
		findByOwner.setClickable(true);
		findByOwner.setOnClickListener(new findByOwnerListener());

		((EditText)findViewById(R.id.trackable)).setOnEditorActionListener(new findTrackableAction());

		final Button displayTrackable = (Button)findViewById(R.id.display_trackable);
		displayTrackable.setClickable(true);
		displayTrackable.setOnClickListener(new findTrackableListener());
	}

	private class update extends cgUpdateLoc {
		@Override
		public void updateLoc(cgGeo geo) {
			if (geo == null) return;

			try {
				if (latEdit == null) latEdit = (EditText)findViewById(R.id.latitude);
				if (lonEdit == null) lonEdit = (EditText)findViewById(R.id.longitude);

				if (geo.latitudeNow != null && geo.longitudeNow != null) {
					latEdit.setHint(base.formatCoordinate(geo.latitudeNow, "lat", false));
					lonEdit.setHint(base.formatCoordinate(geo.longitudeNow, "lon", false));
				}
			} catch (Exception e) {
				Log.w(cgSettings.tag, "Failed to update location.");
			}
		}
	}

	private class findByCoordsAction implements TextView.OnEditorActionListener {
		@Override
		public boolean onEditorAction(TextView view, int action, KeyEvent event) {
			if (action == EditorInfo.IME_ACTION_GO) {
				findByCoordsFn();
				return true;
			}

			return false;
		}
	}

	private class findByCoordsListener implements View.OnClickListener {
		public void onClick(View arg0) {
			findByCoordsFn();
		}
	}

	private void findByCoordsFn() {
		final EditText latView = (EditText)findViewById(R.id.latitude);
		final EditText lonView = (EditText)findViewById(R.id.longitude);
		final String latText = latView.getText().toString();
		final String lonText = lonView.getText().toString();

		if (latText == null || latText.length() == 0 || lonText == null || lonText.length() == 0) {
			latView.setText(base.formatCoordinate(geo.latitudeNow, "lat", true));
			lonView.setText(base.formatCoordinate(geo.longitudeNow, "lon", true));
		} else {
			HashMap latParsed = base.parseCoordinate(latText, "lat");
			HashMap lonParsed = base.parseCoordinate(lonText, "lat");

			if (latParsed == null || latParsed.get("coordinate") == null || latParsed.get("string") == null) {
				warning.showToast("Sorry, c:geo can\'t parse latitude.");
				return;
			}

			if (lonParsed == null || lonParsed.get("coordinate") == null || lonParsed.get("string") == null) {
				warning.showToast("Sorry, c:geo can\'t parse longitude.");
				return;
			}

			final Intent cachesIntent = new Intent(context, cgeocaches.class);
			cachesIntent.putExtra("type", "coordinate");
			cachesIntent.putExtra("latitude", (Double)latParsed.get("coordinate"));
			cachesIntent.putExtra("longitude", (Double)lonParsed.get("coordinate"));
			cachesIntent.putExtra("cachetype", settings.cacheType);
			context.startActivity(cachesIntent);
		}
	}

	private class findByKeywordAction implements TextView.OnEditorActionListener {
		@Override
		public boolean onEditorAction(TextView view, int action, KeyEvent event) {
			if (action == EditorInfo.IME_ACTION_GO) {
				findByKeywordFn();
				return true;
			}

			return false;
		}
	}

	private class findByKeywordListener implements View.OnClickListener {
		public void onClick(View arg0) {
			findByKeywordFn();
		}
	}

	private void findByKeywordFn() {
		// find caches by coordinates
		String keyText = ((EditText)findViewById(R.id.keyword)).getText().toString();

		if (keyText == null || keyText.length() == 0) {
			warning.helpDialog("need some help?", "Fill some word that is supposed to be somewhere in cache name you are trying to find.");
			return;
		}

		final Intent cachesIntent = new Intent(context, cgeocaches.class);
		cachesIntent.putExtra("type", "keyword");
		cachesIntent.putExtra("keyword", keyText);
		cachesIntent.putExtra("cachetype", settings.cacheType);
		context.startActivity(cachesIntent);
	}

	private class findByAddressAction implements TextView.OnEditorActionListener {
		@Override
		public boolean onEditorAction(TextView view, int action, KeyEvent event) {
			if (action == EditorInfo.IME_ACTION_GO) {
				findByAddressFn();
				return true;
			}

			return false;
		}
	}

	private class findByAddressListener implements View.OnClickListener {
		public void onClick(View arg0) {
			findByAddressFn();
		}
	}

	private void findByAddressFn() {
		final String addText = ((EditText)findViewById(R.id.address)).getText().toString();

		if (addText == null || addText.length() == 0) {
			warning.helpDialog("need some help?", "Fill address or location name. For example use street address \"Radlicka 100, Prague, Czech Republic\", city name \"Berlin\" or just name of something like \"Yellowstone Park\".");
			return;
		}

		final Intent addressesIntent = new Intent(context, cgeoaddresses.class);
		addressesIntent.putExtra("keyword", addText);
		context.startActivity(addressesIntent);
	}

	private class findByUsernameAction implements TextView.OnEditorActionListener {
		@Override
		public boolean onEditorAction(TextView view, int action, KeyEvent event) {
			if (action == EditorInfo.IME_ACTION_GO) {
				findByUsernameFn();
				return true;
			}

			return false;
		}
	}

	private class findByUsernameListener implements View.OnClickListener {
		public void onClick(View arg0) {
			findByUsernameFn();
		}
	}

	public void findByUsernameFn() {
		final String usernameText = ((EditText)findViewById(R.id.username)).getText().toString();

		if (usernameText == null || usernameText.length() == 0) {
			warning.helpDialog("need some help?", "Fill name of user of Geocaching.com.");
			return;
		}

		final Intent cachesIntent = new Intent(context, cgeocaches.class);
		cachesIntent.putExtra("type", "username");
		cachesIntent.putExtra("username", usernameText);
		cachesIntent.putExtra("cachetype", settings.cacheType);
		context.startActivity(cachesIntent);
	}

	private class findByOwnerAction implements TextView.OnEditorActionListener {
		@Override
		public boolean onEditorAction(TextView view, int action, KeyEvent event) {
			if (action == EditorInfo.IME_ACTION_GO) {
				findByOwnerFn();
				return true;
			}

			return false;
		}
	}

	private class findByOwnerListener implements View.OnClickListener {
		public void onClick(View arg0) {
			findByOwnerFn();
		}
	}

	private void findByOwnerFn() {
		final String usernameText = ((EditText)findViewById(R.id.owner)).getText().toString();

		if (usernameText == null || usernameText.length() == 0) {
			warning.helpDialog("need some help?", "Fill name of user of Geocaching.com.");
			return;
		}

		final Intent cachesIntent = new Intent(context, cgeocaches.class);
		cachesIntent.putExtra("type", "owner");
		cachesIntent.putExtra("username", usernameText);
		cachesIntent.putExtra("cachetype", settings.cacheType);
		context.startActivity(cachesIntent);
	}

	private class findByGeocodeAction implements TextView.OnEditorActionListener {
		@Override
		public boolean onEditorAction(TextView view, int action, KeyEvent event) {
			if (action == EditorInfo.IME_ACTION_GO) {
				findByGeocodeFn();
				return true;
			}

			return false;
		}
	}

	private class findByGeocodeListener implements View.OnClickListener {
		public void onClick(View arg0) {
			findByGeocodeFn();
		}
	}

	private void findByGeocodeFn() {
		final String geocodeText = ((EditText)findViewById(R.id.geocode)).getText().toString();

		if (geocodeText == null || geocodeText.length() == 0 || geocodeText.equalsIgnoreCase("GC")) {
			warning.helpDialog("need some help?", "Fill code of geocache. For example \"GC1VCAZ\".");
			return;
		}

		final Intent cachesIntent = new Intent(context, cgeodetail.class);
		cachesIntent.putExtra("geocode", geocodeText.toUpperCase());
		context.startActivity(cachesIntent);
	}

	private class findTrackableAction implements TextView.OnEditorActionListener {
		@Override
		public boolean onEditorAction(TextView view, int action, KeyEvent event) {
			if (action == EditorInfo.IME_ACTION_GO) {
				findTrackableFn();
				return true;
			}

			return false;
		}
	}

	private class findTrackableListener implements View.OnClickListener {
		public void onClick(View arg0) {
			findTrackableFn();
		}
	}

	private void findTrackableFn() {
		final String trackableText = ((EditText)findViewById(R.id.trackable)).getText().toString();

		if (trackableText == null || trackableText.length() == 0 || trackableText.equalsIgnoreCase("TB")) {
			warning.helpDialog("need some help?", "Fill code of trackable. For example \"TB29QMZ\".");
			return;
		}

		final Intent trackablesIntent = new Intent(context, cgeotrackable.class);
		trackablesIntent.putExtra("geocode", trackableText.toUpperCase());
		context.startActivity(trackablesIntent);
	}
}
