package carnero.cgeo;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import android.net.Uri;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.EditText;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Button;
import javax.net.ssl.HttpsURLConnection;
import org.apache.http.HttpVersion;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

public class cgeoauth extends Activity {
	private cgeoapplication app = null;
	private Context activity = null;
	private cgSettings settings = null;
	private cgBase base = null;
	private cgWarning warning = null;
	private SharedPreferences prefs = null;
	private String OAtoken = null;
	private String OAtokenSecret = null;
	private final Pattern paramsPattern1 = Pattern.compile("oauth_token=([a-zA-Z0-9\\-\\_\\.]+)");
	private final Pattern paramsPattern2 = Pattern.compile("oauth_token_secret=([a-zA-Z0-9\\-\\_\\.]+)");
	private Button startButton = null;
	private EditText pinEntry = null;
	private Button pinEntryButton = null;

	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// init
		activity = this;
        app = (cgeoapplication)this.getApplication();
		app.setAction("setting up");
        prefs = getSharedPreferences(cgSettings.preferences, 0);
        settings = new cgSettings(this, prefs);
        base = new cgBase(app, settings, prefs);
        warning = new cgWarning(this);

		// set layout
		setTitle("twitter");
		if (settings.skin == 1) setContentView(R.layout.auth_light);
		else setContentView(R.layout.auth_dark);

