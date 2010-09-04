package carnero.cgeo;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.openintents.intents.WikitudePOI;

public class cgeoapplication extends Application {
    private cgData storage = null;
	private List<WikitudePOI> pois = null; // for Wikitude
	final private ArrayList<cgGeo> geos = new ArrayList<cgGeo>(); // list of location providers
	final private ArrayList<cgDirection> dirs = new ArrayList<cgDirection>(); // list of direction providers
	final private HashMap<Long, cgSearch> searches = new HashMap<Long, cgSearch>(); // information about searches
	final private HashMap<String, cgCache> cachesCache = new HashMap<String, cgCache>(); // caching caches into memory
	private String action = null;

	public boolean warnedLanguage = false;

	@Override
	public void onLowMemory() {
		Log.i(cgSettings.tag, "Cleaning applications cache.");
		
		cachesCache.clear();
	}

	@Override
	public void onTerminate() {
		Log.d(cgSettings.tag, "Terminating c:geo...");

		for (cgGeo geo : geos) {
			removeGeo(geo);
		}

		for (cgDirection dir : dirs) {
			removeDir(dir);
		}

        if (storage != null) {
			storage.clearCache();
			storage.closeDb();
			storage = null;
		}

		super.onTerminate();
	}

	public List<WikitudePOI> getPois() {
		return pois;
	}

	public void cleanGeo() {
		for (cgGeo geo : geos) {
			removeGeo(geo);
		}
	}

	public void cleanDir() {
		for (cgDirection dir : dirs) {
			removeDir(dir);
		}
	}

	public cgGeo startGeo(Context context, cgUpdateLoc geoUpdate, cgBase base, cgSettings settings, cgWarning warning, int time, int distance) {
		cgGeo geo = new cgGeo(context, this, geoUpdate, base, settings, warning, time, distance);
		geo.initGeo();
		
		geos.add(geo);

		return geo;
	}

	public cgGeo removeGeo(cgGeo geo) {
		if (geo == null) return null;

		geo.closeGeo();
		if (geos.contains(geo) == true) geos.remove(geo);

		return null;
	}

	public cgDirection startDir(Context context, cgUpdateDir dirUpdate, cgWarning warning) {
		cgDirection dir = new cgDirection(context, dirUpdate, warning);
		dir.initDir();

		dirs.add(dir);

		return dir;
	}

	public cgDirection removeDir(cgDirection dir) {
		if (dir == null) return null;

		dir.closeDir();
		if (dirs.contains(dir) == true) dirs.remove(dir);

		return null;
	}

	public void setPois(List<WikitudePOI> poisIn) {
		pois = poisIn;
	}

    public Boolean isThere(String geocode, String guid, boolean detailed, boolean checkTime) {
		if (storage == null) storage = new cgData(this);
        return storage.isThere(geocode, guid, detailed, checkTime);
    }

	public Boolean isOffline(String geocode, String guid) {
		if (storage == null) storage = new cgData(this);
		return storage.isOffline(geocode, guid);
	}

    public String getGeocode(String guid) {
		if (storage == null) storage = new cgData(this);
        return storage.getGeocodeForGuid(guid);
    }

    public String getCacheid(String geocode) {
		if (storage == null) storage = new cgData(this);
        return storage.getCacheidForGeocode(geocode);
    }

    public String getError(Long searchId) {
		if (searchId == null || searches.containsKey(searchId) == false) {
			return null;
		}

        return searches.get(searchId).error;
    }

    public boolean setError(Long searchId, String error) {
		if (searchId == null || searches.containsKey(searchId) == false) {
			return false;
		}

        searches.get(searchId).error = error;

        return true;
    }

    public String getUrl(Long searchId) {
		if (searchId == null || searches.containsKey(searchId) == false) {
			return null;
		}

        return searches.get(searchId).url;
    }

    public boolean setUrl(Long searchId, String url) {
		if (searchId == null || searches.containsKey(searchId) == false) {
			return false;
		}

        searches.get(searchId).url = url;

        return true;
    }

    public String getViewstate(Long searchId) {
		if (searchId == null || searches.containsKey(searchId) == false) return null;

        return searches.get(searchId).viewstate;
    }

