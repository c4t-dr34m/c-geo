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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class cgeovisit extends Activity {
	private cgeoapplication app = null;
	private Activity activity = null;
	private cgBase base = null;
	private cgSettings settings = null;
	private cgWarning warning = null;
	private ProgressDialog waitDialog = null;
	private String cacheid = null;
	private String geocode = null;
	private String type = null;
	private String text = null;
	private String viewstate = null;
	private Boolean gettingViewstate = true;
	private Calendar date = Calendar.getInstance();
	private int typeSelected = 2;
    private int attempts = 0;
	private boolean progressBar = false;

	private Handler showProgressHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (progressBar == true) setProgressBarIndeterminateVisibility(true);
		}
	};

	private Handler loadViewstateHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if ((viewstate == null || viewstate.length() == 0) && attempts < 2) {
				warning.showToast("Sorry, c:geo can\'t load data required to log visit. Trying again.");

				loadViewstate thread;
				thread = new loadViewstate(loadViewstateHandler, cacheid);
				thread.start();
				
				return;
			} else if ((viewstate == null || viewstate.length() == 0) && attempts >= 2) {
				warning.showToast("Sorry, c:geo can\'t load data required to log visit.");

                return;
            }

			gettingViewstate = false; // we're done, user can post log

			Button buttonPost = (Button)findViewById(R.id.post);
			buttonPost.setClickable(true);
			buttonPost.setOnTouchListener(new cgViewTouch(settings, buttonPost));
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
			type = extras.getString("type");
			text = extras.getString("text");
		}

		if ((cacheid == null || cacheid.length() == 0) && geocode != null && geocode.length() > 0) cacheid = app.getCacheid(geocode);
		if ((geocode == null || geocode.length() == 0) && cacheid != null && cacheid.length() > 0) geocode = app.getGeocode(cacheid);

		app.setAction(geocode);

		if (cacheid == null || cacheid.length() == 0 || geocode == null || geocode.length() == 0) {
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

	public void init() {
		if (geocode != null) app.setAction(geocode);

		if (base.logTypes2.get(typeSelected) == null) typeSelected = 2;

		Button typeButton = (Button)findViewById(R.id.type);
		typeButton.setText(base.logTypes2.get(typeSelected));
		typeButton.setClickable(true);
		typeButton.setOnTouchListener(new cgViewTouch(settings, typeButton));
		typeButton.setOnClickListener(new cgeovisitTypeListener());

		Button dateButton = (Button)findViewById(R.id.date);
		dateButton.setText(base.dateOutShort.format(date.getTime()));
		dateButton.setClickable(true);
		dateButton.setOnTouchListener(new cgViewTouch(settings, dateButton));
		dateButton.setOnClickListener(new cgeovisitDateListener());

		Button buttonPost = (Button)findViewById(R.id.post);
		if (viewstate == null || viewstate.length() == 0) {
			buttonPost.setClickable(false);
			buttonPost.setOnTouchListener(null);
			buttonPost.setOnClickListener(null);
			if (settings.skin == 1) buttonPost.setBackgroundResource(R.drawable.action_button_light_off);
			else buttonPost.setBackgroundResource(R.drawable.action_button_dark_off);
			
			loadViewstate thread;
			thread = new loadViewstate(loadViewstateHandler, cacheid);
			thread.start();
		} else {
			buttonPost.setClickable(true);
			buttonPost.setOnTouchListener(new cgViewTouch(settings, buttonPost));
			buttonPost.setOnClickListener(new postListener());
			if (settings.skin == 1) buttonPost.setBackgroundResource(R.drawable.action_button_light);
			else buttonPost.setBackgroundResource(R.drawable.action_button_dark);
		}
	}

	public void setDate(Calendar dateIn) {
		date = dateIn;

		Button dateButton = (Button)findViewById(R.id.date);
		dateButton.setText(base.dateOutShort.format(date.getTime()));
	}

	public void setType(int type) {
		if (base.logTypes2.get(type) != null) {
			typeSelected = type;
		}
		
		Button typeButton = (Button)findViewById(R.id.type);
		if (base.logTypes2.get(typeSelected) == null) {
			typeSelected = 0;
		}
		typeButton.setText(base.logTypes2.get(typeSelected));
	}

	public void showDatePicker() {
		Dialog dateDialog = new cgeodate(activity, this, date);
		dateDialog.setCancelable(true);
		dateDialog.show();
	}

	public void showTypePicker() {
		Dialog typeDialog = new cgeotypes(activity, this, settings, base, type);
		typeDialog.setCancelable(true);
		typeDialog.show();
	}

	private class cgeovisitDateListener implements View.OnClickListener {
		public void onClick(View arg0) {
			showDatePicker();
		}
	}

	private class cgeovisitTypeListener implements View.OnClickListener {
		public void onClick(View arg0) {
			showTypePicker();
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

	private class loadViewstate extends Thread {
		private Handler handler = null;
		private String cacheid = null;

		public loadViewstate(Handler handlerIn, String cacheidIn) {
			handler = handlerIn;
			cacheid = cacheidIn;

			if (cacheid == null) {
				warning.showToast("Sorry, c:geo forgot which geocache you visited.");

				finish();
				return;
			}
		}

		@Override
		public void run() {
			showProgressHandler.sendEmptyMessage(0);
            gettingViewstate = true;
			
            attempts ++;
			loadViewstateFn(cacheid);
			handler.sendEmptyMessage(0);
		}
	}

	public void loadViewstateFn(String cacheid) {
		HashMap<String, String> params = new HashMap<String, String>();
		if (cacheid != null && cacheid.length() > 0) params.put("ID", cacheid);
		else return;

		try {
			viewstate = base.requestViewstate("www.geocaching.com", "/seek/log.aspx", "GET", params, false, false);
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgeovisit.loadViewstateFn: " + e.toString());
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
			HashMap<String, String> parameters = new HashMap<String, String>();

			parameters.put("cacheid", cacheid);
			parameters.put("viewstate", viewstate);
			parameters.put("logtype", Integer.toString(typeSelected));
			parameters.put("year", Integer.toString(date.get(Calendar.YEAR)));
			parameters.put("month", Integer.toString(date.get(Calendar.MONTH ) + 1));
			parameters.put("day", Integer.toString(date.get(Calendar.DATE)));
			parameters.put("log", log);

			status = base.postLog(parameters);

			if (
				status == 1 && typeSelected == 2 && settings.twitter == 1 &&
				settings.tokenPublic != null && settings.tokenPublic.length() > 0 && settings.tokenSecret != null && settings.tokenSecret.length() > 0
				) {
				base.postTweet(app, settings, geocode);
			}

			return status;
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgeovisit.postLogFn: " + e.toString());
		}

		return 1000;
	}
}
