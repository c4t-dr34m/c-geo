package carnero.cgeo;

import java.util.List;
import java.util.HashMap;
import android.app.Activity;
import android.text.Spannable;
import android.text.style.StrikethroughSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class cgCacheListAdapter extends ArrayAdapter<cgCache> {
	private List<cgCache> list = null;
	private cgSettings settings = null;
	private cgCacheView holder = null;
	private LayoutInflater inflater = null;
	private Activity activity = null;
	private cgBase base = null;
	private Double latitude = null;
	private Double longitude = null;
	private float azimuth = 0.0f;
	private long lastSort = 0l;
	private boolean sort = true;
	private int checked = 0;
	private boolean selectMode = false;
	private HashMap<String, Drawable> gcIcons = new HashMap<String, Drawable>();
	private ArrayList<cgCompassMini> compasses = new ArrayList<cgCompassMini>();
	private ArrayList<cgDistanceView> distances = new ArrayList<cgDistanceView>();
	private float pixelDensity = 1f;
	private static final int SWIPE_MIN_DISTANCE = 100;
	private static final int SWIPE_MAX_OFF_PATH = 200;
	private static final int SWIPE_DISTANCE = 80;
	private static final float SWIPE_OPACITY = 0.5f;

	public cgCacheListAdapter(Activity activityIn, cgSettings settingsIn, List<cgCache> listIn, cgBase baseIn) {
		super(activityIn, 0, listIn);

		activity = activityIn;
		settings = settingsIn;
		list = listIn;
		base = baseIn;

		DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		pixelDensity = metrics.density;

		if (gcIcons == null || gcIcons.isEmpty()) {
			gcIcons.put("ape", (Drawable)activity.getResources().getDrawable(R.drawable.type_ape));
			gcIcons.put("cito", (Drawable)activity.getResources().getDrawable(R.drawable.type_cito));
			gcIcons.put("earth", (Drawable)activity.getResources().getDrawable(R.drawable.type_earth));
			gcIcons.put("event", (Drawable)activity.getResources().getDrawable(R.drawable.type_event));
			gcIcons.put("letterbox", (Drawable)activity.getResources().getDrawable(R.drawable.type_letterbox));
			gcIcons.put("locationless", (Drawable)activity.getResources().getDrawable(R.drawable.type_locationless));
			gcIcons.put("mega", (Drawable)activity.getResources().getDrawable(R.drawable.type_mega));
			gcIcons.put("multi", (Drawable)activity.getResources().getDrawable(R.drawable.type_multi));
			gcIcons.put("traditional", (Drawable)activity.getResources().getDrawable(R.drawable.type_traditional));
			gcIcons.put("virtual", (Drawable)activity.getResources().getDrawable(R.drawable.type_virtual));
			gcIcons.put("webcam", (Drawable)activity.getResources().getDrawable(R.drawable.type_webcam));
			gcIcons.put("wherigo", (Drawable)activity.getResources().getDrawable(R.drawable.type_wherigo));
			gcIcons.put("mystery", (Drawable)activity.getResources().getDrawable(R.drawable.type_mystery));
		}
	}

	public int getChecked() {
		return checked;
	}

	public boolean setSelectMode(boolean status, boolean clear) {
		selectMode = status;

		if (selectMode == false && clear == true) {
			for (cgCache cache : list) {
				cache.statusChecked = false;
			}
			checked = 0;
		}
		checkChecked(0);

		notifyDataSetChanged();

		return selectMode;
	}

	public boolean getSelectMode() {
		return selectMode;
	}

	public void switchSelectMode() {
		selectMode = !selectMode;
		
		if (selectMode == false) {
			for (cgCache cache : list) {
				cache.statusChecked = false;
			}
			checked = 0;
		}
		checkChecked(0);

		notifyDataSetChanged();
	}

	public void forceSort(Double latitudeIn, Double longitudeIn) {
		if (latitudeIn == null || longitudeIn == null) return;
		if (list == null || list.isEmpty() == true) return;
		if (sort == false) return;

		try {
			Collections.sort((List<cgCache>)list, new cgCacheComparator(latitudeIn, longitudeIn));
			notifyDataSetChanged();
		} catch (Exception e) {
			Log.w(cgSettings.tag, "cgCacheListAdapter.setActualCoordinates: failed to sort caches in list");
		}
	}

	public void setActualCoordinates(Double latitudeIn, Double longitudeIn) {
		if (latitudeIn == null || longitudeIn == null) return;

		latitude = latitudeIn;
		longitude = longitudeIn;
		
		if (list != null && list.isEmpty() == false&& (System.currentTimeMillis() - lastSort) > 1000 && sort == true) {
			try {
				Collections.sort((List<cgCache>)list, new cgCacheComparator(latitudeIn, longitudeIn));
				notifyDataSetChanged();
			} catch (Exception e) {
				Log.w(cgSettings.tag, "cgCacheListAdapter.setActualCoordinates: failed to sort caches in list");
			}

			lastSort = System.currentTimeMillis();
		}
		
		if (distances != null && distances.size() > 0) {
			for (cgDistanceView distance : distances) {
				distance.update(latitudeIn, longitudeIn);
			}
		}

		if (compasses != null && compasses.size() > 0) {
			for (cgCompassMini compass : compasses) {
				compass.updateCoords(latitudeIn, longitudeIn);
			}
		}
	}

	public void setActualHeading(Float azimuthIn) {
		if (azimuthIn == null) return;
		
		azimuth = azimuthIn;

		if (compasses != null && compasses.size() > 0) {
			for (cgCompassMini compass : compasses) {
				compass.updateAzimuth(azimuth);
			}
		}
	}

	public boolean resetChecks() {
		if (list.isEmpty() == true) return false;
		if (checked <= 0) return false;

		boolean status = getSelectMode();
		int cleared = 0;
		for (cgCache cache : list) {
			if (cache.statusChecked == true) {
				cache.statusChecked = false;
				
				checkChecked(-1);
				cleared ++;
			}
		}
		setSelectMode(false, false);
		notifyDataSetChanged();

		if (cleared > 0 || status == true) {
			return true;
		} else {
			return false;
		}
	}

    @Override
    public View getView(int position, View rowView, ViewGroup parent) {
		if (inflater == null) inflater = ((Activity)getContext()).getLayoutInflater();

		if (position > getCount()) {
			Log.w(cgSettings.tag, "cgCacheListAdapter.getView: Attempt to access missing item #" + position);
			return null;
		}

		cgCache cache = getItem(position);

		if (rowView == null) {
			if (settings.skin == 1) rowView = (View)inflater.inflate(R.layout.cache_light, null);
			else rowView = (View) inflater.inflate(R.layout.cache_dark, null);

			holder = new cgCacheView();
			holder.oneCache = (RelativeLayout)rowView.findViewById(R.id.one_cache);
			holder.checkbox = (CheckBox)rowView.findViewById(R.id.checkbox);
			holder.oneInfo = (RelativeLayout)rowView.findViewById(R.id.one_info);
			holder.foundMark = (ImageView)rowView.findViewById(R.id.found_mark);
			holder.ratingMark1 = (ImageView)rowView.findViewById(R.id.rating_mark_1);
			holder.ratingMark2 = (ImageView)rowView.findViewById(R.id.rating_mark_2);
			holder.ratingMark3 = (ImageView)rowView.findViewById(R.id.rating_mark_3);
			holder.ratingMark4 = (ImageView)rowView.findViewById(R.id.rating_mark_4);
			holder.ratingMark5 = (ImageView)rowView.findViewById(R.id.rating_mark_5);
			holder.oneCache = (RelativeLayout)rowView.findViewById(R.id.one_cache);
			holder.text = (TextView)rowView.findViewById(R.id.text);
			holder.distance = (cgDistanceView)rowView.findViewById(R.id.distance);
			holder.direction = (cgCompassMini)rowView.findViewById(R.id.direction);
			holder.inventory = (LinearLayout)rowView.findViewById(R.id.inventory);
			holder.info = (TextView)rowView.findViewById(R.id.info);
			
			rowView.setTag(holder);
		} else {
			holder = (cgCacheView)rowView.getTag();
		}

		final touchListener touchLst = new touchListener(cache.geocode, cache.name, cache);
		rowView.setOnTouchListener(touchLst);
		rowView.setOnClickListener(touchLst);
		rowView.setOnLongClickListener(touchLst);
		rowView.setLongClickable(true);

		if (selectMode == true) {
			if (cache.statusCheckedView == true) {
				moveRight(holder, cache, true); // move fast when already slided
			} else {
				moveRight(holder, cache, false);
			}
		} else if (cache.statusChecked == true) {
			holder.checkbox.setChecked(true);
			if (cache.statusCheckedView == true) {
				moveRight(holder, cache, true); // move fast when already slided
			} else {
				moveRight(holder, cache, false);
			}
		} else {
			holder.checkbox.setChecked(false);
			if (cache.statusCheckedView == false) {
				holder.oneInfo.clearAnimation();
			} else {
				moveLeft(holder, cache, false);
			}
		}

		if (distances.contains(holder.distance) == false) distances.add(holder.distance);
		holder.distance.setContent(base, cache.latitude, cache.longitude);
		if (compasses.contains(holder.direction) == false) compasses.add(holder.direction);
		holder.direction.setContent(base, cache.latitude, cache.longitude);

		if (cache.found == true) {
			holder.foundMark.setVisibility(View.VISIBLE);
		} else {
			holder.foundMark.setVisibility(View.GONE);
		}

		holder.ratingMark1.setVisibility(View.GONE);
		holder.ratingMark2.setVisibility(View.GONE);
		holder.ratingMark3.setVisibility(View.GONE);
		holder.ratingMark4.setVisibility(View.GONE);
		holder.ratingMark5.setVisibility(View.GONE);

		if (cache.vote != null && cache.vote > 0) {
			if (cache.vote > 0) holder.ratingMark1.setVisibility(View.VISIBLE);
			else if(cache.vote > 1) holder.ratingMark2.setVisibility(View.VISIBLE);
			else if(cache.vote > 2) holder.ratingMark3.setVisibility(View.VISIBLE);
			else if(cache.vote > 3) holder.ratingMark4.setVisibility(View.VISIBLE);
			else if(cache.vote > 4) holder.ratingMark5.setVisibility(View.VISIBLE);
		} else if (cache.rating != null && cache.rating > 0) {
			if (cache.rating > 0.0) holder.ratingMark1.setVisibility(View.VISIBLE);
			if (cache.rating >= 1.5) holder.ratingMark2.setVisibility(View.VISIBLE);
			if (cache.rating >= 2.5) holder.ratingMark3.setVisibility(View.VISIBLE);
			if (cache.rating >= 3.5) holder.ratingMark4.setVisibility(View.VISIBLE);
			if (cache.rating >= 4.5) holder.ratingMark5.setVisibility(View.VISIBLE);
		}

		if (cache.nameSp == null) {
			cache.nameSp = (new Spannable.Factory()).newSpannable(cache.name);
			if (cache.disabled == true || cache.archived == true) { // strike
				cache.nameSp.setSpan(new StrikethroughSpan(), 0, cache.nameSp.toString().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}

		holder.text.setText(cache.nameSp, TextView.BufferType.SPANNABLE);
		if (gcIcons.containsKey(cache.type) == true) { // cache icon
			holder.text.setCompoundDrawablesWithIntrinsicBounds(gcIcons.get(cache.type), null, null, null);
		} else { // unknown cache type, "mystery" icon
			holder.text.setCompoundDrawablesWithIntrinsicBounds(gcIcons.get("mystery"), null, null, null);
		}

		if (holder.inventory.getChildCount() > 0) {
			holder.inventory.removeAllViews();
		}

		ImageView tbIcon = null;
		if (cache.inventoryCoins == 1) {
			if (settings.skin == 1) tbIcon = (ImageView)inflater.inflate(R.layout.trackable_icon_light, null);
			else tbIcon = (ImageView)inflater.inflate(R.layout.trackable_icon_dark, null);
			tbIcon.setImageResource(R.drawable.trackable_coin);
			holder.inventory.addView(tbIcon);
		} else if (cache.inventoryCoins > 1) {
			if (settings.skin == 1) tbIcon = (ImageView)inflater.inflate(R.layout.trackable_icon_light, null);
			else tbIcon = (ImageView)inflater.inflate(R.layout.trackable_icon_dark, null);
			tbIcon.setImageResource(R.drawable.trackable_coins);
			holder.inventory.addView(tbIcon);
		}
		if (cache.inventoryTags == 1) {
			if (settings.skin == 1) tbIcon = (ImageView)inflater.inflate(R.layout.trackable_icon_light, null);
			else tbIcon = (ImageView)inflater.inflate(R.layout.trackable_icon_dark, null);
			tbIcon.setImageResource(R.drawable.trackable_tb);
			holder.inventory.addView(tbIcon);
		} else if (cache.inventoryTags > 1) {
			if (settings.skin == 1) tbIcon = (ImageView)inflater.inflate(R.layout.trackable_icon_light, null);
			else tbIcon = (ImageView)inflater.inflate(R.layout.trackable_icon_dark, null);
			tbIcon.setImageResource(R.drawable.trackable_tbs);
			holder.inventory.addView(tbIcon);
		}
		if (cache.inventoryUnknown > 0) {
			if (settings.skin == 1) tbIcon = (ImageView)inflater.inflate(R.layout.trackable_icon_light, null);
			else tbIcon = (ImageView)inflater.inflate(R.layout.trackable_icon_dark, null);
			tbIcon.setImageResource(R.drawable.trackable_all);
			holder.inventory.addView(tbIcon);
		}

		boolean setDiDi = false;
		if (cache.latitude != null && cache.longitude != null) {
			holder.direction.setVisibility(View.VISIBLE);
			holder.direction.updateAzimuth(azimuth);
			if (latitude != null && longitude != null) {
				holder.distance.update(latitude, longitude);
				holder.direction.updateCoords(latitude, longitude);
			}
			setDiDi = true;
		} else {
			if (cache.distance != null) {
				holder.distance.setDistance(cache.distance);
				setDiDi = true;
            }
			if (cache.direction != null && cache.direction.length() > 0) {
				String letters = cache.direction.toUpperCase();
				int numbersFromLetters = 0;

				if (letters.equals("N")) { numbersFromLetters = 0; }
				else if (letters.equals("NE")) { numbersFromLetters = 45; }
				else if (letters.equals("E")) { numbersFromLetters = 90; }
				else if (letters.equals("SE")) { numbersFromLetters = 135; }
				else if (letters.equals("S")) { numbersFromLetters = 180; }
				else if (letters.equals("SW")) { numbersFromLetters = 225; }
				else if (letters.equals("W")) { numbersFromLetters = 270; }
				else if (letters.equals("NW")) { numbersFromLetters = 315; }
				else { numbersFromLetters = 0; }

				holder.direction.setVisibility(View.VISIBLE);
				holder.direction.updateAzimuth(azimuth);
				holder.direction.updateHeading(new Float(numbersFromLetters));
				setDiDi = true;
			}
		}

		if (setDiDi == false) {
			holder.distance.clear();
			holder.direction.setVisibility(View.GONE);
        }

		StringBuilder cacheInfo = new StringBuilder();
		if (cache.geocode != null && cache.geocode.length() > 0) {
			cacheInfo.append(cache.geocode);
		}
		if (cache.size != null && cache.size.length() > 0) {
			if (cacheInfo.length() > 0) cacheInfo.append(" | ");
			cacheInfo.append(cache.size);
		}
		if ((cache.difficulty != null && cache.difficulty > 0f) || (cache.terrain != null && cache.terrain > 0f) || (cache.rating != null && cache.rating > 0f)) {
			if (cacheInfo.length() > 0) cacheInfo.append(" |");

			if (cache.difficulty != null && cache.difficulty > 0f) {
				cacheInfo.append(" D:");
				cacheInfo.append(String.format(Locale.getDefault(), "%.1f", cache.difficulty));
			}
			if (cache.terrain != null && cache.terrain > 0f) {
				cacheInfo.append(" T:");
				cacheInfo.append(String.format(Locale.getDefault(), "%.1f", cache.terrain));
			}
		}
		if (cache.members == true) {
			if (cacheInfo.length() > 0) cacheInfo.append(" | ");
			cacheInfo.append("premium");
		}
		if (cache.reason != null && cache.reason == 1) {
			if (cacheInfo.length() > 0) cacheInfo.append(" | ");
			cacheInfo.append("offline");
		}
		holder.info.setText(cacheInfo.toString());

		return rowView;
	}
	
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();

		checked = 0;
		for (cgCache cache : list) {
			if (cache.statusChecked == true) {
				checked ++;
			}
		}

		distances.clear();
		compasses.clear();
	}

	private class touchListener implements View.OnLongClickListener, View.OnClickListener, View.OnTouchListener {
		private String geocode = null;
		private String name = null;
		private cgCache cache = null;
		private boolean touch = true;
		private GestureDetector gestureDetector = null;

		public touchListener(String geocodeIn, String nameIn, cgCache cacheIn) {
			geocode = geocodeIn;
			name = nameIn;
			cache = cacheIn;

			final detectGesture dGesture = new detectGesture(holder, cache);
			gestureDetector = new GestureDetector(dGesture);
		}

		// tap on item
		public void onClick(View view) {
			if (touch == false) {
				touch = true;
				
				return;
			}

			// load cache details
			Intent cachesIntent = new Intent(getContext(), cgeodetail.class);
			cachesIntent.putExtra("geocode", geocode);
			cachesIntent.putExtra("name", name);
			getContext().startActivity(cachesIntent);
		}

		// long tap on item
		public boolean onLongClick(View view) {
			if (touch == false) {
				touch = true;

				return true;
			}

			return view.showContextMenu();
		}

		// swipe on item
		public boolean onTouch(View view, MotionEvent event) {
			if (gestureDetector.onTouchEvent(event) == true) {
				touch = false;

				return true;
			}

			return false;
		}
	}

	class detectGesture extends GestureDetector.SimpleOnGestureListener {
		private cgCacheView holder = null;
		private cgCache cache = null;

		public detectGesture(cgCacheView holderIn, cgCache cacheIn) {
			holder = holderIn;
			cache = cacheIn;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			try {
				if (getSelectMode() == true) {
					return false;
				}

				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
					return false;
				}

				if ((e2.getX() - e1.getX()) > SWIPE_MIN_DISTANCE && Math.abs(velocityX) >  Math.abs(velocityY)) {
					// left to right swipe
					if (cache.statusChecked == true) {
						return true;
					}

					if (holder != null && holder.oneInfo != null) {
						checkChecked(+1);
						holder.checkbox.setChecked(true);
						cache.statusChecked = true;
						moveRight(holder, cache, false);
					}

					return true;
				} else if ((e1.getX() - e2.getX()) > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > Math.abs(velocityY)) {
					// right to left swipe
					if (cache.statusChecked == false) {
						return true;
					}

					if (holder != null && holder.oneInfo != null) {
						if (getSelectMode() == true) {
							setSelectMode(false, false);
						}

						checkChecked(-1);
						holder.checkbox.setChecked(false);
						cache.statusChecked = false;
						moveLeft(holder, cache, false);
					}
					
					return true;
				}
			} catch (Exception e) {
				Log.w(cgSettings.tag, "cgCacheListAdapter.detectGesture.onFling: " + e.toString());
			}

			return false;
		}
	}

	private void checkChecked(int cnt) {
		// check how many caches are selected, if any block sorting of list
		boolean statusChecked = false;
		boolean statusSort = false;
		checked += cnt;

		if (checked > 0) {
			statusChecked = false;
		} else {
			statusChecked = true;
		}

		if (getSelectMode() == true) {
			statusSort = false;
		} else {
			statusSort = true;
		}

		if (statusChecked == false || statusSort == false) {
			sort = false;
		} else {
			sort = true;
		}

		if (sort == true) {
			forceSort(latitude, longitude);
		}
	}

	private void moveRight(cgCacheView holder, cgCache cache, boolean force) {
		if (cache == null) return;

		try {
			holder.checkbox.setChecked(cache.statusChecked);

			// slide cache info
			Animation showCheckbox = new TranslateAnimation(0, (int)(SWIPE_DISTANCE * pixelDensity), 0, 0);
			showCheckbox.setRepeatCount(0);
			if (force == true) {
				showCheckbox.setDuration(0);
			} else {
				showCheckbox.setDuration(400);
			}
			showCheckbox.setFillEnabled(true);
			showCheckbox.setFillAfter(true);
			showCheckbox.setInterpolator(new AccelerateDecelerateInterpolator());

			// dim cache info
			Animation dimInfo = new AlphaAnimation(1.0f, SWIPE_OPACITY);
			dimInfo.setRepeatCount(0);
			if (force == true) {
				dimInfo.setDuration(0);
			} else {
				dimInfo.setDuration(400);
			}
			dimInfo.setFillEnabled(true);
			dimInfo.setFillAfter(true);
			dimInfo.setInterpolator(new AccelerateDecelerateInterpolator());

			// animation set (container)
			AnimationSet selectAnimation = new AnimationSet(true);
			selectAnimation.setFillEnabled(true);
			selectAnimation.setFillAfter(true);

			selectAnimation.addAnimation(showCheckbox);
			selectAnimation.addAnimation(dimInfo);

			holder.oneInfo.startAnimation(selectAnimation);
			cache.statusCheckedView = true;
		} catch (Exception e) {
			// nothing
		}
	}

	private void moveLeft(cgCacheView holder, cgCache cache, boolean force) {
		if (cache == null) return;

		try {
			holder.checkbox.setChecked(cache.statusChecked);

			// slide cache info
			Animation hideCheckbox = new TranslateAnimation((int)(SWIPE_DISTANCE * pixelDensity), 0, 0, 0);
			hideCheckbox.setRepeatCount(0);
			if (force == true) {
				hideCheckbox.setDuration(0);
			} else {
				hideCheckbox.setDuration(400);
			}
			hideCheckbox.setFillEnabled(true);
			hideCheckbox.setFillAfter(true);
			hideCheckbox.setInterpolator(new AccelerateDecelerateInterpolator());

			// brighten cache info
			Animation brightenInfo = new AlphaAnimation(SWIPE_OPACITY, 1.0f);
			brightenInfo.setRepeatCount(0);
			if (force == true) {
				brightenInfo.setDuration(0);
			} else {
				brightenInfo.setDuration(400);
			}
			brightenInfo.setFillEnabled(true);
			brightenInfo.setFillAfter(true);
			brightenInfo.setInterpolator(new AccelerateDecelerateInterpolator());

			// animation set (container)
			AnimationSet selectAnimation = new AnimationSet(true);
			selectAnimation.setFillEnabled(true);
			selectAnimation.setFillAfter(true);

			selectAnimation.addAnimation(hideCheckbox);
			selectAnimation.addAnimation(brightenInfo);

			holder.oneInfo.startAnimation(selectAnimation);
			cache.statusCheckedView = false;
		} catch (Exception e) {
			// nothing
		}
	}
}
