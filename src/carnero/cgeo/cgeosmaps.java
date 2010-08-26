package carnero.cgeo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.TextView;
import java.util.ArrayList;

public class cgeosmaps extends Activity {
	private ArrayList<Bitmap> maps = new ArrayList<Bitmap>();
	private String geocode = null;
	private cgeoapplication app = null;
	private Activity activity = null;
	private cgSettings settings = null;
	private cgBase base = null;
	private cgWarning warning = null;
	private LayoutInflater inflater = null;
	private ProgressDialog waitDialog = null;
	private LinearLayout smapsView = null;
	private BitmapFactory factory = null;

	private Handler loadMapsHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				if (maps == null || maps.isEmpty()) {
					if (waitDialog != null) waitDialog.dismiss();

					warning.showToast("Sorry, c:geo failed to load static maps.");

					finish();
					return;
				} else {
					if (waitDialog != null) waitDialog.dismiss();
					
					if (smapsView == null) smapsView = (LinearLayout)findViewById(R.id.maps_list);
					smapsView.removeAllViews();

					int cnt = 1;
					for (Bitmap image : maps) {
						if (image != null) {
							LinearLayout mapView = null;
							
							if (settings.skin == 1) mapView = (LinearLayout)inflater.inflate(R.layout.smapitem_light, null);
							else mapView = (LinearLayout)inflater.inflate(R.layout.smapitem_dark, null);

							((TextView)mapView.findViewById(R.id.title)).setText("map #" + cnt);
							((ImageView)mapView.findViewById(R.id.map_image)).setImageBitmap(image);

							smapsView.addView(mapView);
							cnt ++;
						}
					}
				}
			} catch (Exception e) {
				if (waitDialog != null) waitDialog.dismiss();
				Log.e(cgSettings.tag, "cgeosmaps.loadMapsHandler: " + e.toString());
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
		setTitle("static maps");
		if (settings.skin == 1) setContentView(R.layout.smaps_light);
		else setContentView(R.layout.smaps_dark);

		// get parameters
		Bundle extras = getIntent().getExtras();

		// try to get data from extras
		if (extras != null) {
			geocode = extras.getString("geocode");
		}

		if (geocode == null) {
			warning.showToast("Sorry, c:geo forgot for what cache you want to load static maps.");
			finish();
			return;
		}

		inflater = activity.getLayoutInflater();

		waitDialog = ProgressDialog.show(this, null, "loading static maps...", true);
		waitDialog.setCancelable(true);

		(new loadMaps()).start();
	}

	private class loadMaps extends Thread {
	   @Override
	   public void run() {
			try {
				if (factory == null) factory = new BitmapFactory();

				for (int level = 1; level <= 5; level ++) {
					try {
						Bitmap image = factory.decodeFile(settings.getStorage() + geocode + "/map_" + level);
						if (image != null) maps.add(image);
					} catch (Exception e) {
						Log.e(cgSettings.tag, "cgeosmaps.loadMaps.run.1: " + e.toString());
					}
				}

				if (maps.isEmpty() == true) {
					for (int level = 1; level <= 5; level ++) {
						try {
							Bitmap image = factory.decodeFile(settings.getStorageSec() + geocode + "/map_" + level);
							if (image != null) maps.add(image);
						} catch (Exception e) {
							Log.e(cgSettings.tag, "cgeosmaps.loadMaps.run.2: " + e.toString());
						}
					}
				}

				loadMapsHandler.sendMessage(new Message());
			} catch (Exception e) {
				Log.e(cgSettings.tag, "cgeosmaps.loadMaps.run: " + e.toString());
			}
	   }
	}
}