    public String getViewstate1(Long searchId) {
		if (searchId == null || searches.containsKey(searchId) == false) return null;

        return searches.get(searchId).viewstate1;
    }

    public boolean setViewstate(Long searchId, String viewstate) {
		if (viewstate == null || viewstate.length() == 0) return false;
		if (searchId == null || searches.containsKey(searchId) == false) return false;

		searches.get(searchId).viewstate = viewstate;

		return true;
    }

    public boolean setViewstate1(Long searchId, String viewstate1) {
		if (searchId == null || searches.containsKey(searchId) == false) {
			return false;
		}

        searches.get(searchId).viewstate1 = viewstate1;

        return true;
    }

    public Integer getTotal(Long searchId) {
		if (searchId == null || searches.containsKey(searchId) == false) return null;

        return searches.get(searchId).totalCnt;
    }

    public Integer getCount(Long searchId) {
		if (searchId == null || searches.containsKey(searchId) == false) {
			return 0;
		}

		ArrayList<String> geocodes = searches.get(searchId).getGeocodes();
		if (geocodes != null) {
			return searches.get(searchId).getGeocodes().size();
		}

		return 0;
    }

    public Integer getNotOfflineCount(Long searchId) {
		if (searchId == null || searches.containsKey(searchId) == false) return 0;

		int count = 0;
		ArrayList<String> geocodes = searches.get(searchId).getGeocodes();
		if (geocodes != null) {
			for (String geocode : geocodes) {
				if (isOffline(geocode, null) == false) {
					count ++;
				}
			}
		}

		return count;
    }

    public cgCache getCacheByGeocode(String geocode) {
		return getCacheByGeocode(geocode, false, true, false, false, false);
	}

    public cgCache getCacheByGeocode(String geocode, boolean loadA, boolean loadW, boolean loadS, boolean loadL, boolean loadI) {
		if (geocode == null || geocode.length() == 0) return null;
		
		cgCache cache = null;
		if (cachesCache.containsKey(geocode) == true) {
			cache = cachesCache.get(geocode);
		} else {
			if (storage == null) storage = new cgData(this);
			cache = storage.loadCache(geocode, null, loadA, loadW, loadS, loadL, loadI);

			if (cache.detailed == true) putCacheInCache(cache);
		}

		return cache;
    }

	public void removeCacheFromCache(String geocode) {
		if (geocode != null && cachesCache.containsKey(geocode) == true) cachesCache.remove(geocode);
	}

	public void putCacheInCache(cgCache cache) {
		if (cache == null || cache.geocode == null) return;

		if (cachesCache.containsKey(cache.geocode) == true) cachesCache.remove(cache.geocode);
		
		cachesCache.put(cache.geocode, cache);
	}

	public String[] geocodesInCache() {
		String[] geocodes = null;

		if (cachesCache != null && cachesCache.isEmpty() == false) {
			geocodes = cachesCache.keySet().toArray(new String[cachesCache.size()]);
		}

		return geocodes;
	}

    public cgWaypoint getWaypointById(Integer id) {
		if (id == null || id == 0) return null;

		if (storage == null) storage = new cgData(this);
		return storage.loadWaypoint(id);
    }
	
    public cgCache getCache(Long searchId) {
		if (searchId == null || searches.containsKey(searchId) == false) return null;

		cgSearch search = searches.get(searchId);
		ArrayList<String> geocodeList = search.getGeocodes();

		return getCacheByGeocode(geocodeList.get(0), true, true, true, true, true);
    }

	public ArrayList<cgCache> getCaches(Long searchId) {
		return getCaches(searchId, false, true, false, false, false);
	}

	public ArrayList<cgCache> getCaches(Long searchId, boolean loadA, boolean loadW, boolean loadS, boolean loadL, boolean loadI) {
		if (searchId == null || searches.containsKey(searchId) == false) return null;

		ArrayList<cgCache> cachesOut = new ArrayList<cgCache>();

		cgSearch search = searches.get(searchId);
		ArrayList<String> geocodeList = search.getGeocodes();

		if (storage == null) storage = new cgData(this);
		final ArrayList<cgCache> cachesPre = storage.loadCaches(geocodeList.toArray(), null, loadA, loadW, loadS, loadL, loadI);
		if (cachesPre != null) cachesOut.addAll(cachesPre);

		return cachesOut;
	}

