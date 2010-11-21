package carnero.cgeo;

import java.util.HashMap;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.app.Activity;
import android.app.ProgressDialog;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.widget.ScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.SubMenu;
import android.widget.ImageView;

public class cgeotrackable extends Activity {
	public cgTrackable trackable = null;
	public String geocode = null;
	public String name = null;
	public String guid = null;
	private cgeoapplication app = null;
	private Activity activity = null;
	private LayoutInflater inflater = null;
	private cgSettings settings = null;
	private cgBase base = null;
	private cgWarning warning = null;
	private ProgressDialog waitDialog = null;
	private Handler loadTrackableHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			RelativeLayout itemLayout;
			TextView itemName;
			TextView itemValue;

			if (trackable != null && trackable.errorRetrieve != 0) {
				warning.showToast("Sorry, c:geo failed to download trackable details because of " + base.errorRetrieve.get(trackable.errorRetrieve) + ".");

				finish();
				return;
			}

			if (trackable != null && trackable.error.length() > 0) {
				warning.showToast("Sorry, c:geo failed to download trackable details because of " + trackable.error + ".");

				finish();
				return;
			}

			if (trackable == null) {
				if (waitDialog != null) {
					waitDialog.dismiss();
				}

				if (geocode != null && geocode.length() > 0) {
					warning.showToast("Sorry, c:geo can\'t find trackable " + geocode + ".");
				} else {
					warning.showToast("Sorry, c:geo can\'t find that trackable.");
				}

				finish();
				return;
			}

