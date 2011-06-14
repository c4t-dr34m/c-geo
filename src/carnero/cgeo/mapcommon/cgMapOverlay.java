package carnero.cgeo.mapcommon;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.location.Location;
import android.text.Html;
import android.util.Log;
import carnero.cgeo.cgBase;
import carnero.cgeo.cgCoord;
import carnero.cgeo.cgSettings;
import carnero.cgeo.cgeodetail;
import carnero.cgeo.cgeonavigate;
import carnero.cgeo.cgeopopup;
import carnero.cgeo.cgeowaypoint;
import carnero.cgeo.mapinterfaces.GeoPointImpl;
import carnero.cgeo.mapinterfaces.ItemizedOverlayImpl;
import carnero.cgeo.mapinterfaces.MapFactory;
import carnero.cgeo.mapinterfaces.MapProjectionImpl;
import carnero.cgeo.mapinterfaces.OverlayBase;
import carnero.cgeo.mapinterfaces.MapViewImpl;
import carnero.cgeo.mapinterfaces.CacheOverlayItemImpl;

import java.util.ArrayList;

import org.mapsforge.android.maps.Projection;

public class cgMapOverlay extends ItemizedOverlayBase implements OverlayBase {

	private ArrayList<CacheOverlayItemImpl> items = new ArrayList<CacheOverlayItemImpl>();
	private Context context = null;
	private Boolean fromDetail = false;
	private boolean displayCircles = false;
	private ProgressDialog waitDialog = null;
	private Point center = new Point();
	private Point left = new Point();
	private Paint blockedCircle = null;
	private PaintFlagsDrawFilter setfil = null;
	private PaintFlagsDrawFilter remfil = null;
	private cgSettings settings;

	public cgMapOverlay(cgSettings settingsIn, ItemizedOverlayImpl ovlImpl, Context contextIn, Boolean fromDetailIn) {
		super(ovlImpl);

		populate();
		settings = settingsIn;

		context = contextIn;
		fromDetail = fromDetailIn;
	}
	
	public void updateItems(CacheOverlayItemImpl item) {
		ArrayList<CacheOverlayItemImpl> itemsPre = new ArrayList<CacheOverlayItemImpl>();
		itemsPre.add(item);
		
		updateItems(itemsPre);
	}

	public void updateItems(ArrayList<CacheOverlayItemImpl> itemsPre) {
		if (itemsPre == null) {
			return;
		}

		for (CacheOverlayItemImpl item : itemsPre) {
			item.setMarker(boundCenterBottom(item.getMarker(0)));
		}

		items.clear();
		
		if (itemsPre.size() > 0) {
			items = (ArrayList<CacheOverlayItemImpl>) itemsPre.clone();
		}
		
		setLastFocusedItemIndex(-1); // to reset tap during data change
		populate();
	}
	
	public boolean getCircles() {
		return displayCircles;
	}

	public void switchCircles() {
		displayCircles = !displayCircles;
	}

	@Override
	public void draw(Canvas canvas, MapViewImpl mapView, boolean shadow) {

		drawInternal(canvas, mapView.getMapProjection());
		
		super.draw(canvas, mapView, false);
	}
	
	@Override
	public void drawOverlayBitmap(Canvas canvas, Point drawPosition,
			MapProjectionImpl projection, byte drawZoomLevel) {
		
		drawInternal(canvas, projection);
		
		super.drawOverlayBitmap(canvas, drawPosition, projection, drawZoomLevel);
	}
	
