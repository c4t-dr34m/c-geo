package carnero.cgeo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Calendar;
import java.util.Locale;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import android.util.Log;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spannable;
import android.text.style.StrikethroughSpan;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.json.JSONArray;
import org.json.JSONObject;

public class cgBase {

	public static HashMap<String, String> cacheTypes = new HashMap<String, String>();
	public static HashMap<String, String> cacheTypesInv = new HashMap<String, String>();
	public static HashMap<String, String> cacheIDs = new HashMap<String, String>();
	public static HashMap<String, String> cacheIDsChoices = new HashMap<String, String>();
	public static HashMap<String, String> waypointTypes = new HashMap<String, String>();
	public static HashMap<String, Integer> logTypes = new HashMap<String, Integer>();
	public static HashMap<String, Integer> logTypes0 = new HashMap<String, Integer>();
	public static HashMap<Integer, String> logTypes1 = new HashMap<Integer, String>();
	public static HashMap<Integer, String> logTypes2 = new HashMap<Integer, String>();
	public static HashMap<Integer, String> logTypesTrackable = new HashMap<Integer, String>();
	public static HashMap<Integer, String> logTypesTrackableAction = new HashMap<Integer, String>();
	public static HashMap<Integer, String> errorRetrieve = new HashMap<Integer, String>();
	public static SimpleDateFormat dateIn = new SimpleDateFormat("MM/dd/yyyy");
	public static SimpleDateFormat dateEvIn = new SimpleDateFormat("dd MMMMM yyyy", Locale.ENGLISH); // 28 March 2009
	public static SimpleDateFormat dateTbIn1 = new SimpleDateFormat("EEEEE, dd MMMMM yyyy", Locale.ENGLISH); // Saturday, 28 March 2009
	public static SimpleDateFormat dateTbIn2 = new SimpleDateFormat("EEEEE, MMMMM dd, yyyy", Locale.ENGLISH); // Saturday, March 28, 2009
	public static SimpleDateFormat dateSqlIn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 2010-07-25 14:44:01
	public static SimpleDateFormat dateGPXIn = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // 2010-04-20T07:00:00Z
	public static DateFormat dateOut = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
	public static DateFormat timeOut = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());
	public static DateFormat dateOutShort = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
	private Resources res = null;
	private HashMap<String, String> cookies = new HashMap<String, String>();
	private final String passMatch = "[/\\?&]*[Pp]ass(word)?=[^&^#^$]+";
	private final Pattern patternLoggedIn = Pattern.compile("<span class=\"Success\">You are logged in as[^<]*<strong[^>]*>([^<]+)</strong>[^<]*</span>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	private final Pattern patternLogged2In = Pattern.compile("<strong>[^\\w]*Hello,[^<]*<a[^>]+>([^<]+)</a>[^<]*</strong>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	private final Pattern patternViewstate = Pattern.compile("id=\"__VIEWSTATE\"[^(value)]+value=\"([^\"]+)\"[^>]+>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	private final Pattern patternViewstate1 = Pattern.compile("id=\"__VIEWSTATE1\"[^(value)]+value=\"([^\"]+)\"[^>]+>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	public static final double kmInMiles = 1 / 1.609344;
	public static final double deg2rad = Math.PI / 180;
	public static final double rad2deg = 180 / Math.PI;
	public static final float erad = 6371.0f;
	public static final int mapAppAny = 0;
	public static final int mapAppLocus = 1;
	public static final int mapAppRmaps = 2;
	private cgeoapplication app = null;
	private cgSettings settings = null;
	private SharedPreferences prefs = null;
	public String version = null;
	private String idBrowser = "Mozilla/5.0 (X11; U; Linux i686; en-US) AppleWebKit/533.4 (KHTML, like Gecko) Chrome/5.0.375.86 Safari/533.4";
	private Context context = null;
	final private static HashMap<String, Integer> gcIcons = new HashMap<String, Integer>();
	final private static HashMap<String, Integer> wpIcons = new HashMap<String, Integer>();

	public static final int LOG_FOUND_IT = 2;
	public static final int LOG_DIDNT_FIND_IT = 3;
	public static final int LOG_NOTE = 4;
	public static final int LOG_PUBLISH_LISTING = 1003; // unknown ID; used number doesn't match any GC.com's ID
	public static final int LOG_ENABLE_LISTING = 23;
	public static final int LOG_ARCHIVE = 5;
	public static final int LOG_TEMP_DISABLE_LISTING = 22;
	public static final int LOG_NEEDS_ARCHIVE = 7;
	public static final int LOG_WILL_ATTEND = 9;
	public static final int LOG_ATTENDED = 10;
	public static final int LOG_RETRIEVED_IT = 13;
	public static final int LOG_PLACED_IT = 14;
	public static final int LOG_GRABBED_IT = 19;
	public static final int LOG_NEEDS_MAINTENANCE = 45;
	public static final int LOG_OWNER_MAINTENANCE = 46;
	public static final int LOG_UPDATE_COORDINATES = 47;
	public static final int LOG_DISCOVERED_IT = 48;
	public static final int LOG_POST_REVIEWER_NOTE = 18;
	public static final int LOG_VISIT = 1001; // unknown ID; used number doesn't match any GC.com's ID
	public static final int LOG_WEBCAM_PHOTO_TAKEN = 11;
	public static final int LOG_ANNOUNCEMENT = 74;

	public cgBase(cgeoapplication appIn, cgSettings settingsIn, SharedPreferences prefsIn) {
		context = appIn.getBaseContext();
		res = appIn.getBaseContext().getResources();

		// cache types
		cacheTypes.put("traditional cache", "traditional");
		cacheTypes.put("multi-cache", "multi");
		cacheTypes.put("unknown cache", "mystery");
		cacheTypes.put("letterbox hybrid", "letterbox");
		cacheTypes.put("event cache", "event");
		cacheTypes.put("mega-event cache", "mega");
		cacheTypes.put("earthcache", "earth");
		cacheTypes.put("cache in trash out event", "cito");
		cacheTypes.put("webcam cache", "webcam");
		cacheTypes.put("virtual cache", "virtual");
		cacheTypes.put("wherigo cache", "wherigo");
		cacheTypes.put("lost & found", "lostfound");
		cacheTypes.put("project ape cache", "ape");
		cacheTypes.put("groundspeak hq", "gchq");
		cacheTypes.put("gps cache exhibit", "gps");

		// cache types inverted
		cacheTypesInv.put("traditional", res.getString(R.string.traditional));
		cacheTypesInv.put("multi", res.getString(R.string.multi));
		cacheTypesInv.put("mystery", res.getString(R.string.mystery));
		cacheTypesInv.put("letterbox", res.getString(R.string.letterbox));
		cacheTypesInv.put("event", res.getString(R.string.event));
		cacheTypesInv.put("mega", res.getString(R.string.mega));
		cacheTypesInv.put("earth", res.getString(R.string.earth));
		cacheTypesInv.put("cito", res.getString(R.string.cito));
		cacheTypesInv.put("webcam", res.getString(R.string.webcam));
		cacheTypesInv.put("virtual", res.getString(R.string.virtual));
		cacheTypesInv.put("wherigo", res.getString(R.string.wherigo));
		cacheTypesInv.put("lostfound", res.getString(R.string.lostfound));
		cacheTypesInv.put("ape", res.getString(R.string.ape));
		cacheTypesInv.put("gchq", res.getString(R.string.gchq));
		cacheTypesInv.put("gps", res.getString(R.string.gps));

		// cache ids
		cacheIDs.put("all", "9a79e6ce-3344-409c-bbe9-496530baf758"); // hard-coded also in cgSettings
		cacheIDs.put("traditional", "32bc9333-5e52-4957-b0f6-5a2c8fc7b257");
		cacheIDs.put("multi", "a5f6d0ad-d2f2-4011-8c14-940a9ebf3c74");
		cacheIDs.put("mystery", "40861821-1835-4e11-b666-8d41064d03fe");
		cacheIDs.put("letterbox", "4bdd8fb2-d7bc-453f-a9c5-968563b15d24");
		cacheIDs.put("event", "69eb8534-b718-4b35-ae3c-a856a55b0874");
		cacheIDs.put("mega-event", "69eb8535-b718-4b35-ae3c-a856a55b0874");
		cacheIDs.put("earth", "c66f5cf3-9523-4549-b8dd-759cd2f18db8");
		cacheIDs.put("cito", "57150806-bc1a-42d6-9cf0-538d171a2d22");
		cacheIDs.put("webcam", "31d2ae3c-c358-4b5f-8dcd-2185bf472d3d");
		cacheIDs.put("virtual", "294d4360-ac86-4c83-84dd-8113ef678d7e");
		cacheIDs.put("wherigo", "0544fa55-772d-4e5c-96a9-36a51ebcf5c9");
		cacheIDs.put("lostfound", "3ea6533d-bb52-42fe-b2d2-79a3424d4728");
		cacheIDs.put("ape", "2555690d-b2bc-4b55-b5ac-0cb704c0b768");
		cacheIDs.put("gchq", "416f2494-dc17-4b6a-9bab-1a29dd292d8c");
		cacheIDs.put("gps", "72e69af2-7986-4990-afd9-bc16cbbb4ce3");

		// cache choices
		cacheIDsChoices.put(res.getString(R.string.all), cacheIDs.get("all"));
		cacheIDsChoices.put(res.getString(R.string.traditional), cacheIDs.get("traditional"));
		cacheIDsChoices.put(res.getString(R.string.multi), cacheIDs.get("multi"));
		cacheIDsChoices.put(res.getString(R.string.mystery), cacheIDs.get("mystery"));
		cacheIDsChoices.put(res.getString(R.string.letterbox), cacheIDs.get("letterbox"));
		cacheIDsChoices.put(res.getString(R.string.event), cacheIDs.get("event"));
		cacheIDsChoices.put(res.getString(R.string.mega), cacheIDs.get("mega"));
		cacheIDsChoices.put(res.getString(R.string.earth), cacheIDs.get("earth"));
		cacheIDsChoices.put(res.getString(R.string.cito), cacheIDs.get("cito"));
		cacheIDsChoices.put(res.getString(R.string.webcam), cacheIDs.get("webcam"));
		cacheIDsChoices.put(res.getString(R.string.virtual), cacheIDs.get("virtual"));
		cacheIDsChoices.put(res.getString(R.string.wherigo), cacheIDs.get("whereigo"));
		cacheIDsChoices.put(res.getString(R.string.lostfound), cacheIDs.get("lostfound"));
		cacheIDsChoices.put(res.getString(R.string.ape), cacheIDs.get("ape"));
		cacheIDsChoices.put(res.getString(R.string.gchq), cacheIDs.get("gchq"));
		cacheIDsChoices.put(res.getString(R.string.gps), cacheIDs.get("gps"));

		// waypoint types
		waypointTypes.put("flag", res.getString(R.string.wp_final));
		waypointTypes.put("stage", res.getString(R.string.wp_stage));
		waypointTypes.put("puzzle", res.getString(R.string.wp_puzzle));
		waypointTypes.put("pkg", res.getString(R.string.wp_pkg));
		waypointTypes.put("trailhead", res.getString(R.string.wp_trailhead));
		waypointTypes.put("waypoint", res.getString(R.string.wp_waypoint));

		// log types
		logTypes.put("icon_smile", LOG_FOUND_IT);
		logTypes.put("icon_sad", LOG_DIDNT_FIND_IT);
		logTypes.put("icon_note", LOG_NOTE);
		logTypes.put("icon_greenlight", LOG_PUBLISH_LISTING);
		logTypes.put("icon_enabled", LOG_ENABLE_LISTING);
		logTypes.put("traffic_cone", LOG_ARCHIVE);
		logTypes.put("icon_disabled", LOG_TEMP_DISABLE_LISTING);
		logTypes.put("icon_remove", LOG_NEEDS_ARCHIVE);
		logTypes.put("icon_rsvp", LOG_WILL_ATTEND);
		logTypes.put("icon_attended", LOG_ATTENDED);
		logTypes.put("picked_up", LOG_RETRIEVED_IT);
		logTypes.put("dropped_off", LOG_PLACED_IT);
		logTypes.put("transfer", LOG_GRABBED_IT);
		logTypes.put("icon_needsmaint", LOG_NEEDS_MAINTENANCE);
		logTypes.put("icon_maint", LOG_OWNER_MAINTENANCE);
		logTypes.put("coord_update", LOG_UPDATE_COORDINATES);
		logTypes.put("icon_discovered", LOG_DISCOVERED_IT);
		logTypes.put("big_smile", LOG_POST_REVIEWER_NOTE);
		logTypes.put("icon_visited", LOG_VISIT); // unknown ID; used number doesn't match any GC.com's ID
		logTypes.put("icon_camera", LOG_WEBCAM_PHOTO_TAKEN); // unknown ID; used number doesn't match any GC.com's ID
		logTypes.put("icon_announcement", LOG_ANNOUNCEMENT); // unknown ID; used number doesn't match any GC.com's ID

		logTypes0.put("found it", LOG_FOUND_IT);
		logTypes0.put("didn't find it", LOG_DIDNT_FIND_IT);
		logTypes0.put("write note", LOG_NOTE);
		logTypes0.put("publish listing", LOG_PUBLISH_LISTING);
		logTypes0.put("enable listing", LOG_ENABLE_LISTING);
		logTypes0.put("archive", LOG_ARCHIVE);
		logTypes0.put("temporarily disable listing", LOG_TEMP_DISABLE_LISTING);
		logTypes0.put("needs archived", LOG_NEEDS_ARCHIVE);
		logTypes0.put("will attend", LOG_WILL_ATTEND);
		logTypes0.put("attended", LOG_ATTENDED);
		logTypes0.put("retrieved it", LOG_RETRIEVED_IT);
		logTypes0.put("placed it", LOG_PLACED_IT);
		logTypes0.put("grabbed it", LOG_GRABBED_IT);
		logTypes0.put("needs maintenance", LOG_NEEDS_MAINTENANCE);
		logTypes0.put("owner maintenance", LOG_OWNER_MAINTENANCE);
		logTypes0.put("update coordinates", LOG_UPDATE_COORDINATES);
		logTypes0.put("discovered it", LOG_DISCOVERED_IT);
		logTypes0.put("post reviewer note", LOG_POST_REVIEWER_NOTE);
		logTypes0.put("visit", LOG_VISIT); // unknown ID; used number doesn't match any GC.com's ID
		logTypes0.put("webcam photo taken", LOG_WEBCAM_PHOTO_TAKEN); // unknown ID; used number doesn't match any GC.com's ID
		logTypes0.put("announcement", LOG_ANNOUNCEMENT); // unknown ID; used number doesn't match any GC.com's ID

		logTypes1.put(LOG_FOUND_IT, res.getString(R.string.log_found));
		logTypes1.put(LOG_DIDNT_FIND_IT, res.getString(R.string.log_dnf));
		logTypes1.put(LOG_NOTE, res.getString(R.string.log_note));
		logTypes1.put(LOG_PUBLISH_LISTING, res.getString(R.string.log_published));
		logTypes1.put(LOG_ENABLE_LISTING, res.getString(R.string.log_enabled));
		logTypes1.put(LOG_ARCHIVE, res.getString(R.string.log_archived));
		logTypes1.put(LOG_TEMP_DISABLE_LISTING, res.getString(R.string.log_disabled));
		logTypes1.put(LOG_NEEDS_ARCHIVE, res.getString(R.string.log_needs_archived));
		logTypes1.put(LOG_WILL_ATTEND, res.getString(R.string.log_attend));
		logTypes1.put(LOG_ATTENDED, res.getString(R.string.log_attended));
		logTypes1.put(LOG_RETRIEVED_IT, res.getString(R.string.log_retrieved));
		logTypes1.put(LOG_PLACED_IT, res.getString(R.string.log_placed));
		logTypes1.put(LOG_GRABBED_IT, res.getString(R.string.log_grabbed));
		logTypes1.put(LOG_NEEDS_MAINTENANCE, res.getString(R.string.log_maintenance_needed));
		logTypes1.put(LOG_OWNER_MAINTENANCE, res.getString(R.string.log_maintained));
		logTypes1.put(LOG_UPDATE_COORDINATES, res.getString(R.string.log_update));
		logTypes1.put(LOG_DISCOVERED_IT, res.getString(R.string.log_discovered));
		logTypes1.put(LOG_POST_REVIEWER_NOTE, res.getString(R.string.log_reviewed));
		logTypes1.put(LOG_VISIT, res.getString(R.string.log_taken));
		logTypes1.put(LOG_WEBCAM_PHOTO_TAKEN, res.getString(R.string.log_webcam));
		logTypes1.put(LOG_ANNOUNCEMENT, res.getString(R.string.log_announcement));

		logTypes2.put(LOG_FOUND_IT, res.getString(R.string.log_found)); // traditional, multi, unknown, earth, wherigo, virtual, letterbox
		logTypes2.put(LOG_DIDNT_FIND_IT, res.getString(R.string.log_dnf)); // traditional, multi, unknown, earth, wherigo, virtual, letterbox, webcam
		logTypes2.put(LOG_NOTE, res.getString(R.string.log_note)); // traditional, multi, unknown, earth, wherigo, virtual, event, letterbox, webcam, trackable
		logTypes2.put(LOG_PUBLISH_LISTING, res.getString(R.string.log_published)); // X
		logTypes2.put(LOG_ENABLE_LISTING, res.getString(R.string.log_enabled)); // owner
		logTypes2.put(LOG_ARCHIVE, res.getString(R.string.log_archived)); // traditional, multi, unknown, earth, event, wherigo, virtual, letterbox, webcam
		logTypes2.put(LOG_TEMP_DISABLE_LISTING, res.getString(R.string.log_disabled)); // owner
		logTypes2.put(LOG_NEEDS_ARCHIVE, res.getString(R.string.log_needs_archived)); // traditional, multi, unknown, earth, event, wherigo, virtual, letterbox, webcam
		logTypes2.put(LOG_WILL_ATTEND, res.getString(R.string.log_attend)); // event
		logTypes2.put(LOG_ATTENDED, res.getString(R.string.log_attended)); // event
		logTypes2.put(LOG_WEBCAM_PHOTO_TAKEN, res.getString(R.string.log_webcam)); // webcam
		logTypes2.put(LOG_RETRIEVED_IT, res.getString(R.string.log_retrieved)); //trackable
		logTypes2.put(LOG_GRABBED_IT, res.getString(R.string.log_grabbed)); //trackable
		logTypes2.put(LOG_NEEDS_MAINTENANCE, res.getString(R.string.log_maintenance_needed)); // traditional, unknown, multi, wherigo, virtual, letterbox, webcam
		logTypes2.put(LOG_OWNER_MAINTENANCE, res.getString(R.string.log_maintained)); // owner
		logTypes2.put(LOG_DISCOVERED_IT, res.getString(R.string.log_discovered)); //trackable
		logTypes2.put(LOG_POST_REVIEWER_NOTE, res.getString(R.string.log_reviewed)); // X
		logTypes2.put(LOG_ANNOUNCEMENT, res.getString(R.string.log_announcement)); // X

		// trackables for logs
		logTypesTrackable.put(0, res.getString(R.string.log_tb_nothing)); // do nothing
		logTypesTrackable.put(1, res.getString(R.string.log_tb_visit)); // visit cache
		logTypesTrackable.put(2, res.getString(R.string.log_tb_drop)); // drop here
		logTypesTrackableAction.put(0, ""); // do nothing
		logTypesTrackableAction.put(1, "_Visited"); // visit cache
		logTypesTrackableAction.put(2, "_DroppedOff"); // drop here

		// retrieving errors (because of ____ )
		errorRetrieve.put(1, res.getString(R.string.err_none));
		errorRetrieve.put(0, res.getString(R.string.err_start));
		errorRetrieve.put(-1, res.getString(R.string.err_parse));
		errorRetrieve.put(-2, res.getString(R.string.err_server));
		errorRetrieve.put(-3, res.getString(R.string.err_login));
		errorRetrieve.put(-4, res.getString(R.string.err_unknown));
		errorRetrieve.put(-5, res.getString(R.string.err_comm));
		errorRetrieve.put(-6, res.getString(R.string.err_wrong));
		errorRetrieve.put(-7, res.getString(R.string.err_license));

		// init
		app = appIn;
		settings = settingsIn;
		prefs = prefsIn;

		try {
			PackageManager manager = app.getPackageManager();
			PackageInfo info = manager.getPackageInfo(app.getPackageName(), 0);
			version =  info.versionName;
		} catch (Exception e) {
			// nothing
		}

		if (settings.asBrowser == 1) {
			final long rndBrowser = Math.round(Math.random() * 6);
			if (rndBrowser == 0) {
				idBrowser = "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.1 (KHTML, like Gecko) Chrome/5.0.322.2 Safari/533.1";
			} else if (rndBrowser == 1) {
				idBrowser = "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; MDDC)";
			} else if (rndBrowser == 2) {
				idBrowser = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.3) Gecko/20100401 Firefox/3.6.3";
			} else if (rndBrowser == 3) {
				idBrowser = "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_2; en-us) AppleWebKit/531.21.8 (KHTML, like Gecko) Version/4.0.4 Safari/531.21.10";
			} else if (rndBrowser == 4) {
				idBrowser = "Mozilla/5.0 (iPod; U; CPU iPhone OS 2_2_1 like Mac OS X; en-us) AppleWebKit/525.18.1 (KHTML, like Gecko) Version/3.1.1 Mobile/5H11a Safari/525.20";
			} else if (rndBrowser == 5) {
				idBrowser = "Mozilla/5.0 (Linux; U; Android 1.1; en-gb; dream) AppleWebKit/525.10+ (KHTML, like Gecko) Version/3.0.4 Mobile Safari/523.12.2";
			} else if (rndBrowser == 6) {
				idBrowser = "Mozilla/5.0 (X11; U; Linux i686; en-US) AppleWebKit/533.4 (KHTML, like Gecko) Chrome/5.0.375.86 Safari/533.4";
			} else {
				idBrowser = "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_2; en-US) AppleWebKit/532.9 (KHTML, like Gecko) Chrome/5.0.307.11 Safari/532.9";
			}
		}
	}

	public String findViewstate(String page, int index) {
		String viewstate = null;

		if (index == 0) {
			final Matcher matcherViewstate = patternViewstate.matcher(page);
			while (matcherViewstate.find()) {
				if (matcherViewstate.groupCount() > 0) {
					viewstate = matcherViewstate.group(1);
				}
			}
		} else if (index == 1) {
			final Matcher matcherViewstate = patternViewstate1.matcher(page);
			while (matcherViewstate.find()) {
				if (matcherViewstate.groupCount() > 0) {
					viewstate = matcherViewstate.group(1);
				}
			}
		}

		return viewstate;
	}

	public class loginThread extends Thread {

		@Override
		public void run() {
			login();
		}
	}

	public int login() {
		final String host = "www.geocaching.com";
		final String path = "/login/default.aspx";
		cgResponse loginResponse = null;
		String loginData = null;

		String viewstate = null;
		String viewstate1 = null;

		final HashMap<String, String> loginStart = settings.getLogin();

		if (loginStart == null) {
			return -3; // no login information stored
		}

		loginResponse = request(true, host, path, "GET", new HashMap<String, String>(), false, false, false);
		loginData = loginResponse.getData();
		if (loginData != null && loginData.length() > 0) {
			if (checkLogin(loginData) == true) {
				Log.i(cgSettings.tag, "Already logged in Geocaching.com as " + loginStart.get("username"));

				switchToEnglish(viewstate, viewstate1);

				return 1; // logged in
			}

			viewstate = findViewstate(loginData, 0);
			viewstate1 = findViewstate(loginData, 1);

			if (viewstate == null || viewstate.length() == 0) {
				Log.e(cgSettings.tag, "cgeoBase.login: Failed to find viewstate");
				return -1; // no viewstate
			}
		} else {
			Log.e(cgSettings.tag, "cgeoBase.login: Failed to retrieve login page (1st)");
			return -2; // no loginpage
		}

		final HashMap<String, String> login = settings.getLogin();
		final HashMap<String, String> params = new HashMap<String, String>();

		if (login == null || login.get("username") == null || login.get("username").length() == 0 || login.get("password") == null || login.get("password").length() == 0) {
			Log.e(cgSettings.tag, "cgeoBase.login: No login information stored");
			return -3;
		}

		settings.deleteCookies();

		params.put("__EVENTTARGET", "");
		params.put("__EVENTARGUMENT", "");
		params.put("__VIEWSTATE", viewstate);
		if (viewstate1 != null) {
			params.put("__VIEWSTATE1", viewstate1);
			params.put("__VIEWSTATEFIELDCOUNT", "2");
		}
		params.put("ctl00$SiteContent$tbUsername", login.get("username"));
		params.put("ctl00$SiteContent$tbPassword", login.get("password"));
		params.put("ctl00$SiteContent$cbRememberMe", "on");
		params.put("ctl00$SiteContent$btnSignIn", "Login");

		loginResponse = request(true, host, path, "POST", params, false, false, false);
		loginData = loginResponse.getData();

		if (loginData != null && loginData.length() > 0) {
			if (checkLogin(loginData) == true) {
				Log.i(cgSettings.tag, "Successfully logged in Geocaching.com as " + login.get("username"));

				switchToEnglish(findViewstate(loginData, 0), findViewstate(loginData, 1));

				return 1; // logged in
			} else {
				if (loginData.indexOf("Your username/password combination does not match.") != -1) {
					Log.i(cgSettings.tag, "Failed to log in Geocaching.com as " + login.get("username") + " because of wrong username/password");

					return -6; // wrong login
				} else {
					Log.i(cgSettings.tag, "Failed to log in Geocaching.com as " + login.get("username") + " for some unknown reason");

					return -4; // can't login
				}
			}
		} else {
			Log.e(cgSettings.tag, "cgeoBase.login: Failed to retrieve login page (2nd)");

			return -5; // no login page
		}
	}

	public Boolean checkLogin(String page) {
		if (page == null || page.length() == 0) {
			Log.e(cgSettings.tag, "cgeoBase.checkLogin: No page given");
			return false;
		}

		// on every page
		final Matcher matcherLogged2In = patternLogged2In.matcher(page);
		while (matcherLogged2In.find()) {
			return true;
		}

		// after login
		final Matcher matcherLoggedIn = patternLoggedIn.matcher(page);
		while (matcherLoggedIn.find()) {
			return true;
		}

		return false;
	}

	public String switchToEnglish(String viewstate, String viewstate1) {
		final String host = "www.geocaching.com";
		final String path = "/default.aspx";
		final HashMap<String, String> params = new HashMap<String, String>();

		if (viewstate != null) {
			params.put("__VIEWSTATE", viewstate);
		}
		if (viewstate1 != null) {
			params.put("__VIEWSTATE1", viewstate1);
			params.put("__VIEWSTATEFIELDCOUNT", "2");
		}
		params.put("__EVENTTARGET", "ctl00$uxLocaleList$uxLocaleList$ctl00$uxLocaleItem"); // switch to english
		params.put("__EVENTARGUMENT", "");

		return request(false, host, path, "POST", params, false, false, false).getData();
	}

	public cgCacheWrap parseSearch(cgSearchThread thread, String url, String page, boolean showCaptcha) {
		if (page == null || page.length() == 0) {
			Log.e(cgSettings.tag, "cgeoBase.parseSearch: No page given");
			return null;
		}

		final cgCacheWrap caches = new cgCacheWrap();
		final ArrayList<String> cids = new ArrayList<String>();
		final ArrayList<String> guids = new ArrayList<String>();
		String recaptchaChallenge = null;
		String recaptchaText = null;

		caches.url = url;

		final Pattern patternCacheType = Pattern.compile("<td>[^<]*<a href=\"[^\"]*/seek/cache_details\\.aspx\\?guid=[^\"]+\"[^>]+>[^<]*<img src=\"[^\"]*/images/wpttypes/[^\\.]+\\.gif\" alt=\"([^\"]+)\" title=\"[^\"]+\"[^>]*>[^<]*</a>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		final Pattern patternGuidAndDisabled = Pattern.compile("<img src=\"[^\"]*/images/wpttypes/[^>]*>[^<]*</a>[^<]*<a href=\"[^\"]*/seek/cache_details\\.aspx\\?guid=([a-z0-9\\-]+)\" class=\"lnk([^\"]*)\">([^<]*<span>)?([^<]*)(</span>[^<]*)?</a>[^<]+<br />([^<]*)<span[^>]+>([^<]*)</span>([^<]*<img[^>]+>)?[^<]*<br />[^<]*</td>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		final Pattern patternTbs = Pattern.compile("<a id=\"ctl00_ContentBody_dlResults_ctl[0-9]+_uxTravelBugList\" class=\"tblist\" data-tbcount=\"([0-9]+)\" data-id=\"[^\"]*\"[^>]*>(.*)</a>", Pattern.CASE_INSENSITIVE);
		final Pattern patternTbsInside = Pattern.compile("(<img src=\"[^\"]+\" alt=\"([^\"]+)\" title=\"[^\"]*\" />[^<]*)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		final Pattern patternDirection = Pattern.compile("<img id=\"ctl00_ContentBody_dlResults_ctl[0-9]+_uxDistanceAndHeading\" title=\"[^\"]*\" src=\"[^\"]*/seek/CacheDir\\.ashx\\?k=([^\"]+)\"[^>]*>", Pattern.CASE_INSENSITIVE);
		final Pattern patternCode = Pattern.compile("\\|[^\\w]*(GC[a-z0-9]+)[^\\|]*\\|", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		final Pattern patternId = Pattern.compile("name=\"CID\"[^v]*value=\"([0-9]+)\"", Pattern.CASE_INSENSITIVE);
		final Pattern patternFavourite = Pattern.compile("<span id=\"ctl00_ContentBody_dlResults_ctl[0-9]+_uxFavoritesValue\" title=\"[^\"]*\" class=\"favorite-rank\">([0-9]+)</span>", Pattern.CASE_INSENSITIVE);
		final Pattern patternTotalCnt = Pattern.compile("<td class=\"PageBuilderWidget\"><span>Total Records[^<]*<b>(\\d+)<\\/b>", Pattern.CASE_INSENSITIVE);
		final Pattern patternRecaptcha = Pattern.compile("<script[^>]*src=\"[^\"]*/recaptcha/api/challenge\\?k=([^\"]+)\"[^>]*>", Pattern.CASE_INSENSITIVE);
		final Pattern patternRecaptchaChallenge = Pattern.compile("challenge : '([^']+)'", Pattern.CASE_INSENSITIVE);

		caches.viewstate = findViewstate(page, 0);
		caches.viewstate1 = findViewstate(page, 1);

		// recaptcha
		if (showCaptcha == true) {
			try {
				String recaptchaJsParam = null;
				final Matcher matcherRecaptcha = patternRecaptcha.matcher(page);
				while (matcherRecaptcha.find()) {
					if (matcherRecaptcha.groupCount() > 0) {
						recaptchaJsParam = matcherRecaptcha.group(1);
					}
				}

				if (recaptchaJsParam != null) {
					final String recaptchaJs = request(false, "www.google.com", "/recaptcha/api/challenge", "GET", "k=" + urlencode_rfc3986(recaptchaJsParam.trim()), 0, true).getData();

					if (recaptchaJs != null && recaptchaJs.length() > 0) {
						final Matcher matcherRecaptchaChallenge = patternRecaptchaChallenge.matcher(recaptchaJs);
						while (matcherRecaptchaChallenge.find()) {
							if (matcherRecaptchaChallenge.groupCount() > 0) {
								recaptchaChallenge = matcherRecaptchaChallenge.group(1).trim();
							}
						}
					}
				}
			} catch (Exception e) {
				// failed to parse recaptcha challenge
				Log.w(cgSettings.tag, "cgeoBase.parseSearch: Failed to parse recaptcha challenge");
			}

			if (thread != null && recaptchaChallenge != null && recaptchaChallenge.length() > 0) {
				thread.setChallenge(recaptchaChallenge);
				thread.notifyNeed();
			}
		}

		int startPos = -1;
		int endPos = -1;

		startPos = page.indexOf("<div id=\"ctl00_ContentBody_ResultsPanel\"");
		if (startPos == -1) {
			Log.e(cgSettings.tag, "cgeoBase.parseSearch: ID \"ctl00_ContentBody_dlResults\" not found on page");
			return null;
		}

		page = page.substring(startPos); // cut on <table

		startPos = page.indexOf(">");
		endPos = page.indexOf("ctl00_ContentBody_UnitTxt");
		if (startPos == -1 || endPos == -1) {
			Log.e(cgSettings.tag, "cgeoBase.parseSearch: ID \"ctl00_ContentBody_UnitTxt\" not found on page");
			return null;
		}

		page = page.substring(startPos + 1, endPos - startPos + 1); // cut between <table> and </table>

		final String[] rows = page.split("<tr class=");
		final int rows_count = rows.length;

		for (int z = 1; z < rows_count; z++) {
			cgCache cache = new cgCache();
			String row = rows[z];

			// check for cache type presence
			if (row.indexOf("images/wpttypes") == -1) {
				continue;
			}

			try {
				final Matcher matcherGuidAndDisabled = patternGuidAndDisabled.matcher(row);

				while (matcherGuidAndDisabled.find()) {
					if (matcherGuidAndDisabled.groupCount() > 0) {
						guids.add(matcherGuidAndDisabled.group(1));

						cache.guid = matcherGuidAndDisabled.group(1);
						if (matcherGuidAndDisabled.group(4) != null) {
							cache.name = Html.fromHtml(matcherGuidAndDisabled.group(4).trim()).toString();
						}
						if (matcherGuidAndDisabled.group(6) != null) {
							cache.location = Html.fromHtml(matcherGuidAndDisabled.group(6).trim()).toString();
						}

						final String attr = matcherGuidAndDisabled.group(2);
						if (attr != null) {
							if (attr.contains("Strike") == true) {
								cache.disabled = true;
							} else {
								cache.disabled = false;
							}

							if (attr.contains("OldWarning") == true) {
								cache.archived = true;
							} else {
								cache.archived = false;
							}
						}
					}
				}
			} catch (Exception e) {
				// failed to parse GUID and/or Disabled
				Log.w(cgSettings.tag, "cgeoBase.parseSearch: Failed to parse GUID and/or Disabled data");
			}

			if (settings.excludeDisabled == 1 && (cache.disabled == true || cache.archived == true)) {
				// skip disabled and archived caches
				cache = null;
				continue;
			}

			String inventoryPre = null;

			// GC* code
			try {
				final Matcher matcherCode = patternCode.matcher(row);
				while (matcherCode.find()) {
					if (matcherCode.groupCount() > 0) {
						cache.geocode = matcherCode.group(1).toUpperCase();
					}
				}
			} catch (Exception e) {
				// failed to parse code
				Log.w(cgSettings.tag, "cgeoBase.parseSearch: Failed to parse cache code");
			}

			// cache type
			try {
				final Matcher matcherCacheType = patternCacheType.matcher(row);
				while (matcherCacheType.find()) {
					if (matcherCacheType.groupCount() > 0) {
						cache.type = cacheTypes.get(matcherCacheType.group(1).toLowerCase());
					}
				}
			} catch (Exception e) {
				// failed to parse type
				Log.w(cgSettings.tag, "cgeoBase.parseSearch: Failed to parse cache type");
			}

			// cache direction - image
			try {
				final Matcher matcherDirection = patternDirection.matcher(row);
				while (matcherDirection.find()) {
					if (matcherDirection.groupCount() > 0) {
						cache.directionImg = matcherDirection.group(1);
					}
				}
			} catch (Exception e) {
				// failed to parse direction image
				Log.w(cgSettings.tag, "cgeoBase.parseSearch: Failed to parse cache direction image");
			}

			// cache inventory
			try {
				final Matcher matcherTbs = patternTbs.matcher(row);
				while (matcherTbs.find()) {
					if (matcherTbs.groupCount() > 0) {
						cache.inventoryItems = Integer.parseInt(matcherTbs.group(1));
						inventoryPre = matcherTbs.group(2);
					}
				}
			} catch (Exception e) {
				// failed to parse inventory
				Log.w(cgSettings.tag, "cgeoBase.parseSearch: Failed to parse cache inventory (1)");
			}

			if (inventoryPre != null && inventoryPre.trim().length() > 0) {
				try {
					final Matcher matcherTbsInside = patternTbsInside.matcher(inventoryPre);
					while (matcherTbsInside.find()) {
						if (matcherTbsInside.groupCount() == 2 && matcherTbsInside.group(2) != null) {
							final String inventoryItem = matcherTbsInside.group(2).toLowerCase();
							if (inventoryItem.equals("premium member only cache")) {
								continue;
							} else {
								if (cache.inventoryItems <= 0) {
									cache.inventoryItems = 1;
								}
							}
						}
					}
				} catch (Exception e) {
					// failed to parse cache inventory info
					Log.w(cgSettings.tag, "cgeoBase.parseSearch: Failed to parse cache inventory info");
				}
			}

			// premium cache
			if (row.indexOf("/images/small_profile.gif") != -1) {
				cache.members = true;
			} else {
				cache.members = false;
			}

			// found it
			if (row.indexOf("/images/icons/icon_smile.gif") != -1) {
				cache.found = true;
			} else {
				cache.found = false;
			}

			// own it
			if (row.indexOf("/images/silk/star.png") != -1) {
				cache.own = true;
			} else {
				cache.own = false;
			}

			// id
			try {
				final Matcher matcherId = patternId.matcher(row);
				while (matcherId.find()) {
					if (matcherId.groupCount() > 0) {
						cache.cacheid = matcherId.group(1);
						cids.add(cache.cacheid);
					}
				}
			} catch (Exception e) {
				// failed to parse cache id
				Log.w(cgSettings.tag, "cgeoBase.parseSearch: Failed to parse cache id");
			}

			// favourite count
			try {
				final Matcher matcherFavourite = patternFavourite.matcher(row);
				while (matcherFavourite.find()) {
					if (matcherFavourite.groupCount() > 0) {
						cache.favouriteCnt = Integer.parseInt(matcherFavourite.group(1));
					}
				}
			} catch (Exception e) {
				// failed to parse favourite count
				Log.w(cgSettings.tag, "cgeoBase.parseSearch: Failed to parse favourite count");
			}

			if (cache.nameSp == null) {
				cache.nameSp = (new Spannable.Factory()).newSpannable(cache.name);
				if (cache.disabled == true || cache.archived == true) { // strike
					cache.nameSp.setSpan(new StrikethroughSpan(), 0, cache.nameSp.toString().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}

			caches.cacheList.add(cache);
		}

		// total caches found
		try {
			final Matcher matcherTotalCnt = patternTotalCnt.matcher(page);
			while (matcherTotalCnt.find()) {
				if (matcherTotalCnt.groupCount() > 0) {
					if (matcherTotalCnt.group(1) != null) {
						caches.totalCnt = new Integer(matcherTotalCnt.group(1));
					}
				}
			}
		} catch (Exception e) {
			// failed to parse cache count
			Log.w(cgSettings.tag, "cgeoBase.parseSearch: Failed to parse cache count");
		}

		if (thread != null && recaptchaChallenge != null) {
			if (thread.getText() == null) {
				thread.waitForUser();
			}

			recaptchaText = thread.getText();
		}

		if (cids.size() > 0 && (recaptchaChallenge == null || (recaptchaChallenge != null && recaptchaText != null && recaptchaText.length() > 0))) {
			Log.i(cgSettings.tag, "Trying to get .loc for " + cids.size() + " caches");

			try {
				// get coordinates for parsed caches
				final String host = "www.geocaching.com";
				final String path = "/seek/nearest.aspx";
				final StringBuilder params = new StringBuilder();
				params.append("__EVENTTARGET=");
				params.append("&");
				params.append("__EVENTARGUMENT=");
				params.append("&");
				params.append("__VIEWSTATE=");
				params.append(urlencode_rfc3986(caches.viewstate));
				if (caches.viewstate1 != null) {
					params.append("&");
					params.append("__VIEWSTATE1=");
					params.append(urlencode_rfc3986(caches.viewstate1));
					params.append("&");
					params.append("__VIEWSTATEFIELDCOUNT=2");
				}

				for (String cid : cids) {
					params.append("&");
					params.append("CID=");
					params.append(urlencode_rfc3986(cid));
				}

				if (recaptchaChallenge != null && recaptchaText != null && recaptchaText.length() > 0) {
					params.append("&");
					params.append("recaptcha_challenge_field=");
					params.append(urlencode_rfc3986(recaptchaChallenge));
					params.append("&");
					params.append("recaptcha_response_field=");
					params.append(urlencode_rfc3986(recaptchaText));
				}
				params.append("&");
				params.append("ctl00%24ContentBody%24uxDownloadLoc=Download+Waypoints");

				final String coordinates = request(false, host, path, "POST", params.toString(), 0, true).getData();

				if (coordinates != null && coordinates.length() > 0) {
					if (coordinates.indexOf("You have not agreed to the license agreement. The license agreement is required before you can start downloading GPX or LOC files from Geocaching.com") > -1) {
						Log.i(cgSettings.tag, "User has not agreed to the license agreement. Can\'t download .loc file.");

						caches.error = errorRetrieve.get(-7);

						return caches;
					}
				}

				if (coordinates != null && coordinates.length() > 0) {
					final HashMap<String, cgCoord> cidCoords = new HashMap<String, cgCoord>();
					final Pattern patternCidCode = Pattern.compile("name id=\"([^\"]+)\"");
					final Pattern patternCidLat = Pattern.compile("lat=\"([^\"]+)\"");
					final Pattern patternCidLon = Pattern.compile("lon=\"([^\"]+)\"");
					// premium only >>
					final Pattern patternCidDif = Pattern.compile("<difficulty>([^<]+)</difficulty>");
					final Pattern patternCidTer = Pattern.compile("<terrain>([^<]+)</terrain>");
					final Pattern patternCidCon = Pattern.compile("<container>([^<]+)</container>");
					// >> premium only

					final String[] points = coordinates.split("<waypoint>");

					// parse coordinates
					for (String point : points) {
						final cgCoord pointCoord = new cgCoord();
						final Matcher matcherCidCode = patternCidCode.matcher(point);
						final Matcher matcherLatCode = patternCidLat.matcher(point);
						final Matcher matcherLonCode = patternCidLon.matcher(point);
						final Matcher matcherDifCode = patternCidDif.matcher(point);
						final Matcher matcherTerCode = patternCidTer.matcher(point);
						final Matcher matcherConCode = patternCidCon.matcher(point);
						HashMap<String, Object> tmp = null;

						if (matcherCidCode.find() == true) {
							pointCoord.name = matcherCidCode.group(1).trim().toUpperCase();
						}
						if (matcherLatCode.find() == true) {
							tmp = parseCoordinate(matcherLatCode.group(1), "lat");
							pointCoord.latitude = (Double) tmp.get("coordinate");
						}
						if (matcherLonCode.find() == true) {
							tmp = parseCoordinate(matcherLonCode.group(1), "lon");
							pointCoord.longitude = (Double) tmp.get("coordinate");
						}
						if (matcherDifCode.find() == true) {
							pointCoord.difficulty = new Float(matcherDifCode.group(1));
						}
						if (matcherTerCode.find() == true) {
							pointCoord.terrain = new Float(matcherTerCode.group(1));
						}
						if (matcherConCode.find() == true) {
							final int size = Integer.parseInt(matcherConCode.group(1));

							if (size == 1) {
								pointCoord.size = "not chosen";
							} else if (size == 2) {
								pointCoord.size = "micro";
							} else if (size == 3) {
								pointCoord.size = "regular";
							} else if (size == 4) {
								pointCoord.size = "large";
							} else if (size == 5) {
								pointCoord.size = "virtual";
							} else if (size == 6) {
								pointCoord.size = "other";
							} else if (size == 8) {
								pointCoord.size = "small";
							} else {
								pointCoord.size = "unknown";
							}
						}

						cidCoords.put(pointCoord.name, pointCoord);
					}

					Log.i(cgSettings.tag, "Coordinates found in .loc file: " + cidCoords.size());

					// save found cache coordinates
					for (cgCache oneCache : caches.cacheList) {
						if (cidCoords.containsKey(oneCache.geocode) == true) {
							cgCoord thisCoords = cidCoords.get(oneCache.geocode);

							oneCache.latitude = thisCoords.latitude;
							oneCache.longitude = thisCoords.longitude;
							oneCache.difficulty = thisCoords.difficulty;
							oneCache.terrain = thisCoords.terrain;
							oneCache.size = thisCoords.size;
						}
					}
				}
			} catch (Exception e) {
				Log.e(cgSettings.tag, "cgBase.parseSearch.CIDs: " + e.toString());
			}
		}

		// get direction images
		cgDirectionImg dirImgDownloader = new cgDirectionImg(settings);
		for (cgCache oneCache : caches.cacheList) {
			if (oneCache.latitude == null && oneCache.longitude == null && oneCache.direction == null && oneCache.directionImg != null) {
				dirImgDownloader.getDrawable(oneCache.geocode, oneCache.directionImg);
			}
		}
		dirImgDownloader = null;

		// get ratings
		if (guids.size() > 0) {
			Log.i(cgSettings.tag, "Trying to get ratings for " + cids.size() + " caches");

			try {
				final HashMap<String, cgRating> ratings = getRating(guids, null);

				if (ratings != null) {
					// save found cache coordinates
					for (cgCache oneCache : caches.cacheList) {
						if (ratings.containsKey(oneCache.guid) == true) {
							cgRating thisRating = ratings.get(oneCache.guid);

							oneCache.rating = thisRating.rating;
							oneCache.votes = thisRating.votes;
							oneCache.myVote = thisRating.myVote;
						}
					}
				}
			} catch (Exception e) {
				Log.e(cgSettings.tag, "cgBase.parseSearch.GCvote: " + e.toString());
			}
		}

		return caches;
	}

	public cgCacheWrap parseMapJSON(String url, String data) {
		if (data == null || data.length() == 0) {
			Log.e(cgSettings.tag, "cgeoBase.parseMapJSON: No page given");
			return null;
		}

		final cgCacheWrap caches = new cgCacheWrap();
		caches.url = url;

		try {
			final JSONObject yoDawg = new JSONObject(data);
			final String json = yoDawg.getString("d");

			if (json == null || json.length() == 0) {
				Log.e(cgSettings.tag, "cgeoBase.parseMapJSON: No JSON inside JSON");
				return null;
			}

			final JSONObject dataJSON = new JSONObject(json);
			final JSONObject extra = dataJSON.getJSONObject("cs");
			if (extra != null && extra.length() > 0) {
				int count = extra.getInt("count");

				if (count > 0 && extra.has("cc")) {
					final JSONArray cachesData = extra.getJSONArray("cc");
					if (cachesData != null && cachesData.length() > 0) {
						JSONObject oneCache = null;
						for (int i = 0; i < count; i++) {
							oneCache = cachesData.getJSONObject(i);
							if (oneCache == null) {
								break;
							}

							final cgCache cacheToAdd = new cgCache();
							cacheToAdd.geocode = oneCache.getString("gc");
							cacheToAdd.latitude = oneCache.getDouble("lat");
							cacheToAdd.longitude = oneCache.getDouble("lon");
							cacheToAdd.name = oneCache.getString("nn");
							cacheToAdd.found = oneCache.getBoolean("f");
							cacheToAdd.own = oneCache.getBoolean("o");
							cacheToAdd.disabled = !oneCache.getBoolean("ia");
							int ctid = oneCache.getInt("ctid");
							if (ctid == 2) {
								cacheToAdd.type = "traditional";
							} else if (ctid == 3) {
								cacheToAdd.type = "multi";
							} else if (ctid == 4) {
								cacheToAdd.type = "virtual";
							} else if (ctid == 5) {
								cacheToAdd.type = "letterbox";
							} else if (ctid == 6) {
								cacheToAdd.type = "event";
							} else if (ctid == 8) {
								cacheToAdd.type = "mystery";
							} else if (ctid == 11) {
								cacheToAdd.type = "webcam";
							} else if (ctid == 13) {
								cacheToAdd.type = "cito";
							} else if (ctid == 137) {
								cacheToAdd.type = "earth";
							} else if (ctid == 453) {
								cacheToAdd.type = "mega";
							} else if (ctid == 1858) {
								cacheToAdd.type = "wherigo";
							} else if (ctid == 3653) {
								cacheToAdd.type = "lost";
							}

							caches.cacheList.add(cacheToAdd);
						}
					}
				} else {
					Log.w(cgSettings.tag, "There are no caches in viewport");
				}
				caches.totalCnt = caches.cacheList.size();
			}
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgBase.parseMapJSON: " + e.toString());
		}

		return caches;
	}

	public cgCacheWrap parseCache(String page, int reason) {
		if (page == null || page.length() == 0) {
			Log.e(cgSettings.tag, "cgeoBase.parseCache: No page given");
			return null;
		}

		final Pattern patternGeocode = Pattern.compile("<meta name=\"og:url\" content=\"[^\"]+/(GC[0-9A-Z]+)\"[^>]*>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		final Pattern patternCacheId = Pattern.compile("/seek/log\\.aspx\\?ID=(\\d+)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		final Pattern patternCacheGuid = Pattern.compile("<link rel=\"alternate\" href=\"[^\"]*/datastore/rss_galleryimages\\.ashx\\?guid=([0-9a-z\\-]+)\"[^>]*>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		final Pattern patternType = Pattern.compile("<img src=\"[^\"]*/WptTypes/\\d+\\.gif\" alt=\"([^\"]+)\" (title=\"[^\"]*\" )?width=\"32\" height=\"32\"[^>]*>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

		final Pattern patternName = Pattern.compile("<h2[^>]*>[^<]*<span id=\"ctl00_ContentBody_CacheName\">([^<]+)<\\/span>[^<]*<\\/h2>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		final Pattern patternSize = Pattern.compile("<div class=\"CacheSize[^\"]*\">[^<]*<p[^>]*>[^S]*Size[^:]*:[^<]*<span[^>]*>[^<]*<img src=\"[^\"]*/icons/container/[a-z_]+\\.gif\" alt=\"Size: ([^\"]+)\"[^>]*>[^<]*<small>[^<]*</small>[^<]*</span>[^<]*</p>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		final Pattern patternDifficulty = Pattern.compile("<span id=\"ctl00_ContentBody_uxLegendScale\"[^>]*>[^<]*<img src=\"[^\"]*/images/stars/stars([0-9_]+)\\.gif\" alt=\"[^\"]+\"[^>]*>[^<]*</span>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		final Pattern patternTerrain = Pattern.compile("<span id=\"ctl00_ContentBody_Localize6\"[^>]*>[^<]*<img src=\"[^\"]*/images/stars/stars([0-9_]+)\\.gif\" alt=\"[^\"]+\"[^>]*>[^<]*</span>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		final Pattern patternOwner = Pattern.compile("<span class=\"minorCacheDetails\">[^\\w]*An?([^\\w]*Event)?[^\\w]*cache[^\\w]*by[^<]*<a href=\"[^\"]+\">([^<]+)</a>[^<]*</span>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		final Pattern patternOwnerReal = Pattern.compile("<a id=\"ctl00_ContentBody_uxFindLinksHiddenByThisUser\" href=\"[^\"]*/seek/nearest\\.aspx\\?u=*([^\"]+)\">[^<]+</a>", Pattern.CASE_INSENSITIVE);
		final Pattern patternHidden = Pattern.compile("<span[^>]*>[^\\w]*Hidden[^:]*:[^\\d]*((\\d+)\\/(\\d+)\\/(\\d+))[^<]*</span>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		final Pattern patternHiddenEvent = Pattern.compile("<span[^>]*>[^\\w]*Event[^\\w]*Date[^:]*:[^\\w]*[a-zA-Z]+,[^\\d]*((\\d+)[^\\w]*(\\w+)[^\\d]*(\\d+))[^<]*</span>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		final Pattern patternFavourite = Pattern.compile("<a id=\"uxFavContainerLink\"[^>]*>[^<]*<div[^<]*<span class=\"favorite-value\">[^\\d]*([0-9]+)[^\\d^<]*</span>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

		final Pattern patternFound = Pattern.compile("<p>[^<]*<a id=\"ctl00_ContentBody_hlFoundItLog\"[^<]*<img src=\".*/images/stockholm/16x16/check\\.gif\"[^>]*>[^<]*</a>[^<]*</p>", Pattern.CASE_INSENSITIVE);
		final Pattern patternLatLon = Pattern.compile("<span id=\"ctl00_ContentBody_LatLon\"[^>]*>(<b>)?([^<]*)(<\\/b>)?<\\/span>", Pattern.CASE_INSENSITIVE);
		final Pattern patternLocation = Pattern.compile("<span id=\"ctl00_ContentBody_Location\"[^>]*>In ([^<]*)", Pattern.CASE_INSENSITIVE);
		final Pattern patternHint = Pattern.compile("<p>([^<]*<strong>)?[^\\w]*Additional Hints([^<]*<\\/strong>)?[^\\(]*\\(<a[^>]+>Encrypt</a>\\)[^<]*<\\/p>[^<]*<div id=\"div_hint\"[^>]*>(.*)</div>[^<]*<div id=[\\'|\"]dk[\\'|\"]", Pattern.CASE_INSENSITIVE);
		final Pattern patternDescShort = Pattern.compile("<div class=\"UserSuppliedContent\">[^<]*<span id=\"ctl00_ContentBody_ShortDescription\"[^>]*>((?:(?!</span>[^\\w^<]*</div>).)*)</span>[^<]*</div>", Pattern.CASE_INSENSITIVE);
		final Pattern patternDesc = Pattern.compile("<div class=\"UserSuppliedContent\">[^<]*<span id=\"ctl00_ContentBody_LongDescription\"[^>]*>((?:(?!</span>[^\\w^<]*</div>).)*)</span>[^<]*</div>[^<]*<p>[^<]*</p>[^<]*<p>[^<]*<strong>[^\\w]*Additional Hints</strong>", Pattern.CASE_INSENSITIVE);
		final Pattern patternCountLogs = Pattern.compile("<span id=\"ctl00_ContentBody_lblFindCounts\"><p>(.*)<\\/p><\\/span>", Pattern.CASE_INSENSITIVE);
		final Pattern patternCountLog = Pattern.compile(" src=\"\\/images\\/icons\\/([^\\.]*).gif\" alt=\"[^\"]*\" title=\"[^\"]*\" />([0-9]*)[^0-9]+", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		final Pattern patternLogs = Pattern.compile("<table class=\"LogsTable[^\"]*\"[^>]*>[^<]*<tr>(.*)</tr>[^<]*</table>[^<]*<p", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		final Pattern patternLog = Pattern.compile("<td[^>]*>[^<]*<strong>[^<]*<img src=\"[^\"]*/images/icons/([^\\.]+)\\.[a-z]{2,5}\"[^>]*>&nbsp;([a-zA-Z]+) (\\d+)(, (\\d+))? by <a href=[^>]+>([^<]+)</a>[<^]*</strong>([^\\(]*\\((\\d+) found\\))?(<br[^>]*>)+((?:(?!<small>).)*)(<br[^>]*>)+<small>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		final Pattern patternAttributes = Pattern.compile("<h3 class=\"WidgetHeader\">[^<]*<img[^>]+>[^\\w]*Attributes[^<]*</h3>[^<]*<div class=\"WidgetBody\">(([^<]*<img src=\"[^\"]+\" alt=\"[^\"]+\"[^>]*>)+)[^<]*<p", Pattern.CASE_INSENSITIVE);
		final Pattern patternAttributesInside = Pattern.compile("[^<]*<img src=\"([^\"]+)\" alt=\"([^\"]+)\"[^>]*>", Pattern.CASE_INSENSITIVE);
		final Pattern patternSpoilers = Pattern.compile("<span id=\"ctl00_ContentBody_Images\">((<a href=\"[^\"]+\"[^>]*>[^<]*<img[^>]+>[^<]*<span>[^>]+</span>[^<]*</a>[^<]*<br[^>]*>([^<]*(<br[^>]*>)+)?)+)[^<]*</span>", Pattern.CASE_INSENSITIVE);
		final Pattern patternSpoilersInside = Pattern.compile("[^<]*<a href=\"([^\"]+)\"[^>]*>[^<]*<img[^>]+>[^<]*<span>([^>]+)</span>[^<]*</a>[^<]*<br[^>]*>(([^<]*)(<br[^<]*>)+)?", Pattern.CASE_INSENSITIVE);
		final Pattern patternInventory = Pattern.compile("<span id=\"ctl00_ContentBody_uxTravelBugList_uxInventoryLabel\">[^\\w]*Inventory[^<]*</span>[^<]*</h3>[^<]*<div class=\"WidgetBody\">([^<]*<ul>(([^<]*<li>[^<]*<a href=\"[^\"]+\"[^>]*>[^<]*<img src=\"[^\"]+\"[^>]*>[^<]*<span>[^<]+<\\/span>[^<]*<\\/a>[^<]*<\\/li>)+)[^<]*<\\/ul>)?", Pattern.CASE_INSENSITIVE);
		final Pattern patternInventoryInside = Pattern.compile("[^<]*<li>[^<]*<a href=\"[a-z0-9\\-\\_\\.\\?\\/\\:\\@]*\\/track\\/details\\.aspx\\?guid=([0-9a-z\\-]+)[^\"]*\"[^>]*>[^<]*<img src=\"[^\"]+\"[^>]*>[^<]*<span>([^<]+)<\\/span>[^<]*<\\/a>[^<]*<\\/li>", Pattern.CASE_INSENSITIVE);

		final cgCacheWrap caches = new cgCacheWrap();
		final cgCache cache = new cgCache();

		if (page.indexOf("Cache is Unpublished") > -1) {
			caches.error = "cache was unpublished";
			return caches;
		}

		if (page.indexOf("Sorry, the owner of this listing has made it viewable to Premium Members only.") != -1) {
			caches.error = "requested cache is for premium members only";
			return caches;
		}

		if (page.indexOf("has chosen to make this cache listing visible to Premium Members only.") != -1) {
			caches.error = "requested cache is for premium members only";
			return caches;
		}

		if (page.indexOf("<li>This cache is temporarily unavailable.") != -1) {
			cache.disabled = true;
		} else {
			cache.disabled = false;
		}

		if (page.indexOf("<li>This cache has been archived,") != -1) {
			cache.archived = true;
		} else {
			cache.archived = false;
		}

		if (page.indexOf("<p class=\"Warning\">This is a Premium Member Only cache.</p>") != -1) {
			cache.members = true;
		} else {
			cache.members = false;
		}

		cache.reason = reason;

		// cache geocode
		try {
			final Matcher matcherGeocode = patternGeocode.matcher(page);
			while (matcherGeocode.find()) {
				if (matcherGeocode.groupCount() > 0) {
					cache.geocode = (String) matcherGeocode.group(1);
				}
			}
		} catch (Exception e) {
			// failed to parse cache geocode
			Log.w(cgSettings.tag, "cgeoBase.parseCache: Failed to parse cache geocode");
		}

		// cache id
		try {
			final Matcher matcherCacheId = patternCacheId.matcher(page);
			while (matcherCacheId.find()) {
				if (matcherCacheId.groupCount() > 0) {
					cache.cacheid = (String) matcherCacheId.group(1);
				}
			}
		} catch (Exception e) {
			// failed to parse cache id
			Log.w(cgSettings.tag, "cgeoBase.parseCache: Failed to parse cache id");
		}

		// cache guid
		try {
			final Matcher matcherCacheGuid = patternCacheGuid.matcher(page);
			while (matcherCacheGuid.find()) {
				if (matcherCacheGuid.groupCount() > 0) {
					cache.guid = (String) matcherCacheGuid.group(1);
				}
			}
		} catch (Exception e) {
			// failed to parse cache guid
			Log.w(cgSettings.tag, "cgeoBase.parseCache: Failed to parse cache guid");
		}

		// name
		try {
			final Matcher matcherName = patternName.matcher(page);
			while (matcherName.find()) {
				if (matcherName.groupCount() > 0) {
					cache.name = Html.fromHtml(matcherName.group(1)).toString();
				}
			}
		} catch (Exception e) {
			// failed to parse cache name
			Log.w(cgSettings.tag, "cgeoBase.parseCache: Failed to parse cache name");
		}

		// owner real name
		try {
			final Matcher matcherOwnerReal = patternOwnerReal.matcher(page);
			while (matcherOwnerReal.find()) {
				if (matcherOwnerReal.groupCount() > 0) {
					cache.ownerReal = URLDecoder.decode(matcherOwnerReal.group(1));
				}
			}
		} catch (Exception e) {
			// failed to parse owner real name
			Log.w(cgSettings.tag, "cgeoBase.parseCache: Failed to parse cache owner real name");
		}

		final String username = settings.getUsername();
		if (cache.ownerReal != null && username != null && cache.ownerReal.equalsIgnoreCase(username)) {
			cache.own = true;
		}

		int pos = -1;
		String tableInside = page;

		pos = tableInside.indexOf("id=\"cacheDetails\"");
		if (pos == -1) {
			Log.e(cgSettings.tag, "cgeoBase.parseCache: ID \"cacheDetails\" not found on page");
			return null;
		}

		tableInside = tableInside.substring(pos);

		pos = tableInside.indexOf("<div class=\"CacheInformationTable\"");
		if (pos == -1) {
			Log.e(cgSettings.tag, "cgeoBase.parseCache: ID \"CacheInformationTable\" not found on page");
			return null;
		}

		tableInside = tableInside.substring(0, pos);

		if (tableInside != null && tableInside.length() > 0) {
			// cache terrain
			try {
				final Matcher matcherTerrain = patternTerrain.matcher(tableInside);
				while (matcherTerrain.find()) {
					if (matcherTerrain.groupCount() > 0) {
						cache.terrain = new Float(Pattern.compile("_").matcher(matcherTerrain.group(1)).replaceAll("."));
					}
				}
			} catch (Exception e) {
				// failed to parse terrain
				Log.w(cgSettings.tag, "cgeoBase.parseCache: Failed to parse cache terrain");
			}

			// cache difficulty
			try {
				final Matcher matcherDifficulty = patternDifficulty.matcher(tableInside);
				while (matcherDifficulty.find()) {
					if (matcherDifficulty.groupCount() > 0) {
						cache.difficulty = new Float(Pattern.compile("_").matcher(matcherDifficulty.group(1)).replaceAll("."));
					}
				}
			} catch (Exception e) {
				// failed to parse difficulty
				Log.w(cgSettings.tag, "cgeoBase.parseCache: Failed to parse cache difficulty");
			}

			// owner
			try {
				final Matcher matcherOwner = patternOwner.matcher(tableInside);
				while (matcherOwner.find()) {
					if (matcherOwner.groupCount() > 0) {
						cache.owner = Html.fromHtml(matcherOwner.group(2)).toString();
					}
				}
			} catch (Exception e) {
				// failed to parse owner
				Log.w(cgSettings.tag, "cgeoBase.parseCache: Failed to parse cache owner");
			}

			// hidden
			try {
				final Matcher matcherHidden = patternHidden.matcher(tableInside);
				while (matcherHidden.find()) {
					if (matcherHidden.groupCount() > 0) {
						cache.hidden = dateIn.parse(matcherHidden.group(1));
					}
				}
			} catch (Exception e) {
				// failed to parse cache hidden date
				Log.w(cgSettings.tag, "cgeoBase.parseCache: Failed to parse cache hidden date");
			}

			if (cache.hidden == null) {
				// event date
				try {
					final Matcher matcherHiddenEvent = patternHiddenEvent.matcher(tableInside);
					while (matcherHiddenEvent.find()) {
						if (matcherHiddenEvent.groupCount() > 0) {
							cache.hidden = dateEvIn.parse(matcherHiddenEvent.group(1));
						}
					}
				} catch (Exception e) {
					// failed to parse cache event date
					Log.w(cgSettings.tag, "cgeoBase.parseCache: Failed to parse cache event date");
				}
			}

			// favourite
			try {
				final Matcher matcherFavourite = patternFavourite.matcher(tableInside);
				while (matcherFavourite.find()) {
					if (matcherFavourite.groupCount() > 0) {
						cache.favouriteCnt = Integer.parseInt(matcherFavourite.group(1));
					}
				}
			} catch (Exception e) {
				// failed to parse favourite count
				Log.w(cgSettings.tag, "cgeoBase.parseCache: Failed to parse favourite count");
			}

			// cache size
			try {
				final Matcher matcherSize = patternSize.matcher(tableInside);
				while (matcherSize.find()) {
					if (matcherSize.groupCount() > 0) {
						cache.size = matcherSize.group(1).toLowerCase();
					}
				}
			} catch (Exception e) {
				// failed to parse size
				Log.w(cgSettings.tag, "cgeoBase.parseCache: Failed to parse cache size");
			}
		}

		// cache found
		try {
			final Matcher matcherFound = patternFound.matcher(page);
			while (matcherFound.find()) {
				if (matcherFound.group() != null && matcherFound.group().length() > 0) {
					cache.found = true;
				}
			}
		} catch (Exception e) {
			// failed to parse found
			Log.w(cgSettings.tag, "cgeoBase.parseCache: Failed to parse found");
		}

		// cache type
		try {
			final Matcher matcherType = patternType.matcher(page);
			while (matcherType.find()) {
				if (matcherType.groupCount() > 0) {
					cache.type = cacheTypes.get(matcherType.group(1).toLowerCase());
				}
			}
		} catch (Exception e) {
			// failed to parse type
			Log.w(cgSettings.tag, "cgeoBase.parseCache: Failed to parse cache type");
		}

		// latitude and logitude
		try {
			final Matcher matcherLatLon = patternLatLon.matcher(page);
			while (matcherLatLon.find()) {
				if (matcherLatLon.groupCount() > 0) {
					cache.latlon = matcherLatLon.group(2); // first is <b>

					HashMap<String, Object> tmp = this.parseLatlon(cache.latlon);
					if (tmp.size() > 0) {
						cache.latitude = (Double) tmp.get("latitude");
						cache.longitude = (Double) tmp.get("longitude");
						cache.latitudeString = (String) tmp.get("latitudeString");
						cache.longitudeString = (String) tmp.get("longitudeString");
					}
					tmp = null;
				}
			}
		} catch (Exception e) {
			// failed to parse latitude and/or longitude
			Log.w(cgSettings.tag, "cgeoBase.parseCache: Failed to parse cache coordinates");
		}

		// cache location
		try {
			final Matcher matcherLocation = patternLocation.matcher(page);
			while (matcherLocation.find()) {
				if (matcherLocation.groupCount() > 0) {
					cache.location = matcherLocation.group(1);
				}
			}
		} catch (Exception e) {
			// failed to parse location
			Log.w(cgSettings.tag, "cgeoBase.parseCache: Failed to parse cache location");
		}

		// cache hint
		try {
			final Matcher matcherHint = patternHint.matcher(page);
			while (matcherHint.find()) {
				if (matcherHint.groupCount() > 2 && matcherHint.group(3) != null) {
					// replace linebreak and paragraph tags
					String hint = Pattern.compile("<(br|p)[^>]*>").matcher(matcherHint.group(3)).replaceAll("\n");
					if (hint != null) {
						cache.hint = hint.replaceAll(Pattern.quote("</p>"), "").trim();
					}
				}
			}
		} catch (Exception e) {
			// failed to parse hint
			Log.w(cgSettings.tag, "cgeoBase.parseCache: Failed to parse cache hint");
		}

		/*
		// short info debug
		Log.d(cgSettings.tag, "gc-code: " + cache.geocode);
		Log.d(cgSettings.tag, "id: " + cache.cacheid);
		Log.d(cgSettings.tag, "guid: " + cache.guid);
		Log.d(cgSettings.tag, "name: " + cache.name);
		Log.d(cgSettings.tag, "terrain: " + cache.terrain);
		Log.d(cgSettings.tag, "difficulty: " + cache.difficulty);
		Log.d(cgSettings.tag, "owner: " + cache.owner);
		Log.d(cgSettings.tag, "owner (real): " + cache.ownerReal);
		Log.d(cgSettings.tag, "hidden: " + dateOutShort.format(cache.hidden));
		Log.d(cgSettings.tag, "favorite: " + cache.favouriteCnt);
		Log.d(cgSettings.tag, "size: " + cache.size);
		if (cache.found) {
			Log.d(cgSettings.tag, "found!");
		} else {
			Log.d(cgSettings.tag, "not found");
		}
		Log.d(cgSettings.tag, "type: " + cache.type);
		Log.d(cgSettings.tag, "latitude: " + String.format("%.6f", cache.latitude));
		Log.d(cgSettings.tag, "longitude: " + String.format("%.6f", cache.longitude));
		Log.d(cgSettings.tag, "location: " + cache.location);
		Log.d(cgSettings.tag, "hint: " + cache.hint);
		 */

		// cache short description
		try {
			final Matcher matcherDescShort = patternDescShort.matcher(page);
			while (matcherDescShort.find()) {
				if (matcherDescShort.groupCount() > 0) {
					cache.shortdesc = matcherDescShort.group(1).trim();
				}
			}
		} catch (Exception e) {
			// failed to parse short description
			Log.w(cgSettings.tag, "cgeoBase.parseCache: Failed to parse cache short description");
		}

		// cache description
		try {
			final Matcher matcherDesc = patternDesc.matcher(page);
			while (matcherDesc.find()) {
				if (matcherDesc.groupCount() > 0) {
					cache.description = matcherDesc.group(1);
				}
			}
		} catch (Exception e) {
			// failed to parse short description
			Log.w(cgSettings.tag, "cgeoBase.parseCache: Failed to parse cache description");
		}

		// cache attributes
		try {
			final Matcher matcherAttributes = patternAttributes.matcher(page);
			while (matcherAttributes.find()) {
				if (matcherAttributes.groupCount() > 0) {
					final String attributesPre = matcherAttributes.group(1);
					final Matcher matcherAttributesInside = patternAttributesInside.matcher(attributesPre);

					while (matcherAttributesInside.find()) {
						if (matcherAttributesInside.groupCount() > 1 && matcherAttributesInside.group(2).equalsIgnoreCase("blank") != true) {
							if (cache.attributes == null) {
								cache.attributes = new ArrayList<String>();
							}
							// by default, use the tooltip of the attribute
							String attribute = matcherAttributesInside.group(2).toLowerCase();

							// now try to find a translation for the attribute
							String imageName = matcherAttributesInside.group(1).trim();
							if (imageName.length() > 0) {
								int start = imageName.lastIndexOf('/');
								int end = imageName.lastIndexOf('.');
								if (start >= 0 && end>= 0) {
									imageName = imageName.substring(start + 1, end).replace('-', '_');
								    int id = res.getIdentifier("attribute_" + imageName, "string", context.getPackageName());
								    if (id > 0) {
								    	String translated = res.getString(id);
								    	if (translated != null && translated.length() > 0) {
								    		attribute = translated;
								    	}
								    }
								}
							}
							cache.attributes.add(attribute );
						}
					}
				}
			}
		} catch (Exception e) {
			// failed to parse cache attributes
			Log.w(cgSettings.tag, "cgeoBase.parseCache: Failed to parse cache attributes");
		}

		// cache spoilers
		try {
			final Matcher matcherSpoilers = patternSpoilers.matcher(page);
			while (matcherSpoilers.find()) {
				if (matcherSpoilers.groupCount() > 0) {
					final String spoilersPre = matcherSpoilers.group(1);
					final Matcher matcherSpoilersInside = patternSpoilersInside.matcher(spoilersPre);

					while (matcherSpoilersInside.find()) {
						if (matcherSpoilersInside.groupCount() > 0) {
							final cgSpoiler spoiler = new cgSpoiler();
							spoiler.url = matcherSpoilersInside.group(1);

							if (matcherSpoilersInside.group(2) != null) {
								spoiler.title = matcherSpoilersInside.group(2);
							}
							if (matcherSpoilersInside.group(4) != null) {
								spoiler.description = matcherSpoilersInside.group(4);
							}

							if (cache.spoilers == null) {
								cache.spoilers = new ArrayList<cgSpoiler>();
							}
							cache.spoilers.add(spoiler);
						}
					}
				}
			}
		} catch (Exception e) {
			// failed to parse cache spoilers
			Log.w(cgSettings.tag, "cgeoBase.parseCache: Failed to parse cache spoilers");
		}

		// cache inventory
		try {
			cache.inventoryItems = 0;

			final Matcher matcherInventory = patternInventory.matcher(page);
			while (matcherInventory.find()) {
				if (cache.inventory == null) {
					cache.inventory = new ArrayList<cgTrackable>();
				}

				if (matcherInventory.groupCount() > 1) {
					final String inventoryPre = matcherInventory.group(2);

					if (inventoryPre != null && inventoryPre.length() > 0) {
						final Matcher matcherInventoryInside = patternInventoryInside.matcher(inventoryPre);

						while (matcherInventoryInside.find()) {
							if (matcherInventoryInside.groupCount() > 0) {
								final cgTrackable inventoryItem = new cgTrackable();
								inventoryItem.guid = matcherInventoryInside.group(1);
								inventoryItem.name = matcherInventoryInside.group(2);

								cache.inventory.add(inventoryItem);
								cache.inventoryItems++;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// failed to parse cache inventory
			Log.w(cgSettings.tag, "cgeoBase.parseCache: Failed to parse cache inventory (2)");
		}

		// cache logs counts
		try {
			final Matcher matcherLogCounts = patternCountLogs.matcher(page);
			while (matcherLogCounts.find()) {
				if (matcherLogCounts.groupCount() > 0) {
					final String[] logs = matcherLogCounts.group(1).split("<img");
					final int logsCnt = logs.length;

					for (int k = 1; k < logsCnt; k++) {
						Integer type = null;
						Integer count = null;
						final Matcher matcherLog = patternCountLog.matcher(logs[k]);

						if (matcherLog.find()) {
							String typeStr = matcherLog.group(1);
							String countStr = matcherLog.group(2);
							if (typeStr != null && typeStr.length() > 0) {
								if (logTypes.containsKey(typeStr.toLowerCase()) == true) {
									type = logTypes.get(typeStr.toLowerCase());
								}
							}
							if (countStr != null && countStr.length() > 0) {
								count = Integer.parseInt(countStr);
							}
							if (type != null && count != null) {
								cache.logCounts.put(type, count);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// failed to parse logs
			Log.w(cgSettings.tag, "cgeoBase.parseCache: Failed to parse cache log count");
		}

		// cache logs
		try {
			final Matcher matcherLogs = patternLogs.matcher(page);
			while (matcherLogs.find()) {
				if (matcherLogs.groupCount() > 0) {
					final String[] logs = matcherLogs.group(1).split("</tr><tr>");
					final int logsCnt = logs.length;

					for (int k = 0; k < logsCnt; k++) {
						final Matcher matcherLog = patternLog.matcher(logs[k]);

						if (matcherLog.find()) {
							final cgLog logDone = new cgLog();

							String logTmp = matcherLog.group(10);

							int day = -1;
							try {
								day = Integer.parseInt(matcherLog.group(3));
							} catch (Exception e) {
								Log.w(cgSettings.tag, "Failed to parse logs date (day): " + e.toString());
							}

							int month = -1;
							// January  | February  | March  | April  | May | June | July | August  | September | October  | November  | December
							if (matcherLog.group(2).equalsIgnoreCase("January")) {
								month = 0;
							} else if (matcherLog.group(2).equalsIgnoreCase("February")) {
								month = 1;
							} else if (matcherLog.group(2).equalsIgnoreCase("March")) {
								month = 2;
							} else if (matcherLog.group(2).equalsIgnoreCase("April")) {
								month = 3;
							} else if (matcherLog.group(2).equalsIgnoreCase("May")) {
								month = 4;
							} else if (matcherLog.group(2).equalsIgnoreCase("June")) {
								month = 5;
							} else if (matcherLog.group(2).equalsIgnoreCase("July")) {
								month = 6;
							} else if (matcherLog.group(2).equalsIgnoreCase("August")) {
								month = 7;
							} else if (matcherLog.group(2).equalsIgnoreCase("September")) {
								month = 8;
							} else if (matcherLog.group(2).equalsIgnoreCase("October")) {
								month = 9;
							} else if (matcherLog.group(2).equalsIgnoreCase("November")) {
								month = 10;
							} else if (matcherLog.group(2).equalsIgnoreCase("December")) {
								month = 11;
							} else {
								Log.w(cgSettings.tag, "Failed to parse logs date (month).");
							}


							int year = -1;
							final String yearPre = matcherLog.group(5);

							if (yearPre == null) {
								Calendar date = Calendar.getInstance();
								year = date.get(Calendar.YEAR);
							} else {
								try {
									year = Integer.parseInt(matcherLog.group(5));
								} catch (Exception e) {
									Log.w(cgSettings.tag, "Failed to parse logs date (year): " + e.toString());
								}
							}

							long logDate;
							if (year > 0 && month >= 0 && day > 0) {
								Calendar date = Calendar.getInstance();
								date.set(year, month, day, 12, 0, 0);
								logDate = date.getTimeInMillis();
								logDate = (long) (Math.ceil(logDate / 1000)) * 1000;
							} else {
								logDate = 0;
							}

							if (logTypes.containsKey(matcherLog.group(1).toLowerCase()) == true) {
								logDone.type = logTypes.get(matcherLog.group(1).toLowerCase());
							} else {
								logDone.type = logTypes.get("icon_note");
							}

							logDone.author = Html.fromHtml(matcherLog.group(6)).toString();
							logDone.date = logDate;
							if (matcherLog.group(8) != null) {
								logDone.found = new Integer(matcherLog.group(8));
							}
							logDone.log = logTmp;

							if (cache.logs == null) {
								cache.logs = new ArrayList<cgLog>();
							}
							cache.logs.add(logDone);
						}
					}
				}
			}
		} catch (Exception e) {
			// failed to parse logs
			Log.w(cgSettings.tag, "cgeoBase.parseCache: Failed to parse cache logs");
		}

		int wpBegin = 0;
		int wpEnd = 0;

		wpBegin = page.indexOf("<table class=\"Table\" id=\"ctl00_ContentBody_Waypoints\">");
		if (wpBegin != -1) { // parse waypoints
			final Pattern patternWpType = Pattern.compile("\\/wpttypes\\/sm\\/(.+)\\.jpg", Pattern.CASE_INSENSITIVE);
			final Pattern patternWpPrefixOrLookupOrLatlon = Pattern.compile(">([^<]*<[^>]+>)?([^<]+)(<[^>]+>[^<]*)?<\\/td>", Pattern.CASE_INSENSITIVE);
			final Pattern patternWpName = Pattern.compile(">[^<]*<a[^>]+>([^<]*)<\\/a>", Pattern.CASE_INSENSITIVE);
			final Pattern patternWpNote = Pattern.compile("colspan=\"6\">(.*)<\\/td>", Pattern.CASE_INSENSITIVE);

			String wpList = page.substring(wpBegin);

			wpEnd = wpList.indexOf("</p>");
			if (wpEnd > -1 && wpEnd <= wpList.length()) {
				wpList = wpList.substring(0, wpEnd);
			}

			if (wpList.indexOf("No additional waypoints to display.") == -1) {
				wpEnd = wpList.indexOf("</table>");
				wpList = wpList.substring(0, wpEnd);

				wpBegin = wpList.indexOf("<tbody>");
				wpEnd = wpList.indexOf("</tbody>");
				if (wpBegin >= 0 && wpEnd >= 0 && wpEnd <= wpList.length()) {
					wpList = wpList.substring(wpBegin + 7, wpEnd);
				}

				final String[] wpItems = wpList.split("<tr");

				String[] wp;
				for (int j = 1; j < wpItems.length; j++) {
					final cgWaypoint waypoint = new cgWaypoint();

					wp = wpItems[j].split("<td");

					// waypoint type
					try {
						final Matcher matcherWpType = patternWpType.matcher(wp[3]);
						while (matcherWpType.find()) {
							if (matcherWpType.groupCount() > 0) {
								waypoint.type = matcherWpType.group(1);
								if (waypoint.type != null) {
									waypoint.type = waypoint.type.trim();
								}
							}
						}
					} catch (Exception e) {
						// failed to parse type
						Log.w(cgSettings.tag, "cgeoBase.parseCache: Failed to parse waypoint type");
					}

					// waypoint prefix
					try {
						final Matcher matcherWpPrefix = patternWpPrefixOrLookupOrLatlon.matcher(wp[4]);
						while (matcherWpPrefix.find()) {
							if (matcherWpPrefix.groupCount() > 1) {
								waypoint.prefix = matcherWpPrefix.group(2);
								if (waypoint.prefix != null) {
									waypoint.prefix = waypoint.prefix.trim();
								}
							}
						}
					} catch (Exception e) {
						// failed to parse prefix
						Log.w(cgSettings.tag, "cgeoBase.parseCache: Failed to parse waypoint prefix");
					}

					// waypoint lookup
					try {
						final Matcher matcherWpLookup = patternWpPrefixOrLookupOrLatlon.matcher(wp[5]);
						while (matcherWpLookup.find()) {
							if (matcherWpLookup.groupCount() > 1) {
								waypoint.lookup = matcherWpLookup.group(2);
								if (waypoint.lookup != null) {
									waypoint.lookup = waypoint.lookup.trim();
								}
							}
						}
					} catch (Exception e) {
						// failed to parse lookup
						Log.w(cgSettings.tag, "cgeoBase.parseCache: Failed to parse waypoint lookup");
					}

					// waypoint name
					try {
						final Matcher matcherWpName = patternWpName.matcher(wp[6]);
						while (matcherWpName.find()) {
							if (matcherWpName.groupCount() > 0) {
								waypoint.name = matcherWpName.group(1);
								if (waypoint.name != null) {
									waypoint.name = waypoint.name.trim();
								}
							}
						}
					} catch (Exception e) {
						// failed to parse name
						Log.w(cgSettings.tag, "cgeoBase.parseCache: Failed to parse waypoint name");
					}

					// waypoint latitude and logitude
					try {
						final Matcher matcherWpLatLon = patternWpPrefixOrLookupOrLatlon.matcher(wp[7]);
						while (matcherWpLatLon.find()) {
							if (matcherWpLatLon.groupCount() > 1) {
								waypoint.latlon = Html.fromHtml(matcherWpLatLon.group(2)).toString();

								final HashMap<String, Object> tmp = this.parseLatlon(waypoint.latlon);
								if (tmp.size() > 0) {
									waypoint.latitude = (Double) tmp.get("latitude");
									waypoint.longitude = (Double) tmp.get("longitude");
									waypoint.latitudeString = (String) tmp.get("latitudeString");
									waypoint.longitudeString = (String) tmp.get("longitudeString");
								}
							}
						}
					} catch (Exception e) {
						// failed to parse latitude and/or longitude
						Log.w(cgSettings.tag, "cgeoBase.parseCache: Failed to parse waypoint coordinates");
					}

					j++;
					if (wpItems.length > j) {
						wp = wpItems[j].split("<td");
					}

					// waypoint note
					try {
						final Matcher matcherWpNote = patternWpNote.matcher(wp[3]);
						while (matcherWpNote.find()) {
							if (matcherWpNote.groupCount() > 0) {
								waypoint.note = matcherWpNote.group(1);
								if (waypoint.note != null) {
									waypoint.note = waypoint.note.trim();
								}
							}
						}
					} catch (Exception e) {
						// failed to parse note
						Log.w(cgSettings.tag, "cgeoBase.parseCache: Failed to parse waypoint note");
					}

					if (cache.waypoints == null)
						cache.waypoints = new ArrayList<cgWaypoint>();
					cache.waypoints.add(waypoint);
				}
			}
		}

		if (cache.latitude != null && cache.longitude != null) {
			cache.elevation = getElevation(cache.latitude, cache.longitude);
		}

		final cgRating rating = getRating(cache.guid, cache.geocode);
		if (rating != null) {
			cache.rating = rating.rating;
			cache.votes = rating.votes;
			cache.myVote = rating.myVote;
		}

		cache.updated = System.currentTimeMillis();
		cache.detailedUpdate = System.currentTimeMillis();
		cache.detailed = true;
		caches.cacheList.add(cache);

		return caches;
	}

	public cgRating getRating(String guid, String geocode) {
		ArrayList<String> guids = null;
		ArrayList<String> geocodes = null;

		if (guid != null && guid.length() > 0) {
			guids = new ArrayList<String>();
			guids.add(guid);
		} else if (geocode != null && geocode.length() > 0) {
			geocodes = new ArrayList<String>();
			geocodes.add(geocode);
		} else {
			return null;
		}

		final HashMap<String, cgRating> ratings = getRating(guids, geocodes);
		final Set<String> ratingKeys = ratings.keySet();
		for (String ratingKey : ratingKeys) {
			return ratings.get(ratingKey);
		}

		return null;
	}

	public HashMap<String, cgRating> getRating(ArrayList<String> guids, ArrayList<String> geocodes) {
		if (guids == null && geocodes == null) {
			return null;
		}

		final HashMap<String, cgRating> ratings = new HashMap<String, cgRating>();

		try {
			final HashMap<String, String> params = new HashMap<String, String>();
			if (settings.isLogin() == true) {
				final HashMap<String, String> login = settings.getGCvoteLogin();
				if (login != null) {
					params.put("userName", login.get("username"));
					params.put("password", login.get("password"));
				}
			}
			if (guids != null && guids.size() > 0) {
				params.put("cacheIds", implode(",", guids.toArray()));
			} else {
				params.put("waypoints", implode(",", geocodes.toArray()));
			}
			params.put("version", "cgeo");
			final String votes = request(false, "gcvote.com", "/getVotes.php", "GET", params, false, false, false).getData();
			if (votes == null) {
				return null;
			}

			final Pattern patternLogIn = Pattern.compile("loggedIn='([^']+)'", Pattern.CASE_INSENSITIVE);
			final Pattern patternGuid = Pattern.compile("cacheId='([^']+)'", Pattern.CASE_INSENSITIVE);
			final Pattern patternRating = Pattern.compile("voteAvg='([0-9\\.]+)'", Pattern.CASE_INSENSITIVE);
			final Pattern patternVotes = Pattern.compile("voteCnt='([0-9]+)'", Pattern.CASE_INSENSITIVE);
			final Pattern patternVote = Pattern.compile("voteUser='([0-9\\.]+)'", Pattern.CASE_INSENSITIVE);

			String voteData = null;
			final Pattern patternVoteElement = Pattern.compile("<vote ([^>]+)>", Pattern.CASE_INSENSITIVE);
			final Matcher matcherVoteElement = patternVoteElement.matcher(votes);
			while (matcherVoteElement.find()) {
				if (matcherVoteElement.groupCount() > 0) {
					voteData = matcherVoteElement.group(1);
				}

				if (voteData == null) {
					continue;
				}

				String guid = null;
				cgRating rating = new cgRating();
				boolean loggedIn = false;

				try {
					final Matcher matcherGuid = patternGuid.matcher(voteData);
					if (matcherGuid.find()) {
						if (matcherGuid.groupCount() > 0) {
							guid = (String) matcherGuid.group(1);
						}
					}
				} catch (Exception e) {
					Log.w(cgSettings.tag, "cgBase.getRating: Failed to parse guid");
				}

				try {
					final Matcher matcherLoggedIn = patternLogIn.matcher(votes);
					if (matcherLoggedIn.find()) {
						if (matcherLoggedIn.groupCount() > 0) {
							if (matcherLoggedIn.group(1).equalsIgnoreCase("true") == true) {
								loggedIn = true;
							}
						}
					}
				} catch (Exception e) {
					Log.w(cgSettings.tag, "cgBase.getRating: Failed to parse loggedIn");
				}

				try {
					final Matcher matcherRating = patternRating.matcher(voteData);
					if (matcherRating.find()) {
						if (matcherRating.groupCount() > 0) {
							rating.rating = Float.parseFloat(matcherRating.group(1));
						}
					}
				} catch (Exception e) {
					Log.w(cgSettings.tag, "cgBase.getRating: Failed to parse rating");
				}

				try {
					final Matcher matcherVotes = patternVotes.matcher(voteData);
					if (matcherVotes.find()) {
						if (matcherVotes.groupCount() > 0) {
							rating.votes = Integer.parseInt(matcherVotes.group(1));
						}
					}
				} catch (Exception e) {
					Log.w(cgSettings.tag, "cgBase.getRating: Failed to parse vote count");
				}

				if (loggedIn == true) {
					try {
						final Matcher matcherVote = patternVote.matcher(voteData);
						if (matcherVote.find()) {
							if (matcherVote.groupCount() > 0) {
								rating.myVote = Float.parseFloat(matcherVote.group(1));
							}
						}
					} catch (Exception e) {
						Log.w(cgSettings.tag, "cgBase.getRating: Failed to parse user's vote");
					}
				}

				if (guid != null) {
					ratings.put(guid, rating);
				}
			}
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgBase.getRating: " + e.toString());
		}

		return ratings;
	}

	public boolean setRating(String guid, int vote) {
		if (guid == null || guid.length() == 0) {
			return false;
		}
		if (vote < 0 || vote > 5) {
			return false;
		}

		final HashMap<String, String> login = settings.getGCvoteLogin();
		if (login == null) {
			return false;
		}

		final HashMap<String, String> params = new HashMap<String, String>();
		params.put("userName", login.get("username"));
		params.put("password", login.get("password"));
		params.put("cacheId", guid);
		params.put("voteUser", Integer.toString(vote));
		params.put("version", "cgeo");

		final String result = request(false, "gcvote.com", "/setVote.php", "GET", params, false, false, false).getData();

		if (result.trim().equalsIgnoreCase("ok") == true) {
			return true;
		}

		return false;
	}

	public Long parseGPX(cgeoapplication app, File file, int listId, Handler handler) {
		cgSearch search = new cgSearch();
		long searchId = 0l;

		try {
			cgGPXParser GPXparser = new cgGPXParser(app, this, listId, search);

			searchId = GPXparser.parse(file, 10, handler);
			if (searchId == 0l) {
				searchId = GPXparser.parse(file, 11, handler);
			}
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgBase.parseGPX: " + e.toString());
		}

		Log.i(cgSettings.tag, "Caches found in .gpx file: " + app.getCount(searchId));

		return search.getCurrentId();
	}

	public cgTrackable parseTrackable(String page) {
		if (page == null || page.length() == 0) {
			Log.e(cgSettings.tag, "cgeoBase.parseTrackable: No page given");
			return null;
		}

		final Pattern patternTrackableId = Pattern.compile("<a id=\"ctl00_ContentBody_LogLink\" title=\"[^\"]*\" href=\".*log\\.aspx\\?wid=([a-z0-9\\-]+)\"[^>]*>[^<]*</a>", Pattern.CASE_INSENSITIVE);
		final Pattern patternGeocode = Pattern.compile("<span id=\"ctl00_ContentBody_BugDetails_BugTBNum\" String=\"[^\"]*\">Use[^<]*<strong>(TB[0-9a-z]+)[^<]*</strong> to reference this item.[^<]*</span>", Pattern.CASE_INSENSITIVE);
		final Pattern patternName = Pattern.compile("<h2>([^<]*<img[^>]*>)?[^<]*<span id=\"ctl00_ContentBody_lbHeading\">([^<]+)</span>[^<]*</h2>", Pattern.CASE_INSENSITIVE);
		final Pattern patternOwner = Pattern.compile("<dt>[^\\w]*Owner:[^<]*</dt>[^<]*<dd>[^<]*<a id=\"ctl00_ContentBody_BugDetails_BugOwner\" title=\"[^\"]*\" href=\"[^\"]*/profile/\\?guid=([a-z0-9\\-]+)\">([^<]+)<\\/a>[^<]*</dd>", Pattern.CASE_INSENSITIVE);
		final Pattern patternReleased = Pattern.compile("<dt>[^\\w]*Released:[^<]*</dt>[^<]*<dd>[^<]*<span id=\"ctl00_ContentBody_BugDetails_BugReleaseDate\">([^<]+)<\\/span>[^<]*</dd>", Pattern.CASE_INSENSITIVE);
		final Pattern patternOrigin = Pattern.compile("<dt>[^\\w]*Origin:[^<]*</dt>[^<]*<dd>[^<]*<span id=\"ctl00_ContentBody_BugDetails_BugOrigin\">([^<]+)<\\/span>[^<]*</dd>", Pattern.CASE_INSENSITIVE);
		final Pattern patternSpottedCache = Pattern.compile("<dt>[^\\w]*Recently Spotted:[^<]*</dt>[^<]*<dd>[^<]*<a id=\"ctl00_ContentBody_BugDetails_BugLocation\" title=\"[^\"]*\" href=\"[^\"]*/seek/cache_details.aspx\\?guid=([a-z0-9\\-]+)\">In ([^<]+)</a>[^<]*</dd>", Pattern.CASE_INSENSITIVE);
		final Pattern patternSpottedUser = Pattern.compile("<dt>[^\\w]*Recently Spotted:[^<]*</dt>[^<]*<dd>[^<]*<a id=\"ctl00_ContentBody_BugDetails_BugLocation\" href=\"[^\"]*/profile/\\?guid=([a-z0-9\\-]+)\">In the hands of ([^<]+).</a>[^<]*</dd>", Pattern.CASE_INSENSITIVE);
		final Pattern patternSpottedUnknown = Pattern.compile("<dt>[^\\w]*Recently Spotted:[^<]*</dt>[^<]*<dd>[^<]*<a id=\"ctl00_ContentBody_BugDetails_BugLocation\">Unknown Location[^<]*</a>[^<]*</dd>", Pattern.CASE_INSENSITIVE);
		final Pattern patternSpottedOwner = Pattern.compile("<dt>[^\\w]*Recently Spotted:[^<]*</dt>[^<]*<dd>[^<]*<a id=\"ctl00_ContentBody_BugDetails_BugLocation\">In the hands of the owner[^<]*</a>[^<]*</dd>", Pattern.CASE_INSENSITIVE);
		final Pattern patternGoal = Pattern.compile("<h3>[^\\w]*Current GOAL[^<]*</h3>[^<]*<p[^>]*>(.*)</p>[^<]*<h3>[^\\w]*About This Item[^<]*</h3>", Pattern.CASE_INSENSITIVE);
		final Pattern patternDetailsImage = Pattern.compile("<h3>[^\\w]*About This Item[^<]*</h3>([^<]*<p>([^<]*<img id=\"ctl00_ContentBody_BugDetails_BugImage\" class=\"[^\"]+\" src=\"([^\"]+)\"[^>]*>)?[^<]*</p>)?[^<]*<p[^>]*>(.*)</p>[^<]*<div id=\"ctl00_ContentBody_BugDetails_uxAbuseReport\">", Pattern.CASE_INSENSITIVE);
		final Pattern patternLogs = Pattern.compile("<table class=\"TrackableItemLogTable Table\">(.*)<\\/table>[^<]*<ul", Pattern.CASE_INSENSITIVE);
		final Pattern patternIcon = Pattern.compile("<img id=\"ctl00_ContentBody_BugTypeImage\" class=\"TravelBugHeaderIcon\" src=\"([^\"]+)\"[^>]*>", Pattern.CASE_INSENSITIVE);
		final Pattern patternType = Pattern.compile("<img id=\"ctl00_ContentBody_BugTypeImage\" class=\"TravelBugHeaderIcon\" src=\"[^\"]+\" alt=\"([^\"]+)\"[^>]*>", Pattern.CASE_INSENSITIVE);
		final Pattern patternDistance = Pattern.compile("<h4[^>]*[^\\w]*Tracking History \\(([0-9\\.,]+(km|mi))[^\\)]*\\)", Pattern.CASE_INSENSITIVE);

		final cgTrackable trackable = new cgTrackable();

		// trackable geocode
		try {
			final Matcher matcherGeocode = patternGeocode.matcher(page);
			while (matcherGeocode.find()) {
				if (matcherGeocode.groupCount() > 0) {
					trackable.geocode = matcherGeocode.group(1).toUpperCase();
				}
			}
		} catch (Exception e) {
			// failed to parse trackable geocode
			Log.w(cgSettings.tag, "cgeoBase.parseTrackable: Failed to parse trackable geocode");
		}

		// trackable id
		try {
			final Matcher matcherTrackableId = patternTrackableId.matcher(page);
			while (matcherTrackableId.find()) {
				if (matcherTrackableId.groupCount() > 0) {
					trackable.guid = matcherTrackableId.group(1);
				}
			}
		} catch (Exception e) {
			// failed to parse trackable id
			Log.w(cgSettings.tag, "cgeoBase.parseTrackable: Failed to parse trackable id");
		}

		// trackable icon
		try {
			final Matcher matcherTrackableIcon = patternIcon.matcher(page);
			while (matcherTrackableIcon.find()) {
				if (matcherTrackableIcon.groupCount() > 0) {
					trackable.iconUrl = matcherTrackableIcon.group(1);
				}
			}
		} catch (Exception e) {
			// failed to parse trackable icon
			Log.w(cgSettings.tag, "cgeoBase.parseTrackable: Failed to parse trackable icon");
		}

		// trackable name
		try {
			final Matcher matcherName = patternName.matcher(page);
			while (matcherName.find()) {
				if (matcherName.groupCount() > 1) {
					trackable.name = matcherName.group(2);
				}
			}
		} catch (Exception e) {
			// failed to parse trackable name
			Log.w(cgSettings.tag, "cgeoBase.parseTrackable: Failed to parse trackable name");
		}

		// trackable type
		if (trackable.name != null && trackable.name.length() > 0) {
			try {
				final Matcher matcherType = patternType.matcher(page);
				while (matcherType.find()) {
					if (matcherType.groupCount() > 0) {
						trackable.type = matcherType.group(1);
					}
				}
			} catch (Exception e) {
				// failed to parse trackable type
				Log.w(cgSettings.tag, "cgeoBase.parseTrackable: Failed to parse trackable type");
			}
		}

		// trackable owner name
		try {
			final Matcher matcherOwner = patternOwner.matcher(page);
			while (matcherOwner.find()) {
				if (matcherOwner.groupCount() > 0) {
					trackable.ownerGuid = matcherOwner.group(1);
					trackable.owner = matcherOwner.group(2);
				}
			}
		} catch (Exception e) {
			// failed to parse trackable owner name
			Log.w(cgSettings.tag, "cgeoBase.parseTrackable: Failed to parse trackable owner name");
		}

		// trackable origin
		try {
			final Matcher matcherOrigin = patternOrigin.matcher(page);
			while (matcherOrigin.find()) {
				if (matcherOrigin.groupCount() > 0) {
					trackable.origin = matcherOrigin.group(1);
				}
			}
		} catch (Exception e) {
			// failed to parse trackable origin
			Log.w(cgSettings.tag, "cgeoBase.parseTrackable: Failed to parse trackable origin");
		}

		// trackable spotted
		try {
			final Matcher matcherSpottedCache = patternSpottedCache.matcher(page);
			while (matcherSpottedCache.find()) {
				if (matcherSpottedCache.groupCount() > 0) {
					trackable.spottedGuid = matcherSpottedCache.group(1);
					trackable.spottedName = matcherSpottedCache.group(2);
					trackable.spottedType = cgTrackable.SPOTTED_CACHE;
				}
			}

			final Matcher matcherSpottedUser = patternSpottedUser.matcher(page);
			while (matcherSpottedUser.find()) {
				if (matcherSpottedUser.groupCount() > 0) {
					trackable.spottedGuid = matcherSpottedUser.group(1);
					trackable.spottedName = matcherSpottedUser.group(2);
					trackable.spottedType = cgTrackable.SPOTTED_USER;
				}
			}

			final Matcher matcherSpottedUnknown = patternSpottedUnknown.matcher(page);
			if (matcherSpottedUnknown.find()) {
				trackable.spottedType = cgTrackable.SPOTTED_UNKNOWN;
			}

			final Matcher matcherSpottedOwner = patternSpottedOwner.matcher(page);
			if (matcherSpottedOwner.find()) {
				trackable.spottedType = cgTrackable.SPOTTED_OWNER;
			}
		} catch (Exception e) {
			// failed to parse trackable last known place
			Log.w(cgSettings.tag, "cgeoBase.parseTrackable: Failed to parse trackable last known place");
		}

		// released
		try {
			final Matcher matcherReleased = patternReleased.matcher(page);
			while (matcherReleased.find()) {
				if (matcherReleased.groupCount() > 0 && matcherReleased.group(1) != null) {
					try {
						if (trackable.released == null) {
							trackable.released = dateTbIn1.parse(matcherReleased.group(1));
						}
					} catch (Exception e) {
						//
					}

					try {
						if (trackable.released == null) {
							trackable.released = dateTbIn2.parse(matcherReleased.group(1));
						}
					} catch (Exception e) {
						//
					}
				}
			}
		} catch (Exception e) {
			// failed to parse trackable released date
			Log.w(cgSettings.tag, "cgeoBase.parseTrackable: Failed to parse trackable released date");
		}

		// trackable distance
		try {
			final Matcher matcherDistance = patternDistance.matcher(page);
			while (matcherDistance.find()) {
				if (matcherDistance.groupCount() > 0) {
					trackable.distance = parseDistance(matcherDistance.group(1));
				}
			}
		} catch (Exception e) {
			// failed to parse trackable distance
			Log.w(cgSettings.tag, "cgeoBase.parseTrackable: Failed to parse trackable distance");
		}

		// trackable goal
		try {
			final Matcher matcherGoal = patternGoal.matcher(page);
			while (matcherGoal.find()) {
				if (matcherGoal.groupCount() > 0) {
					trackable.goal = matcherGoal.group(1);
				}
			}
		} catch (Exception e) {
			// failed to parse trackable goal
			Log.w(cgSettings.tag, "cgeoBase.parseTrackable: Failed to parse trackable goal");
		}

		// trackable details & image
		try {
			final Matcher matcherDetailsImage = patternDetailsImage.matcher(page);
			while (matcherDetailsImage.find()) {
				if (matcherDetailsImage.groupCount() > 0) {
					final String image = matcherDetailsImage.group(3);
					final String details = matcherDetailsImage.group(4);

					if (image != null) {
						trackable.image = image;
					}
					if (details != null) {
						trackable.details = details;
					}
				}
			}
		} catch (Exception e) {
			// failed to parse trackable details & image
			Log.w(cgSettings.tag, "cgeoBase.parseTrackable: Failed to parse trackable details & image");
		}

		// trackable logs
		try {
			final Matcher matcherLogs = patternLogs.matcher(page);
			while (matcherLogs.find()) {
				if (matcherLogs.groupCount() > 0) {
					final Pattern patternLog = Pattern.compile("[^>]*>" +
							"[^<]*<td[^<]*<img src=[\"|'].*\\/icons\\/([^\\.]+)\\.[a-z]{2,5}[\"|'][^>]*>&nbsp;(\\d+).(\\d+).(\\d+)[^<]*</td>" +
							"[^<]*<td>[^<]*<a href=[^>]+>([^<]+)<.a>([^<]*|[^<]*<a href=[\"|'].*guid=([^\"]*)\">([^<]*)</a>[^<]*)</td>" +
							"[^<]*<td>[^<]*</td>" +
							"[^<]*<td[^<]*<a href=[^>]+>[^<]+</a>[^<]*</td>[^<]*</tr>" +
							"[^<]*<tr[^>]*>[^<]*<td[^>]*>(.*?)</td>[^<]*</tr>.*" +
							"");
					// 1 filename == type
					// 2 month
					// 3 date
					// 4 year
					// 5 user
					// 6 action dependent
					// 7 cache guid
					// 8 cache name
					// 9 text
					final String[] logs = matcherLogs.group(1).split("<tr class=\"Data BorderTop");
					final int logsCnt = logs.length;

					for (int k = 1; k < logsCnt; k++) {
						final Matcher matcherLog = patternLog.matcher(logs[k]);
						if (matcherLog.find()) {
							final cgLog logDone = new cgLog();

							String logTmp = matcherLog.group(9);
							logTmp = Pattern.compile("<p>").matcher(logTmp).replaceAll("\n");
							logTmp = Pattern.compile("<br[^>]*>").matcher(logTmp).replaceAll("\n");
							logTmp = Pattern.compile("<\\/p>").matcher(logTmp).replaceAll("");
							logTmp = Pattern.compile("\r+").matcher(logTmp).replaceAll("\n");

							int day = -1;
							try {
								day = Integer.parseInt(matcherLog.group(3));
							} catch (Exception e) {
								Log.w(cgSettings.tag, "Failed to parse logs date (day): " + e.toString());
							}

							int month = -1;
							try {
								month = Integer.parseInt(matcherLog.group(2));
								month -= 1;
							} catch (Exception e) {
								Log.w(cgSettings.tag, "Failed to parse logs date (month): " + e.toString());
							}

							int year = -1;
							try {
								year = Integer.parseInt(matcherLog.group(4));
							} catch (Exception e) {
								Log.w(cgSettings.tag, "Failed to parse logs date (year): " + e.toString());
							}

							long logDate;
							if (year > 0 && month >= 0 && day > 0) {
								Calendar date = Calendar.getInstance();
								date.set(year, month, day, 12, 0, 0);
								logDate = date.getTimeInMillis();
								logDate = (long) (Math.ceil(logDate / 1000)) * 1000;
							} else {
								logDate = 0;
							}

							if (logTypes.containsKey(matcherLog.group(1).toLowerCase()) == true) {
								logDone.type = logTypes.get(matcherLog.group(1).toLowerCase());
							} else {
								logDone.type = logTypes.get("icon_note");
							}

							logDone.author = Html.fromHtml(matcherLog.group(5)).toString();
							logDone.date = logDate;
							logDone.log = logTmp;
							if (matcherLog.group(7) != null && matcherLog.group(8) != null) {
								logDone.cacheGuid = matcherLog.group(7);
								logDone.cacheName = matcherLog.group(8);
							}

							trackable.logs.add(logDone);
						}
					}
				}
			}
		} catch (Exception e) {
			// failed to parse logs
			Log.w(cgSettings.tag, "cgeoBase.parseCache: Failed to parse cache logs");
		}

		app.saveTrackable(trackable);

		return trackable;
	}

	public ArrayList<Integer> parseTypes(String page) {
		if (page == null || page.length() == 0) {
			return null;
		}

		final ArrayList<Integer> types = new ArrayList<Integer>();

		final Pattern typeBoxPattern = Pattern.compile("<select name=\"ctl00\\$ContentBody\\$LogBookPanel1\\$ddLogType\" id=\"ctl00_ContentBody_LogBookPanel1_ddLogType\"[^>]*>"
				+ "(([^<]*<option[^>]*>[^<]+</option>)+)[^<]*</select>", Pattern.CASE_INSENSITIVE);
		final Matcher typeBoxMatcher = typeBoxPattern.matcher(page);
		String typesText = null;
		if (typeBoxMatcher.find()) {
			if (typeBoxMatcher.groupCount() > 0) {
				typesText = typeBoxMatcher.group(1);
			}
		}

		if (typesText != null) {
			final Pattern typePattern = Pattern.compile("<option( selected=\"selected\")? value=\"(\\d+)\">[^<]+</option>", Pattern.CASE_INSENSITIVE);
			final Matcher typeMatcher = typePattern.matcher(typesText);
			while (typeMatcher.find()) {
				if (typeMatcher.groupCount() > 1) {
					final int type = Integer.parseInt(typeMatcher.group(2));

					if (type > 0) {
						types.add(type);
					}
				}
			}
		}

		return types;
	}

	public ArrayList<cgTrackableLog> parseTrackableLog(String page) {
		if (page == null || page.length() == 0) {
			return null;
		}

		final ArrayList<cgTrackableLog> trackables = new ArrayList<cgTrackableLog>();

		int startPos = -1;
		int endPos = -1;

		startPos = page.indexOf("<table id=\"tblTravelBugs\"");
		if (startPos == -1) {
			Log.e(cgSettings.tag, "cgeoBase.parseTrackableLog: ID \"tblTravelBugs\" not found on page");
			return null;
		}

		page = page.substring(startPos); // cut on <table

		endPos = page.indexOf("</table>");
		if (endPos == -1) {
			Log.e(cgSettings.tag, "cgeoBase.parseTrackableLog: end of ID \"tblTravelBugs\" not found on page");
			return null;
		}

		page = page.substring(0, endPos); // cut on </table>

		startPos = page.indexOf("<tbody>");
		if (startPos == -1) {
			Log.e(cgSettings.tag, "cgeoBase.parseTrackableLog: tbody not found on page");
			return null;
		}

		page = page.substring(startPos); // cut on <tbody>

		endPos = page.indexOf("</tbody>");
		if (endPos == -1) {
			Log.e(cgSettings.tag, "cgeoBase.parseTrackableLog: end of tbody not found on page");
			return null;
		}

		page = page.substring(0, endPos); // cut on </tbody>

		final Pattern trackablePattern = Pattern.compile("<tr id=\"ctl00_ContentBody_LogBookPanel1_uxTrackables_repTravelBugs_ctl[0-9]+_row\"[^>]*>"
				+ "[^<]*<td>[^<]*<a href=\"[^\"]+\">([A-Z0-9]+)</a>[^<]*</td>[^<]*<td>([^<]+)</td>[^<]*<td>"
				+ "[^<]*<select name=\"ctl00\\$ContentBody\\$LogBookPanel1\\$uxTrackables\\$repTravelBugs\\$ctl([0-9]+)\\$ddlAction\"[^>]*>"
				+ "([^<]*<option value=\"([0-9]+)(_[a-z]+)?\">[^<]+</option>)+"
				+ "[^<]*</select>[^<]*</td>[^<]*</tr>", Pattern.CASE_INSENSITIVE);
		final Matcher trackableMatcher = trackablePattern.matcher(page);
		while (trackableMatcher.find()) {
			if (trackableMatcher.groupCount() > 0) {
				final cgTrackableLog trackable = new cgTrackableLog();

				if (trackableMatcher.group(1) != null) {
					trackable.trackCode = trackableMatcher.group(1);
				} else {
					continue;
				}
				if (trackableMatcher.group(2) != null) {
					trackable.name = Html.fromHtml(trackableMatcher.group(2)).toString();
				} else {
					continue;
				}
				if (trackableMatcher.group(3) != null) {
					trackable.ctl = new Integer(trackableMatcher.group(3));
				} else {
					continue;
				}
				if (trackableMatcher.group(5) != null) {
					trackable.id = new Integer(trackableMatcher.group(5));
				} else {
					continue;
				}

				Log.i(cgSettings.tag, "Trackable in inventory (#" + trackable.ctl + "/" + trackable.id + "): " + trackable.trackCode + " - " + trackable.name);

				trackables.add(trackable);
			}
		}

		return trackables;
	}

	public int parseFindCount(String page) {
		if (page == null || page.length() == 0) {
			return -1;
		}

		int findCount = -1;

		try {
			final Pattern findPattern = Pattern.compile("<strong>Caches Found:<\\/strong>([^<]+)<br", Pattern.CASE_INSENSITIVE);
			final Matcher findMatcher = findPattern.matcher(page);
			if (findMatcher.find() == true) {
				if (findMatcher.groupCount() > 0) {
					String count = findMatcher.group(1);

					if (count != null) {
						count = count.trim();

						if (count.length() == 0) {
							findCount = 0;
						} else {
							findCount = Integer.parseInt(count);
						}
					}
				}
			}
		} catch (Exception e) {
			Log.w(cgSettings.tag, "cgBase.parseFindCount: " + e.toString());
		}

		return findCount;
	}


	public static String stripParagraphs(String text) {
		if (text == null) {
			return "";
		}

		final Pattern patternP = Pattern.compile("(<p>|</p>|<br \\/>|<br>)", Pattern.CASE_INSENSITIVE);
		final Pattern patternP2 = Pattern.compile("([ ]+)", Pattern.CASE_INSENSITIVE);
		final Matcher matcherP = patternP.matcher(text);
		final Matcher matcherP2 = patternP2.matcher(text);

		matcherP.replaceAll(" ");
		matcherP2.replaceAll(" ");

		return text.trim();
	}

	public static String stripTags(String text) {
		if (text == null) {
			return "";
		}

		final Pattern patternP = Pattern.compile("(<[^>]+>)", Pattern.CASE_INSENSITIVE);
		final Matcher matcherP = patternP.matcher(text);

		matcherP.replaceAll(" ");

		return text.trim();
	}

	public static String capitalizeSentence(String sentence) {
		if (sentence == null) {
			return "";
		}

		final String[] word = sentence.split(" ");

		for (int i = 0; i < word.length; i++) {
			word[i] = capitalizeWord(word[i]);
		}

		return implode(" ", word);
	}

	public static String capitalizeWord(String word) {
		if (word.length() == 0) {
			return word;
		}

		return (word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase());
	}

	public static Double parseDistance(String dst) {
		Double distance = null;

		final Pattern pattern = Pattern.compile("([0-9\\.,]+)[ ]*(km|mi)", Pattern.CASE_INSENSITIVE);
		final Matcher matcher = pattern.matcher(dst);
		while (matcher.find()) {
			if (matcher.groupCount() > 1) {
				if (matcher.group(2).equalsIgnoreCase("km") == true) {
					distance = new Double(matcher.group(1));
				} else {
					distance = new Double(matcher.group(1)) / kmInMiles;
				}
			}
		}

		return distance;
	}

	public static double getDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
		if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
			return 0d;
		}

		lat1 *= deg2rad;
		lon1 *= deg2rad;
		lat2 *= deg2rad;
		lon2 *= deg2rad;

		final double d = Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2);
		final double distance = erad * Math.acos(d); // distance in km

		if (Double.isNaN(distance) == false && distance > 0) {
			return distance;
		} else {
			return 0d;
		}
	}

	public static Double getHeading(Double lat1, Double lon1, Double lat2, Double lon2) {
		Double result = new Double(0);

		int ilat1 = (int) Math.round(0.5 + lat1 * 360000);
		int ilon1 = (int) Math.round(0.5 + lon1 * 360000);
		int ilat2 = (int) Math.round(0.5 + lat2 * 360000);
		int ilon2 = (int) Math.round(0.5 + lon2 * 360000);

		lat1 *= deg2rad;
		lon1 *= deg2rad;
		lat2 *= deg2rad;
		lon2 *= deg2rad;

		if (ilat1 == ilat2 && ilon1 == ilon2) {
			return new Double(result);
		} else if (ilat1 == ilat2) {
			if (ilon1 > ilon2) {
				result = new Double(270);
			} else {
				result = new Double(90);
			}
		} else if (ilon1 == ilon2) {
			if (ilat1 > ilat2) {
				result = new Double(180);
			}
		} else {
			Double c = Math.acos(Math.sin(lat2) * Math.sin(lat1) + Math.cos(lat2) * Math.cos(lat1) * Math.cos(lon2 - lon1));
			Double A = Math.asin(Math.cos(lat2) * Math.sin(lon2 - lon1) / Math.sin(c));
			result = new Double(A * rad2deg);
			if (ilat2 > ilat1 && ilon2 > ilon1) {
				// result don't need change
			} else if (ilat2 < ilat1 && ilon2 < ilon1) {
				result = 180f - result;
			} else if (ilat2 < ilat1 && ilon2 > ilon1) {
				result = 180f - result;
			} else if (ilat2 > ilat1 && ilon2 < ilon1) {
				result += 360f;
			}
		}

		return result;
	}

	public HashMap<String, Double> getRadialDistance(Double latitude, Double longitude, Double bearing, Double distance) {
		final Double rlat1 = latitude * deg2rad;
		final Double rlon1 = longitude * deg2rad;
		final Double rbearing = bearing * deg2rad;
		final Double rdistance = distance / erad;

		final Double rlat = Math.asin(Math.sin(rlat1) * Math.cos(rdistance) + Math.cos(rlat1) * Math.sin(rdistance) * Math.cos(rbearing));
		final Double rlon = rlon1 + Math.atan2(Math.sin(rbearing) * Math.sin(rdistance) * Math.cos(rlat1), Math.cos(rdistance) - Math.sin(rlat1) * Math.sin(rlat));

		HashMap<String, Double> result = new HashMap<String, Double>();
		result.put("latitude", rlat * rad2deg);
		result.put("longitude", rlon * rad2deg);

		return result;
	}

	public String getHumanDistance(Float distance) {
		if (distance == null) {
			return "?";
		}

		return getHumanDistance(new Double(distance));
	}

	public String getHumanDistance(Double distance) {
		if (distance == null) {
			return "?";
		}

		if (settings.units == cgSettings.unitsImperial) {
			distance *= kmInMiles;
			if (distance > 100) {
				return String.format(Locale.getDefault(), "%.0f", new Double(Math.round(distance))) + " mi";
			} else if (distance > 0.5) {
				return String.format(Locale.getDefault(), "%.1f", new Double(Math.round(distance * 10.0) / 10.0)) + " mi";
			} else if (distance > 0.1) {
				return String.format(Locale.getDefault(), "%.2f", new Double(Math.round(distance * 100.0) / 100.0)) + " mi";
			} else if (distance > 0.05) {
				return String.format(Locale.getDefault(), "%.0f", new Double(Math.round(distance * 5280.0))) + " ft";
			} else if (distance > 0.01) {
				return String.format(Locale.getDefault(), "%.1f", new Double(Math.round(distance * 5280 * 10.0) / 10.0)) + " ft";
			} else {
				return String.format(Locale.getDefault(), "%.2f", new Double(Math.round(distance * 5280 * 100.0) / 100.0)) + " ft";
			}
		} else {
			if (distance > 100) {
				return String.format(Locale.getDefault(), "%.0f", new Double(Math.round(distance))) + " km";
			} else if (distance > 10) {
				return String.format(Locale.getDefault(), "%.1f", new Double(Math.round(distance * 10.0) / 10.0)) + " km";
			} else if (distance > 1) {
				return String.format(Locale.getDefault(), "%.2f", new Double(Math.round(distance * 100.0) / 100.0)) + " km";
			} else if (distance > 0.1) {
				return String.format(Locale.getDefault(), "%.0f", new Double(Math.round(distance * 1000.0))) + " m";
			} else if (distance > 0.01) {
				return String.format(Locale.getDefault(), "%.1f", new Double(Math.round(distance * 1000.0 * 10.0) / 10.0)) + " m";
			} else {
				return String.format(Locale.getDefault(), "%.2f", new Double(Math.round(distance * 1000.0 * 100.0) / 100.0)) + " m";
			}
		}
	}

	public String getHumanSpeed(float speed) {
		double kph = speed * 3.6;
		String unit = "kmh";

		if (this.settings.units == cgSettings.unitsImperial) {
			kph *= kmInMiles;
			unit = "mph";
		}

		if (kph < 10) {
			return String.format(Locale.getDefault(), "%.1f", new Double((Math.round(kph * 10) / 10))) + " " + unit;
		} else {
			return String.format(Locale.getDefault(), "%.0f", new Double(Math.round(kph))) + " " + unit;
		}
	}

	public HashMap<String, Object> parseLatlon(String latlon) {
		final HashMap<String, Object> result = new HashMap<String, Object>();
		final Pattern patternLatlon = Pattern.compile("([NS])[^\\d]*(\\d+)[^°]*° (\\d+)\\.(\\d+) ([WE])[^\\d]*(\\d+)[^°]*° (\\d+)\\.(\\d+)", Pattern.CASE_INSENSITIVE);
		final Matcher matcherLatlon = patternLatlon.matcher(latlon);

		while (matcherLatlon.find()) {
			if (matcherLatlon.groupCount() > 0) {
				result.put("latitudeString", (String) (matcherLatlon.group(1) + " " + matcherLatlon.group(2) + "° " + matcherLatlon.group(3) + "." + matcherLatlon.group(4)));
				result.put("longitudeString", (String) (matcherLatlon.group(5) + " " + matcherLatlon.group(6) + "° " + matcherLatlon.group(7) + "." + matcherLatlon.group(8)));
				int latNegative = -1;
				int lonNegative = -1;
				if (matcherLatlon.group(1).equalsIgnoreCase("N")) {
					latNegative = 1;
				}
				if (matcherLatlon.group(5).equalsIgnoreCase("E")) {
					lonNegative = 1;
				}
				result.put("latitude", new Double(latNegative * (new Float(matcherLatlon.group(2)) + new Float(matcherLatlon.group(3) + "." + matcherLatlon.group(4)) / 60)));
				result.put("longitude", new Double(lonNegative * (new Float(matcherLatlon.group(6)) + new Float(matcherLatlon.group(7) + "." + matcherLatlon.group(8)) / 60)));
			} else {
				Log.w(cgSettings.tag, "cgBase.parseLatlon: Failed to parse coordinates.");
			}
		}

		return result;
	}

	public String formatCoordinate(Double coord, String latlon, boolean degrees) {
		String formatted = "";

		if (coord == null) {
			return formatted;
		}

		String worldSide = "";
		if (latlon.equalsIgnoreCase("lat") == true) {
			if (coord >= 0) {
				// have the blanks here at the direction to avoid one String concatenation
				worldSide = "N ";
			} else {
				worldSide = "S ";
			}
		} else if (latlon.equalsIgnoreCase("lon") == true) {
			if (coord >= 0) {
				worldSide = "E ";
			} else {
				worldSide = "W ";
			}
		}

		coord = Math.abs(coord);

		if (latlon.equalsIgnoreCase("lat") == true) {
			if (degrees == true) {
				formatted = worldSide + String.format(Locale.getDefault(), "%02.0f", Math.floor(coord)) + "° " + String.format(Locale.getDefault(), "%06.3f", ((coord - Math.floor(coord)) * 60));
			} else {
				formatted = worldSide + String.format(Locale.getDefault(), "%02.0f", Math.floor(coord)) + " " + String.format(Locale.getDefault(), "%06.3f", ((coord - Math.floor(coord)) * 60));
			}
		} else {
			if (degrees == true) {
				formatted = worldSide + String.format(Locale.getDefault(), "%03.0f", Math.floor(coord)) + "° " + String.format(Locale.getDefault(), "%06.3f", ((coord - Math.floor(coord)) * 60));
			} else {
				formatted = worldSide + String.format(Locale.getDefault(), "%03.0f", Math.floor(coord)) + " " + String.format(Locale.getDefault(), "%06.3f", ((coord - Math.floor(coord)) * 60));
			}
		}

		return formatted;
	}

	public HashMap<String, Object> parseCoordinate(String coord, String latlon) {
		final HashMap<String, Object> coords = new HashMap<String, Object>();

		final Pattern patternA = Pattern.compile("^([NSWE])[^\\d]*(\\d+)°? +(\\d+)([\\.|,](\\d+))?$", Pattern.CASE_INSENSITIVE);
		final Pattern patternB = Pattern.compile("^([NSWE])[^\\d]*(\\d+)([\\.|,](\\d+))?$", Pattern.CASE_INSENSITIVE);
		final Pattern patternC = Pattern.compile("^(-?\\d+)([\\.|,](\\d+))?$", Pattern.CASE_INSENSITIVE);
		final Pattern patternD = Pattern.compile("^([NSWE])[^\\d]*(\\d+)°?$", Pattern.CASE_INSENSITIVE);
		final Pattern patternE = Pattern.compile("^(-?\\d+)°?$", Pattern.CASE_INSENSITIVE);
		final Pattern patternF = Pattern.compile("^([NSWE])[^\\d]*(\\d+)$", Pattern.CASE_INSENSITIVE);
		final Pattern pattern0 = Pattern.compile("^(-?\\d+)([\\.|,](\\d+))?$", Pattern.CASE_INSENSITIVE);

		coord = coord.trim().toUpperCase();

		final Matcher matcherA = patternA.matcher(coord);
		final Matcher matcherB = patternB.matcher(coord);
		final Matcher matcherC = patternC.matcher(coord);
		final Matcher matcherD = patternD.matcher(coord);
		final Matcher matcherE = patternE.matcher(coord);
		final Matcher matcherF = patternF.matcher(coord);
		final Matcher matcher0 = pattern0.matcher(coord);

		int latlonNegative;
		if (matcherA.find() == true && matcherA.groupCount() > 0) {
			if (matcherA.group(1).equalsIgnoreCase("N") || matcherA.group(1).equalsIgnoreCase("E")) {
				latlonNegative = 1;
			} else {
				latlonNegative = -1;
			}

			if (matcherA.groupCount() < 5 || matcherA.group(5) == null) {
				coords.put("coordinate", new Double(latlonNegative * (new Double(matcherA.group(2)) + new Double(matcherA.group(3) + ".0") / 60)));
				coords.put("string", matcherA.group(1) + " " + matcherA.group(2) + "° " + matcherA.group(3) + ".000");
			} else {
				coords.put("coordinate", new Double(latlonNegative * (new Double(matcherA.group(2)) + new Double(matcherA.group(3) + "." + matcherA.group(5)) / 60)));
				coords.put("string", matcherA.group(1) + " " + matcherA.group(2) + "° " + matcherA.group(3) + "." + matcherA.group(5));
			}

			return coords;
		} else if (matcherB.find() == true && matcherB.groupCount() > 0) {
			if (matcherB.group(1).equalsIgnoreCase("N") || matcherB.group(1).equalsIgnoreCase("E")) {
				latlonNegative = 1;
			} else {
				latlonNegative = -1;
			}

			if (matcherB.groupCount() < 4 || matcherB.group(4) == null) {
				coords.put("coordinate", new Double(latlonNegative * (new Double(matcherB.group(2) + ".0"))));
			} else {
				coords.put("coordinate", new Double(latlonNegative * (new Double(matcherB.group(2) + "." + matcherB.group(4)))));
			}
		} else if (matcherC.find() == true && matcherC.groupCount() > 0) {
			if (matcherC.groupCount() < 3 || matcherC.group(3) == null) {
				coords.put("coordinate", new Double(new Float(matcherC.group(1) + ".0")));
			} else {
				coords.put("coordinate", new Double(new Float(matcherC.group(1) + "." + matcherC.group(3))));
			}
		} else if (matcherD.find() == true && matcherD.groupCount() > 0) {
			if (matcherD.group(1).equalsIgnoreCase("N") || matcherD.group(1).equalsIgnoreCase("E")) {
				latlonNegative = 1;
			} else {
				latlonNegative = -1;
			}

			coords.put("coordinate", new Double(latlonNegative * (new Double(matcherB.group(2)))));
		} else if (matcherE.find() == true && matcherE.groupCount() > 0) {
			coords.put("coordinate", new Double(matcherE.group(1)));
		} else if (matcherF.find() == true && matcherF.groupCount() > 0) {
			if (matcherF.group(1).equalsIgnoreCase("N") || matcherF.group(1).equalsIgnoreCase("E")) {
				latlonNegative = 1;
			} else {
				latlonNegative = -1;
			}

			coords.put("coordinate", new Double(latlonNegative * (new Double(matcherB.group(2)))));
		} else {
			return null;
		}

		if (matcher0.find() == true && matcher0.groupCount() > 0) {
			String tmpDir = null;
			Float tmpCoord;
			if (matcher0.groupCount() < 3 || matcher0.group(3) == null) {
				tmpCoord = new Float("0.0");
			} else {
				tmpCoord = new Float("0." + matcher0.group(3));
			}

			if (latlon.equalsIgnoreCase("lat")) {
				if (matcher0.group(1).equals("+")) {
					tmpDir = "N";
				}
				if (matcher0.group(1).equals("-")) {
					tmpDir = "S";
				}
			} else if (latlon.equalsIgnoreCase("lon")) {
				if (matcher0.group(1).equals("+")) {
					tmpDir = "E";
				}
				if (matcher0.group(1).equals("-")) {
					tmpDir = "W";
				}
			}

			coords.put("string", tmpDir + " " + matcher0.group(1) + "° " + (Math.round(tmpCoord / (1 / 60) * 1000) * 1000));

			return coords;
		} else {
			return new HashMap<String, Object>();
		}
	}

	public Long searchByNextPage(cgSearchThread thread, Long searchId, int reason, boolean showCaptcha) {
		final String viewstate = app.getViewstate(searchId);
		final String viewstate1 = app.getViewstate1(searchId);
		cgCacheWrap caches = new cgCacheWrap();
		String url = app.getUrl(searchId);

		if (url == null || url.length() == 0) {
			Log.e(cgSettings.tag, "cgeoBase.searchByNextPage: No url found");
			return searchId;
		}

		if (viewstate == null || viewstate.length() == 0) {
			Log.e(cgSettings.tag, "cgeoBase.searchByNextPage: No viewstate given");
			return searchId;
		}

		String host = "www.geocaching.com";
		String path = "/";
		final String method = "POST";

		int dash = -1;
		if (url.indexOf("http://") > -1) {
			url = url.substring(7);
		}

		dash = url.indexOf("/");
		if (dash > -1) {
			host = url.substring(0, dash);
			url = url.substring(dash);
		} else {
			host = url;
			url = "";
		}

		dash = url.indexOf("?");
		if (dash > -1) {
			path = url.substring(0, dash);
		} else {
			path = url;
		}

		final HashMap<String, String> params = new HashMap<String, String>();
		params.put("__VIEWSTATE", viewstate);
		if (viewstate1 != null) {
			params.put("__VIEWSTATE1", viewstate1);
			params.put("__VIEWSTATEFIELDCOUNT", "2");
		}
		params.put("__EVENTTARGET", "ctl00$ContentBody$pgrBottom$ctl08");
		params.put("__EVENTARGUMENT", "");

		String page = request(false, host, path, method, params, false, false, true).getData();
		if (checkLogin(page) == false) {
			int loginState = login();
			if (loginState == 1) {
				page = request(false, host, path, method, params, false, false, true).getData();
			} else if (loginState == -3) {
				Log.i(cgSettings.tag, "Working as guest.");
			} else {
				app.setError(searchId, errorRetrieve.get(loginState));
				Log.e(cgSettings.tag, "cgeoBase.searchByNextPage: Can not log in geocaching");
				return searchId;
			}
		}

		if (page == null || page.length() == 0) {
			Log.e(cgSettings.tag, "cgeoBase.searchByNextPage: No data from server");
			return searchId;
		}

		caches = parseSearch(thread, url, page, showCaptcha);
		if (caches == null || caches.cacheList == null || caches.cacheList.isEmpty()) {
			Log.e(cgSettings.tag, "cgeoBase.searchByNextPage: No cache parsed");
			return searchId;
		}

		// save to application
		app.setError(searchId, caches.error);
		app.setViewstate(searchId, caches.viewstate);
		app.setViewstate1(searchId, caches.viewstate1);

		final ArrayList<cgCache> cacheList = new ArrayList<cgCache>();
		for (cgCache cache : caches.cacheList) {
			app.addGeocode(searchId, cache.geocode);
			cacheList.add(cache);
		}

		app.addSearch(searchId, cacheList, true, reason);

		return searchId;
	}

	public Long searchByGeocode(HashMap<String, String> parameters, int reason, boolean forceReload) {
		final cgSearch search = new cgSearch();
		String geocode = parameters.get("geocode");
		String guid = parameters.get("guid");

		if ((geocode == null || geocode.length() == 0) && ((guid == null || guid.length() == 0))) {
			Log.e(cgSettings.tag, "cgeoBase.searchByGeocode: No geocode nor guid given");
			return null;
		}

		if (forceReload == false && reason == 0 && (app.isOffline(geocode, guid) == true || app.isThere(geocode, guid, true, true) == true)) {
			if ((geocode == null || geocode.length() == 0) && guid != null && guid.length() > 0) {
				geocode = app.getGeocode(guid);
			}

			ArrayList<cgCache> cacheList = new ArrayList<cgCache>();
			cacheList.add(app.getCacheByGeocode(geocode, true, true, true, true, true, true));
			search.addGeocode(geocode);

			app.addSearch(search, cacheList, false, reason);

			cacheList.clear();
			cacheList = null;

			return search.getCurrentId();
		}

		final String host = "www.geocaching.com";
		final String path = "/seek/cache_details.aspx";
		final String method = "GET";
		final HashMap<String, String> params = new HashMap<String, String>();
		if (geocode != null && geocode.length() > 0) {
			params.put("wp", geocode);
		} else if (guid != null && guid.length() > 0) {
			params.put("guid", guid);
		}
		params.put("decrypt", "y");
		params.put("log", "y"); // download logs (more than 5
		params.put("numlogs", "35"); // 35 logs

		String page = requestLogged(false, host, path, method, params, false, false, false);

		if (page == null || page.length() == 0) {
			if (app.isThere(geocode, guid, true, false) == true) {
				if ((geocode == null || geocode.length() == 0) && guid != null && guid.length() > 0) {
					Log.i(cgSettings.tag, "Loading old cache from cache.");

					geocode = app.getGeocode(guid);
				}

				final ArrayList<cgCache> cacheList = new ArrayList<cgCache>();
				cacheList.add(app.getCacheByGeocode(geocode));
				search.addGeocode(geocode);
				search.error = null;
				search.errorRetrieve = 0; // reset errors from previous failed request

				app.addSearch(search, cacheList, false, reason);

				cacheList.clear();

				return search.getCurrentId();
			}

			Log.e(cgSettings.tag, "cgeoBase.searchByGeocode: No data from server");
			return null;
		}

		final cgCacheWrap caches = parseCache(page, reason);
		if (caches == null || caches.cacheList == null || caches.cacheList.isEmpty()) {
			if (caches != null && caches.error != null && caches.error.length() > 0) {
				search.error = caches.error;
			}
			if (caches != null && caches.url != null && caches.url.length() > 0) {
				search.url = caches.url;
			}

			app.addSearch(search, null, true, reason);

			Log.e(cgSettings.tag, "cgeoBase.searchByGeocode: No cache parsed");
			return null;
		}

		if (app == null) {
			Log.e(cgSettings.tag, "cgeoBase.searchByGeocode: No application found");
			return null;
		}

		final ArrayList<cgCache> cacheList = new ArrayList<cgCache>();
		if (caches != null) {
			if (caches.error != null && caches.error.length() > 0) {
				search.error = caches.error;
			}
			if (caches.url != null && caches.url.length() > 0) {
				search.url = caches.url;
			}
			if (caches.viewstate != null && caches.viewstate.length() > 0) {
				search.viewstate = caches.viewstate;
			}
			if (caches.viewstate1 != null && caches.viewstate1.length() > 0) {
				search.viewstate1 = caches.viewstate1;
			}
			search.totalCnt = caches.totalCnt;

			for (cgCache cache : caches.cacheList) {
				search.addGeocode(cache.geocode);
				cacheList.add(cache);
			}
		}

		app.addSearch(search, cacheList, true, reason);

		page = null;
		cacheList.clear();

		return search.getCurrentId();
	}

	public Long searchByOffline(HashMap<String, Object> parameters) {
		if (app == null) {
			Log.e(cgSettings.tag, "cgeoBase.searchByOffline: No application found");
			return null;
		}

		Double latitude = null;
		Double longitude = null;
		String cachetype = null;
		Integer list = 1;

		if (parameters.containsKey("latitude") == true && parameters.containsKey("longitude") == true) {
			latitude = (Double) parameters.get("latitude");
			longitude = (Double) parameters.get("longitude");
		}

		if (parameters.containsKey("cachetype") == true) {
			cachetype = (String) parameters.get("cachetype");
		}

		if (parameters.containsKey("list") == true) {
			list = (Integer) parameters.get("list");
		}

		final cgSearch search = app.getBatchOfStoredCaches(true, latitude, longitude, cachetype, list);
		search.totalCnt = app.getAllStoredCachesCount(true, cachetype, list);

		return search.getCurrentId();
	}

	public Long searchByHistory(HashMap<String, Object> parameters) {
		if (app == null) {
			Log.e(cgSettings.tag, "cgeoBase.searchByHistory: No application found");
			return null;
		}

		String cachetype = null;

		if (parameters.containsKey("cachetype") == true) {
			cachetype = (String) parameters.get("cachetype");
		}

		final cgSearch search = app.getHistoryOfCaches(true, cachetype);
		search.totalCnt = app.getAllHistoricCachesCount(true, cachetype);

		return search.getCurrentId();
	}

	public Long searchByCoords(cgSearchThread thread, HashMap<String, String> parameters, int reason, boolean showCaptcha) {
		final cgSearch search = new cgSearch();
		final String latitude = parameters.get("latitude");
		final String longitude = parameters.get("longitude");
		cgCacheWrap caches = new cgCacheWrap();
		String cacheType = parameters.get("cachetype");

		if (latitude == null || latitude.length() == 0) {
			Log.e(cgSettings.tag, "cgeoBase.searchByCoords: No latitude given");
			return null;
		}

		if (longitude == null || longitude.length() == 0) {
			Log.e(cgSettings.tag, "cgeoBase.searchByCoords: No longitude given");
			return null;
		}

		if (cacheType != null && cacheType.length() == 0) {
			cacheType = null;
		}

		final String host = "www.geocaching.com";
		final String path = "/seek/nearest.aspx";
		final String method = "GET";
		final HashMap<String, String> params = new HashMap<String, String>();
		if (cacheType != null && cacheIDs.containsKey(cacheType) == true) {
			params.put("tx", cacheIDs.get(cacheType));
		} else {
			params.put("tx", cacheIDs.get("all"));
		}
		params.put("lat", latitude);
		params.put("lng", longitude);

		final String url = "http://" + host + path + "?" + prepareParameters(params, false, true);
		String page = requestLogged(false, host, path, method, params, false, false, true);

		if (page == null || page.length() == 0) {
			Log.e(cgSettings.tag, "cgeoBase.searchByCoords: No data from server");
			return null;
		}

		caches = parseSearch(thread, url, page, showCaptcha);
		if (caches == null || caches.cacheList == null || caches.cacheList.isEmpty()) {
			Log.e(cgSettings.tag, "cgeoBase.searchByCoords: No cache parsed");
		}

		if (app == null) {
			Log.e(cgSettings.tag, "cgeoBase.searchByCoords: No application found");
			return null;
		}

		final ArrayList<cgCache> cacheList = new ArrayList<cgCache>();
		if (caches != null) {
			if (caches.error != null && caches.error.length() > 0) {
				search.error = caches.error;
			}
			if (caches.url != null && caches.url.length() > 0) {
				search.url = caches.url;
			}
			if (caches.viewstate != null && caches.viewstate.length() > 0) {
				search.viewstate = caches.viewstate;
			}
			if (caches.viewstate1 != null && caches.viewstate1.length() > 0) {
				search.viewstate1 = caches.viewstate1;
			}
			search.totalCnt = caches.totalCnt;

			for (cgCache cache : caches.cacheList) {
				if (settings.excludeDisabled == 0 || (settings.excludeDisabled == 1 && cache.disabled == false)) {
					search.addGeocode(cache.geocode);
					cacheList.add(cache);
				}
			}
		}

		app.addSearch(search, cacheList, true, reason);

		return search.getCurrentId();
	}

	public Long searchByKeyword(cgSearchThread thread, HashMap<String, String> parameters, int reason, boolean showCaptcha) {
		final cgSearch search = new cgSearch();
		final String keyword = parameters.get("keyword");
		cgCacheWrap caches = new cgCacheWrap();
		String cacheType = parameters.get("cachetype");

		if (keyword == null || keyword.length() == 0) {
			Log.e(cgSettings.tag, "cgeoBase.searchByKeyword: No keyword given");
			return null;
		}

		if (cacheType != null && cacheType.length() == 0) {
			cacheType = null;
		}

		final String host = "www.geocaching.com";
		final String path = "/seek/nearest.aspx";
		final String method = "GET";
		final HashMap<String, String> params = new HashMap<String, String>();
		if (cacheType != null && cacheIDs.containsKey(cacheType) == true) {
			params.put("tx", cacheIDs.get(cacheType));
		} else {
			params.put("tx", cacheIDs.get("all"));
		}
		params.put("key", keyword);

		final String url = "http://" + host + path + "?" + prepareParameters(params, false, true);
		String page = requestLogged(false, host, path, method, params, false, false, true);

		if (page == null || page.length() == 0) {
			Log.e(cgSettings.tag, "cgeoBase.searchByKeyword: No data from server");
			return null;
		}

		caches = parseSearch(thread, url, page, showCaptcha);
		if (caches == null || caches.cacheList == null || caches.cacheList.isEmpty()) {
			Log.e(cgSettings.tag, "cgeoBase.searchByKeyword: No cache parsed");
		}

		if (app == null) {
			Log.e(cgSettings.tag, "cgeoBase.searchByCoords: No application found");
			return null;
		}

		final ArrayList<cgCache> cacheList = new ArrayList<cgCache>();
		if (caches != null) {
			if (caches.error != null && caches.error.length() > 0) {
				search.error = caches.error;
			}
			if (caches.url != null && caches.url.length() > 0) {
				search.url = caches.url;
			}
			if (caches.viewstate != null && caches.viewstate.length() > 0) {
				search.viewstate = caches.viewstate;
			}
			if (caches.viewstate1 != null && caches.viewstate1.length() > 0) {
				search.viewstate1 = caches.viewstate1;
			}
			search.totalCnt = caches.totalCnt;

			for (cgCache cache : caches.cacheList) {
				if (settings.excludeDisabled == 0 || (settings.excludeDisabled == 1 && cache.disabled == false)) {
					search.addGeocode(cache.geocode);
					cacheList.add(cache);
				}
			}
		}

		app.addSearch(search, cacheList, true, reason);

		return search.getCurrentId();
	}

	public Long searchByUsername(cgSearchThread thread, HashMap<String, String> parameters, int reason, boolean showCaptcha) {
		final cgSearch search = new cgSearch();
		final String userName = parameters.get("username");
		cgCacheWrap caches = new cgCacheWrap();
		String cacheType = parameters.get("cachetype");

		if (userName == null || userName.length() == 0) {
			Log.e(cgSettings.tag, "cgeoBase.searchByUsername: No user name given");
			return null;
		}

		if (cacheType != null && cacheType.length() == 0) {
			cacheType = null;
		}

		final String host = "www.geocaching.com";
		final String path = "/seek/nearest.aspx";
		final String method = "GET";
		final HashMap<String, String> params = new HashMap<String, String>();
		if (cacheType != null && cacheIDs.containsKey(cacheType) == true) {
			params.put("tx", cacheIDs.get(cacheType));
		} else {
			params.put("tx", cacheIDs.get("all"));
		}
		params.put("ul", userName);

		boolean my = false;
		if (userName.equalsIgnoreCase(settings.getLogin().get("username")) == true) {
			my = true;
			Log.i(cgSettings.tag, "cgBase.searchByUsername: Overriding users choice, downloading all caches.");
		}

		final String url = "http://" + host + path + "?" + prepareParameters(params, my, true);
		String page = requestLogged(false, host, path, method, params, false, my, true);

		if (page == null || page.length() == 0) {
			Log.e(cgSettings.tag, "cgeoBase.searchByUsername: No data from server");
			return null;
		}

		caches = parseSearch(thread, url, page, showCaptcha);
		if (caches == null || caches.cacheList == null || caches.cacheList.isEmpty()) {
			Log.e(cgSettings.tag, "cgeoBase.searchByUsername: No cache parsed");
		}

		if (app == null) {
			Log.e(cgSettings.tag, "cgeoBase.searchByCoords: No application found");
			return null;
		}

		final ArrayList<cgCache> cacheList = new ArrayList<cgCache>();
		if (caches != null) {
			if (caches.error != null && caches.error.length() > 0) {
				search.error = caches.error;
			}
			if (caches.url != null && caches.url.length() > 0) {
				search.url = caches.url;
			}
			if (caches.viewstate != null && caches.viewstate.length() > 0) {
				search.viewstate = caches.viewstate;
			}
			if (caches.viewstate1 != null && caches.viewstate1.length() > 0) {
				search.viewstate1 = caches.viewstate1;
			}
			search.totalCnt = caches.totalCnt;

			for (cgCache cache : caches.cacheList) {
				if (settings.excludeDisabled == 0 || (settings.excludeDisabled == 1 && cache.disabled == false)) {
					search.addGeocode(cache.geocode);
					cacheList.add(cache);
				}
			}
		}

		app.addSearch(search, cacheList, true, reason);

		return search.getCurrentId();
	}

	public Long searchByOwner(cgSearchThread thread, HashMap<String, String> parameters, int reason, boolean showCaptcha) {
		final cgSearch search = new cgSearch();
		final String userName = parameters.get("username");
		cgCacheWrap caches = new cgCacheWrap();
		String cacheType = parameters.get("cachetype");

		if (userName == null || userName.length() == 0) {
			Log.e(cgSettings.tag, "cgeoBase.searchByOwner: No user name given");
			return null;
		}

		if (cacheType != null && cacheType.length() == 0) {
			cacheType = null;
		}

		final String host = "www.geocaching.com";
		final String path = "/seek/nearest.aspx";
		final String method = "GET";
		final HashMap<String, String> params = new HashMap<String, String>();
		if (cacheType != null && cacheIDs.containsKey(cacheType) == true) {
			params.put("tx", cacheIDs.get(cacheType));
		} else {
			params.put("tx", cacheIDs.get("all"));
		}
		params.put("u", userName);

		final String url = "http://" + host + path + "?" + prepareParameters(params, false, true);
		String page = requestLogged(false, host, path, method, params, false, false, true);

		if (page == null || page.length() == 0) {
			Log.e(cgSettings.tag, "cgeoBase.searchByOwner: No data from server");
			return null;
		}

		caches = parseSearch(thread, url, page, showCaptcha);
		if (caches == null || caches.cacheList == null || caches.cacheList.isEmpty()) {
			Log.e(cgSettings.tag, "cgeoBase.searchByOwner: No cache parsed");
		}

		if (app == null) {
			Log.e(cgSettings.tag, "cgeoBase.searchByCoords: No application found");
			return null;
		}

		final ArrayList<cgCache> cacheList = new ArrayList<cgCache>();
		if (caches != null) {
			if (caches.error != null && caches.error.length() > 0) {
				search.error = caches.error;
			}
			if (caches.url != null && caches.url.length() > 0) {
				search.url = caches.url;
			}
			if (caches.viewstate != null && caches.viewstate.length() > 0) {
				search.viewstate = caches.viewstate;
			}
			if (caches.viewstate1 != null && caches.viewstate1.length() > 0) {
				search.viewstate1 = caches.viewstate1;
			}
			search.totalCnt = caches.totalCnt;

			for (cgCache cache : caches.cacheList) {
				if (settings.excludeDisabled == 0 || (settings.excludeDisabled == 1 && cache.disabled == false)) {
					search.addGeocode(cache.geocode);
					cacheList.add(cache);
				}
			}
		}

		app.addSearch(search, cacheList, true, reason);

		return search.getCurrentId();
	}

	public Long searchByViewport(HashMap<String, String> parameters, int reason) {
		final cgSearch search = new cgSearch();
		final String latMin = parameters.get("latitude-min");
		final String latMax = parameters.get("latitude-max");
		final String lonMin = parameters.get("longitude-min");
		final String lonMax = parameters.get("longitude-max");

		String usertoken = null;
		if (parameters.get("usertoken") != null) {
			usertoken = parameters.get("usertoken");
		} else {
			usertoken = "";
		}
		cgCacheWrap caches = new cgCacheWrap();

		String page = null;

		if (latMin == null || latMin.length() == 0 || latMax == null || latMax.length() == 0 || lonMin == null || lonMin.length() == 0 || lonMax == null || lonMax.length() == 0) {
			Log.e(cgSettings.tag, "cgeoBase.searchByViewport: Not enough parameters to recognize viewport");
			return null;
		}

		final String host = "www.geocaching.com";
		final String path = "/map/default.aspx/MapAction";

		String params = "{\"dto\":{\"data\":{\"c\":1,\"m\":\"\",\"d\":\"" + latMax + "|" + latMin + "|" + lonMax + "|" + lonMin + "\"},\"ut\":\"" + usertoken + "\"}}";

		final String url = "http://" + host + path + "?" + params;
		page = requestJSONgc(host, path, params);

		if (page == null || page.length() == 0) {
			Log.e(cgSettings.tag, "cgeoBase.searchByViewport: No data from server");
			return null;
		}

		caches = parseMapJSON(url, page);
		if (caches == null || caches.cacheList == null || caches.cacheList.isEmpty()) {
			Log.e(cgSettings.tag, "cgeoBase.searchByViewport: No cache parsed");
		}

		if (app == null) {
			Log.e(cgSettings.tag, "cgeoBase.searchByViewport: No application found");
			return null;
		}

		final ArrayList<cgCache> cacheList = new ArrayList<cgCache>();
		if (caches != null) {
			if (caches.error != null && caches.error.length() > 0) {
				search.error = caches.error;
			}
			if (caches.url != null && caches.url.length() > 0) {
				search.url = caches.url;
			}
			if (caches.viewstate != null && caches.viewstate.length() > 0) {
				search.viewstate = caches.viewstate;
			}
			if (caches.viewstate1 != null && caches.viewstate1.length() > 0) {
				search.viewstate1 = caches.viewstate1;
			}
			search.totalCnt = caches.totalCnt;

			if (caches.cacheList != null && caches.cacheList.size() > 0) {
				for (cgCache cache : caches.cacheList) {
					if ((settings.excludeDisabled == 0 || (settings.excludeDisabled == 1 && cache.disabled == false))
							&& (settings.excludeMine == 0 || (settings.excludeMine == 1 && cache.own == false))
							&& (settings.excludeMine == 0 || (settings.excludeMine == 1 && cache.found == false))
							&& (settings.cacheType == null || (settings.cacheType.equals(cache.type) == true))) {
						search.addGeocode(cache.geocode);
						cacheList.add(cache);
					}
				}
			}
		}

		app.addSearch(search, cacheList, true, reason);

		return search.getCurrentId();
	}

	public ArrayList<cgUser> getGeocachersInViewport(String username, Double latMin, Double latMax, Double lonMin, Double lonMax) {
		final ArrayList<cgUser> users = new ArrayList<cgUser>();

		if (username == null) {
			return users;
		}
		if (latMin == null || latMax == null || lonMin == null || lonMax == null) {
			return users;
		}

		final String host = "api.go4cache.com";
		final String path = "/get.php";
		final String method = "POST";
		final HashMap<String, String> params = new HashMap<String, String>();

		params.put("u", username);
		params.put("ltm", String.format((Locale) null, "%.6f", latMin));
		params.put("ltx", String.format((Locale) null, "%.6f", latMax));
		params.put("lnm", String.format((Locale) null, "%.6f", lonMin));
		params.put("lnx", String.format((Locale) null, "%.6f", lonMax));

		final String data = request(false, host, path, method, params, false, false, false).getData();

		if (data == null || data.length() == 0) {
			Log.e(cgSettings.tag, "cgeoBase.getGeocachersInViewport: No data from server");
			return null;
		}

		try {
			final JSONObject dataJSON = new JSONObject(data);

			final JSONArray usersData = dataJSON.getJSONArray("users");
			if (usersData != null && usersData.length() > 0) {
				int count = usersData.length();
				JSONObject oneUser = null;
				for (int i = 0; i < count; i++) {
					final cgUser user = new cgUser();
					oneUser = usersData.getJSONObject(i);
					if (oneUser != null) {
						final String located = oneUser.getString("located");
						if (located != null) {
							user.located = dateSqlIn.parse(located);
						} else {
							user.located = new Date();
						}
						user.username = oneUser.getString("user");
						user.latitude = oneUser.getDouble("latitude");
						user.longitude = oneUser.getDouble("longitude");
						user.action = oneUser.getString("action");
						user.client = oneUser.getString("client");

						if (user.latitude != null && user.longitude != null) {
							users.add(user);
						}
					}
				}
			}
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgBase.getGeocachersInViewport: " + e.toString());
		}

		return users;
	}

	public cgTrackable searchTrackable(HashMap<String, String> parameters) {
		final String geocode = parameters.get("geocode");
		final String guid = parameters.get("guid");
		final String id = parameters.get("id");
		cgTrackable trackable = new cgTrackable();

		if ((geocode == null || geocode.length() == 0) && (guid == null || guid.length() == 0) && (id == null || id.length() == 0)) {
			Log.e(cgSettings.tag, "cgeoBase.searchTrackable: No geocode nor guid nor id given");
			return null;
		}

		final String host = "www.geocaching.com";
		final String path = "/track/details.aspx";
		final String method = "GET";
		final HashMap<String, String> params = new HashMap<String, String>();
		if (geocode != null && geocode.length() > 0) {
			params.put("tracker", geocode);
		} else if (guid != null && guid.length() > 0) {
			params.put("guid", guid);
		} else if (id != null && id.length() > 0) {
			params.put("id", id);
		}

		String page = requestLogged(false, host, path, method, params, false, false, false);

		if (page == null || page.length() == 0) {
			Log.e(cgSettings.tag, "cgeoBase.searchTrackable: No data from server");
			return trackable;
		}

		trackable = parseTrackable(page);
		if (trackable == null) {
			Log.e(cgSettings.tag, "cgeoBase.searchTrackable: No trackable parsed");
			return trackable;
		}

		return trackable;
	}

	public int postLog(cgeoapplication app, String geocode, String cacheid, String viewstate, String viewstate1, int logType, int year, int month, int day, String log, ArrayList<cgTrackableLog> trackables) {
		if (viewstate == null || viewstate.length() == 0) {
			Log.e(cgSettings.tag, "cgeoBase.postLog: No viewstate given");
			return 1000;
		}

		if (logTypes2.containsKey(logType) == false) {
			Log.e(cgSettings.tag, "cgeoBase.postLog: Unknown logtype");
			return 1000;
		}

		if (log == null || log.length() == 0) {
			Log.e(cgSettings.tag, "cgeoBase.postLog: No log text given");
			return 1001;
		}

		// fix log (non-Latin characters converted to HTML entities)
		final int logLen = log.length();
		final StringBuilder logUpdated = new StringBuilder();

		for (int i = 0; i < logLen; i++) {
			char c = log.charAt(i);

			if (c > 300) {
				logUpdated.append("&#");
				logUpdated.append(Integer.toString((int) c));
				logUpdated.append(";");
			} else {
				logUpdated.append(c);
			}
		}
		log = logUpdated.toString();

		log = log.replace("\n", "\r\n"); // windows' eol

		if (trackables != null) {
			Log.i(cgSettings.tag, "Trying to post log for cache #" + cacheid + " - action: " + logType + "; date: " + year + "." + month + "." + day + ", log: " + log + "; trackables: " + trackables.size());
		} else {
			Log.i(cgSettings.tag, "Trying to post log for cache #" + cacheid + " - action: " + logType + "; date: " + year + "." + month + "." + day + ", log: " + log + "; trackables: 0");
		}

		final String host = "www.geocaching.com";
		final String path = "/seek/log.aspx?ID=" + cacheid;
		final String method = "POST";
		final HashMap<String, String> params = new HashMap<String, String>();

		params.put("__VIEWSTATE", viewstate);
		if (viewstate1 != null) {
			params.put("__VIEWSTATE1", viewstate1);
			params.put("__VIEWSTATEFIELDCOUNT", "2");
		}
		params.put("__EVENTTARGET", "");
		params.put("__EVENTARGUMENT", "");
		params.put("__LASTFOCUS", "");
		params.put("ctl00$ContentBody$LogBookPanel1$ddLogType", Integer.toString(logType));
		params.put("ctl00$ContentBody$LogBookPanel1$DateTimeLogged", String.format("%02d", month) + "/" + String.format("%02d", day) + "/" + String.format("%04d", year));
		params.put("ctl00$ContentBody$LogBookPanel1$DateTimeLogged$Month", Integer.toString(month));
		params.put("ctl00$ContentBody$LogBookPanel1$DateTimeLogged$Day", Integer.toString(day));
		params.put("ctl00$ContentBody$LogBookPanel1$DateTimeLogged$Year", Integer.toString(year));
		params.put("ctl00$ContentBody$LogBookPanel1$uxLogInfo", log);
		params.put("ctl00$ContentBody$LogBookPanel1$LogButton", "Submit Log Entry");
		params.put("ctl00$ContentBody$uxVistOtherListingGC", "");
		if (trackables != null && trackables.isEmpty() == false) { //  we have some trackables to proceed
			final StringBuilder hdnSelected = new StringBuilder();

			for (cgTrackableLog tb : trackables) {
				final String action = Integer.toString(tb.id) + logTypesTrackableAction.get(tb.action);

				if (tb.action > 0) {
					hdnSelected.append(action);
					hdnSelected.append(",");
				}
			}

			params.put("ctl00$ContentBody$LogBookPanel1$uxTrackables$hdnSelectedActions", hdnSelected.toString()); // selected trackables
			params.put("ctl00$ContentBody$LogBookPanel1$uxTrackables$hdnCurrentFilter", "");
		}

		String page = request(false, host, path, method, params, false, false, false).getData();
		if (checkLogin(page) == false) {
			int loginState = login();
			if (loginState == 1) {
				page = request(false, host, path, method, params, false, false, false).getData();
			} else {
				Log.e(cgSettings.tag, "cgeoBase.postLog: Can not log in geocaching (error: " + loginState + ")");
				return loginState;
			}
		}

		if (page == null || page.length() == 0) {
			Log.e(cgSettings.tag, "cgeoBase.postLog: No data from server");
			return 1002;
		}

		// maintenance, archived needs to be confirmed
		final Pattern pattern = Pattern.compile("<span id=\"ctl00_ContentBody_LogBookPanel1_lbConfirm\"[^>]*>([^<]*<font[^>]*>)?([^<]+)(</font>[^<]*)?</span>", Pattern.CASE_INSENSITIVE);
		final Matcher matcher = pattern.matcher(page);

		try {
			if (matcher.find() == true && matcher.groupCount() > 0) {
				final String viewstateConfirm = findViewstate(page, 0);
				final String viewstate1Confirm = findViewstate(page, 1);

				if (viewstateConfirm == null || viewstateConfirm.length() == 0) {
					Log.e(cgSettings.tag, "cgeoBase.postLog: No viewstate for confirm log");
					return 1000;
				}

				params.clear();
				params.put("__VIEWSTATE", viewstateConfirm);
				if (viewstate1 != null) {
					params.put("__VIEWSTATE1", viewstate1Confirm);
					params.put("__VIEWSTATEFIELDCOUNT", "2");
				}
				params.put("__EVENTTARGET", "");
				params.put("__EVENTARGUMENT", "");
				params.put("__LASTFOCUS", "");
				params.put("ctl00$ContentBody$LogBookPanel1$btnConfirm", "Yes");
				params.put("ctl00$ContentBody$LogBookPanel1$uxLogInfo", log);
				params.put("ctl00$ContentBody$uxVistOtherListingGC", "");
				if (trackables != null && trackables.isEmpty() == false) { //  we have some trackables to proceed
					final StringBuilder hdnSelected = new StringBuilder();

					for (cgTrackableLog tb : trackables) {
						String ctl = null;
						final String action = Integer.toString(tb.id) + logTypesTrackableAction.get(tb.action);

						if (tb.ctl < 10) {
							ctl = "0" + Integer.toString(tb.ctl);
						} else {
							ctl = Integer.toString(tb.ctl);
						}

						params.put("ctl00$ContentBody$LogBookPanel1$uxTrackables$repTravelBugs$ctl" + ctl + "$ddlAction", action);
						if (tb.action > 0) {
							hdnSelected.append(action);
							hdnSelected.append(",");
						}
					}

					params.put("ctl00$ContentBody$LogBookPanel1$uxTrackables$hdnSelectedActions", hdnSelected.toString()); // selected trackables
					params.put("ctl00$ContentBody$LogBookPanel1$uxTrackables$hdnCurrentFilter", "");
				}

				page = request(false, host, path, method, params, false, false, false).getData();
			}
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgeoBase.postLog.confim: " + e.toString());
		}

		try {
			final Pattern patternOk = Pattern.compile("<h2[^>]*>[^<]*<span id=\"ctl00_ContentBody_lbHeading\"[^>]*>[^<]*</span>[^<]*</h2>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
			final Matcher matcherOk = patternOk.matcher(page);
			if (matcherOk.find() == true) {
				Log.i(cgSettings.tag, "Log successfully posted to cache #" + cacheid);

				if (app != null && geocode != null) {
					app.saveVisitDate(geocode);
				}

				return 1;
			}
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgeoBase.postLog.check: " + e.toString());
		}

		Log.e(cgSettings.tag, "cgeoBase.postLog: Failed to post log because of unknown error");
		return 1000;
	}

	public int postLogTrackable(String tbid, String trackingCode, String viewstate, String viewstate1, int logType, int year, int month, int day, String log) {
		if (viewstate == null || viewstate.length() == 0) {
			Log.e(cgSettings.tag, "cgeoBase.postLogTrackable: No viewstate given");
			return 1000;
		}

		if (logTypes2.containsKey(logType) == false) {
			Log.e(cgSettings.tag, "cgeoBase.postLogTrackable: Unknown logtype");
			return 1000;
		}

		if (log == null || log.length() == 0) {
			Log.e(cgSettings.tag, "cgeoBase.postLogTrackable: No log text given");
			return 1001;
		}

		Log.i(cgSettings.tag, "Trying to post log for trackable #" + trackingCode + " - action: " + logType + "; date: " + year + "." + month + "." + day + ", log: " + log);

		log = log.replace("\n", "\r\n"); // windows' eol

		final Calendar currentDate = Calendar.getInstance();
		final String host = "www.geocaching.com";
		final String path = "/track/log.aspx?wid=" + tbid;
		final String method = "POST";
		final HashMap<String, String> params = new HashMap<String, String>();

		params.put("__VIEWSTATE", viewstate);
		if (viewstate1 != null) {
			params.put("__VIEWSTATE1", viewstate1);
			params.put("__VIEWSTATEFIELDCOUNT", "2");
		}
		params.put("__EVENTTARGET", "");
		params.put("__EVENTARGUMENT", "");
		params.put("__LASTFOCUS", "");
		params.put("ctl00$ContentBody$LogBookPanel1$ddLogType", Integer.toString(logType));
		params.put("ctl00$ContentBody$LogBookPanel1$tbCode", trackingCode);
		if (currentDate.get(Calendar.YEAR) == year && (currentDate.get(Calendar.MONTH) + 1) == month && currentDate.get(Calendar.DATE) == day) {
			params.put("ctl00$ContentBody$LogBookPanel1$DateTimeLogged", "");
		} else {
			params.put("ctl00$ContentBody$LogBookPanel1$DateTimeLogged", Integer.toString(month) + "/" + Integer.toString(day) + "/" + Integer.toString(year));
		}
		params.put("ctl00$ContentBody$LogBookPanel1$DateTimeLogged$Day", Integer.toString(day));
		params.put("ctl00$ContentBody$LogBookPanel1$DateTimeLogged$Month", Integer.toString(month));
		params.put("ctl00$ContentBody$LogBookPanel1$DateTimeLogged$Year", Integer.toString(year));
		params.put("ctl00$ContentBody$LogBookPanel1$uxLogInfo", log);
		params.put("ctl00$ContentBody$LogBookPanel1$LogButton", "Submit Log Entry");
		params.put("ctl00$ContentBody$uxVistOtherListingGC", "");

		String page = request(false, host, path, method, params, false, false, false).getData();
		if (checkLogin(page) == false) {
			int loginState = login();
			if (loginState == 1) {
				page = request(false, host, path, method, params, false, false, false).getData();
			} else {
				Log.e(cgSettings.tag, "cgeoBase.postLogTrackable: Can not log in geocaching (error: " + loginState + ")");
				return loginState;
			}
		}

		if (page == null || page.length() == 0) {
			Log.e(cgSettings.tag, "cgeoBase.postLogTrackable: No data from server");
			return 1002;
		}

		try {
			final Pattern patternOk = Pattern.compile("<div id=[\"|']ctl00_ContentBody_LogBookPanel1_ViewLogPanel[\"|']>", Pattern.CASE_INSENSITIVE);
			final Matcher matcherOk = patternOk.matcher(page);
			if (matcherOk.find() == true) {
				Log.i(cgSettings.tag, "Log successfully posted to trackable #" + trackingCode);
				return 1;
			}
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgeoBase.postLogTrackable.check: " + e.toString());
		}

		Log.e(cgSettings.tag, "cgeoBase.postLogTrackable: Failed to post log because of unknown error");
		return 1000;
	}

	final public static HostnameVerifier doNotVerify = new HostnameVerifier() {

		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};

	public static void trustAllHosts() {
		TrustManager[] trustAllCerts = new TrustManager[]{
			new X509TrustManager() {

				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return new java.security.cert.X509Certificate[]{};
				}

				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}
			}
		};

		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgBase.trustAllHosts: " + e.toString());
		}
	}

	public void postTweetCache(cgeoapplication app, cgSettings settings, String geocode) {
		final cgCache cache = app.getCacheByGeocode(geocode);
		String name = cache.name;
		if (name.length() > 84) {
			name = name.substring(0, 81) + "...";
		}
		final String status = "I found " + name + " (http://coord.info/" + cache.geocode.toUpperCase() + ")! #cgeo #geocaching"; // 56 chars + cache name

		postTweet(app, settings, status, null, null);
	}

	public void postTweetTrackable(cgeoapplication app, cgSettings settings, String geocode) {
		final cgTrackable trackable = app.getTrackableByGeocode(geocode);
		String name = trackable.name;
		if (name.length() > 82) {
			name = name.substring(0, 79) + "...";
		}
		final String status = "I touched " + name + " (http://coord.info/" + trackable.geocode.toUpperCase() + ")! #cgeo #geocaching"; // 58 chars + trackable name

		postTweet(app, settings, status, null, null);
	}

	public void postTweet(cgeoapplication app, cgSettings settings, String status, Double latitude, Double longitude) {
		if (app == null) {
			return;
		}
		if (settings == null || settings.tokenPublic == null || settings.tokenPublic.length() == 0 || settings.tokenSecret == null || settings.tokenSecret.length() == 0) {
			return;
		}

		try {
			HashMap<String, String> parameters = new HashMap<String, String>();

			parameters.put("status", status);
			if (latitude != null && longitude != null) {
				parameters.put("lat", String.format("%.6f", latitude));
				parameters.put("long", String.format("%.6f", longitude));
				parameters.put("display_coordinates", "true");
			}

			final String paramsDone = cgOAuth.signOAuth("api.twitter.com", "/1/statuses/update.json", "POST", false, parameters, settings.tokenPublic, settings.tokenSecret);

			HttpURLConnection connection = null;
			try {
				final StringBuffer buffer = new StringBuffer();
				final URL u = new URL("http://api.twitter.com/1/statuses/update.json");
				final URLConnection uc = u.openConnection();

				uc.setRequestProperty("Host", "api.twitter.com");

				connection = (HttpURLConnection) uc;
				connection.setReadTimeout(30000);
				connection.setRequestMethod("POST");
				HttpURLConnection.setFollowRedirects(true);
				connection.setDoInput(true);
				connection.setDoOutput(true);

				final OutputStream out = connection.getOutputStream();
				final OutputStreamWriter wr = new OutputStreamWriter(out);
				wr.write(paramsDone);
				wr.flush();
				wr.close();

				Log.i(cgSettings.tag, "Twitter.com: " + connection.getResponseCode() + " " + connection.getResponseMessage());

				InputStream ins;
				final String encoding = connection.getContentEncoding();

				if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
					ins = new GZIPInputStream(connection.getInputStream());
				} else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
					ins = new InflaterInputStream(connection.getInputStream(), new Inflater(true));
				} else {
					ins = connection.getInputStream();
				}

				final InputStreamReader inr = new InputStreamReader(ins);
				final BufferedReader br = new BufferedReader(inr);

				readIntoBuffer(br, buffer);

				br.close();
				ins.close();
				inr.close();
				connection.disconnect();
			} catch (IOException e) {
				Log.e(cgSettings.tag, "cgBase.postTweet.IO: " + connection.getResponseCode() + ": " + connection.getResponseMessage() + " ~ " + e.toString());

				final InputStream ins = connection.getErrorStream();
				final StringBuffer buffer = new StringBuffer();
				final InputStreamReader inr = new InputStreamReader(ins);
				final BufferedReader br = new BufferedReader(inr);

				readIntoBuffer(br, buffer);

				br.close();
				ins.close();
				inr.close();
			} catch (Exception e) {
				Log.e(cgSettings.tag, "cgBase.postTweet.inner: " + e.toString());
			}

			connection.disconnect();
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgBase.postTweet: " + e.toString());
		}
	}

	private void readIntoBuffer(BufferedReader br, StringBuffer buffer) throws IOException {
		int bufferSize = 1024*16;
		char[] bytes = new char[bufferSize];
		int bytesRead;
		while ((bytesRead = br.read(bytes)) > 0) {
			if (bytesRead == bufferSize) {
				buffer.append(bytes);
			}
			else {
				buffer.append(bytes, 0, bytesRead);
			}
		}
	}

	/*
	public ArrayList<String> translate(ArrayList<String> text, String target) {
		if (settings.translate == false) {
			return text;
		}

		String[] languages = null;
		if (settings.languages != null) {
			languages = settings.languages.split(" ");
		}

		ArrayList<String> translated = new ArrayList<String>();
		String language = null;

		if (text == null || text.isEmpty()) {
			return text;
		}

		// cut to 5000 characters (limitation of Google Translation API)
		for (String textOne : text) {
			int len = urlencode_rfc3986(textOne).length();
			if (len > 5000) {
				textOne = Html.fromHtml(textOne).toString();
				len = urlencode_rfc3986(textOne).length();

				if (len > 5000) {
					int cut = 2000;
					if (textOne.length() > cut) {
						cut = 1000;
					}

					textOne = textOne.substring(0, cut) + "...";
				}
			}
		}

		try {
			if (target == null) {
				final Locale locale = Locale.getDefault();
				target = locale.getLanguage();
			}

			final String scheme = "https://";
			final String host = "www.googleapis.com";
			final String path = "/language/translate/v2";

			final ArrayList<String> params = new ArrayList<String>();
			params.add("key=" + urlencode_rfc3986("AIzaSyAJH8x5etFHUbFifmgChlWoCVmwBFSwShQ"));
			params.add("target=" + urlencode_rfc3986(target));
			for (String textOne : text) {
				params.add("q=" + urlencode_rfc3986(textOne));
			}
			params.add("format=" + urlencode_rfc3986("html"));

			String page = requestJSON(scheme, host, path, "POST", implode("&", params.toArray()));

			if (page == null || page.length() == 0) {
				return text;
			}

			JSONObject json = new JSONObject(page);
			JSONObject jsonData = json.getJSONObject("data");
			JSONArray jsonTranslations = jsonData.getJSONArray("translations");
			int translationCnt = jsonTranslations.length();

			for (int i = 0; i < translationCnt; i ++) {
				JSONObject jsonTranslation = jsonTranslations.getJSONObject(i);
				language = jsonTranslation.getString("detectedSourceLanguage");

				boolean toTranslate = true;
				if (languages != null) {
					for (String lng : languages) {
						if (lng.equalsIgnoreCase(language)) {
							toTranslate = false;
						}
					}
				}

				if (toTranslate == false) {
					translated.add(text.get(i));
				} else {
					Log.i(cgSettings.tag, "Translating #" + i + ": " + language + ">" + target);
					translated.add(jsonTranslation.getString("translatedText"));
				}
			}
		} catch (Exception e) {
			Log.w(cgSettings.tag, "cgBase.translate: " + e.toString());
		}

		return translated;
	}
	*/

	public String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException e) {
			// nothing
		}

		return null;
	}

	public static String implode(String delim, Object[] array) {
		String out = "";

		try {
			for (int i = 0; i < array.length; i++) {
				if (i != 0) {
					out += delim;
				}
				out += array[i].toString();
			}
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgeoBase.implode: " + e.toString());
		}
		return out;
	}

	public static String urlencode_rfc3986(String text) {
		final String encoded = URLEncoder.encode(text).replace("+", "%20").replaceAll("%7E", "~");

		return encoded;
	}

	public String prepareParameters(HashMap<String, String> params, boolean my, boolean addF) {
		String paramsDone = null;

		if (my != true && settings.excludeMine > 0) {
			if (params == null) {
				params = new HashMap<String, String>();
			}
			if (addF == true) {
				params.put("f", "1");
			}

			Log.i(cgSettings.tag, "Skipping caches found or hidden by user.");
		}

		if (params != null) {
			Object[] keys = params.keySet().toArray();
			ArrayList<String> paramsEncoded = new ArrayList<String>();
			String key;
			String value;

			for (int i = 0; i < keys.length; i++) {
				key = (String) keys[i];
				value = (String) params.get(key);

				if (key.charAt(0) == '^') {
					key = "";
				}
				if (value == null) {
					value = "";
				}

				paramsEncoded.add(key + "=" + urlencode_rfc3986(value));
			}

			paramsDone = implode("&", paramsEncoded.toArray());
		} else {
			paramsDone = "";
		}

		return paramsDone;
	}

	public String requestViewstate(boolean secure, String host, String path, String method, HashMap<String, String> params, boolean xContentType, boolean my) {
		final cgResponse response = request(secure, host, path, method, params, xContentType, my, false);

		return findViewstate(response.getData(), 0);
	}

	public String requestViewstate1(boolean secure, String host, String path, String method, HashMap<String, String> params, boolean xContentType, boolean my) {
		final cgResponse response = request(secure, host, path, method, params, xContentType, my, false);

		return findViewstate(response.getData(), 1);
	}

	public String requestLogged(boolean secure, String host, String path, String method, HashMap<String, String> params, boolean xContentType, boolean my, boolean addF) {
		cgResponse response = request(secure, host, path, method, params, xContentType, my, addF);
		String data = response.getData();

		if (checkLogin(data) == false) {
			int loginState = login();
			if (loginState == 1) {
				response = request(secure, host, path, method, params, xContentType, my, addF);
				data = response.getData();
			} else {
				Log.i(cgSettings.tag, "Working as guest.");
			}
		}

		return data;
	}

	public cgResponse request(boolean secure, String host, String path, String method, HashMap<String, String> params, boolean xContentType, boolean my, boolean addF) {
		// prepare parameters
		final String paramsDone = prepareParameters(params, my, addF);

		return request(secure, host, path, method, paramsDone, 0, xContentType);
	}

	public cgResponse request(boolean secure, String host, String path, String method, HashMap<String, String> params, int requestId, boolean xContentType, boolean my, boolean addF) {
		// prepare parameters
		final String paramsDone = prepareParameters(params, my, addF);

		return request(secure, host, path, method, paramsDone, requestId, xContentType);
	}

	public cgResponse request(boolean secure, String host, String path, String method, String params, int requestId, Boolean xContentType) {
		URL u = null;
		int httpCode = -1;
		String httpMessage = null;
		String httpLocation = null;

		if (requestId == 0) {
			requestId = (int) (Math.random() * 1000);
		}

		if (method == null || (method.equalsIgnoreCase("GET") == false && method.equalsIgnoreCase("POST") == false)) {
			method = "POST";
		} else {
			method = method.toUpperCase();
		}

		// https
		String scheme = "http://";
		if (secure) {
			scheme = "https://";
		}

		// prepare cookies
		String cookiesDone = null;
		if (cookies == null || cookies.isEmpty() == true) {
			if (cookies == null) {
				cookies = new HashMap<String, String>();
			}

			final Map<String, ?> prefsAll = prefs.getAll();
			final Set<String> prefsKeys = prefsAll.keySet();

			for (String key : prefsKeys) {
				if (key.matches("cookie_.+") == true) {
					final String cookieKey = key.substring(7);
					final String cookieValue = (String) prefsAll.get(key);

					cookies.put(cookieKey, cookieValue);
				}
			}
		}

		if (cookies != null && !cookies.isEmpty() && cookies.keySet().size() > 0) {
			final Object[] keys = cookies.keySet().toArray();
			final ArrayList<String> cookiesEncoded = new ArrayList<String>();

			for (int i = 0; i < keys.length; i++) {
				String value = cookies.get(keys[i].toString());
				cookiesEncoded.add(keys[i] + "=" + value);
			}

			if (cookiesEncoded.size() > 0) {
				cookiesDone = implode("; ", cookiesEncoded.toArray());
			}
		}

		if (cookiesDone == null) {
			Map<String, ?> prefsValues = prefs.getAll();

			if (prefsValues != null && prefsValues.size() > 0 && prefsValues.keySet().size() > 0) {
				final Object[] keys = prefsValues.keySet().toArray();
				final ArrayList<String> cookiesEncoded = new ArrayList<String>();
				final int length = keys.length;

				for (int i = 0; i < length; i++) {
					if (keys[i].toString().length() > 7 && keys[i].toString().substring(0, 7).equals("cookie_") == true) {
						cookiesEncoded.add(keys[i].toString().substring(7) + "=" + prefsValues.get(keys[i].toString()));
					}
				}

				if (cookiesEncoded.size() > 0) {
					cookiesDone = implode("; ", cookiesEncoded.toArray());
				}
			}
		}

		if (cookiesDone == null) {
			cookiesDone = "";
		}

		URLConnection uc = null;
		HttpURLConnection connection = null;
		Integer timeout = 30000;
		StringBuffer buffer = null;

		for (int i = 0; i < 5; i++) {
			if (i > 0) {
				Log.w(cgSettings.tag, "Failed to download data, retrying. Attempt #" + (i + 1));
			}

			buffer = new StringBuffer();
			timeout = 30000 + (i * 10000);

			try {
				if (method.equals("GET")) {
					// GET
					u = new URL(scheme + host + path + "?" + params);
					uc = u.openConnection();

					uc.setRequestProperty("Host", host);
					uc.setRequestProperty("Cookie", cookiesDone);
					if (xContentType == true) {
						uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
					}

					if (settings.asBrowser == 1) {
						uc.setRequestProperty("Accept", "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
						// uc.setRequestProperty("Accept-Encoding", "gzip"); // not supported via cellular network
						uc.setRequestProperty("Accept-Charset", "utf-8, iso-8859-1, utf-16, *;q=0.7");
						uc.setRequestProperty("Accept-Language", "en-US");
						uc.setRequestProperty("User-Agent", idBrowser);
						uc.setRequestProperty("Connection", "keep-alive");
						uc.setRequestProperty("Keep-Alive", "300");
					}

					connection = (HttpURLConnection) uc;
					connection.setReadTimeout(timeout);
					connection.setRequestMethod(method);
					HttpURLConnection.setFollowRedirects(false);
					connection.setDoInput(true);
					connection.setDoOutput(false);
				} else {
					// POST
					u = new URL(scheme + host + path);
					uc = u.openConnection();

					uc.setRequestProperty("Host", host);
					uc.setRequestProperty("Cookie", cookiesDone);
					if (xContentType == true) {
						uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
					}

					if (settings.asBrowser == 1) {
						uc.setRequestProperty("Accept", "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
						// uc.setRequestProperty("Accept-Encoding", "gzip"); // not supported via cellular network
						uc.setRequestProperty("Accept-Charset", "utf-8, iso-8859-1, utf-16, *;q=0.7");
						uc.setRequestProperty("Accept-Language", "en-US");
						uc.setRequestProperty("User-Agent", idBrowser);
						uc.setRequestProperty("Connection", "keep-alive");
						uc.setRequestProperty("Keep-Alive", "300");
					}

					connection = (HttpURLConnection) uc;
					connection.setReadTimeout(timeout);
					connection.setRequestMethod(method);
					HttpURLConnection.setFollowRedirects(false);
					connection.setDoInput(true);
					connection.setDoOutput(true);

					final OutputStream out = connection.getOutputStream();
					final OutputStreamWriter wr = new OutputStreamWriter(out);
					wr.write(params);
					wr.flush();
					wr.close();
				}

				String headerName = null;
				final SharedPreferences.Editor prefsEditor = prefs.edit();
				for (int j = 1; (headerName = uc.getHeaderFieldKey(j)) != null; j++) {
					if (headerName != null && headerName.equalsIgnoreCase("Set-Cookie")) {
						int index;
						String cookie = uc.getHeaderField(j);

						index = cookie.indexOf(";");
						if (index > -1) {
							cookie = cookie.substring(0, cookie.indexOf(";"));
						}

						index = cookie.indexOf("=");
						if (index > - 1 && cookie.length() > (index + 1)) {
							String name = cookie.substring(0, cookie.indexOf("="));
							String value = cookie.substring(cookie.indexOf("=") + 1, cookie.length());

							cookies.put(name, value);
							prefsEditor.putString("cookie_" + name, value);
						}
					}
				}
				prefsEditor.commit();

				final String encoding = connection.getContentEncoding();
				InputStream ins;

				if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
					ins = new GZIPInputStream(connection.getInputStream());
				} else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
					ins = new InflaterInputStream(connection.getInputStream(), new Inflater(true));
				} else {
					ins = connection.getInputStream();
				}
				final InputStreamReader inr = new InputStreamReader(ins);
				final BufferedReader br = new BufferedReader(inr);

				readIntoBuffer(br, buffer);

				httpCode = connection.getResponseCode();
				httpMessage = connection.getResponseMessage();
				httpLocation = uc.getHeaderField("Location");

				final String paramsLog = params.replaceAll(passMatch, "password=***");
				if (buffer != null && connection != null) {
					Log.i(cgSettings.tag + "|" + requestId, "[" + method + " " + (int)(params.length() / 1024) +  "k | " + httpCode + " | " + (int)(buffer.length() / 1024) + "k] Downloaded " + scheme + host + path + "?" + paramsLog);
				} else {
					Log.i(cgSettings.tag + "|" + requestId, "[" + method + " | " + httpCode + "] Failed to download " + scheme + host + path + "?" + paramsLog);
				}

				connection.disconnect();
				br.close();
				ins.close();
				inr.close();
			} catch (IOException e) {
				Log.e(cgSettings.tag, "cgeoBase.request.IOException: " + e.toString());
			} catch (Exception e) {
				Log.e(cgSettings.tag, "cgeoBase.request: " + e.toString());
			}

			if (buffer != null && buffer.length() > 0) {
				break;
			}
		}

		cgResponse response = new cgResponse();
		String data = null;

		try {
			if (httpCode == 302 && httpLocation != null) {
				final Uri newLocation = Uri.parse(httpLocation);
				if (newLocation.isRelative() == true) {
					response = request(secure, host, path, "GET", new HashMap<String, String>(), requestId, false, false, false);
				} else {
					boolean secureRedir = false;
					if (newLocation.getScheme().equals("https")) {
						secureRedir = true;
					}
					response = request(secureRedir, newLocation.getHost(), newLocation.getPath(), "GET", new HashMap<String, String>(), requestId, false, false, false);
				}
			} else {
				if (buffer != null && buffer.length() > 0) {
					data = replaceWhitespace(buffer);
					buffer = null;

					if (data != null) {
						response.setData(data);
					} else {
						response.setData("");
					}
					response.setStatusCode(httpCode);
					response.setStatusMessage(httpMessage);
					response.setUrl(u.toString());
				}
			}
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgeoBase.page: " + e.toString());
		}

		return response;
	}

	private String replaceWhitespace(final StringBuffer buffer) {
		final int length = buffer.length();
		final char[] bytes = new char[length];
		buffer.getChars(0, length, bytes, 0);
		int resultSize = 0;
		boolean lastWasWhitespace = false;
		for (int i = 0; i < length; i++) {
			char c = bytes[i];
			if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
				if (!lastWasWhitespace) {
					bytes[resultSize++] =' ';
				}
				lastWasWhitespace = true;
			}
			else {
				bytes[resultSize++] = c;
				lastWasWhitespace = false;
			}
		}
		return new String(bytes, 0, resultSize);
	}

	public String requestJSONgc(String host, String path, String params) {
		int httpCode = -1;
		String httpLocation = null;

		// prepare cookies
		String cookiesDone = null;
		if (cookies == null || cookies.isEmpty() == true) {
			if (cookies == null) {
				cookies = new HashMap<String, String>();
			}

			final Map<String, ?> prefsAll = prefs.getAll();
			final Set<String> prefsKeys = prefsAll.keySet();

			for (String key : prefsKeys) {
				if (key.matches("cookie_.+") == true) {
					final String cookieKey = key.substring(7);
					final String cookieValue = (String) prefsAll.get(key);

					cookies.put(cookieKey, cookieValue);
				}
			}
		}

		if (cookies != null) {
			final Object[] keys = cookies.keySet().toArray();
			final ArrayList<String> cookiesEncoded = new ArrayList<String>();

			for (int i = 0; i < keys.length; i++) {
				String value = cookies.get(keys[i].toString());
				cookiesEncoded.add(keys[i] + "=" + value);
			}

			if (cookiesEncoded.size() > 0) {
				cookiesDone = implode("; ", cookiesEncoded.toArray());
			}
		}

		if (cookiesDone == null) {
			Map<String, ?> prefsValues = prefs.getAll();

			if (prefsValues != null && prefsValues.size() > 0) {
				final Object[] keys = prefsValues.keySet().toArray();
				final ArrayList<String> cookiesEncoded = new ArrayList<String>();
				final int length = keys.length;

				for (int i = 0; i < length; i++) {
					if (keys[i].toString().length() > 7 && keys[i].toString().substring(0, 7).equals("cookie_") == true) {
						cookiesEncoded.add(keys[i].toString().substring(7) + "=" + prefsValues.get(keys[i].toString()));
					}
				}

				if (cookiesEncoded.size() > 0) {
					cookiesDone = implode("; ", cookiesEncoded.toArray());
				}
			}
		}

		if (cookiesDone == null) {
			cookiesDone = "";
		}

		URLConnection uc = null;
		HttpURLConnection connection = null;
		Integer timeout = 30000;
		final StringBuffer buffer = new StringBuffer();

		for (int i = 0; i < 3; i++) {
			if (i > 0) {
				Log.w(cgSettings.tag, "Failed to download data, retrying. Attempt #" + (i + 1));
			}

			buffer.delete(0, buffer.length());
			timeout = 30000 + (i * 15000);

			try {
				// POST
				final URL u = new URL("http://" + host + path);
				uc = u.openConnection();

				uc.setRequestProperty("Host", host);
				uc.setRequestProperty("Cookie", cookiesDone);
				uc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
				uc.setRequestProperty("X-Requested-With", "XMLHttpRequest");
				uc.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
				uc.setRequestProperty("Referer", host + "/" + path);

				if (settings.asBrowser == 1) {
					uc.setRequestProperty("Accept-Charset", "utf-8, iso-8859-1, utf-16, *;q=0.7");
					uc.setRequestProperty("Accept-Language", "en-US");
					uc.setRequestProperty("User-Agent", idBrowser);
					uc.setRequestProperty("Connection", "keep-alive");
					uc.setRequestProperty("Keep-Alive", "300");
				}

				connection = (HttpURLConnection) uc;
				connection.setReadTimeout(timeout);
				connection.setRequestMethod("POST");
				HttpURLConnection.setFollowRedirects(false); // TODO: Fix these (FilCab)
				connection.setDoInput(true);
				connection.setDoOutput(true);

				final OutputStream out = connection.getOutputStream();
				final OutputStreamWriter wr = new OutputStreamWriter(out);
				wr.write(params);
				wr.flush();
				wr.close();

				String headerName = null;
				final SharedPreferences.Editor prefsEditor = prefs.edit();
				for (int j = 1; (headerName = uc.getHeaderFieldKey(j)) != null; j++) {
					if (headerName != null && headerName.equalsIgnoreCase("Set-Cookie")) {
						int index;
						String cookie = uc.getHeaderField(j);

						index = cookie.indexOf(";");
						if (index > -1) {
							cookie = cookie.substring(0, cookie.indexOf(";"));
						}

						index = cookie.indexOf("=");
						if (index > - 1 && cookie.length() > (index + 1)) {
							String name = cookie.substring(0, cookie.indexOf("="));
							String value = cookie.substring(cookie.indexOf("=") + 1, cookie.length());

							cookies.put(name, value);
							prefsEditor.putString("cookie_" + name, value);
						}
					}
				}
				prefsEditor.commit();

				final String encoding = connection.getContentEncoding();
				InputStream ins;

				if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
					ins = new GZIPInputStream(connection.getInputStream());
				} else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
					ins = new InflaterInputStream(connection.getInputStream(), new Inflater(true));
				} else {
					ins = connection.getInputStream();
				}
				final InputStreamReader inr = new InputStreamReader(ins);
				final BufferedReader br = new BufferedReader(inr);

				readIntoBuffer(br, buffer);

				httpCode = connection.getResponseCode();
				httpLocation = uc.getHeaderField("Location");

				final String paramsLog = params.replaceAll(passMatch, "password=***");
				Log.i(cgSettings.tag + " | JSON", "[POST " + (int)(params.length() / 1024) + "k | " + httpCode + " | " + (int)(buffer.length() / 1024) + "k] Downloaded " + "http://" + host + path + "?" + paramsLog);

				connection.disconnect();
				br.close();
				ins.close();
				inr.close();
			} catch (IOException e) {
				Log.e(cgSettings.tag, "cgeoBase.requestJSONgc.IOException: " + e.toString());
			} catch (Exception e) {
				Log.e(cgSettings.tag, "cgeoBase.requestJSONgc: " + e.toString());
			}

			if (buffer != null && buffer.length() > 0) {
				break;
			}
		}

		String page = null;
		if (httpCode == 302 && httpLocation != null) {
			final Uri newLocation = Uri.parse(httpLocation);
			if (newLocation.isRelative() == true) {
				page = requestJSONgc(host, path, params);
			} else {
				page = requestJSONgc(newLocation.getHost(), newLocation.getPath(), params);
			}
		} else {
			page = replaceWhitespace(buffer);
		}

		if (page != null) {
			return page;
		} else {
			return "";
		}
	}

	public String requestJSON(String host, String path, String params) {
		return requestJSON("http://", host, path, "GET", params);
	}

	public String requestJSON(String scheme, String host, String path, String method, String params) {
		int httpCode = -1;
		String httpLocation = null;

		if (method == null) {
			method = "GET";
		} else {
			method = method.toUpperCase();
		}

		boolean methodPost = false;
		if (method.equalsIgnoreCase("POST")) {
			methodPost = true;
		}

		URLConnection uc = null;
		HttpURLConnection connection = null;
		Integer timeout = 30000;
		final StringBuffer buffer = new StringBuffer();

		for (int i = 0; i < 3; i++) {
			if (i > 0) {
				Log.w(cgSettings.tag, "Failed to download data, retrying. Attempt #" + (i + 1));
			}

			buffer.delete(0, buffer.length());
			timeout = 30000 + (i * 15000);

			try {
				try {
					URL u = null;
					if (methodPost) {
						u = new URL(scheme + host + path);
					} else {
						u = new URL(scheme + host + path + "?" + params);
					}

					if (u.getProtocol().toLowerCase().equals("https")) {
						trustAllHosts();
						HttpsURLConnection https = (HttpsURLConnection) u.openConnection();
						https.setHostnameVerifier(doNotVerify);
						uc = https;
					} else {
						uc = (HttpURLConnection) u.openConnection();
					}

					uc.setRequestProperty("Host", host);
					uc.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
					if (methodPost) {
						uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
						uc.setRequestProperty("Content-Length", Integer.toString(params.length()));
						uc.setRequestProperty("X-HTTP-Method-Override", "GET");
					} else {
						uc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
					}
					uc.setRequestProperty("X-Requested-With", "XMLHttpRequest");

					connection = (HttpURLConnection) uc;
					connection.setReadTimeout(timeout);
					connection.setRequestMethod(method);
					HttpURLConnection.setFollowRedirects(false); // TODO: Fix these (FilCab)
					connection.setDoInput(true);
					if (methodPost) {
						connection.setDoOutput(true);

						final OutputStream out = connection.getOutputStream();
						final OutputStreamWriter wr = new OutputStreamWriter(out);
						wr.write(params);
						wr.flush();
						wr.close();
					} else {
						connection.setDoOutput(false);
					}


					final String encoding = connection.getContentEncoding();
					InputStream ins;

					if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
						ins = new GZIPInputStream(connection.getInputStream());
					} else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
						ins = new InflaterInputStream(connection.getInputStream(), new Inflater(true));
					} else {
						ins = connection.getInputStream();
					}
					final InputStreamReader inr = new InputStreamReader(ins);
					final BufferedReader br = new BufferedReader(inr);

					readIntoBuffer(br, buffer);

					httpCode = connection.getResponseCode();

					final String paramsLog = params.replaceAll(passMatch, "password=***");
					Log.i(cgSettings.tag + " | JSON", "[POST " + (int)(params.length() / 1024) + "k | " + httpCode + " | " + (int)(buffer.length() / 1024) + "k] Downloaded " + "http://" + host + path + "?" + paramsLog);

					connection.disconnect();
					br.close();
					ins.close();
					inr.close();
				} catch (IOException e) {
					httpCode = connection.getResponseCode();

					Log.e(cgSettings.tag, "cgeoBase.requestJSON.IOException: " + httpCode + ": " + connection.getResponseMessage() + " ~ " +  e.toString());
				}
			} catch (Exception e) {
				Log.e(cgSettings.tag, "cgeoBase.requestJSON: " + e.toString());
			}

			if (buffer != null && buffer.length() > 0) {
				break;
			}

			if (httpCode == 403) {
				// we're not allowed to download content, so let's move
				break;
			}
		}

		String page = null;
		if (httpCode == 302 && httpLocation != null) {
			final Uri newLocation = Uri.parse(httpLocation);
			if (newLocation.isRelative() == true) {
				page = requestJSONgc(host, path, params);
			} else {
				page = requestJSONgc(newLocation.getHost(), newLocation.getPath(), params);
			}
		} else {
			page = replaceWhitespace(buffer);
		}

		if (page != null) {
			return page;
		} else {
			return "";
		}
	}

	public static String rot13(String text) {
		final StringBuilder result = new StringBuilder();
		// plaintext flag (do not convert)
		boolean plaintext = false;

		int length = text.length();
		for (int index = 0; index < length; index++) {
			int c = text.charAt(index);
			if (c == '[') {
				plaintext = true;
			} else if (c == ']') {
				plaintext = false;
			} else if (!plaintext) {
				int capitalized = c & 32;
				c &= ~capitalized;
				c = ((c >= 'A') && (c <= 'Z') ? ((c - 'A' + 13) % 26 + 'A') : c)
						| capitalized;
			}
			result.append((char) c);
		}
		return result.toString();
	}

	public static String md5(String text) {
		String hashed = "";

		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(text.getBytes(), 0, text.length());
			hashed = new BigInteger(1, digest.digest()).toString(16);
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgBase.md5: " + e.toString());
		}

		return hashed;
	}

	public static String sha1(String text) {
		String hashed = "";

		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			digest.update(text.getBytes(), 0, text.length());
			hashed = new BigInteger(1, digest.digest()).toString(16);
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgBase.sha1: " + e.toString());
		}

		return hashed;
	}

	public static byte[] hashHmac(String text, String salt) {
		byte[] macBytes = {};

		try {
			SecretKeySpec secretKeySpec = new SecretKeySpec(salt.getBytes(), "HmacSHA1");
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(secretKeySpec);
			macBytes = mac.doFinal(text.getBytes());
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgBase.hashHmac: " + e.toString());
		}

		return macBytes;
	}

	public static boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();

			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}

		return (path.delete());
	}

	public static boolean isIntentAvailable(Context context, String action) {
		final Intent intent = new Intent(action);

		return isIntentAvailable(context, intent);
	}

	public static boolean isIntentAvailable(Context context, Intent intent) {
		final PackageManager packageManager = context.getPackageManager();
		final List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

		return (list.size() > 0);
	}

	public void storeCache(cgeoapplication app, Activity activity, cgCache cache, String geocode, int listId, Handler handler) {
		try {
			// cache details
			if (cache != null) {
				final HashMap<String, String> params = new HashMap<String, String>();
				params.put("geocode", cache.geocode);
				final Long searchId = searchByGeocode(params, listId, false);
				cache = app.getCache(searchId);
			} else if (geocode != null) {
				final HashMap<String, String> params = new HashMap<String, String>();
				params.put("geocode", geocode);
				final Long searchId = searchByGeocode(params, listId, false);
				cache = app.getCache(searchId);
			}

			if (cache == null) {
				if (handler != null) {
					handler.sendMessage(new Message());
				}

				return;
			}

			final cgHtmlImg imgGetter = new cgHtmlImg(activity, settings, cache.geocode, false, listId, true);

			// store images from description
			if (cache.description != null) {
				Html.fromHtml(cache.description, imgGetter, null);
			}

			// store spoilers
			if (cache.spoilers != null && cache.spoilers.isEmpty() == false) {
				for (cgSpoiler oneSpoiler : cache.spoilers) {
					imgGetter.getDrawable(oneSpoiler.url);
				}
			}

			// store map previews
			if (settings.storeOfflineMaps == 1 && cache.latitude != null && cache.longitude != null) {
				final String latlonMap = String.format((Locale) null, "%.6f", cache.latitude) + "," + String.format((Locale) null, "%.6f", cache.longitude);
				final Display display = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
				final int maxWidth = display.getWidth() - 25;
				final int maxHeight = display.getHeight() - 25;
				int edge = 0;
				if (maxWidth > maxHeight) {
					edge = maxWidth;
				} else {
					edge = maxHeight;
				}

				String type = "mystery";
				if (cache.found == true) {
					type = cache.type + "_found";
				} else if (cache.disabled == true) {
					type = cache.type + "_disabled";
				} else {
					type = cache.type;
				}

				final String markerUrl = urlencode_rfc3986("http://cgeo.carnero.cc/_markers/marker_cache_" + type + ".png");
				final StringBuilder waypoints = new StringBuilder();
				if (cache.waypoints != null && cache.waypoints.size() > 0) {
					for (cgWaypoint waypoint : cache.waypoints) {
						if (waypoint.latitude == null && waypoint.longitude == null) {
							continue;
						}

						waypoints.append("&markers=icon%3Ahttp://cgeo.carnero.cc/_markers/marker_waypoint_");
						waypoints.append(waypoint.type);
						waypoints.append(".png%7C");
						waypoints.append(String.format((Locale) null, "%.6f", waypoint.latitude));
						waypoints.append(",");
						waypoints.append(String.format((Locale) null, "%.6f", waypoint.longitude));
					}
				}

				// download map images in separate background thread for higher performance
				final String code = cache.geocode;
				final int finalEdge = edge;
				Thread staticMapsThread = new Thread("getting static map") {@Override
				public void run() {
					cgMapImg mapGetter = new cgMapImg(settings, code);

					mapGetter.getDrawable("http://maps.google.com/maps/api/staticmap?center=" + latlonMap + "&zoom=20&size=" + finalEdge + "x" + finalEdge + "&maptype=satellite&markers=icon%3A" + markerUrl + "%7C" + latlonMap + waypoints.toString() + "&sensor=false", 1);
					mapGetter.getDrawable("http://maps.google.com/maps/api/staticmap?center=" + latlonMap + "&zoom=18&size=" + finalEdge + "x" + finalEdge + "&maptype=satellite&markers=icon%3A" + markerUrl + "%7C" + latlonMap + waypoints.toString() + "&sensor=false", 2);
					mapGetter.getDrawable("http://maps.google.com/maps/api/staticmap?center=" + latlonMap + "&zoom=16&size=" + finalEdge + "x" + finalEdge + "&maptype=roadmap&markers=icon%3A" + markerUrl + "%7C" + latlonMap + waypoints.toString() + "&sensor=false", 3);
					mapGetter.getDrawable("http://maps.google.com/maps/api/staticmap?center=" + latlonMap + "&zoom=14&size=" + finalEdge + "x" + finalEdge + "&maptype=roadmap&markers=icon%3A" + markerUrl + "%7C" + latlonMap + waypoints.toString() + "&sensor=false", 4);
					mapGetter.getDrawable("http://maps.google.com/maps/api/staticmap?center=" + latlonMap + "&zoom=11&size=" + finalEdge + "x" + finalEdge + "&maptype=roadmap&markers=icon%3A" + markerUrl + "%7C" + latlonMap + waypoints.toString() + "&sensor=false", 5);
				}};
				staticMapsThread.setPriority(Thread.MIN_PRIORITY);
				staticMapsThread.start();
			}

			app.markStored(cache.geocode, listId);
			app.removeCacheFromCache(cache.geocode);

			if (handler != null) {
				handler.sendMessage(new Message());
			}
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgBase.storeCache: " + e.toString());
		}
	}

	public void dropCache(cgeoapplication app, Activity activity, cgCache cache, Handler handler) {
		try {
			app.markDropped(cache.geocode);
			app.removeCacheFromCache(cache.geocode);

			handler.sendMessage(new Message());
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgBase.dropCache: " + e.toString());
		}
	}

	public boolean isInViewPort(int centerLat1, int centerLon1, int centerLat2, int centerLon2, int spanLat1, int spanLon1, int spanLat2, int spanLon2) {
		try {
			// expects coordinates in E6 format
			final int left1 = centerLat1 - (spanLat1 / 2);
			final int right1 = centerLat1 + (spanLat1 / 2);
			final int top1 = centerLon1 + (spanLon1 / 2);
			final int bottom1 = centerLon1 - (spanLon1 / 2);

			final int left2 = centerLat2 - (spanLat2 / 2);
			final int right2 = centerLat2 + (spanLat2 / 2);
			final int top2 = centerLon2 + (spanLon2 / 2);
			final int bottom2 = centerLon2 - (spanLon2 / 2);

			if (left2 <= left1) {
				return false;
			}
			if (right2 >= right1) {
				return false;
			}
			if (top2 >= top1) {
				return false;
			}
			if (bottom2 <= bottom1) {
				return false;
			}

			return true;
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgBase.isInViewPort: " + e.toString());
			return false;
		}
	}

	public boolean isCacheInViewPort(int centerLat, int centerLon, int spanLat, int spanLon, Double cacheLat, Double cacheLon) {
		if (cacheLat == null || cacheLon == null) {
			return false;
		}

		// viewport is defined by center, span and some (10%) reserve on every side
		int minLat = centerLat - (spanLat / 2) - (spanLat / 10);
		int maxLat = centerLat + (spanLat / 2) + (spanLat / 10);
		int minLon = centerLon - (spanLon / 2) - (spanLon / 10);
		int maxLon = centerLon + (spanLon / 2) + (spanLon / 10);
		int cLat = (int) Math.round(cacheLat * 1e6);
		int cLon = (int) Math.round(cacheLon * 1e6);
		int mid = 0;

		if (maxLat < minLat) {
			mid = minLat;
			minLat = maxLat;
			maxLat = mid;
		}

		if (maxLon < minLon) {
			mid = minLon;
			minLon = maxLon;
			maxLon = mid;
		}

		boolean latOk = false;
		boolean lonOk = false;

		if (cLat >= minLat && cLat <= maxLat) {
			latOk = true;
		}
		if (cLon >= minLon && cLon <= maxLon) {
			lonOk = true;
		}

		if (latOk == true && lonOk == true) {
			return true;
		} else {
			return false;
		}
	}
	private static char[] base64map1 = new char[64];

	static {
		int i = 0;
		for (char c = 'A'; c <= 'Z'; c++) {
			base64map1[i++] = c;
		}
		for (char c = 'a'; c <= 'z'; c++) {
			base64map1[i++] = c;
		}
		for (char c = '0'; c <= '9'; c++) {
			base64map1[i++] = c;
		}
		base64map1[i++] = '+';
		base64map1[i++] = '/';
	}
	private static byte[] base64map2 = new byte[128];

	static {
		for (int i = 0; i < base64map2.length; i++) {
			base64map2[i] = -1;
		}
		for (int i = 0; i < 64; i++) {
			base64map2[base64map1[i]] = (byte) i;
		}
	}

	public static String base64Encode(byte[] in) {
		int iLen = in.length;
		int oDataLen = (iLen * 4 + 2) / 3; // output length without padding
		int oLen = ((iLen + 2) / 3) * 4; // output length including padding
		char[] out = new char[oLen];
		int ip = 0;
		int op = 0;

		while (ip < iLen) {
			int i0 = in[ip++] & 0xff;
			int i1 = ip < iLen ? in[ip++] & 0xff : 0;
			int i2 = ip < iLen ? in[ip++] & 0xff : 0;
			int o0 = i0 >>> 2;
			int o1 = ((i0 & 3) << 4) | (i1 >>> 4);
			int o2 = ((i1 & 0xf) << 2) | (i2 >>> 6);
			int o3 = i2 & 0x3F;
			out[op++] = base64map1[o0];
			out[op++] = base64map1[o1];
			out[op] = op < oDataLen ? base64map1[o2] : '=';
			op++;
			out[op] = op < oDataLen ? base64map1[o3] : '=';
			op++;
		}

		return new String(out);
	}

	public static byte[] base64Decode(String text) {
		char[] in = text.toCharArray();

		int iLen = in.length;
		if (iLen % 4 != 0) {
			throw new IllegalArgumentException("Length of Base64 encoded input string is not a multiple of 4.");
		}
		while (iLen > 0 && in[iLen - 1] == '=') {
			iLen--;
		}
		int oLen = (iLen * 3) / 4;
		byte[] out = new byte[oLen];
		int ip = 0;
		int op = 0;
		while (ip < iLen) {
			int i0 = in[ip++];
			int i1 = in[ip++];
			int i2 = ip < iLen ? in[ip++] : 'A';
			int i3 = ip < iLen ? in[ip++] : 'A';
			if (i0 > 127 || i1 > 127 || i2 > 127 || i3 > 127) {
				throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
			}
			int b0 = base64map2[i0];
			int b1 = base64map2[i1];
			int b2 = base64map2[i2];
			int b3 = base64map2[i3];
			if (b0 < 0 || b1 < 0 || b2 < 0 || b3 < 0) {
				throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
			}
			int o0 = (b0 << 2) | (b1 >>> 4);
			int o1 = ((b1 & 0xf) << 4) | (b2 >>> 2);
			int o2 = ((b2 & 3) << 6) | b3;
			out[op++] = (byte) o0;
			if (op < oLen) {
				out[op++] = (byte) o1;
			}
			if (op < oLen) {
				out[op++] = (byte) o2;
			}
		}
		return out;
	}

	public int getIcon(boolean cache, String type, boolean own, boolean found, boolean disabled) {
		if (gcIcons.isEmpty()) {
			// default markers
			gcIcons.put("ape", R.drawable.marker_cache_ape);
			gcIcons.put("cito", R.drawable.marker_cache_cito);
			gcIcons.put("earth", R.drawable.marker_cache_earth);
			gcIcons.put("event", R.drawable.marker_cache_event);
			gcIcons.put("letterbox", R.drawable.marker_cache_letterbox);
			gcIcons.put("locationless", R.drawable.marker_cache_locationless);
			gcIcons.put("mega", R.drawable.marker_cache_mega);
			gcIcons.put("multi", R.drawable.marker_cache_multi);
			gcIcons.put("traditional", R.drawable.marker_cache_traditional);
			gcIcons.put("virtual", R.drawable.marker_cache_virtual);
			gcIcons.put("webcam", R.drawable.marker_cache_webcam);
			gcIcons.put("wherigo", R.drawable.marker_cache_wherigo);
			gcIcons.put("mystery", R.drawable.marker_cache_mystery);
			gcIcons.put("gchq", R.drawable.marker_cache_gchq);
			// own cache markers
			gcIcons.put("ape-own", R.drawable.marker_cache_ape_own);
			gcIcons.put("cito-own", R.drawable.marker_cache_cito_own);
			gcIcons.put("earth-own", R.drawable.marker_cache_earth_own);
			gcIcons.put("event-own", R.drawable.marker_cache_event_own);
			gcIcons.put("letterbox-own", R.drawable.marker_cache_letterbox_own);
			gcIcons.put("locationless-own", R.drawable.marker_cache_locationless_own);
			gcIcons.put("mega-own", R.drawable.marker_cache_mega_own);
			gcIcons.put("multi-own", R.drawable.marker_cache_multi_own);
			gcIcons.put("traditional-own", R.drawable.marker_cache_traditional_own);
			gcIcons.put("virtual-own", R.drawable.marker_cache_virtual_own);
			gcIcons.put("webcam-own", R.drawable.marker_cache_webcam_own);
			gcIcons.put("wherigo-own", R.drawable.marker_cache_wherigo_own);
			gcIcons.put("mystery-own", R.drawable.marker_cache_mystery_own);
			gcIcons.put("gchq-own", R.drawable.marker_cache_gchq_own);
			// found cache markers
			gcIcons.put("ape-found", R.drawable.marker_cache_ape_found);
			gcIcons.put("cito-found", R.drawable.marker_cache_cito_found);
			gcIcons.put("earth-found", R.drawable.marker_cache_earth_found);
			gcIcons.put("event-found", R.drawable.marker_cache_event_found);
			gcIcons.put("letterbox-found", R.drawable.marker_cache_letterbox_found);
			gcIcons.put("locationless-found", R.drawable.marker_cache_locationless_found);
			gcIcons.put("mega-found", R.drawable.marker_cache_mega_found);
			gcIcons.put("multi-found", R.drawable.marker_cache_multi_found);
			gcIcons.put("traditional-found", R.drawable.marker_cache_traditional_found);
			gcIcons.put("virtual-found", R.drawable.marker_cache_virtual_found);
			gcIcons.put("webcam-found", R.drawable.marker_cache_webcam_found);
			gcIcons.put("wherigo-found", R.drawable.marker_cache_wherigo_found);
			gcIcons.put("mystery-found", R.drawable.marker_cache_mystery_found);
			gcIcons.put("gchq-found", R.drawable.marker_cache_gchq_found);
			// disabled cache markers
			gcIcons.put("ape-disabled", R.drawable.marker_cache_ape_disabled);
			gcIcons.put("cito-disabled", R.drawable.marker_cache_cito_disabled);
			gcIcons.put("earth-disabled", R.drawable.marker_cache_earth_disabled);
			gcIcons.put("event-disabled", R.drawable.marker_cache_event_disabled);
			gcIcons.put("letterbox-disabled", R.drawable.marker_cache_letterbox_disabled);
			gcIcons.put("locationless-disabled", R.drawable.marker_cache_locationless_disabled);
			gcIcons.put("mega-disabled", R.drawable.marker_cache_mega_disabled);
			gcIcons.put("multi-disabled", R.drawable.marker_cache_multi_disabled);
			gcIcons.put("traditional-disabled", R.drawable.marker_cache_traditional_disabled);
			gcIcons.put("virtual-disabled", R.drawable.marker_cache_virtual_disabled);
			gcIcons.put("webcam-disabled", R.drawable.marker_cache_webcam_disabled);
			gcIcons.put("wherigo-disabled", R.drawable.marker_cache_wherigo_disabled);
			gcIcons.put("mystery-disabled", R.drawable.marker_cache_mystery_disabled);
			gcIcons.put("gchq-disabled", R.drawable.marker_cache_gchq_disabled);
		}

		if (wpIcons.isEmpty()) {
			wpIcons.put("waypoint", R.drawable.marker_waypoint_waypoint);
			wpIcons.put("flag", R.drawable.marker_waypoint_flag);
			wpIcons.put("pkg", R.drawable.marker_waypoint_pkg);
			wpIcons.put("puzzle", R.drawable.marker_waypoint_puzzle);
			wpIcons.put("stage", R.drawable.marker_waypoint_stage);
			wpIcons.put("trailhead", R.drawable.marker_waypoint_trailhead);
		}

		int icon = -1;
		String iconTxt = null;

		if (cache == true) {
			if (type != null && type.length() > 0) {
				if (own == true) {
					iconTxt = type + "-own";
				} else if (found == true) {
					iconTxt = type + "-found";
				} else if (disabled == true) {
					iconTxt = type + "-disabled";
				} else {
					iconTxt = type;
				}
			} else {
				iconTxt = "traditional";
			}

			if (gcIcons.containsKey(iconTxt) == true) {
				icon = gcIcons.get(iconTxt);
			} else {
				icon = gcIcons.get("traditional");
			}
		} else {
			if (type != null && type.length() > 0) {
				iconTxt = type;
			} else {
				iconTxt = "waypoint";
			}

			if (wpIcons.containsKey(iconTxt) == true) {
				icon = wpIcons.get(iconTxt);
			} else {
				icon = wpIcons.get("waypoint");
			}
		}

		return icon;
	}

	public boolean isLocus(Context context) {
		boolean locus = false;
		final Intent intentTest = new Intent(Intent.ACTION_VIEW);
		intentTest.setData(Uri.parse("menion.points:x"));
		if (isIntentAvailable(context, intentTest) == true) {
			locus = true;
		}

		return locus;
	}

	public boolean isRmaps(Context context) {
		boolean rmaps = false;
		final Intent intent = new Intent("com.robert.maps.action.SHOW_POINTS");
		if (isIntentAvailable(context, intent) == true) {
			rmaps = true;
		}

		return rmaps;
	}

	public boolean runExternalMap(int application, Activity activity, Resources res, cgWarning warning, GoogleAnalyticsTracker tracker, Double latitude, Double longitude) {
		// waypoint
		return runExternalMap(application, activity, res, warning, tracker, null, null, latitude, longitude);
	}

	public boolean runExternalMap(int application, Activity activity, Resources res, cgWarning warning, GoogleAnalyticsTracker tracker, cgWaypoint waypoint) {
		// waypoint
		return runExternalMap(application, activity, res, warning, tracker, null, waypoint, null, null);
	}

	public boolean runExternalMap(int application, Activity activity, Resources res, cgWarning warning, GoogleAnalyticsTracker tracker, cgCache cache) {
		// cache
		return runExternalMap(application, activity, res, warning, tracker, cache, null, null, null);
	}

	public boolean runExternalMap(int application, Activity activity, Resources res, cgWarning warning, GoogleAnalyticsTracker tracker, cgCache cache, cgWaypoint waypoint, Double latitude, Double longitude) {
		if (cache == null && waypoint == null && latitude == null && longitude == null) {
			return false;
		}

		if (application == mapAppLocus) {
			// locus
			try {
				final Intent intentTest = new Intent(Intent.ACTION_VIEW);
				intentTest.setData(Uri.parse("menion.points:x"));

				if (isIntentAvailable(activity, intentTest) == true) {
					final ArrayList<cgWaypoint> waypoints = new ArrayList<cgWaypoint>();
					// get only waypoints with coordinates
					if (cache != null && cache.waypoints != null && cache.waypoints.isEmpty() == false) {
						for (cgWaypoint wp : cache.waypoints) {
							if (wp.latitude != null && wp.longitude != null) {
								waypoints.add(wp);
							}
						}
					}

					final ByteArrayOutputStream baos = new ByteArrayOutputStream();
					final DataOutputStream dos = new DataOutputStream(baos);

					dos.writeInt(1); // not used
					if (cache != null) {
						if (waypoints == null || waypoints.isEmpty() == true) {
							dos.writeInt(1); // cache only
						} else {
							dos.writeInt((1 + waypoints.size())); // cache and waypoints
						}
					} else {
						dos.writeInt(1); // one waypoint
					}

					int icon = -1;
					if (cache != null) {
						icon = getIcon(true, cache.type, cache.own, cache.found, cache.disabled || cache.archived);
					} else if (waypoint != null) {
						icon = getIcon(false, waypoint.type, false, false, false);
					} else {
						icon = getIcon(false, "waypoint", false, false, false);
					}

					if (icon > 0) {
						// load icon
						Bitmap bitmap = BitmapFactory.decodeResource(res, icon);
						ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
						bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos2);
						byte[] image = baos2.toByteArray();

						dos.writeInt(image.length);
						dos.write(image);
					} else {
						// no icon
						dos.writeInt(0); // no image
					}

					// name
					if (cache != null && cache.name != null && cache.name.length() > 0) {
						dos.writeUTF(cache.name);
					} else if (waypoint != null && waypoint.name != null && waypoint.name.length() > 0) {
						dos.writeUTF(waypoint.name);
					} else {
						dos.writeUTF("");
					}

					// description
					if (cache != null && cache.geocode != null && cache.geocode.length() > 0) {
						dos.writeUTF(cache.geocode.toUpperCase());
					} else if (waypoint != null && waypoint.lookup != null && waypoint.lookup.length() > 0) {
						dos.writeUTF(waypoint.lookup.toUpperCase());
					} else {
						dos.writeUTF("");
					}

					// additional data :: keyword, button title, package, activity, data name, data content
					if (cache != null && cache.geocode != null && cache.geocode.length() > 0) {
						dos.writeUTF("intent;c:geo;carnero.cgeo;carnero.cgeo.cgeodetail;geocode;" + cache.geocode);
					} else if (waypoint != null && waypoint.id != null && waypoint.id > 0) {
						dos.writeUTF("intent;c:geo;carnero.cgeo;carnero.cgeo.cgeowaypoint;id;" + waypoint.id);
					} else {
						dos.writeUTF("");
					}

					if (cache != null && cache.latitude != null && cache.longitude != null) {
						dos.writeDouble(cache.latitude); // latitude
						dos.writeDouble(cache.longitude); // longitude
					} else if (waypoint != null && waypoint.latitude != null && waypoint.longitude != null) {
						dos.writeDouble(waypoint.latitude); // latitude
						dos.writeDouble(waypoint.longitude); // longitude
					} else {
						dos.writeDouble(latitude); // latitude
						dos.writeDouble(longitude); // longitude
					}

					// cache waypoints
					if (waypoints != null && waypoints.isEmpty() == false) {
						for (cgWaypoint wp : waypoints) {
							if (wp == null || wp.latitude == null || wp.longitude == null) {
								continue;
							}

							final int wpIcon = getIcon(false, wp.type, false, false, false);

							if (wpIcon > 0) {
								// load icon
								Bitmap bitmap = BitmapFactory.decodeResource(res, wpIcon);
								ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
								bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos2);
								byte[] image = baos2.toByteArray();

								dos.writeInt(image.length);
								dos.write(image);
							} else {
								// no icon
								dos.writeInt(0); // no image
							}

							// name
							if (wp.lookup != null && wp.lookup.length() > 0) {
								dos.writeUTF(wp.lookup.toUpperCase());
							} else {
								dos.writeUTF("");
							}

							// description
							if (wp.name != null && wp.name.length() > 0) {
								dos.writeUTF(wp.name);
							} else {
								dos.writeUTF("");
							}

							// additional data :: keyword, button title, package, activity, data name, data content
							if (wp.id != null && wp.id > 0) {
								dos.writeUTF("intent;c:geo;carnero.cgeo;carnero.cgeo.cgeowaypoint;id;" + wp.id);
							} else {
								dos.writeUTF("");
							}

							dos.writeDouble(wp.latitude); // latitude
							dos.writeDouble(wp.longitude); // longitude
						}
					}

					final Intent intent = new Intent();
					intent.setAction(Intent.ACTION_VIEW);
					intent.setData(Uri.parse("menion.points:data"));
					intent.putExtra("data", baos.toByteArray());

					activity.startActivity(intent);

					sendAnal(activity, tracker, "/external/locus");

					return true;
				}
			} catch (Exception e) {
				// nothing
			}
		}

		if (application == mapAppRmaps) {
			// rmaps
			try {
				final Intent intent = new Intent("com.robert.maps.action.SHOW_POINTS");

				if (isIntentAvailable(activity, intent) == true) {
					final ArrayList<String> locations = new ArrayList<String>();
					if (cache != null && cache.latitude != null && cache.longitude != null) {
						locations.add(String.format((Locale) null, "%.6f", cache.latitude) + "," + String.format((Locale) null, "%.6f", cache.longitude) + ";" + cache.geocode + ";" + cache.name);
					} else if (waypoint != null && waypoint.latitude != null && waypoint.longitude != null) {
						locations.add(String.format((Locale) null, "%.6f", waypoint.latitude) + "," + String.format((Locale) null, "%.6f", waypoint.longitude) + ";" + waypoint.lookup + ";" + waypoint.name);
					}

					intent.putStringArrayListExtra("locations", locations);

					activity.startActivity(intent);

					sendAnal(activity, tracker, "/external/rmaps");

					return true;
				}
			} catch (Exception e) {
				// nothing
			}
		}

		if (application == mapAppAny) {
			// fallback
			try {
				if (cache != null && cache.latitude != null && cache.longitude != null) {
					activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + cache.latitude + "," + cache.longitude)));
					// INFO: q parameter works with Google Maps, but breaks cooperation with all other apps
				} else if (waypoint != null && waypoint.latitude != null && waypoint.longitude != null) {
					activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + waypoint.latitude + "," + waypoint.longitude)));
					// INFO: q parameter works with Google Maps, but breaks cooperation with all other apps
				}

				sendAnal(activity, tracker, "/external/native/maps");

				return true;
			} catch (Exception e) {
				// nothing
			}
		}

		Log.i(cgSettings.tag, "cgBase.runExternalMap: No maps application available.");

		if (warning != null && res != null) {
			warning.showToast(res.getString(R.string.err_application_no));
		}

		return false;
	}

	public boolean runNavigation(Activity activity, Resources res, cgSettings settings, cgWarning warning, GoogleAnalyticsTracker tracker, Double latitude, Double longitude) {
		return runNavigation(activity, res, settings, warning, tracker, latitude, longitude, null, null);
	}

	public boolean runNavigation(Activity activity, Resources res, cgSettings settings, cgWarning warning, GoogleAnalyticsTracker tracker, Double latitude, Double longitude, Double latitudeNow, Double longitudeNow) {
		if (activity == null) {
			return false;
		}
		if (settings == null) {
			return false;
		}

		// Google Navigation
		if (settings.useGNavigation == 1) {
			try {
				activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:ll=" + latitude + "," + longitude)));

				sendAnal(activity, tracker, "/external/native/navigation");

				return true;
			} catch (Exception e) {
				// nothing
			}
		}

		// Google Maps Directions
		try {
			if (latitudeNow != null && longitudeNow != null) {
				activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?f=d&saddr=" + latitudeNow + "," + longitudeNow + "&daddr=" + latitude + "," + longitude)));
			} else {
				activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?f=d&daddr=" + latitude + "," + longitude)));
			}

			sendAnal(activity, tracker, "/external/native/maps");

			return true;
		} catch (Exception e) {
			// nothing
		}

		Log.i(cgSettings.tag, "cgBase.runNavigation: No navigation application available.");

		if (warning != null && res != null) {
			warning.showToast(res.getString(R.string.err_navigation_no));
		}

		return false;
	}

	public String getMapUserToken(Handler noTokenHandler) {
		final cgResponse response = request(false, "www.geocaching.com", "/map/default.aspx", "GET", "", 0, false);
		final String data = response.getData();
		String usertoken = null;

		if (data != null && data.length() > 0) {
			final Pattern pattern = Pattern.compile("var userToken[^=]*=[^']*'([^']+)';", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

			final Matcher matcher = pattern.matcher(data);
			while (matcher.find()) {
				if (matcher.groupCount() > 0) {
					usertoken = matcher.group(1);
				}
			}
		}

		if (noTokenHandler != null && (usertoken == null || usertoken.length() == 0)) {
			noTokenHandler.sendEmptyMessage(0);
		}

		return usertoken;
	}

	public void sendAnal(Context context, String page) {
		(new sendAnalThread(context, null, page)).start();
	}

	public void sendAnal(Context context, GoogleAnalyticsTracker tracker, String page) {
		(new sendAnalThread(context, tracker, page)).start();
	}

	private class sendAnalThread extends Thread {

		Context context = null;
		GoogleAnalyticsTracker tracker = null;
		String page = null;
		boolean startedHere = false;

		public sendAnalThread(Context contextIn, GoogleAnalyticsTracker trackerIn, String pageIn) {
			context = contextIn;
			tracker = trackerIn;
			page = pageIn;
		}

		@Override
		public void run() {
			try {
				if (page == null || page.length() == 0) {
					page = "/";
				}

				if (tracker == null && context != null) {
					startedHere = true;
					tracker = GoogleAnalyticsTracker.getInstance();
					tracker.start(cgSettings.analytics, context);
				}

				tracker.trackPageView(page);
				tracker.dispatch();

				Log.i(cgSettings.tag, "Logged use of " + page);

				if (startedHere == true) {
					tracker.stop();
				}
			} catch (Exception e) {
				// nothing
			}
		}
	}

	public Double getElevation(Double latitude, Double longitude) {
		Double elv = null;

		try {
			final String host = "maps.googleapis.com";
			final String path = "/maps/api/elevation/json";
			final String params = "sensor=false&locations=" + String.format((Locale) null, "%.6f", latitude) + "," + String.format((Locale) null, "%.6f", longitude);

			final String data = requestJSON(host, path, params);

			if (data == null || data.length() == 0) {
				return elv;
			}

			JSONObject response = new JSONObject(data);
			String status = response.getString("status");

			if (status == null || status.equalsIgnoreCase("OK") == false) {
				return elv;
			}

			if (response.has("results") == true) {
				JSONArray results = response.getJSONArray("results");
				JSONObject result = results.getJSONObject(0);
				elv = result.getDouble("elevation");
			}
		} catch (Exception e) {
			Log.w(cgSettings.tag, "cgBase.getElevation: " + e.toString());
		}

		return elv;
	}

	public void showProgress(Activity activity, boolean status) {
		if (activity == null) {
			return;
		}

		final ProgressBar progress = (ProgressBar) activity.findViewById(R.id.actionbar_progress);
		if (status == true) {
			progress.setVisibility(View.VISIBLE);
		} else {
			progress.setVisibility(View.GONE);
		}
	}

	public void goHome(Activity activity) {
		final Intent intent = new Intent(activity, cgeo.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		activity.startActivity(intent);
		activity.finish();
	}

	public void setTitle(Activity activity, String text) {
		if (activity == null || text == null) {
			return;
		}

		final TextView title = (TextView) activity.findViewById(R.id.actionbar_title);
		if (title != null) {
			title.setText(text);
		}
	}
}