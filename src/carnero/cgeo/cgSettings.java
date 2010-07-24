package carnero.cgeo;

import java.util.Map;
import java.util.HashMap;
import android.os.Environment;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class cgSettings {
	// constants
	public final int unitsMetric = 1;
	public final int unitsImperial = 2;
	public final int mapSatellite = 1;
	public final int mapClassic = 2;
	public final String imgCache = "cgeo";
	public final String imgCacheHidden = ".cgeo";

	// twitter api keys
	public static final String keyConsumerPublic = "RFafPiNi3xRhcS1TPE3wTw";
	public static final String keyConsumerSecret = "7iDJprNPI9hzRwWhpzycSr9SPZMFrdVdsxD2OauI9k";

	// skin
	public int skin = 0;
	public int buttonActive = R.drawable.action_button_dark;
	public int buttonInactive = R.drawable.action_button_dark_off;
	public int buttonPressed = R.drawable.action_button_dark_pressed;

	// settings
	public boolean loginFired = false;
	public boolean loaded = false;
	public boolean hideMySearch = false;
	public int cachesFound = 0;
	public int autoLoadDesc = 0;
	public int units = unitsMetric;
	public int livelist = 1;
	public int maptype = mapSatellite;
	public int mapzoom = 14;
	public int maplive = 1;
	public int maptrail = 1;
	public int excludeMine = 0;
	public int excludeDisabled = 0;
	public int storeOfflineMaps = 1;
	public int asBrowser = 1;
	public int useCompass = 1;
	public int useGNavigation = 1;
	public int publicLoc = 0;
	public int twitter = 0;
	public String cacheType = null;
	public String directoryImg = imgCacheHidden;
	public String tokenPublic = null;
	public String tokenSecret = null;

	// usable values
	public static final String tag = "c:geo";

	// preferences file
	public static final String preferences = "cgeo.pref";

	// private variables
	private Context context = null;
	private SharedPreferences prefs = null;
	private String username = null;
	private String password = null;

	public cgSettings(Context contextIn, SharedPreferences prefsIn) {
		context = contextIn;
		prefs = prefsIn;

		skin = prefs.getInt("skin", 0);
		setSkinDefaults();

		cachesFound = prefs.getInt("found", 0);
		autoLoadDesc = prefs.getInt("autoloaddesc", 0);
		units = prefs.getInt("units", 1);
		livelist = prefs.getInt("livelist", 1);
		maptype = prefs.getInt("maptype", 1);
		maplive = prefs.getInt("maplive", 1);
		mapzoom = prefs.getInt("mapzoom", 14);
		maptrail = prefs.getInt("maptrail", 1);
		excludeMine = prefs.getInt("excludemine", 0);
		excludeDisabled = prefs.getInt("excludedisabled", 0);
		storeOfflineMaps = prefs.getInt("offlinemaps", 1);
		asBrowser = prefs.getInt("asbrowser", 1);
		useCompass = prefs.getInt("usecompass", 1);
		useGNavigation = prefs.getInt("usegnav", 1);
		publicLoc = prefs.getInt("publicloc", 0);
		twitter = prefs.getInt("twitter", 0);
		cacheType = prefs.getString("cachetype", null);
		directoryImg = prefs.getString("directoryimg", imgCacheHidden);
		tokenPublic = prefs.getString("tokenpublic", null);
		tokenSecret = prefs.getString("tokensecret", null);
	}

	private void setSkinDefaults() {
		if (skin == 1) {
			buttonActive = R.drawable.action_button_light;
			buttonInactive = R.drawable.action_button_light_off;
			buttonPressed = R.drawable.action_button_light_pressed;
		} else {
			skin = 0;
			buttonActive = R.drawable.action_button_dark;
			buttonInactive = R.drawable.action_button_dark_off;
			buttonPressed = R.drawable.action_button_dark_pressed;
		}
	}

	public void setSkin(int skinIn) {
		if (skin == 1) {
			skin = 1;
			setSkinDefaults();
		} else {
			skin = 0;
			setSkinDefaults();
		}
	}

	public String getStorage() {
		return getStorageSpecific(null);
	}

	public String getStorageSpecific(Boolean hidden) {
		String storage = "";

		if (hidden == null) {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				storage = Environment.getExternalStorageDirectory() + "/" + directoryImg + "/";
			} else {
				storage = Environment.getDataDirectory() + "/data/carnero.cgeo/" + directoryImg + "/";
			}
		} else if (hidden == false) {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				storage = Environment.getExternalStorageDirectory() + "/" + imgCache + "/";
			} else {
				storage = Environment.getDataDirectory() + "/data/carnero.cgeo/" + imgCache + "/";
			}
		} else if (hidden == true) {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				storage = Environment.getExternalStorageDirectory() + "/" + imgCacheHidden + "/";
			} else {
				storage = Environment.getDataDirectory() + "/data/carnero.cgeo/" + imgCacheHidden + "/";
			}
		}

		return storage;
	}

	public HashMap<String, String> getLogin() {
		final HashMap<String, String> login = new HashMap<String, String>();

		if (username == null || password == null) {
			String preUsername = prefs.getString("username", null);
			String prePassword = prefs.getString("password", null);

			if (loginFired == false && (preUsername == null || prePassword == null)) {
				Intent initIntent = new Intent(context, cgeoinit.class);
				context.startActivity(initIntent);

				loginFired= true;

				return null;
			}

			login.put("username", preUsername);
			login.put("password", prePassword);

			username = preUsername;
			password = prePassword;
		} else {
			login.put("username", username);
			login.put("password", password);
		}

		return login;
	}

	public String getUsername() {
		String user = null;

		if (username == null) {
			user = prefs.getString("username", null);
		} else {
			user = username;
		}

		return user;
	}

	public boolean setLogin(String username, String password) {
		final SharedPreferences.Editor prefsEdit = prefs.edit();

		if (username == null || username.length() == 0 || password == null || password.length() == 0) {
			// erase username and password
			prefsEdit.remove("username");
			prefsEdit.remove("password");
		} else {
			// save username and password
			prefsEdit.putString("username", username);
			prefsEdit.putString("password", password);
		}

		this.username = username;
		this.password = password;
		
		return prefsEdit.commit();
	}

	public void deleteCookies() {
		SharedPreferences.Editor prefsEdit = prefs.edit();

		// delete cookies
		Map prefsValues = prefs.getAll();

		if (prefsValues != null && prefsValues.size() > 0) {
			Object[] keys = prefsValues.keySet().toArray();

			for (int i = 0; i < keys.length; i++) {
				if (keys[i].toString().length() > 7 && keys[i].toString().substring(0, 7).equals("cookie_") == true) {
					prefsEdit.remove(keys[i].toString());
				}
			}
		}

		prefsEdit.commit();
	}

	public String setCacheType(String cacheTypeIn) {
		SharedPreferences.Editor edit = prefs.edit();
		edit.putString("cachetype", cacheTypeIn);
		edit.commit();

		cacheType = prefs.getString("cachetype", null);

		return cacheType;
	}

	public int setFoundCount(int count) {
		SharedPreferences.Editor edit = prefs.edit();
		edit.putInt("found", count);
		edit.commit();

		cachesFound = prefs.getInt("found", 0);

		return cachesFound;
	}

	public int getFoundCount() {
		cachesFound = prefs.getInt("found", 0);

		return cachesFound;
	}
}
