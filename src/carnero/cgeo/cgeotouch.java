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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class cgeotouch extends cgLogForm {
	private cgeoapplication app = null;
	private Activity activity = null;
	private LayoutInflater inflater = null;
	private cgBase base = null;
	private cgSettings settings = null;
	private cgWarning warning = null;
	private cgTrackable trackable = null;
	private ArrayList<Integer> types = new ArrayList<Integer>();
	private ProgressDialog waitDialog = null;
	private String guid = null;
	private String geocode = null;
	private String text = null;
	private String viewstate = null;
	private String viewstate1 = null;
	private Boolean gettingViewstate = true;
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
				thread = new loadData(guid);
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
			buttonPost.setOnClickListener(new postListener());
			if (settings.skin == 1) buttonPost.setBackgroundResource(R.drawable.action_button_light);
			else buttonPost.setBackgroundResource(R.drawable.action_button_dark);

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
				if (msg.what == 1001) {
					warning.showToast("Please, fill some log text.");
				} else if(msg.what == 1002) {
					warning.showToast("Sorry, c:geo failed to post log because server is not responding.");
				} else {
					warning.showToast("Sorry, c:geo failed to post log.");
				}
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
		setTitle("touch");
		if (settings.skin == 1) setContentView(R.layout.touch_light);
		else setContentView(R.layout.touch_dark);

		// get parameters
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			geocode = extras.getString("geocode");
			guid = extras.getString("guid");
			text = extras.getString("text");
		}

		trackable = app.getTrackableByGeocode("logging trackable");

		if (trackable.name != null && trackable.name.length() > 0) setTitle("touch " + trackable.name);
		else setTitle("touch " + trackable.geocode.toUpperCase());

		app.setAction("logging trackable");

		if (trackable == null || guid == null) {
			warning.showToast("Sorry, c:geo forgot which trackable you saw.");
			
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
		subMenu.add(0, 4, 0, "signature");

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (settings.getSignature() == null) {
			menu.findItem(4).setVisible(false);
		} else {
			menu.findItem(4).setVisible(true);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		EditText text = null;
		String textContent = null;
		String dateString = null;
		String timeString = null;

		if (id >= 1 && id <= 4) {
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
		} else if (id == 4) {
			if (settings.getSignature() == null) {
				return true;
			}

			if (textContent.length() == 0) {
				text.setText(settings.getSignature() + "\n", TextView.BufferType.NORMAL);
			} else {
				text.setText(textContent + "\n" + settings.getSignature() + "\n", TextView.BufferType.NORMAL);
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
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int group = item.getGroupId();
		final int id = item.getItemId();

		if (group == R.id.type) {
			setType(id);

			return true;
		}

		return false;
	}

	public void init() {
		if (geocode != null) app.setAction("logging trackable");

		types.clear();
		types.add(13);
		types.add(19);
		types.add(4);
		types.add(48);

		if (typeSelected < 0 && base.logTypes2.get(typeSelected) == null) typeSelected = types.get(2);
		setType(typeSelected);

		Button typeButton = (Button)findViewById(R.id.type);
		registerForContextMenu(typeButton);
		typeButton.setText(base.logTypes2.get(typeSelected));
		typeButton.setClickable(true);
		typeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				openContextMenu(view);
			}
		});

		Button dateButton = (Button)findViewById(R.id.date);
		dateButton.setText(base.dateOutShort.format(date.getTime()));
		dateButton.setClickable(true);
		dateButton.setOnClickListener(new cgeotouchDateListener());

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
			thread = new loadData(guid);
			thread.start();
		} else {
			buttonPost.setClickable(true);
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
        if (settings.twitter == 1) tweetBox.setVisibility(View.VISIBLE);
        else tweetBox.setVisibility(View.GONE);
	}

	private class cgeotouchDateListener implements View.OnClickListener {
		public void onClick(View arg0) {
			Dialog dateDialog = new cgeodate(activity, (cgeotouch)activity, date);
			dateDialog.setCancelable(true);
			dateDialog.show();
		}
	}

	private class postListener implements View.OnClickListener {
		public void onClick(View arg0) {
			if (gettingViewstate == false) {
				waitDialog = ProgressDialog.show(activity, null, "saving log...", true);
				waitDialog.setCancelable(true);

				String tracking = ((EditText)findViewById(R.id.tracking)).getText().toString();
				String log = ((EditText)findViewById(R.id.log)).getText().toString();
				Thread thread = new postLog(postLogHandler, tracking, log);
				thread.start();
			} else {
				warning.showToast("c:geo is still loading data required to post log. Please wait a little while longer.");
			}
		}
	}

	private class loadData extends Thread {
		private String guid = null;

		public loadData(String guidIn) {
			guid = guidIn;

			if (guid == null) {
				warning.showToast("Sorry, c:geo forgot which trackable you saw.");

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
				if (guid != null && guid.length() > 0) {
					params.put("wid", guid);
				} else {
					loadDataHandler.sendEmptyMessage(0);
					return;
				}

				final String page = base.request("www.geocaching.com", "/track/log.aspx", "GET", params, false, false, false);

				viewstate = base.findViewstate(page, 0);
				viewstate1 = base.findViewstate(page, 1);

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
				Log.e(cgSettings.tag, "cgeotouch.loadData.run: " + e.toString());
			}

			loadDataHandler.sendEmptyMessage(0);
		}
	}

	private class postLog extends Thread {
		Handler handler = null;
		String tracking = null;
		String log = null;
		
		public postLog(Handler handlerIn, String trackingIn, String logIn) {
			handler = handlerIn;
			tracking = trackingIn;
			log = logIn;
		}

		@Override
		public void run() {
			int ret = -1;

			ret = postLogFn(tracking, log);

			handler.sendEmptyMessage(ret);
		}
	}

	public int postLogFn(String tracking, String log) {
		int status = -1;

		try {
			if (tweetBox == null) tweetBox = (LinearLayout)findViewById(R.id.tweet_box);
			if (tweetCheck == null) tweetCheck = (CheckBox)findViewById(R.id.tweet);

			status = base.postLogTrackable(guid, tracking, viewstate, viewstate1, typeSelected, date.get(Calendar.YEAR), (date.get(Calendar.MONTH ) + 1), date.get(Calendar.DATE), log);

			if (
				status == 1 && settings.twitter == 1 &&
				settings.tokenPublic != null && settings.tokenPublic.length() > 0 && settings.tokenSecret != null && settings.tokenSecret.length() > 0 &&
				tweetCheck.isChecked() == true && tweetBox.getVisibility() == View.VISIBLE
			) {
				base.postTweetTrackable(app, settings, geocode);
			}

			return status;
		} catch (Exception e) {
            Log.e(cgSettings.tag, "cgeotouch.postLogFn: " + e.toString());
		}

		return 1000;
	}
}