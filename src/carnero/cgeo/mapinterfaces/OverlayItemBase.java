package carnero.cgeo.mapinterfaces;

import android.graphics.drawable.Drawable;
import carnero.cgeo.cgCoord;

public interface OverlayItemBase {

	public cgCoord getCoord();
	
	public String getTitle();
	
	public Drawable getMarker(int index);
	
	public void setMarker(Drawable markerIn);

	public String getType();
}