		init();
   }

   private void init() {
		startButton = (Button)findViewById(R.id.start);
		pinEntry = (EditText)findViewById(R.id.pin);
		pinEntryButton = (Button)findViewById(R.id.pin_button);

		SharedPreferences prefs = getSharedPreferences(cgSettings.preferences, 0);
		OAtoken = prefs.getString("temp-token-public", null);
		OAtokenSecret = prefs.getString("temp-token-secret", null);

		startButton.setClickable(true);
		startButton.setOnTouchListener(new cgViewTouch(settings, startButton));
		startButton.setOnClickListener(new startListener());

		if (OAtoken == null || OAtoken.length() == 0 || OAtokenSecret == null || OAtokenSecret.length() == 0) {
			// start authorization process
			startButton.setText("start");
		} else {
			// already have temporary tokens, continue from pin
			startButton.setText("start again");

			pinEntry.setVisibility(View.VISIBLE);
			pinEntryButton.setVisibility(View.VISIBLE);
			pinEntryButton.setOnTouchListener(new cgViewTouch(settings, pinEntryButton));
			pinEntryButton.setOnClickListener(new confirmPINListener());
		}
   }

	public DefaultHttpClient getClient() {
        //sets up parameters
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "utf-8");
        params.setBooleanParameter("http.protocol.expect-continue", false);

        //registers schemes for both http and https
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        final SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
        sslSocketFactory.setHostnameVerifier(SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        registry.register(new Scheme("https", sslSocketFactory, 443));

        ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(params, registry);
        return new DefaultHttpClient(manager, params);
    }

	private void requestToken() {
		String host = "api.twitter.com";
		String path = "/oauth/request_token";
		String method = "GET";
		String params = null;
		URL u = null;
		URLConnection uc = null;
		HttpsURLConnection connection = null;
		InputStream in = null;
		BufferedReader br = null;
		StringBuilder sb = null;
		String line = null;

		// get temporary tokens
		try {
			params = cgOAuth.signOAuth(host, path, method, true, new HashMap<String, String>(), null, null);

			// base.trustAllHosts();
			u = new URL("https://" + host + path + "?" + params);
			uc = u.openConnection();
			connection = (HttpsURLConnection)uc;
			// connection.setHostnameVerifier(base.doNotVerify);
			connection.setReadTimeout(30000);
			connection.setRequestMethod(method);
			connection.setFollowRedirects(true);
			connection.setDoInput(true);
			connection.setDoOutput(false);

			in = connection.getInputStream();
			br = new BufferedReader(new InputStreamReader(in));
			sb = new StringBuilder();

            while ((line = br.readLine()) != null) {
                sb.append(line);
				sb.append("\n");
            }
			in.close();
			
			Log.i(cgSettings.tag, host + ": " + connection.getResponseCode() + " " + connection.getResponseMessage());
			connection.disconnect();

			line = sb.toString();

			if (line == null || line.length() == 0) {
				warning.showToast("Failed to initialize authorization process");

				return;
			}

			Matcher paramsMatcher1  = paramsPattern1.matcher(line);
			if (paramsMatcher1.find() == true) OAtoken = paramsMatcher1.group(1).toString();
			Matcher paramsMatcher2 = paramsPattern2.matcher(line);
			if (paramsMatcher2.find() == true) OAtokenSecret = paramsMatcher2.group(1).toString();

			if (OAtoken.length() == 0 || OAtokenSecret.length() == 0) {
				OAtoken = "";
				OAtokenSecret = "";

				startButton.setClickable(true);
				startButton.setOnTouchListener(new cgViewTouch(settings, startButton));
				startButton.setOnClickListener(new startListener());

				return;
			}

			// save temporary tokens
			SharedPreferences.Editor prefs = getSharedPreferences(cgSettings.preferences, 0).edit();
			prefs.putString("temp-token-public", OAtoken);
			prefs.putString("temp-token-secret", OAtokenSecret);
			prefs.commit();
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgeoauth.requestToken(1): " + e.toString());
		}

		// open browser with auth confirmation and wait for user
		try {
			startButton.setText("start again");

			path = "/oauth/authorize";
			HashMap<String, String> paramsPre = new HashMap<String, String>();
			paramsPre.put("oauth_callback", "oob");

			params = cgOAuth.signOAuth(host, path, "GET", true, paramsPre, OAtoken, OAtokenSecret);

			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + host + path + "?" + params)));

			pinEntry.setVisibility(View.VISIBLE);
			pinEntryButton.setVisibility(View.VISIBLE);
			pinEntryButton.setOnTouchListener(new cgViewTouch(settings, pinEntryButton));
			pinEntryButton.setOnClickListener(new confirmPINListener());
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgeoauth.requestToken(2): " + e.toString());
		}
	}

	private boolean changeToken() {
		String host = "api.twitter.com";
		String path = "/oauth/access_token";
		String method = "POST";
		String params = null;
		URL u = null;
		URLConnection uc = null;
		HttpsURLConnection connection = null;
		InputStream in = null;
		InputStreamReader ins = null;
		OutputStream out = null;
		OutputStreamWriter wr = null;
		BufferedReader br = null;
		StringBuilder sb = null;
		String line = null;

		try {
			HashMap<String, String> paramsPre = new HashMap<String, String>();
			paramsPre.put("oauth_verifier", ((EditText)pinEntry).getText().toString());

			params = cgOAuth.signOAuth(host, path, method, true, paramsPre, OAtoken, OAtokenSecret);

			// base.trustAllHosts();
			u = new URL("https://" + host + path);
			uc = u.openConnection();
			connection = (HttpsURLConnection)uc;
			// connection.setHostnameVerifier(base.doNotVerify);
			connection.setReadTimeout(30000);
			connection.setRequestMethod(method);
			connection.setFollowRedirects(true);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			out = connection.getOutputStream();
			wr = new OutputStreamWriter(out);
			wr.write(params);
			wr.flush();
			wr.close();

			Log.i(cgSettings.tag, host + ": " + connection.getResponseCode() + " " + connection.getResponseMessage());

			in = connection.getInputStream();
			ins = new InputStreamReader(in);
			br = new BufferedReader(ins);
			sb = new StringBuilder();

			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}

			Log.i(cgSettings.tag, host + ": " + connection.getResponseCode() + " " + connection.getResponseMessage());

			ins.close();
			in.close();
			connection.disconnect();

			line = sb.toString();

			OAtoken = "";
			OAtokenSecret = "";

			Matcher paramsMatcher1  = paramsPattern1.matcher(line);
			if (paramsMatcher1.find() == true) {
				OAtoken = paramsMatcher1.group(1).toString();
			}
			Matcher paramsMatcher2 = paramsPattern2.matcher(line);
			if (paramsMatcher2.find() == true) {
				OAtokenSecret = paramsMatcher2.group(1).toString();
			}

			if (OAtoken.length() == 0 || OAtokenSecret.length() == 0) {
				OAtoken = "";
				OAtokenSecret = "";

				warning.showToast("Auhorization process failed.");

				pinEntry.setVisibility(View.GONE);
				pinEntryButton.setVisibility(View.GONE);
				startButton.setText("start");

				SharedPreferences.Editor prefs = getSharedPreferences(cgSettings.preferences, 0).edit();
				prefs.putString("tokenpublic", null);
				prefs.putString("tokensecret", null);
				prefs.putInt("twitter", 0);
				prefs.commit();

				return false;
			} else {
				SharedPreferences.Editor prefs = getSharedPreferences(cgSettings.preferences, 0).edit();
				prefs.remove("temp-token-public");
				prefs.remove("temp-token-secret");
				prefs.putString("tokenpublic", OAtoken);
				prefs.putString("tokensecret", OAtokenSecret);
				prefs.putInt("twitter", 1);
				prefs.commit();

				warning.showToast("c:geo is now authorized.");

				finish();
				return true;
			}
		} catch (Exception e) {
			warning.showToast("Auhorization process failed.");
			
			Log.e(cgSettings.tag, "cgeoauth.changeToken: " + e.toString());
		}

		return false;
	}

	private class startListener implements View.OnClickListener {
		public void onClick(View arg0) {
			SharedPreferences.Editor prefs = getSharedPreferences(cgSettings.preferences, 0).edit();
			prefs.putString("temp-token-public", null);
			prefs.putString("temp-token-secret", null);
			prefs.commit();

			startButton.setText("start again");

			requestToken();
		}
	}

	private class confirmPINListener implements View.OnClickListener {
		public void onClick(View arg0) {
			if (((EditText)findViewById(R.id.pin)).getText().toString().length() == 0) {
				warning.helpDialog("pin", "Please write PIN code provided by Twitter website. It's mandatory to complete authorization.");
				return;
			}

			boolean status = changeToken();
			if (status == true) {
				pinEntryButton.setVisibility(View.GONE);
				pinEntryButton.setClickable(false);
				pinEntryButton.setOnTouchListener(null);
				pinEntryButton.setOnTouchListener(null);
			}
		}
	}
}