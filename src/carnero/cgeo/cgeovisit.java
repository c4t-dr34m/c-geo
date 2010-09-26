package carnero.cgeo;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class cgeovisit extends cgLogForm {
	private cgeoapplication app = null;
	private Activity activity = null;
	private LayoutInflater inflater = null;
	private cgBase base = null;
	private cgSettings settings = null;
	private cgWarning warning = null;
	private cgCache cache = null;
	private ArrayList<Integer> types = new ArrayList<Integer>();
	private ProgressDialog waitDialog = null;
	private String cacheid = null;
	private String geocode = null;
	private String text = null;
	private String viewstate = null;
	private String viewstate1 = null;
	private Boolean gettingViewstate = true;
	private ArrayList<cgTrackableLog> trackables = null;
	private Calendar date = Calendar.getInstance();
	private int typeSelected = -1;
	private int attempts = 0;
	private boolean progressBar = false;
	private CheckBox tweetCheck = null;
	private LinearLayout tweetBox = null;

	private Handler showProgressHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (progressBar == true) setProgressBarIndeterminateVisibility(true);
		}
	};

	private Handler loadDataHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if ((viewstate == null || viewstate.length() == 0) && attempts < 2) {
				warning.showToast("Sorry, c:geo can\'t load data required to log visit. Trying again.");

				loadData thread;
				thread = new loadData(cacheid);
				thread.start();
				
				return;
			} else if ((viewstate == null || viewstate.length() == 0) && attempts >= 2) {
				warning.showToast("Sorry, c:geo can\'t load data required to log visit.");
				if (progressBar == true) setProgressBarIndeterminateVisibility(false);

				return;
			}

			gettingViewstate = false; // we're done, user can post log

			Button buttonPost = (Button)findViewById(R.id.post);
			buttonPost.setClickable(true);
			buttonPost.setOnTouchListener(new cgViewTouch(settings, buttonPost, 0));
			buttonPost.setOnClickListener(new postListener());
			if (settings.skin == 1) buttonPost.setBackgroundResource(R.drawable.action_button_light);
			else buttonPost.setBackgroundResource(R.drawable.action_button_dark);

			// add trackables
			if (trackables != null && trackables.isEmpty() == false) {
				if (inflater == null) inflater = activity.getLayoutInflater();
				
				final LinearLayout inventoryView = (LinearLayout)findViewById(R.id.inventory);
				inventoryView.removeAllViews();

				for (cgTrackableLog tb : trackables) {
					RelativeLayout inventoryItem = null;
					if (settings.skin == 1) inventoryItem = (RelativeLayout)inflater.inflate(R.layout.visit_trackable_light, null);
					else inventoryItem = (RelativeLayout)inflater.inflate(R.layout.visit_trackable_dark, null);

					((TextView)inventoryItem.findViewById(R.id.name)).setText(tb.name);
					((TextView)inventoryItem.findViewById(R.id.action)).setText(base.logTypesTrackable.get(0));

					inventoryItem.setId(tb.id);
					inventoryItem.setClickable(true);
					registerForContextMenu(inventoryItem);
					inventoryItem.setOnClickListener(new View.OnClickListener() {
						public void onClick(View view) {
							openContextMenu(view);
						}
					});

					inventoryView.addView(inventoryItem);
				}

				if (inventoryView.getChildCount() > 0) ((LinearLayout)findViewById(R.id.inventory_box)).setVisibility(View.VISIBLE);
			}

			if (progressBar == true) setProgressBarIndeterminateVisibility(false);
		}
	};
	
	private Handler postLogHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				warning.showToast("c:geo successfully posted log.");

				if (waitDialog != null) {
					waitDialog.dismiss();
				}
				finish();
				return;
			} else if (msg.what >= 1000) {
				warning.showToast("Sorry, c:geo failed to post log.");
			} else {
				if (base.errorRetrieve.get(msg.what) != null) {
					warning.showToast("Sorry, c:geo failed to post log because of " + base.errorRetrieve.get(msg.what) + ".");
				} else {
					warning.showToast("Sorry, c:geo failed to post log.");
				}
			}
			
			if (waitDialog != null) {
				waitDialog.dismiss();
			}
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
		progressBar = requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setTitle("log");
		if (settings.skin == 1) setContentView(R.layout.visit_light);
		else setContentView(R.layout.visit_dark);

		// get parameters
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			cacheid = extras.getString("id");
			geocode = extras.getString("geocode");
			text = extras.getString("text");
		}

		if ((cacheid == null || cacheid.length() == 0) && geocode != null && geocode.length() > 0) cacheid = app.getCacheid(geocode);
		if ((geocode == null || geocode.length() == 0) && cacheid != null && cacheid.length() > 0) geocode = app.getGeocode(cacheid);

		cache = app.getCacheByGeocode(geocode);

		if (cache.name != null && cache.name.length() > 0) setTitle("log " + cache.name);
		else setTitle("log " + cache.geocode.toUpperCase());

		app.setAction(geocode);

		if (cache == null) {
			warning.showToast("Sorry, c:geo forgot which cache you visited.");
			
			finish();
			return;
		}
		
		init();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		init();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		SubMenu subMenu = menu.addSubMenu(0, 0, 0, "add").setIcon(android.R.drawable.ic_menu_add);

		subMenu.add(0, 1, 0, "date & time");
		subMenu.add(0, 2, 0, "date");
		subMenu.add(0, 3, 0, "time");

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		EditText text = null;
		String textContent = null;
		String dateString = null;
		String timeString = null;

		if (id >= 1 && id <= 3) {
			text = (EditText)findViewById(R.id.log);
			textContent = text.getText().toString();
			dateString = base.dateOut.format(new Date());
			timeString = base.timeOut.format(new Date());
		}

		if (id == 1) {
			if (textContent.length() == 0) {
				text.setText(dateString + " | " + timeString + "\n", TextView.BufferType.NORMAL);
			} else {
				text.setText(textContent + "\n" + dateString + " | " + timeString + "\n", TextView.BufferType.NORMAL);
			}
			text.setSelection(text.getText().toString().length());

			return true;
		} else if (id == 2) {
			if (textContent.length() == 0) {
				text.setText(dateString + "\n", TextView.BufferType.NORMAL);
			} else {
				text.setText(textContent + "\n" + dateString + "\n", TextView.BufferType.NORMAL);
			}
			text.setSelection(text.getText().toString().length());

			return true;
		} else if (id == 3) {
			if (textContent.length() == 0) {
				text.setText(timeString + "\n", TextView.BufferType.NORMAL);
			} else {
				text.setText(textContent + "\n" + timeString + "\n", TextView.BufferType.NORMAL);
			}
			text.setSelection(text.getText().toString().length());

			return true;
		}

		return false;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo info) {
		super.onCreateContextMenu(menu, view, info);
		final int viewId = view.getId();

		if (viewId == R.id.type) {
			for (final int typeOne : types) menu.add(viewId, typeOne, 0, base.logTypes2.get(typeOne));
		} else {
			final int realViewId = ((RelativeLayout)findViewById(viewId)).getId();

			for (final cgTrackableLog tb : trackables) {
				if (tb.id == realViewId) menu.setHeaderTitle(tb.name);
			}
			for (final int logTbAction : base.logTypesTrackable.keySet()) {
				menu.add(realViewId, logTbAction, 0, base.logTypesTrackable.get(logTbAction));
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int group = item.getGroupId();
		final int id = item.getItemId();

		if (group == R.id.type) {
			setType(id);

			return true;
		} else {
			try {
				final String logTbAction = base.logTypesTrackable.get(id);
				if (logTbAction != null) {
					final RelativeLayout tbView = (RelativeLayout)findViewById(group);
					if (tbView == null) return false;

					final TextView tbText = (TextView)tbView.findViewById(R.id.action);
					if (tbText == null) return false;

					for (cgTrackableLog tb : trackables) {
						if (tb.id == group) {
							tb.action = id;
							tbText.setText(logTbAction);

							Log.i(cgSettings.tag, "Trackable " + tb.trackCode + " (" + tb.name + ") has new action: #" + id);
						}
					}

					return true;
				}
			} catch (Exception e) {
				Log.e(cgSettings.tag, "cgeovisit.onContextItemSelected: " + e.toString());
			}
		}

		return false;
	}

	public void init() {
		if (geocode != null) app.setAction(geocode);

		types.clear();
		
		if (cache.type.equals("event") || cache.type.equals("mega") || cache.type.equals("cito") || cache.type.equals("lostfound")) {
			types.add(9);
			types.add(10);
			types.add(7);
		} else if (cache.type.equals("earth")) {
			types.add(2);
			types.add(3);
			types.add(4);
			types.add(7);
		} else if (cache.type.equals("webcam")) {
			types.add(11);
			types.add(3);
			types.add(4);
			types.add(7);
			types.add(45);
		} else {
			types.add(2);
			types.add(3);
			types.add(4);
			types.add(7);
			types.add(45);
		}
		if (cache.owner.equalsIgnoreCase(settings.getUsername()) == true) {
			types.add(46);
		}

		if (typeSelected < 0 && base.logTypes2.get(typeSelected) == null) typeSelected = types.get(0);
        setType(typeSelected);

		Button typeButton = (Button)findViewById(R.id.type);
		registerForContextMenu(typeButton);
		typeButton.setText(base.logTypes2.get(typeSelected));
		typeButton.setClickable(true);
		typeButton.setOnTouchListener(new cgViewTouch(settings, typeButton, 0));
		typeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				openContextMenu(view);
			}
		});

		Button dateButton = (Button)findViewById(R.id.date);
		dateButton.setText(base.dateOutShort.format(date.getTime()));
		dateButton.setClickable(true);
		dateButton.setOnTouchListener(new cgViewTouch(settings, dateButton, 0));
		dateButton.setOnClickListener(new cgeovisitDateListener());

        if (tweetBox == null) tweetBox = (LinearLayout)findViewById(R.id.tweet_box);
        if (tweetCheck == null) tweetCheck = (CheckBox)findViewById(R.id.tweet);
        tweetCheck.setChecked(true);
        
		Button buttonPost = (Button)findViewById(R.id.post);
		if (viewstate == null || viewstate.length() == 0) {
			buttonPost.setClickable(false);
			buttonPost.setOnTouchListener(null);
			buttonPost.setOnClickListener(null);
			if (settings.skin == 1) buttonPost.setBackgroundResource(R.drawable.action_button_light_off);
			else buttonPost.setBackgroundResource(R.drawable.action_button_dark_off);
			
			loadData thread;
			thread = new loadData(cacheid);
			thread.start();
		} else {
			buttonPost.setClickable(true);
			buttonPost.setOnTouchListener(new cgViewTouch(settings, buttonPost, 0));
			buttonPost.setOnClickListener(new postListener());
			if (settings.skin == 1) buttonPost.setBackgroundResource(R.drawable.action_button_light);
			else buttonPost.setBackgroundResource(R.drawable.action_button_dark);
		}
	}

	public void setDate(Calendar dateIn) {
		date = dateIn;

		final Button dateButton = (Button)findViewById(R.id.date);
		dateButton.setText(base.dateOutShort.format(date.getTime()));
	}

	public void setType(int type) {
		final Button typeButton = (Button)findViewById(R.id.type);

		if (base.logTypes2.get(type) != null) typeSelected = type;
		if (base.logTypes2.get(typeSelected) == null) typeSelected = 0;
		typeButton.setText(base.logTypes2.get(typeSelected));

        if (tweetBox == null) tweetBox = (LinearLayout)findViewById(R.id.tweet_box);
        if (type == 2 && settings.twitter == 1) tweetBox.setVisibility(View.VISIBLE);
        else tweetBox.setVisibility(View.GONE);
	}

	private class cgeovisitDateListener implements View.OnClickListener {
		public void onClick(View arg0) {
			Dialog dateDialog = new cgeodate(activity, (cgeovisit)activity, date);
			dateDialog.setCancelable(true);
			dateDialog.show();
		}
	}

	private class postListener implements View.OnClickListener {
		public void onClick(View arg0) {
			if (gettingViewstate == false) {
				waitDialog = ProgressDialog.show(activity, null, "saving log...", true);
				waitDialog.setCancelable(true);

				String log = ((EditText)findViewById(R.id.log)).getText().toString();
				Thread thread = new postLog(postLogHandler, log);
				thread.start();
			} else {
				warning.showToast("c:geo is still loading data required to post log. Please wait a little while longer.");
			}
		}
	}

	private class loadData extends Thread {
		private String cacheid = null;

		public loadData(String cacheidIn) {
			cacheid = cacheidIn;

			if (cacheid == null) {
				warning.showToast("Sorry, c:geo forgot which geocache you visited.");

				finish();
				return;
			}
		}

		@Override
		public void run() {
			final HashMap<String, String> params = new HashMap<String, String>();

			showProgressHandler.sendEmptyMessage(0);
			gettingViewstate = true;
			attempts ++;

			try {
				if (cacheid != null && cacheid.length() > 0) {
					params.put("ID", cacheid);
				} else {
					loadDataHandler.sendEmptyMessage(0);
					return;
				}

				final String page = base.request("www.geocaching.com", "/seek/log.aspx", "GET", params, false, false, false);

				viewstate = base.findViewstate(page, 0);
				viewstate1 = base.findViewstate(page, 1);
				trackables = base.parseTrackableLog(page);
				
				final ArrayList<Integer> typesPre = base.parseTypes(page);
				if (typesPre.size() > 0) {
					types.clear();
					types.addAll(typesPre);
				}
				typesPre.clear();

				if (types.contains(typeSelected) == false) {
					typeSelected = types.get(0);
					setType(typeSelected);
					warning.showToast("Type of log has been changed!");
				}
			} catch (Exception e) {
				Log.e(cgSettings.tag, "cgeovisit.loadData.run: " + e.toString());
			}

			loadDataHandler.sendEmptyMessage(0);
		}
	}

	private class postLog extends Thread {
		Handler handler = null;
		String log = null;
		
		public postLog(Handler handlerIn, String logIn) {
			handler = handlerIn;
			log = logIn;
		}

		@Override
		public void run() {
			int ret = -1;

			ret = postLogFn(log);

			handler.sendEmptyMessage(ret);
		}
	}

	public int postLogFn(String log) {
		int status = -1;

		try {
			if (tweetBox == null) tweetBox = (LinearLayout)findViewById(R.id.tweet_box);
			if (tweetCheck == null) tweetCheck = (CheckBox)findViewById(R.id.tweet);

			status = base.postLog(cacheid, viewstate, viewstate1, typeSelected, date.get(Calendar.YEAR), (date.get(Calendar.MONTH ) + 1), date.get(Calendar.DATE), log, trackables);

			if (status == 1) {
				cgLog logNow = new cgLog();
				logNow.author = settings.getUsername();
				logNow.date = (new Date()).getTime();
				logNow.type = typeSelected;
				logNow.log = log;

				cache.logs.add(0, logNow);
				app.addLog(geocode, logNow);

				if (typeSelected == 2) {
					app.markFound(geocode);
					if (cache != null) cache.found = true;
				}

				if (cache != null) app.putCacheInCache(cache);
				else app.removeCacheFromCache(geocode);
			}

			if (
				status == 1 && typeSelected == 2 && settings.twitter == 1 &&
				settings.tokenPublic != null && settings.tokenPublic.length() > 0 && settings.tokenSecret != null && settings.tokenSecret.length() > 0 &&
				tweetCheck.isChecked() == true && tweetBox.getVisibility() == View.VISIBLE
			) {
				base.postTweetCache(app, settings, geocode);
			}

			return status;
		} catch (Exception e) {
            Log.e(cgSettings.tag, "cgeovisit.postLogFn: " + e.toString());
		}

		return 1000;
	}
}