	public cgSearch getBatchOfStoredCaches(boolean detailedOnly, Double latitude, Double longitude, String cachetype) {
		if (storage == null) storage = new cgData(this);
        cgSearch search = new cgSearch();

        ArrayList<String> geocodes = storage.loadBatchOfStoredGeocodes(detailedOnly, latitude, longitude, cachetype);
        if (geocodes != null && geocodes.isEmpty() == false) {
            for (String gccode : geocodes) {
                search.addGeocode(gccode);
            }
        }
        searches.put(search.getCurrentId(), search);

		return search;
	}

    public int getAllStoredCachesCount(boolean detailedOnly, String cachetype) {
		if (storage == null) storage = new cgData(this);
        return storage.getAllStoredCachesCount(detailedOnly, cachetype);
    }

    public boolean markStored(String geocode) {
		if (storage == null) storage = new cgData(this);
        return storage.markStored(geocode);
    }

    public boolean markDropped(String geocode) {
		if (storage == null) storage = new cgData(this);
        return storage.markDropped(geocode);
    }

	public boolean markFound(String geocode) {
		if (storage == null) storage = new cgData(this);
        return storage.markFound(geocode);
	}

    public boolean saveWaypoints(String geocode, ArrayList<cgWaypoint> waypoints, boolean drop) {
		if (storage == null) storage = new cgData(this);
        return storage.saveWaypoints(geocode, waypoints, drop);
    }

    public boolean saveOwnWaypoint(int id, String geocode, cgWaypoint waypoint) {
		if (storage == null) storage = new cgData(this);
        return storage.saveOwnWaypoint(id, geocode, waypoint);
    }

    public boolean deleteWaypoint(int id) {
		if (storage == null) storage = new cgData(this);
        return storage.deleteWaypoint(id);
    }

    public void addGeocode(Long searchId, String geocode) {
        if (this.searches.containsKey(searchId) == false || geocode == null || geocode.length() == 0) return;

        this.searches.get(searchId).addGeocode(geocode);
    }

    public Long addSearch(Long searchId, ArrayList<cgCache> cacheList, Boolean newItem, int reason) {
        if (this.searches.containsKey(searchId) == false) return null;

        cgSearch search = this.searches.get(searchId);

        return addSearch(search, cacheList, newItem, reason);
    }

	public Long addSearch(cgSearch search, ArrayList<cgCache> cacheList, Boolean newItem, int reason) {
		if (cacheList == null || cacheList.isEmpty()) return null;

		Long searchId = search.getCurrentId();
		searches.put(searchId, search);
        
		if (storage == null) storage = new cgData(this);
        if (newItem == true) {
            // save only newly downloaded data
            for (cgCache oneCache : cacheList) {
                String oneGeocode = oneCache.geocode.toUpperCase();
                String oneGuid = oneCache.guid.toLowerCase();

                oneCache.reason = reason;

                if (storage.isThere(oneGeocode, oneGuid, false, false) == false || reason >= 1) { // if for offline, do not merge
                    storage.saveCache(oneCache);
                } else {
                    cgCache mergedCache = oneCache.merge(storage, oneGeocode, oneGuid);
					
                    storage.saveCache(mergedCache);
                }
            }
        }

		return searchId;
	}

    public void dropStored() {
		if (storage == null) storage = new cgData(this);
        storage.dropStored();
    }

	public ArrayList<cgTrackable> loadInventory(String geocode) {
		return storage.loadInventory(geocode);
	}

	public ArrayList<cgSpoiler> loadSpoilers(String geocode) {
		return storage.loadSpoilers(geocode);
	}

	public cgWaypoint loadWaypoint(int id) {
		return storage.loadWaypoint(id);
	}

	public void setAction(String act) {
		action = act;
	}

	public String getAction() {
		if (action == null) return "";
		return action;
	}

	public boolean addLog(String geocode, cgLog log) {
		if (geocode == null || geocode.length() == 0) return false;
		if (log == null) return false;

		ArrayList<cgLog> list = new ArrayList<cgLog>();
		list.add(log);

	    return storage.saveLogs(geocode, list, false);
	}
}
