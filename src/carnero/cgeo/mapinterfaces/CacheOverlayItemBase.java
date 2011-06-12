package carnero.cgeo.mapinterfaces;

import carnero.cgeo.cgCoord;

public interface CacheOverlayItemBase extends OverlayItemBase {

	public cgCoord getCoord();
	
	public String getType();

}