			try {
				inflater = activity.getLayoutInflater();
				geocode = trackable.geocode.toUpperCase();

				if (trackable.name != null && trackable.name.length() > 0) {
					setTitle(Html.fromHtml(trackable.name).toString());
				} else {
					setTitle(trackable.name.toUpperCase());
				}

				((ScrollView) findViewById(R.id.details_list_box)).setVisibility(View.VISIBLE);
				LinearLayout detailsList = (LinearLayout) findViewById(R.id.details_list);

				// trackable geocode
				itemLayout = (RelativeLayout)inflater.inflate(R.layout.cache_item, null);
				itemName = (TextView) itemLayout.findViewById(R.id.name);
				itemValue = (TextView) itemLayout.findViewById(R.id.value);

				itemName.setText("tb-code");
				itemValue.setText(trackable.geocode.toUpperCase());
				detailsList.addView(itemLayout);

				// trackable name
				itemLayout = (RelativeLayout)inflater.inflate(R.layout.cache_item, null);
				itemName = (TextView) itemLayout.findViewById(R.id.name);
				itemValue = (TextView) itemLayout.findViewById(R.id.value);

				itemName.setText("name");
				if (trackable.name != null) {
					itemValue.setText(Html.fromHtml(trackable.name), TextView.BufferType.SPANNABLE);
				} else {
					itemValue.setText("unknown");
				}
				detailsList.addView(itemLayout);

				// trackable type
				itemLayout = (RelativeLayout)inflater.inflate(R.layout.cache_item, null);
				itemName = (TextView) itemLayout.findViewById(R.id.name);
				itemValue = (TextView) itemLayout.findViewById(R.id.value);

				itemName.setText("type");
				if (trackable.type != null) {
					itemValue.setText(Html.fromHtml(trackable.type), TextView.BufferType.SPANNABLE);
				} else {
					itemValue.setText("unknown");
				}
				detailsList.addView(itemLayout);

				// trackable owner
				itemLayout = (RelativeLayout)inflater.inflate(R.layout.cache_item, null);
				itemName = (TextView) itemLayout.findViewById(R.id.name);
				itemValue = (TextView) itemLayout.findViewById(R.id.value);

				itemName.setText("owner");
				if (trackable.owner != null) {
					itemValue.setText(Html.fromHtml(trackable.owner), TextView.BufferType.SPANNABLE);
				} else {
					itemValue.setText("unknown");
				}
				detailsList.addView(itemLayout);

				// trackable spotted
				if (trackable.spottedName != null && trackable.spottedName.length() > 0) {
					itemLayout = (RelativeLayout)inflater.inflate(R.layout.cache_item, null);
					itemName = (TextView) itemLayout.findViewById(R.id.name);
					itemValue = (TextView) itemLayout.findViewById(R.id.value);

					itemName.setText("spotted");
					itemValue.setText(Html.fromHtml(trackable.spottedName), TextView.BufferType.SPANNABLE);
					itemLayout.setClickable(true);
					itemLayout.setOnClickListener(new View.OnClickListener() {

						public void onClick(View arg0) {
							Intent cacheIntent = new Intent(activity, cgeodetail.class);
							cacheIntent.putExtra("guid", (String) trackable.spottedGuid);
							cacheIntent.putExtra("name", (String) trackable.spottedName);

							activity.startActivity(cacheIntent);
						}
					});
					detailsList.addView(itemLayout);
				}

				// trackable origin
				if (trackable.origin != null && trackable.origin.length() > 0) {
					itemLayout = (RelativeLayout)inflater.inflate(R.layout.cache_item, null);
					itemName = (TextView) itemLayout.findViewById(R.id.name);
					itemValue = (TextView) itemLayout.findViewById(R.id.value);

					itemName.setText("origin");
					itemValue.setText(Html.fromHtml(trackable.origin), TextView.BufferType.SPANNABLE);
					detailsList.addView(itemLayout);
				}

				// trackable released
				if (trackable.released != null) {
					itemLayout = (RelativeLayout)inflater.inflate(R.layout.cache_item, null);
					itemName = (TextView) itemLayout.findViewById(R.id.name);
					itemValue = (TextView) itemLayout.findViewById(R.id.value);

					itemName.setText("released");
					itemValue.setText(base.dateOut.format(trackable.released));
					detailsList.addView(itemLayout);
				}

				// trackable goal
				if (trackable.goal != null && trackable.goal.length() > 0) {
					((LinearLayout) findViewById(R.id.goal_box)).setVisibility(View.VISIBLE);
					TextView descView = (TextView) findViewById(R.id.goal);
					descView.setVisibility(View.VISIBLE);
					descView.setText(Html.fromHtml(trackable.goal, new cgHtmlImg(activity, settings, geocode, true, 0, false), null), TextView.BufferType.SPANNABLE);
					descView.setMovementMethod(LinkMovementMethod.getInstance());
				}

				// trackable details
				if (trackable.details != null && trackable.details.length() > 0) {
					((LinearLayout) findViewById(R.id.details_box)).setVisibility(View.VISIBLE);
					TextView descView = (TextView) findViewById(R.id.details);
					descView.setVisibility(View.VISIBLE);
					descView.setText(Html.fromHtml(trackable.details, new cgHtmlImg(activity, settings, geocode, true, 0, false), null), TextView.BufferType.SPANNABLE);
					descView.setMovementMethod(LinkMovementMethod.getInstance());
				}

				// trackable image
				if (trackable.image != null && trackable.image.length() > 0) {
					((LinearLayout) findViewById(R.id.image_box)).setVisibility(View.VISIBLE);
					LinearLayout imgView = (LinearLayout) findViewById(R.id.image);

					final ImageView trackableImage = (ImageView) inflater.inflate(R.layout.trackable_image, null);

					trackableImage.setImageResource(R.drawable.image_not_loaded);
					trackableImage.setClickable(true);
					trackableImage.setOnClickListener(new View.OnClickListener() {

						public void onClick(View arg0) {
							activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(trackable.image)));
						}
					});

					// try to load image
					final Handler handler = new Handler() {

						@Override
						public void handleMessage(Message message) {
							BitmapDrawable image = (BitmapDrawable) message.obj;
							if (image != null) {
								trackableImage.setImageDrawable((BitmapDrawable) message.obj);
							}
						}
					};

					new Thread() {

						@Override
						public void run() {
							BitmapDrawable image = null;
							try {
								cgHtmlImg imgGetter = new cgHtmlImg(activity, settings, geocode, true, 0, false);

								image = imgGetter.getDrawable(trackable.image);
								Message message = handler.obtainMessage(0, image);
								handler.sendMessage(message);
							} catch (Exception e) {
								Log.e(cgSettings.tag, "cgeospoilers.onCreate.onClick.run: " + e.toString());
							}
						}
					}.start();