	private void drawInternal(Canvas canvas, MapProjectionImpl projection) {
		
		MapFactory mapFactory = settings.getMapFactory();
		
		if (displayCircles) {
			if (blockedCircle == null) {
				blockedCircle = new Paint();
				blockedCircle.setAntiAlias(true);
				blockedCircle.setStrokeWidth(1.0f);
			}

			if (setfil == null) setfil = new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG);
			if (remfil == null) remfil = new PaintFlagsDrawFilter(Paint.FILTER_BITMAP_FLAG, 0);

			canvas.setDrawFilter(setfil);

			for (CacheOverlayItemImpl item : items) {
				final cgCoord itemCoord = item.getCoord();
				float[] result = new float[1];

				Location.distanceBetween(itemCoord.latitude, itemCoord.longitude, itemCoord.latitude, itemCoord.longitude + 1, result);
				final float longitudeLineDistance = result[0];

				GeoPointImpl itemGeo = mapFactory.getGeoPointBase((int)(itemCoord.latitude * 1e6), (int)(itemCoord.longitude * 1e6));
				GeoPointImpl leftGeo = mapFactory.getGeoPointBase((int)(itemCoord.latitude * 1e6), (int)((itemCoord.longitude - 161 / longitudeLineDistance) * 1e6));

				projection.toPixels(itemGeo, center);
				projection.toPixels(leftGeo, left);
				int radius = center.x - left.x;

				final String type = item.getType();
				if (type == null || "multi".equals(type) || "mystery".equals(type) || "virtual".equals(type)) {
					blockedCircle.setColor(0x66000000);
					blockedCircle.setStyle(Style.STROKE);
					canvas.drawCircle(center.x, center.y, radius, blockedCircle);
				} else {
					blockedCircle.setColor(0x66BB0000);
					blockedCircle.setStyle(Style.STROKE);
					canvas.drawCircle(center.x, center.y, radius, blockedCircle);

					blockedCircle.setColor(0x44BB0000);
					blockedCircle.setStyle(Style.FILL);
					canvas.drawCircle(center.x, center.y, radius, blockedCircle);
				}
			}

			canvas.setDrawFilter(remfil);
		}		
	}
	
	@Override
	public boolean onTap(int index) {
		try {
			if (items.size() <= index) {
				return false;
			}

			if (waitDialog == null) {
				waitDialog = new ProgressDialog(context);
				waitDialog.setMessage("loading details...");
				waitDialog.setCancelable(false);
			}
			waitDialog.show();

			CacheOverlayItemImpl item = items.get(index);
			cgCoord coordinate = item.getCoord();

			if (coordinate.type != null && coordinate.type.equalsIgnoreCase("cache") == true && coordinate.geocode != null && coordinate.geocode.length() > 0) {
				Intent popupIntent = new Intent(context, cgeopopup.class);

				popupIntent.putExtra("fromdetail", fromDetail);
				popupIntent.putExtra("geocode", coordinate.geocode);

				context.startActivity(popupIntent);
			} else if (coordinate.type != null && coordinate.type.equalsIgnoreCase("waypoint") == true && coordinate.id != null && coordinate.id > 0) {
				Intent popupIntent = new Intent(context, cgeowaypoint.class);

				popupIntent.putExtra("waypoint", coordinate.id);
				popupIntent.putExtra("geocode", coordinate.geocode);

				context.startActivity(popupIntent);
			} else {
				waitDialog.dismiss();
				return false;
			}

			waitDialog.dismiss();
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgMapOverlay.onTap: " + e.toString());
		}

		return false;
	}

	@Override
	public CacheOverlayItemImpl createItem(int index) {
		try {
			return items.get(index);
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgMapOverlay.createItem: " + e.toString());
		}

		return null;
	}

	@Override
	public int size() {
		try {
			return items.size();
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgMapOverlay.size: " + e.toString());
		}

		return 0;
	}

	public void infoDialog(int index) {
		final CacheOverlayItemImpl item = items.get(index);
		final cgCoord coordinate = item.getCoord();

		if (coordinate == null) {
			Log.e(cgSettings.tag, "cgMapOverlay:infoDialog: No coordinates given");
			return;
		}

		try {
			AlertDialog.Builder dialog = new AlertDialog.Builder(context);
			dialog.setCancelable(true);

			if (coordinate.type.equalsIgnoreCase("cache")) {
				dialog.setTitle("cache");

				String cacheType;
				if (cgBase.cacheTypesInv.containsKey(coordinate.typeSpec) == true) {
					cacheType = cgBase.cacheTypesInv.get(coordinate.typeSpec);
				} else {
					cacheType = cgBase.cacheTypesInv.get("mystery");
				}

				dialog.setMessage(Html.fromHtml(item.getTitle()) + "\n\ngeocode: " + coordinate.geocode.toUpperCase() + "\ntype: " + cacheType);
				if (fromDetail == false) {
					dialog.setPositiveButton("detail", new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int id) {
							Intent cachesIntent = new Intent(context, cgeodetail.class);
							cachesIntent.putExtra("geocode", coordinate.geocode.toUpperCase());
							context.startActivity(cachesIntent);

							dialog.cancel();
						}
					});
				} else {
					dialog.setPositiveButton("navigate", new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int id) {
							cgeonavigate navigateActivity = new cgeonavigate();

							cgeonavigate.coordinates = new ArrayList<cgCoord>();
							cgeonavigate.coordinates.add(coordinate);

							Intent navigateIntent = new Intent(context, navigateActivity.getClass());
							navigateIntent.putExtra("latitude", coordinate.latitude);
							navigateIntent.putExtra("longitude", coordinate.longitude);
							navigateIntent.putExtra("geocode", coordinate.geocode.toUpperCase());
							context.startActivity(navigateIntent);
							dialog.cancel();
						}
					});
				}
			} else {
				dialog.setTitle("waypoint");

				String waypointType;
				if (cgBase.cacheTypesInv.containsKey(coordinate.typeSpec) == true) {
					waypointType = cgBase.waypointTypes.get(coordinate.typeSpec);
				} else {
					waypointType = cgBase.waypointTypes.get("waypoint");
				}

				dialog.setMessage(Html.fromHtml(item.getTitle()) + "\n\ntype: " + waypointType);
				dialog.setPositiveButton("navigate", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						cgeonavigate navigateActivity = new cgeonavigate();

						cgeonavigate.coordinates = new ArrayList<cgCoord>();
						cgeonavigate.coordinates.add(coordinate);

						Intent navigateIntent = new Intent(context, navigateActivity.getClass());
						navigateIntent.putExtra("latitude", coordinate.latitude);
						navigateIntent.putExtra("longitude", coordinate.longitude);
						navigateIntent.putExtra("geocode", coordinate.name);

						context.startActivity(navigateIntent);
						dialog.cancel();
					}
				});
			}

			dialog.setNegativeButton("dismiss", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});

			AlertDialog alert = dialog.create();
			alert.show();
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgMapOverlay.infoDialog: " + e.toString());
		}
	}
}
