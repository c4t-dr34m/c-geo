package carnero.cgeo;

import gnu.android.app.appmanualclient.*;

import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.view.View;
import android.widget.EditText;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import java.io.File;

public class cgeoinit extends Activity {

	private cgeoapplication app = null;
	private Resources res = null;
	private Activity activity = null;
	private cgSettings settings = null;
	private cgBase base = null;
	private cgWarning warning = null;
	private SharedPreferences prefs = null;
	private ProgressDialog loginDialog = null;
	private Handler logInHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			try {
				if (loginDialog != null && loginDialog.isShowing() == true) {
					loginDialog.dismiss();
				}

				if (msg.what == 1) {
					warning.helpDialog(res.getString(R.string.init_login_popup), res.getString(R.string.init_login_popup_ok));
				} else {
					if (base.errorRetrieve.containsKey(msg.what) == true) {
						warning.helpDialog(res.getString(R.string.init_login_popup),
								res.getString(R.string.init_login_popup_failed_reason) + " " + base.errorRetrieve.get(msg.what) + ".");
					} else {
						warning.helpDialog(res.getString(R.string.init_login_popup), res.getString(R.string.init_login_popup_failed));
					}
				}
			} catch (Exception e) {
				warning.showToast(res.getString(R.string.err_login_failed));

				Log.e(cgSettings.tag, "cgeoinit.logInHandler: " + e.toString());
			}

			if (loginDialog != null && loginDialog.isShowing() == true) {
				loginDialog.dismiss();
			}

			init();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// init
		activity = this;
		res = this.getResources();
		app = (cgeoapplication) this.getApplication();
		prefs = getSharedPreferences(cgSettings.preferences, 0);
		settings = new cgSettings(this, prefs);
		base = new cgBase(app, settings, prefs);
		warning = new cgWarning(this);

		// set layout
		if (settings.skin == 1) {
			setTheme(R.style.light);
		} else {
			setTheme(R.style.dark);
		}
		setContentView(R.layout.init);
		base.setTitle(activity, res.getString(R.string.settings));

		// google analytics
		base.sendAnal(activity, "/init");

