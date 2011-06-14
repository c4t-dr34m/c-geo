package carnero.cgeo.mapinterfaces;

import carnero.cgeo.cgUser;

/**
 * Common functions of the provider-specific
 * UserOverlayItem implementations
 * @author rsudev
 *
 */
public interface UserOverlayItemImpl extends OverlayItemImpl {

	public cgUser getUser();
}
