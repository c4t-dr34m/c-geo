package carnero.cgeo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.net.Uri;
import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Button;
import android.widget.LinearLayout;

public class cgeowaypoint extends Activity {
	private cgWaypoint waypoint = null;
	private int id = -1;
	private cgeoapplication app = null;
	private Activity activity = null;
	private cgSettings settings = null;
	private cgBase base = null;
	private cgWarning warning = null;
	private ProgressDialog waitDialog = null;
	private cgGeo geo = null;
	private cgUpdateLoc geoUpdate = new update();

	private Handler loadWaypointHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				if (waypoint == null) {
					if (waitDialog != null) {
						waitDialog.dismiss();
						waitDialog = null;
					}

					warning.showToast("Sorry, c:geo failed to load waypoint.");

					finish();
					return;
				} else {
					final TextView name = (TextView)findViewById(R.id.name);
					final TextView identification = (TextView)findViewById(R.id.identification);
					final LinearLayout notePart = (LinearLayout)findViewById(R.id.note_part);
					final TextView note = (TextView)findViewById(R.id.note);
					final LinearLayout navigationPart = (LinearLayout)findViewById(R.id.navigation_part);

					if (waypoint.type == null || waypoint.type.length() == 0 || waypoint.type.equalsIgnoreCase("own") == true) setTitle("waypoint");
					else setTitle(base.waypointTypes.get(waypoint.type));

					name.setText(Html.fromHtml(waypoint.name.trim()), TextView.BufferType.SPANNABLE);

					if (waypoint.prefix.equalsIgnoreCase("OWN") == false) identification.setText(waypoint.prefix.trim() + "/" + waypoint.lookup.trim());
					else identification.setText("custom");
					
					if (waypoint.note != null && waypoint.note.length() > 0) {
						note.setText(Html.fromHtml(waypoint.note.trim()), TextView.BufferType.SPANNABLE);
						notePart.setVisibility(View.VISIBLE);
					}

					if (waypoint.latitude != null && waypoint.longitude != null) {
						Button buttonMap = (Button)findViewById(R.id.map);
						buttonMap.setClickable(true);
						buttonMap.setOnTouchListener(new cgViewTouch(settings, buttonMap));
						buttonMap.setOnClickListener(new mapToListener(waypoint.latitude, waypoint.longitude));

						Button buttonCompass = (Button)findViewById(R.id.compass);
						buttonCompass.setClickable(true);
						buttonCompass.setOnTouchListener(new cgViewTouch(settings, buttonCompass));
						buttonCompass.setOnClickListener(new navigateToListener(waypoint.latitude, waypoint.longitude, waypoint.name, ""));

						Button buttonRadar = (Button)findViewById(R.id.radar);
						if (base.isIntentAvailable(activity, "com.google.android.radar.SHOW_RADAR") == true) {
							buttonRadar.setClickable(true);
							buttonRadar.setOnTouchListener(new cgViewTouch(settings, buttonRadar));
							buttonRadar.setOnClickListener(new radarToListener(waypoint.latitude, waypoint.longitude));
						} else {
							buttonRadar.setVisibility(View.GONE);
						}

						Button buttonTurn = (Button)findViewById(R.id.turn);
						buttonTurn.setClickable(true);
						buttonTurn.setOnTouchListener(new cgViewTouch(settings, buttonTurn));
						buttonTurn.setOnClickListener(new turnToListener(waypoint.latitude, waypoint.longitude));

						navigationPart.setVisibility(View.VISIBLE);
					}

					Button buttonEdit = (Button)findViewById(R.id.edit);
					buttonEdit.setClickable(true);
					buttonEdit.setOnTouchListener(new cgViewTouch(settings, buttonEdit));
					buttonEdit.setOnClickListener(new editWaypointListener(waypoint.id));

					Button buttonDelete = (Button)findViewById(R.id.delete);
					if (waypoint.type != null && waypoint.type.equalsIgnoreCase("own") == true) {
						buttonDelete.setClickable(true);
						buttonDelete.setOnTouchListener(new cgViewTouch(settings, buttonDelete));
						buttonDelete.setOnClickListener(new deleteWaypointListener(waypoint.id));
						buttonDelete.setVisibility(View.VISIBLE);
					}

					if (waitDialog != null) {
						waitDialog.dismiss();
						waitDialog = null;
					}
				}
			} catch (Exception e) {
				if (waitDialog != null) {
					waitDialog.dismiss();
					waitDialog = null;
				}
				Log.e(cgSettings.tag, "cgeowaypoint.loadWaypointHandler: " + e.toString());
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
		setTitle("waypoint");
		if (settings.skin == 1) setContentView(R.layout.waypoint_light);
		else setContentView(R.layout.waypoint_dark);

		// get parameters
		Bundle extras = getIntent().getExtras();

		// try to get data from extras
		if (extras != null) {
			id = extras.getInt("waypoint");
		}

		if (id <= 0) {
			warning.showToast("Sorry, c:geo forgot for what waypoint you want to display.");
			finish();
			return;
		}

        if (geo == null) geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);
		
		waitDialog = ProgressDialog.show(this, null, "loading waypoint...", true);
		waitDialog.setCancelable(true);

		(new loadWaypoint()).start();
	}

	@Override
	public void onResume() {
		super.onResume();

        if (geo == null) geo = app.startGeo(activity, geoUpdate, base, settings, warning, 0, 0);

		if (waitDialog == null) {
			waitDialog = ProgressDialog.show(this, null, "loading waypoint...", true);
			waitDialog.setCancelable(true);

			(new loadWaypoint()).start();
		}
	}

	@Override
	public void onDestroy() {
		if (geo != null) geo = app.removeGeo(geo);

		super.onDestroy();
	}

	@Override
	public void onStop() {
		if (geo != null) geo = app.removeGeo(geo);

		super.onStop();
	}

	@Override
	public void onPause() {
		if (geo != null) geo = app.removeGeo(geo);

		super.onPause();
	}

	private class loadWaypoint extends Thread {
	   @Override
	   public void run() {
			try {
				waypoint = app.loadWaypoint(id);

				loadWaypointHandler.sendMessage(new Message());
			} catch (Exception e) {
				Log.e(cgSettings.tag, "cgeowaypoint.loadWaypoint.run: " + e.toString());
			}
	   }
	}

	private class update extends cgUpdateLoc {
		@Override
		public void updateLoc(cgGeo geo) {
			// nothing
		}
	}

	private class mapToListener implements View.OnClickListener {
		private Double latitude = null;
		private Double longitude = null;

		public mapToListener(Double latitudeIn, Double longitudeIn) {
			latitude = latitudeIn;
			longitude = longitudeIn;
		}

		public void onClick(View arg0) {
			Intent mapIntent = new Intent(activity, cgeomap.class);
			mapIntent.putExtra("latitude", latitude);
			mapIntent.putExtra("longitude", longitude);
			activity.startActivity(mapIntent);
		}
	}

	private class navigateToListener implements View.OnClickListener {
		private Double latitude = null;
		private Double longitude = null;
		private String geocode = null;
		private String name = null;

		public navigateToListener(Double latitudeIn, Double longitudeIn, String nameIn, String geocodeIn) {
			latitude = latitudeIn;
			longitude = longitudeIn;
			geocode = geocodeIn;
			name = nameIn;
		}

		public void onClick(View arg0) {
			final cgeonavigate navigateActivity = new cgeonavigate();

			Intent navigateIntent = new Intent(activity, navigateActivity.getClass());
			navigateIntent.putExtra("latitude", latitude);
			navigateIntent.putExtra("longitude", longitude);
			navigateIntent.putExtra("geocode", geocode.toUpperCase());
			navigateIntent.putExtra("name", name);

			final cgCoord coords = new cgCoord();
			coords.type = "waypoint";
			coords.name = waypoint.name;
			coords.latitude = waypoint.latitude;
			coords.longitude = waypoint.longitude;

			if (navigateActivity.coordinates != null) navigateActivity.coordinates.clear();
			navigateActivity.coordinates.add(coords);
			activity.startActivity(navigateIntent);
		}
	}

	private class radarToListener implements View.OnClickListener {
		private Double latitude = null;
		private Double longitude = null;

		public radarToListener(Double latitudeIn, Double longitudeIn) {
			latitude = latitudeIn;
			longitude = longitudeIn;
		}

		public void onClick(View arg0) {
			try {
				Intent radarIntent = new Intent("com.google.android.radar.SHOW_RADAR");
				radarIntent.putExtra("latitude", new Float(latitude));
				radarIntent.putExtra("longitude", new Float(longitude));
				activity.startActivity(radarIntent);
			} catch (Exception e) {
				warning.showToast("c:geo can\'t use Radar because this application isn't installed.");
				Log.w(cgSettings.tag, "Radar not installed");
			}
		}
	}

	private class turnToListener implements View.OnClickListener {
		private Double latitude = null;
		private Double longitude = null;

		public turnToListener(Double latitudeIn, Double longitudeIn) {
			latitude = latitudeIn;
			longitude = longitudeIn;
		}

		public void onClick(View arg0) {
			if (settings.useGNavigation == 1) {
				try {
					// turn-by-turn navigation
					activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q="+ latitude + "," + longitude)));
				} catch (Exception e) {
					try {
						// google maps directions
						if (geo != null && geo.latitudeNow != null && geo.longitudeNow != null) {
							activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?f=d&saddr="+ geo.latitudeNow + "," + geo.longitudeNow + "&daddr="+ latitude + "," + longitude)));
						} else {
							activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?f=d&daddr="+ latitude + "," + longitude)));
						}
					} catch (Exception e2) {
						Log.d(cgSettings.tag, "cgeodetail.turnTo: No navigation application available.");
						warning.showToast("c:geo can\'t find any supported navigation.");
					}
				}
			} else if (settings.useGNavigation == 0) {
				try {
					// google maps directions
					if (geo != null && geo.latitudeNow != null && geo.longitudeNow != null) {
						activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?f=d&saddr="+ geo.latitudeNow + "," + geo.longitudeNow + "&daddr="+ latitude + "," + longitude)));
					} else {
						activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?f=d&daddr="+ latitude + "," + longitude)));
					}
				} catch (Exception e) {
					Log.d(cgSettings.tag, "cgeodetail.turnTo: No navigation application available.");
					warning.showToast("c:geo can\'t find any suitable application.");
				}
			}
		}
	}

	private class editWaypointListener implements View.OnClickListener {
		private int id = -1;

		public editWaypointListener(int idIn) {
			id = idIn;
		}

		public void onClick(View arg0) {
			Intent editIntent = new Intent(activity, cgeowaypointadd.class);
			editIntent.putExtra("waypoint", id);
			activity.startActivity(editIntent);
		}
	}

	private class deleteWaypointListener implements View.OnClickListener {
		private Integer id = null;

		public deleteWaypointListener(int idIn) {
			id = idIn;
		}

		public void onClick(View arg0) {
            if (app.deleteWaypoint(id) == false) {
                warning.showToast("Sorry, c:geo can\'t delete waypoint.");
            } else {
                finish();
				return;
            }
		}
	}
}
