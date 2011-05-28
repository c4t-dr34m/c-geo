package carnero.cgeo.filter;

import carnero.cgeo.cgCache;

public class cgFilterByTrackables extends cgFilter {

	@Override
	boolean applyFilter(cgCache cache) {
		return cache.hasTrackables();
	}

}
