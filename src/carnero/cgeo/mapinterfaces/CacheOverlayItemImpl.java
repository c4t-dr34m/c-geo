package carnero.cgeo.mapinterfaces;

import carnero.cgeo.cgCoord;

/**
 * Covers the common functions of the provider-specific
 * CacheOverlayItem implementations 
 * @author rsudev
 *
 */
public interface CacheOverlayItemImpl extends OverlayItemImpl {

	public cgCoord getCoord();
	
	public String getType();

}
