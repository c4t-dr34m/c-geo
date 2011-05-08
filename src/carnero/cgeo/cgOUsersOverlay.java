package carnero.cgeo;

import android.graphics.Point;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedOverlay;

public class cgOUsersOverlay extends ItemizedOverlay<cgOOverlayUser> {

	private ArrayList<cgOOverlayUser> items = new ArrayList<cgOOverlayUser>();
	private Context context = null;
	private final Pattern patternGeocode = Pattern.compile("^(GC[A-Z0-9]+)(\\: ?(.+))?$", Pattern.CASE_INSENSITIVE);

	public cgOUsersOverlay(Context contextIn, Drawable markerIn) {
		super(markerIn, new DefaultResourceProxyImpl(contextIn));
		populate();

		context = contextIn;
	}

	protected void updateItems(cgOOverlayUser item) {
		ArrayList<cgOOverlayUser> itemsPre = new ArrayList<cgOOverlayUser>();
		itemsPre.add(item);
		
		updateItems(itemsPre);
	}

	protected void updateItems(ArrayList<cgOOverlayUser> itemsPre) {
		if (itemsPre == null) {
			return;
		}

		for (cgOOverlayUser item : itemsPre) {
			item.setMarker(item.getMarker(0));
		}

		items.clear();
		
		if (itemsPre.size() > 0) {
			items = (ArrayList<cgOOverlayUser>) itemsPre.clone();
		}
		
		populate();
	}

	public boolean onSnapToItem(int i1, int i2, Point point, MapView view) {
		return false;
	}
	
	protected boolean onTap(int index) {
		try {
			if (items.size() <= index) {
				return false;
			}

			final cgOOverlayUser item = items.get(index);
			final cgUser user = item.getUser();

			// set action
			String action = null;
			String geocode = null;
			final Matcher matcherGeocode = patternGeocode.matcher(user.action.trim());

			if (user.action.length() == 0 || user.action.equalsIgnoreCase("pending")) {
				action = "Looking around";
			} else if (user.action.equalsIgnoreCase("tweeting")) {
				action = "Tweeting";
			} else if (matcherGeocode.find() == true) {
				if (matcherGeocode.group(1) != null) {
					geocode = matcherGeocode.group(1).trim().toUpperCase();
				}
				if (matcherGeocode.group(3) != null) {
					action = "Heading to " + geocode + " (" + matcherGeocode.group(3).trim() + ")";
				} else {
					action = "Heading to " + geocode;
				}
			} else {
				action = user.action;
			}

			// set icon
			int icon = -1;
			if (user.client.equalsIgnoreCase("c:geo") == true) {
				icon = R.drawable.client_cgeo;
			} else if (user.client.equalsIgnoreCase("preCaching") == true) {
				icon = R.drawable.client_precaching;
			} else if (user.client.equalsIgnoreCase("Handy Geocaching") == true) {
				icon = R.drawable.client_handygeocaching;
			}

			final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
			if (icon > -1) {
				dialog.setIcon(icon);
			}
			dialog.setTitle(user.username);
			dialog.setMessage(action);
			dialog.setCancelable(true);
			if (geocode != null && geocode.length() > 0) {
				dialog.setPositiveButton(geocode + "?", new cacheDetails(geocode));
			}
			dialog.setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});

			AlertDialog alert = dialog.create();
			alert.show();

			return true;
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgOUsersOverlay.onTap: " + e.toString());
		}

		return false;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, false);
	}

	@Override
	public cgOOverlayUser createItem(int index) {
		try {
			return items.get(index);
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgOUsersOverlay.createItem: " + e.toString());
		}

		return null;
	}

	@Override
	public int size() {
		try {
			return items.size();
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgOUsersOverlay.size: " + e.toString());
		}

		return 0;
	}

	private class cacheDetails implements DialogInterface.OnClickListener {

		private String geocode = null;

		public cacheDetails(String geocodeIn) {
			geocode = geocodeIn;
		}

		public void onClick(DialogInterface dialog, int id) {
			if (geocode != null) {
				Intent detailIntent = new Intent(context, cgeodetail.class);
				detailIntent.putExtra("geocode", geocode);
				context.startActivity(detailIntent);
			}

			dialog.cancel();
		}
	}
}
