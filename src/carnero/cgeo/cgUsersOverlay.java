package carnero.cgeo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import java.util.ArrayList;

public class cgUsersOverlay extends ItemizedOverlay<cgOverlayUser> {
	private ArrayList<cgOverlayUser> items = new ArrayList<cgOverlayUser>();
	private cgeoapplication app = null;
	private Context context = null;
	private cgBase base = null;
	private Drawable marker = null;
	private boolean canTap = true;

	public cgUsersOverlay(cgeoapplication appIn, Context contextIn, cgBase baseIn, Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		populate();

		app = appIn;
		context = contextIn;
		base = baseIn;
		marker = defaultMarker;
	}

	protected void addItem(cgOverlayUser item) {
		if (item == null) return;
		
		item.setMarker(boundCenter(item.getMarker(0)));
		items.add(item);
		
		if (items.size() > 0) {
			setLastFocusedIndex(-1); // to reset tap during data change
			populate();
		}
	}

	protected void removeItem(int index) {
		try {
			items.remove(index);
			setLastFocusedIndex(-1);
			populate();
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgUsersOverlay.removeItem: " + e.toString());
		}
	}

	protected void clearItems() {
		try {
			items.clear();
			setLastFocusedIndex(-1);
			populate();
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgUsersOverlay.clearItems: " + e.toString());
		}
	}

	protected void disableTap() {
		canTap = false;
	}

	protected void enableTap() {
		canTap = true;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, false);
	}

	@Override
	public cgOverlayUser createItem(int index) {
		try {
			return items.get(index);
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgUsersOverlay.draw: " + e.toString());
		}

		return null;
	}

	@Override
	public int size() {
		try {
			return items.size();
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgUsersOverlay.size: " + e.toString());
		}

		return 0;
	}
}