		init();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		init();
	}

	@Override
	public void onPause() {
		saveValues();
		super.onPause();
	}

	@Override
	public void onStop() {
		saveValues();
		super.onStop();
	}

	@Override
	public void onDestroy() {
		saveValues();

		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, res.getString(R.string.init_clear)).setIcon(android.R.drawable.ic_menu_delete);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == 0) {
			boolean status = false;

			((EditText) findViewById(R.id.username)).setText("");
			((EditText) findViewById(R.id.password)).setText("");
			((EditText) findViewById(R.id.passvote)).setText("");

			status = saveValues();
			if (status == true) {
				warning.showToast(res.getString(R.string.init_cleared));
			} else {
				warning.showToast(res.getString(R.string.err_init_cleared));
			}

			finish();
		}

		return false;
	}

	public void init() {
		String usernameNow = prefs.getString("username", null);
		if (usernameNow != null) {
			((EditText) findViewById(R.id.username)).setText(usernameNow);
		}
		String passwordNow = prefs.getString("password", null);
		if (usernameNow != null) {
			((EditText) findViewById(R.id.password)).setText(passwordNow);
		}
		String passvoteNow = prefs.getString("pass-vote", null);
		if (passvoteNow != null) {
			((EditText) findViewById(R.id.passvote)).setText(passvoteNow);
		}

		Button logMeIn = (Button) findViewById(R.id.log_me_in);
		logMeIn.setOnClickListener(new logIn());

		TextView legalNote = (TextView) findViewById(R.id.legal_note);
		legalNote.setClickable(true);
		legalNote.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.geocaching.com/about/termsofuse.aspx")));
			}
		});

		TextView go4cache = (TextView) findViewById(R.id.about_go4cache);
		go4cache.setClickable(true);
		go4cache.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://go4cache.com/")));
			}
		});

		CheckBox publicButton = (CheckBox) findViewById(R.id.publicloc);
		if (prefs.getInt("publicloc", 0) == 0) {
			publicButton.setChecked(false);
		} else {
			publicButton.setChecked(true);
		}
		publicButton.setOnClickListener(new cgeoChangePublic());

		Button authorizeTwitter = (Button) findViewById(R.id.authorize_twitter);
		authorizeTwitter.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				Intent authIntent = new Intent(activity, cgeoauth.class);
				activity.startActivity(authIntent);
			}
		});

		CheckBox twitterButton = (CheckBox) findViewById(R.id.twitter_option);
		if (prefs.getInt("twitter", 0) == 0 || settings.tokenPublic == null || settings.tokenPublic.length() == 0 || settings.tokenSecret == null || settings.tokenSecret.length() == 0) {
			twitterButton.setChecked(false);
		} else {
			twitterButton.setChecked(true);
		}
		twitterButton.setOnClickListener(new cgeoChangeTwitter());

		EditText sigEdit = (EditText) findViewById(R.id.signature);
		if (sigEdit.getText().length() == 0) {
			sigEdit.setText(settings.getSignature());
		}
		Button sigBtn = (Button) findViewById(R.id.signature_help);
		sigBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				warning.helpDialog(res.getString(R.string.init_signature_help_title), res.getString(R.string.init_signature_help_text));
			}
		});

		CheckBox skinButton = (CheckBox) findViewById(R.id.skin);
		if (prefs.getInt("skin", 0) == 0) {
			skinButton.setChecked(false);
		} else {
			skinButton.setChecked(true);
		}
		skinButton.setOnClickListener(new cgeoChangeSkin());

		CheckBox addressButton = (CheckBox) findViewById(R.id.address);
		if (prefs.getInt("showaddress", 1) == 0) {
			addressButton.setChecked(false);
		} else {
			addressButton.setChecked(true);
		}
		addressButton.setOnClickListener(new cgeoChangeAddress());

		CheckBox captchaButton = (CheckBox) findViewById(R.id.captcha);
		if (prefs.getBoolean("showcaptcha", false) == false) {
			captchaButton.setChecked(false);
		} else {
			captchaButton.setChecked(true);
		}
		captchaButton.setOnClickListener(new cgeoChangeCaptcha());

		CheckBox excludeButton = (CheckBox) findViewById(R.id.exclude);
		if (prefs.getInt("excludemine", 0) == 0) {
			excludeButton.setChecked(false);
		} else {
			excludeButton.setChecked(true);
		}
		excludeButton.setOnClickListener(new cgeoChangeExclude());

		CheckBox disabledButton = (CheckBox) findViewById(R.id.disabled);
		if (prefs.getInt("excludedisabled", 0) == 0) {
			disabledButton.setChecked(false);
		} else {
			disabledButton.setChecked(true);
		}
		disabledButton.setOnClickListener(new cgeoChangeDisabled());

		CheckBox offlineButton = (CheckBox) findViewById(R.id.offline);
		if (prefs.getInt("offlinemaps", 1) == 0) {
			offlineButton.setChecked(false);
		} else {
			offlineButton.setChecked(true);
		}
		offlineButton.setOnClickListener(new cgeoChangeOffline());

		CheckBox autoloadButton = (CheckBox) findViewById(R.id.autoload);
		if (prefs.getInt("autoloaddesc", 0) == 0) {
			autoloadButton.setChecked(false);
		} else {
			autoloadButton.setChecked(true);
		}
		autoloadButton.setOnClickListener(new cgeoChangeAutoload());

		CheckBox livelistButton = (CheckBox) findViewById(R.id.livelist);
		if (prefs.getInt("livelist", 1) == 0) {
			livelistButton.setChecked(false);
		} else {
			livelistButton.setChecked(true);
		}
		livelistButton.setOnClickListener(new cgeoChangeLivelist());

		CheckBox unitsButton = (CheckBox) findViewById(R.id.units);
		if (prefs.getInt("units", settings.unitsMetric) == settings.unitsMetric) {
			unitsButton.setChecked(false);
		} else {
			unitsButton.setChecked(true);
		}
		unitsButton.setOnClickListener(new cgeoChangeUnits());

		CheckBox gnavButton = (CheckBox) findViewById(R.id.gnav);
		if (prefs.getInt("usegnav", 1) == 1) {
			gnavButton.setChecked(true);
		} else {
			gnavButton.setChecked(false);
		}
		gnavButton.setOnClickListener(new cgeoChangeGNav());

		CheckBox imgButton = (CheckBox) findViewById(R.id.directoryimg);
		if (prefs.getString("directoryimg", settings.imgCacheHidden).equalsIgnoreCase(settings.imgCache)) {
			imgButton.setChecked(false);
		} else {
			imgButton.setChecked(true);
		}
		imgButton.setOnClickListener(new cgeoChangeImgCache());

		CheckBox browserButton = (CheckBox) findViewById(R.id.browser);
		if (prefs.getInt("asbrowser", 1) == 0) {
			browserButton.setChecked(false);
		} else {
			browserButton.setChecked(true);
		}
		browserButton.setOnClickListener(new cgeoChangeBrowser());
	}

	public boolean saveValues() {
		String usernameNew = ((EditText) findViewById(R.id.username)).getText().toString();
		String passwordNew = ((EditText) findViewById(R.id.password)).getText().toString();
		String passvoteNew = ((EditText) findViewById(R.id.passvote)).getText().toString();
		String signatureNew = ((EditText) findViewById(R.id.signature)).getText().toString();

		if (usernameNew == null) {
			usernameNew = "";
		}
		if (passwordNew == null) {
			passwordNew = "";
		}
		if (passvoteNew == null) {
			passvoteNew = "";
		}

		final boolean status1 = settings.setLogin(usernameNew, passwordNew);
		final boolean status2 = settings.setGCvoteLogin(passvoteNew);
		final boolean status3 = settings.setSignature(signatureNew);

		if (status1 == true && status2 == true && status3 == true) {
			return true;
		}

		return false;
	}

	private class cgeoChangeTwitter implements View.OnClickListener {

		public void onClick(View arg0) {
			CheckBox twitterButton = (CheckBox) findViewById(R.id.twitter_option);

			if (twitterButton.isChecked() == true) {
				settings.reloadTwitterTokens();

				SharedPreferences.Editor edit = prefs.edit();
				if (prefs.getInt("twitter", 0) == 0 || settings.tokenPublic == null || settings.tokenPublic.length() == 0 || settings.tokenSecret == null || settings.tokenSecret.length() == 0) {
					edit.putInt("twitter", 1);
					settings.twitter = 1;
				} else {
					edit.putInt("twitter", 0);
					settings.twitter = 0;
				}
				edit.commit();

				if (settings.twitter == 1 && (settings.tokenPublic == null || settings.tokenPublic.length() == 0 || settings.tokenSecret == null || settings.tokenSecret.length() == 0)) {
					Intent authIntent = new Intent(activity, cgeoauth.class);
					activity.startActivity(authIntent);
				}

				if (prefs.getInt("twitter", 0) == 0) {
					twitterButton.setChecked(false);
				} else {
					twitterButton.setChecked(true);
				}
			} else {
				SharedPreferences.Editor edit = prefs.edit();
				edit.putInt("twitter", 0);
				settings.twitter = 0;
				twitterButton.setChecked(false);
			}

			return;
		}
	}

	private class cgeoChangeSkin implements View.OnClickListener {

		public void onClick(View arg0) {
			SharedPreferences.Editor edit = prefs.edit();
			if (prefs.getInt("skin", 0) == 0) {
				edit.putInt("skin", 1);
				settings.setSkin(1);
			} else {
				edit.putInt("skin", 0);
				settings.setSkin(0);
			}
			edit.commit();

			CheckBox skinButton = (CheckBox) findViewById(R.id.skin);
			if (prefs.getInt("skin", 0) == 0) {
				skinButton.setChecked(false);
			} else {
				skinButton.setChecked(true);
			}

			return;
		}
	}

	private class cgeoChangeAddress implements View.OnClickListener {

		public void onClick(View arg0) {
			SharedPreferences.Editor edit = prefs.edit();
			if (prefs.getInt("showaddress", 1) == 0) {
				edit.putInt("showaddress", 1);
			} else {
				edit.putInt("showaddress", 0);
			}
			edit.commit();

			CheckBox transparentButton = (CheckBox) findViewById(R.id.address);
			if (prefs.getInt("showaddress", 1) == 0) {
				transparentButton.setChecked(false);
			} else {
				transparentButton.setChecked(true);
			}

			return;
		}
	}

	private class cgeoChangePublic implements View.OnClickListener {

		public void onClick(View arg0) {
			SharedPreferences.Editor edit = prefs.edit();
			if (prefs.getInt("publicloc", 0) == 0) {
				edit.putInt("publicloc", 1);
				settings.publicLoc = 1;
			} else {
				edit.putInt("publicloc", 0);
				settings.publicLoc = 0;
			}
			edit.commit();

			CheckBox publicloc = (CheckBox) findViewById(R.id.publicloc);
			if (prefs.getInt("publicloc", 0) == 0) {
				publicloc.setChecked(false);
			} else {
				publicloc.setChecked(true);
			}

			return;
		}
	}

	private class cgeoChangeCaptcha implements View.OnClickListener {

		public void onClick(View arg0) {
			SharedPreferences.Editor edit = prefs.edit();
			if (prefs.getBoolean("showcaptcha", false) == false) {
				edit.putBoolean("showcaptcha", true);
				settings.showCaptcha = true;
			} else {
				edit.putBoolean("showcaptcha", false);
				settings.showCaptcha = false;
			}
			edit.commit();

			CheckBox captchaButton = (CheckBox) findViewById(R.id.captcha);
			if (prefs.getBoolean("showcaptcha", false) == false) {
				captchaButton.setChecked(false);
			} else {
				captchaButton.setChecked(true);
			}

			return;
		}
	}

	private class cgeoChangeExclude implements View.OnClickListener {

		public void onClick(View arg0) {
			SharedPreferences.Editor edit = prefs.edit();
			if (prefs.getInt("excludemine", 0) == 0) {
				edit.putInt("excludemine", 1);
				settings.excludeMine = 1;
			} else {
				edit.putInt("excludemine", 0);
				settings.excludeMine = 0;
			}
			edit.commit();

			CheckBox excludeButton = (CheckBox) findViewById(R.id.exclude);
			if (prefs.getInt("excludemine", 0) == 0) {
				excludeButton.setChecked(false);
			} else {
				excludeButton.setChecked(true);
			}

			return;
		}
	}

	private class cgeoChangeDisabled implements View.OnClickListener {

		public void onClick(View arg0) {
			SharedPreferences.Editor edit = prefs.edit();
			if (prefs.getInt("excludedisabled", 0) == 0) {
				edit.putInt("excludedisabled", 1);
				settings.excludeDisabled = 1;
			} else {
				edit.putInt("excludedisabled", 0);
				settings.excludeDisabled = 0;
			}
			edit.commit();

			CheckBox disabledButton = (CheckBox) findViewById(R.id.disabled);
			if (prefs.getInt("excludedisabled", 0) == 0) {
				disabledButton.setChecked(false);
			} else {
				disabledButton.setChecked(true);
			}

			return;
		}
	}

	private class cgeoChangeOffline implements View.OnClickListener {

		public void onClick(View arg0) {
			SharedPreferences.Editor edit = prefs.edit();
			if (prefs.getInt("offlinemaps", 1) == 0) {
				edit.putInt("offlinemaps", 1);
				settings.excludeDisabled = 1;
			} else {
				edit.putInt("offlinemaps", 0);
				settings.excludeDisabled = 0;
			}
			edit.commit();

			CheckBox offlineButton = (CheckBox) findViewById(R.id.offline);
			if (prefs.getInt("offlinemaps", 0) == 0) {
				offlineButton.setChecked(false);
			} else {
				offlineButton.setChecked(true);
			}

			return;
		}
	}

	private class cgeoChangeLivelist implements View.OnClickListener {

		public void onClick(View arg0) {
			SharedPreferences.Editor edit = prefs.edit();
			if (prefs.getInt("livelist", 1) == 0) {
				edit.putInt("livelist", 1);
				settings.livelist = 1;
			} else {
				edit.putInt("livelist", 0);
				settings.livelist = 0;
			}
			edit.commit();

			CheckBox livelistButton = (CheckBox) findViewById(R.id.livelist);
			if (prefs.getInt("livelist", 1) == 0) {
				livelistButton.setChecked(false);
			} else {
				livelistButton.setChecked(true);
			}

			return;
		}
	}

	private class cgeoChangeAutoload implements View.OnClickListener {

		public void onClick(View arg0) {
			SharedPreferences.Editor edit = prefs.edit();
			if (prefs.getInt("autoloaddesc", 0) == 0) {
				edit.putInt("autoloaddesc", 1);
				settings.autoLoadDesc = 1;
			} else {
				edit.putInt("autoloaddesc", 0);
				settings.autoLoadDesc = 0;
			}
			edit.commit();

			CheckBox autoloadButton = (CheckBox) findViewById(R.id.autoload);
			if (prefs.getInt("autoloaddesc", 0) == 0) {
				autoloadButton.setChecked(false);
			} else {
				autoloadButton.setChecked(true);
			}

			return;
		}
	}

	private class cgeoChangeUnits implements View.OnClickListener {

		public void onClick(View arg0) {
			SharedPreferences.Editor edit = prefs.edit();
			if (prefs.getInt("units", settings.unitsMetric) == settings.unitsMetric) {
				edit.putInt("units", settings.unitsImperial);
				settings.units = settings.unitsImperial;
			} else {
				edit.putInt("units", settings.unitsMetric);
				settings.units = settings.unitsMetric;
			}
			edit.commit();

			CheckBox unitsButton = (CheckBox) findViewById(R.id.units);
			if (prefs.getInt("units", settings.unitsMetric) == settings.unitsMetric) {
				unitsButton.setChecked(false);
			} else {
				unitsButton.setChecked(true);
			}

			return;
		}
	}

	private class cgeoChangeGNav implements View.OnClickListener {

		public void onClick(View arg0) {
			SharedPreferences.Editor edit = prefs.edit();
			if (prefs.getInt("usegnav", 1) == 1) {
				edit.putInt("usegnav", 0);
				settings.useGNavigation = 0;
			} else {
				edit.putInt("usegnav", 1);
				settings.useGNavigation = 1;
			}
			edit.commit();

			CheckBox gnavButton = (CheckBox) findViewById(R.id.gnav);
			if (prefs.getInt("usegnav", 1) == 1) {
				gnavButton.setChecked(true);
			} else {
				gnavButton.setChecked(false);
			}

			return;
		}
	}

	private class cgeoChangeImgCache implements View.OnClickListener {

		public void onClick(View arg0) {
			SharedPreferences.Editor edit = prefs.edit();
			File dir = null;
			File dirNew = null;

			if (prefs.getString("directoryimg", settings.imgCacheHidden).equalsIgnoreCase(settings.imgCache)) {
				dir = new File(settings.getStorageSpecific(false)[0]);
				dirNew = new File(settings.getStorageSpecific(true)[0]);
				if (dir.exists() == true) {
					base.deleteDirectory(dirNew);
					dir.renameTo(dirNew);
				} else {
					base.deleteDirectory(dirNew);
				}

				edit.putString("directoryimg", settings.imgCacheHidden);
				settings.directoryImg = settings.imgCacheHidden;
			} else {
				dir = new File(settings.getStorageSpecific(true)[0]);
				dirNew = new File(settings.getStorageSpecific(false)[0]);
				if (dir.exists() == true) {
					base.deleteDirectory(dirNew);
					dir.renameTo(dirNew);
				} else {
					base.deleteDirectory(dirNew);
				}

				edit.putString("directoryimg", settings.imgCache);
				settings.directoryImg = settings.imgCache;
			}
			edit.commit();

			CheckBox imgButton = (CheckBox) findViewById(R.id.directoryimg);
			if (prefs.getString("directoryimg", settings.imgCacheHidden).equalsIgnoreCase(settings.imgCache)) {
				imgButton.setChecked(false);
			} else {
				imgButton.setChecked(true);
			}

			return;
		}
	}

	private class cgeoChangeBrowser implements View.OnClickListener {

		public void onClick(View arg0) {
			SharedPreferences.Editor edit = prefs.edit();
			if (prefs.getInt("asbrowser", 1) == 0) {
				edit.putInt("asbrowser", 1);
				settings.asBrowser = 1;
			} else {
				edit.putInt("asbrowser", 0);
				settings.asBrowser = 0;
			}
			edit.commit();

			CheckBox browserButton = (CheckBox) findViewById(R.id.browser);
			if (prefs.getInt("asbrowser", 1) == 0) {
				browserButton.setChecked(false);
			} else {
				browserButton.setChecked(true);
			}

			return;
		}
	}

	private class logIn implements View.OnClickListener {

		public void onClick(View arg0) {
			final String username = ((EditText) findViewById(R.id.username)).getText().toString();
			final String password = ((EditText) findViewById(R.id.password)).getText().toString();

			if (username == null || username.length() == 0 || password == null || password.length() == 0) {
				warning.showToast(res.getString(R.string.err_missing_auth));
				return;
			}

			loginDialog = ProgressDialog.show(activity, res.getString(R.string.init_login_popup), res.getString(R.string.init_login_popup_working), true);
			loginDialog.setCancelable(false);

			settings.setLogin(username, password);
			settings.deleteCookies();

			(new Thread() {

				@Override
				public void run() {
					logInHandler.sendEmptyMessage(base.login());
				}
			}).start();
		}
	}

	public void goHome(View view) {
		base.goHome(activity);
	}

	public void goManual(View view) {
		try {
			AppManualReaderClient.openManual(
				"c-geo",
				"c:geo-configuration",
				activity,
				"http://cgeo.carnero.cc/manual/"
			);
		} catch (Exception e) {
			// nothing
		}
	}
}
