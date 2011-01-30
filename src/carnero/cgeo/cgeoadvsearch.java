package carnero.cgeo;

import java.util.HashMap;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

public class cgeoadvsearch extends Activity {
	private Resources res = null;
	private Activity activity = null;
	private cgeoapplication app = null;
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

		// init
		activity = this;
		res = this.getResources();
		app = (cgeoapplication)this.getApplication();
		app.setAction(null);
		settings = new cgSettings(this, getSharedPreferences(cgSettings.preferences, 0));
		base = new cgBase(app, settings, getSharedPreferences(cgSettings.preferences, 0));
		warning = new cgWarning(this);

		// set layout
		if (settings.skin == 1) {
			setTheme(R.style.light);
		} else {
			setTheme(R.style.dark);
		}
		setContentView(R.layout.search);
		base.setTitle(activity, res.getString(R.string.search));

		// google analytics
		base.sendAnal(activity, "/advanced-search");

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

		if (geo == null) geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);

		((EditText)findViewById(R.id.latitude)).setOnEditorActionListener(new findByCoordsAction());
		((EditText)findViewById(R.id.longitude)).setOnEditorActionListener(new findByCoordsAction());

		final Button findByCoords = (Button)findViewById(R.id.search_coordinates);
		findByCoords.setOnClickListener(new findByCoordsListener());

		((EditText)findViewById(R.id.address)).setOnEditorActionListener(new findByAddressAction());

		final Button findByAddress = (Button)findViewById(R.id.search_address);
		findByAddress.setOnClickListener(new findByAddressListener());

		final AutoCompleteTextView geocodeEdit = (AutoCompleteTextView)findViewById(R.id.geocode);
		geocodeEdit.setOnEditorActionListener(new findByGeocodeAction());
		geocodesInCache = app.geocodesInCache();
		if (geocodesInCache != null) {
			final ArrayAdapter<String> geocodesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, geocodesInCache);
			geocodeEdit.setAdapter(geocodesAdapter);
		}
		geocodeEdit.addTextChangedListener(new UpperCaseTextWatcher(geocodeEdit));

		final Button displayByGeocode = (Button)findViewById(R.id.display_geocode);
		displayByGeocode.setOnClickListener(new findByGeocodeListener());


		((EditText)findViewById(R.id.keyword)).setOnEditorActionListener(new findByKeywordAction());

		final Button findByKeyword = (Button)findViewById(R.id.search_keyword);
		findByKeyword.setOnClickListener(new findByKeywordListener());

		((EditText)findViewById(R.id.username)).setOnEditorActionListener(new findByUsernameAction());

		final Button findByUserName = (Button)findViewById(R.id.search_username);
		findByUserName.setOnClickListener(new findByUsernameListener());

		((EditText)findViewById(R.id.owner)).setOnEditorActionListener(new findByOwnerAction());

		final Button findByOwner = (Button)findViewById(R.id.search_owner);
		findByOwner.setOnClickListener(new findByOwnerListener());

		EditText trackable = (EditText)findViewById(R.id.trackable);
		trackable.setOnEditorActionListener(new findTrackableAction());
		trackable.addTextChangedListener(new UpperCaseTextWatcher(trackable));

		final Button displayTrackable = (Button)findViewById(R.id.display_trackable);
		displayTrackable.setOnClickListener(new findTrackableListener());
	}

	/**
	 * converts user input to uppercase during typing
	 * @author bananeweizen
	 *
	 */
	private final class UpperCaseTextWatcher implements TextWatcher {
		private final EditText editText;

		private UpperCaseTextWatcher(EditText editText) {
			this.editText = editText;
		}

		@Override
		public void afterTextChanged(Editable arg0) {
			// empty
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1,
				int arg2, int arg3) {
			// empty
		}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			String oldText = editText.getText().toString();
			String upperText = oldText.toUpperCase();
			if (!oldText.equals(upperText)) {
				int selectionStart = editText.getSelectionStart();
				int selectionEnd = editText.getSelectionEnd();
				editText.setText(upperText);
				editText.setSelection(selectionStart, selectionEnd);
			}
		}
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
				warning.showToast(res.getString(R.string.err_parse_lat));
				return;
			}

			if (lonParsed == null || lonParsed.get("coordinate") == null || lonParsed.get("string") == null) {
				warning.showToast(res.getString(R.string.err_parse_lon));
				return;
			}

			final Intent cachesIntent = new Intent(activity, cgeocaches.class);
			cachesIntent.putExtra("type", "coordinate");
			cachesIntent.putExtra("latitude", (Double)latParsed.get("coordinate"));
			cachesIntent.putExtra("longitude", (Double)lonParsed.get("coordinate"));
			cachesIntent.putExtra("cachetype", settings.cacheType);
			activity.startActivity(cachesIntent);
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
			warning.helpDialog(res.getString(R.string.warn_search_help_title), res.getString(R.string.warn_search_help_keyword));
			return;
		}

		final Intent cachesIntent = new Intent(activity, cgeocaches.class);
		cachesIntent.putExtra("type", "keyword");
		cachesIntent.putExtra("keyword", keyText);
		cachesIntent.putExtra("cachetype", settings.cacheType);
		activity.startActivity(cachesIntent);
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
			warning.helpDialog(res.getString(R.string.warn_search_help_title), res.getString(R.string.warn_search_help_address));
			return;
		}

		final Intent addressesIntent = new Intent(activity, cgeoaddresses.class);
		addressesIntent.putExtra("keyword", addText);
		activity.startActivity(addressesIntent);
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
			warning.helpDialog(res.getString(R.string.warn_search_help_title), res.getString(R.string.warn_search_help_user));
			return;
		}

		final Intent cachesIntent = new Intent(activity, cgeocaches.class);
		cachesIntent.putExtra("type", "username");
		cachesIntent.putExtra("username", usernameText);
		cachesIntent.putExtra("cachetype", settings.cacheType);
		activity.startActivity(cachesIntent);
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
			warning.helpDialog(res.getString(R.string.warn_search_help_title), res.getString(R.string.warn_search_help_user));
			return;
		}

		final Intent cachesIntent = new Intent(activity, cgeocaches.class);
		cachesIntent.putExtra("type", "owner");
		cachesIntent.putExtra("username", usernameText);
		cachesIntent.putExtra("cachetype", settings.cacheType);
		activity.startActivity(cachesIntent);
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
			warning.helpDialog(res.getString(R.string.warn_search_help_title), res.getString(R.string.warn_search_help_gccode));
			return;
		}

		final Intent cachesIntent = new Intent(activity, cgeodetail.class);
		cachesIntent.putExtra("geocode", geocodeText.toUpperCase());
		activity.startActivity(cachesIntent);
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
			warning.helpDialog(res.getString(R.string.warn_search_help_title), res.getString(R.string.warn_search_help_tb));
			return;
		}

		final Intent trackablesIntent = new Intent(activity, cgeotrackable.class);
		trackablesIntent.putExtra("geocode", trackableText.toUpperCase());
		activity.startActivity(trackablesIntent);
	}

	public void goHome(View view) {
		base.goHome(activity);
	}
}