					imgView.addView(trackableImage);
				}
			} catch (Exception e) {
				Log.e(cgSettings.tag, "cgeotrackable.loadTrackableHandler: " + e.toString());
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
		app = (cgeoapplication) this.getApplication();
		settings = new cgSettings(this, getSharedPreferences(cgSettings.preferences, 0));
		base = new cgBase(app, settings, getSharedPreferences(cgSettings.preferences, 0));
		warning = new cgWarning(this);

		// set layout
		if (settings.skin == 1) {
			setTheme(R.style.light);
		} else {
			setTheme(R.style.dark);
		}
		setTitle("trackable");
		setContentView(R.layout.trackable_detail);

		// google analytics
		base.sendAnal(activity, "/trackable/detail");

		// get parameters
		Bundle extras = getIntent().getExtras();
		Uri uri = getIntent().getData();

		// try to get data from extras
		if (extras != null) {
			geocode = extras.getString("geocode");
			name = extras.getString("name");
			guid = extras.getString("guid");
		}

		// try to get data from URI
		if (geocode == null && guid == null && uri != null) {
			geocode = uri.getQueryParameter("tracker");
			guid = uri.getQueryParameter("guid");

			if (geocode != null && geocode.length() > 0) {
				geocode = geocode.toUpperCase();
				guid = null;
			} else if (guid != null && guid.length() > 0) {
				geocode = null;
				guid = guid.toLowerCase();
			} else {
				warning.showToast("Sorry, c:geo can\'t open trackable details.");
				finish();
				return;
			}
		}

		// no given data
		if (geocode == null && guid == null) {
			warning.showToast("Sorry, c:geo can\'t display trackable you want. Is it really trackable?");
			finish();
			return;
		}

		if (name != null && name.length() > 0) {
			waitDialog = ProgressDialog.show(this, Html.fromHtml(name).toString(), "loading trackable details...", true);
		} else if (geocode != null && geocode.length() > 0) {
			waitDialog = ProgressDialog.show(this, geocode.toUpperCase(), "loading trackable details...", true);
		} else {
			waitDialog = ProgressDialog.show(this, "cache", "loading trackable details...", true);
		}
		waitDialog.setCancelable(true);

		loadTrackable thread;
		thread = new loadTrackable(loadTrackableHandler, geocode, guid);
		thread.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 0, "log touch").setIcon(android.R.drawable.ic_menu_agenda); // log touch

		SubMenu subMenu = menu.addSubMenu(1, 0, 0, "get more").setIcon(android.R.drawable.ic_menu_more);
		subMenu.add(1, 2, 0, "open in browser"); // browser
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case 1:
				logTouch();
				return true;
			case 2:
				activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.geocaching.com/track/details.aspx?tracker=" + trackable.geocode)));
				return true;
		}

		return false;
	}

	private class loadTrackable extends Thread {

		private Handler handler = null;
		private String geocode = null;
		private String guid = null;

		public loadTrackable(Handler handlerIn, String geocodeIn, String guidIn) {
			handler = handlerIn;
			geocode = geocodeIn;
			guid = guidIn;

			if (geocode == null && guid == null) {
				warning.showToast("Sorry, c:geo forgot which trackable you want.");

				stop();
				finish();
				return;
			}
		}

		@Override
		public void run() {
			loadTrackableFn(geocode, guid);
			handler.sendMessage(new Message());
		}
	}

	public void loadTrackableFn(String geocode, String guid) {
		HashMap<String, String> params = new HashMap<String, String>();
		if (geocode != null && geocode.length() > 0) {
			params.put("geocode", geocode);
		} else if (guid != null && guid.length() > 0) {
			params.put("guid", guid);
		} else {
			return;
		}

		trackable = base.searchTrackable(params);
	}

	private void logTouch() {
		Intent logTouchIntent = new Intent(activity, cgeotouch.class);
		logTouchIntent.putExtra("geocode", trackable.geocode.toUpperCase());
		logTouchIntent.putExtra("guid", trackable.guid);
		activity.startActivity(logTouchIntent);
	}
}
