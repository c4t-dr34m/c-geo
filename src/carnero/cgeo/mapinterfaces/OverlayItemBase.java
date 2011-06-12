package carnero.cgeo.mapinterfaces;

import android.graphics.drawable.Drawable;

public interface OverlayItemBase {

	public String getTitle();
	
	public Drawable getMarker(int index);
	
	public void setMarker(Drawable markerIn);
}
