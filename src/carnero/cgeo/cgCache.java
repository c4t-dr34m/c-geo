package carnero.cgeo;

import android.text.Spannable;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;

public class cgCache {

	public Long updated = null;
	public Long detailedUpdate = null;
	public Long visitedDate = null;
	public Integer reason = 0;
	public Boolean detailed = false;
	public String geocode = "";
	public String cacheid = "";
	public String guid = "";
	public String type = "";
	public String name = "";
	public Spannable nameSp = null;
	public String owner = "";
	public String ownerReal = "";
	public Date hidden = null;
	public String hint = "";
	public String size = "";
	public Float difficulty = new Float(0);
	public Float terrain = new Float(0);
	public Double direction = null;
	public Double distance = null;
	public String latlon = "";
	public String latitudeString = "";
	public String longitudeString = "";
	public String location = "";
	public Double latitude = null;
	public Double longitude = null;
	public Double elevation = null;
	public String shortdesc = "";
	public String description = "";
	public boolean disabled = false;
	public boolean archived = false;
	public boolean members = false;
	public boolean found = false;
	public boolean favourite = false;
	public boolean own = false;
	public Integer favouriteCnt = null;
	public Float rating = null;
	public Integer votes = null;
	public Float myVote = null;
	public int inventoryItems = 0;
	public ArrayList<String> attributes = null;
	public ArrayList<cgWaypoint> waypoints = null;
	public ArrayList<cgSpoiler> spoilers = null;
	public ArrayList<cgLog> logs = null;
	public ArrayList<cgTrackable> inventory = null;
	public HashMap<Integer, Integer> logCounts = new HashMap<Integer, Integer>();
	public boolean logOffline = false;
	// temporary values
	public boolean statusChecked = false;
	public boolean statusCheckedView = false;
	public String directionImg = null;

	public cgCache merge(cgData storage) {
		boolean loadA = true;
		boolean loadW = true;
		boolean loadS = true;
		boolean loadL = true;
		boolean loadI = true;

		if (attributes == null || attributes.isEmpty() == true) {
			loadA = false;
		}
		if (waypoints == null || waypoints.isEmpty() == true) {
			loadW = false;
		}
		if (spoilers == null || spoilers.isEmpty() == true) {
			loadS = false;
		}
		if (logs == null || logs.isEmpty() == true) {
			loadL = false;
		}
		if (inventory == null || inventory.isEmpty() == true) {
			loadI = false;
		}

		final cgCache oldCache = storage.loadCache(geocode, guid, loadA, loadW, loadS, loadL, loadI, false);

		if (oldCache == null) {
			return this;
		}

		updated = System.currentTimeMillis();
		if (detailed == false && oldCache.detailed == true) {
			detailed = true;
			detailedUpdate = System.currentTimeMillis();
		}

		if (visitedDate == null || visitedDate == 0) {
			visitedDate = oldCache.visitedDate;
		}
		if (reason == null || reason == 0) {
			reason = oldCache.reason;
		}
		if (geocode == null || geocode.length() == 0) {
			geocode = oldCache.geocode;
		}
		if (cacheid == null || cacheid.length() == 0) {
			cacheid = oldCache.cacheid;
		}
		if (guid == null || guid.length() == 0) {
			guid = oldCache.guid;
		}
		if (type == null || type.length() == 0) {
			type = oldCache.type;
		}
		if (name == null || name.length() == 0) {
			name = oldCache.name;
		}
		if (nameSp == null || nameSp.length() == 0) {
			nameSp = oldCache.nameSp;
		}
		if (owner == null || owner.length() == 0) {
			owner = oldCache.owner;
		}
		if (ownerReal == null || ownerReal.length() == 0) {
			ownerReal = oldCache.ownerReal;
		}
		if (hidden == null) {
			hidden = oldCache.hidden;
		}
		if (hint == null || hint.length() == 0) {
			hint = oldCache.hint;
		}
		if (size == null || size.length() == 0) {
			size = oldCache.size;
		}
		if (difficulty == null || difficulty == 0) {
			difficulty = oldCache.difficulty;
		}
		if (terrain == null || terrain == 0) {
			terrain = oldCache.terrain;
		}
		if (direction == null) {
			direction = oldCache.direction;
		}
		if (distance == null) {
			distance = oldCache.distance;
		}
		if (latlon == null || latlon.length() == 0) {
			latlon = oldCache.latlon;
		}
		if (latitudeString == null || latitudeString.length() == 0) {
			latitudeString = oldCache.latitudeString;
		}
		if (longitudeString == null || longitudeString.length() == 0) {
			longitudeString = oldCache.longitudeString;
		}
		if (location == null || location.length() == 0) {
			location = oldCache.location;
		}
		if (latitude == null) {
			latitude = oldCache.latitude;
		}
		if (longitude == null) {
			longitude = oldCache.longitude;
		}
		if (elevation == null) {
			elevation = oldCache.elevation;
		}
		if (shortdesc == null || shortdesc.length() == 0) {
			shortdesc = oldCache.shortdesc;
		}
		if (description == null || description.length() == 0) {
			description = oldCache.description;
		}
		if (favouriteCnt == null) {
			favouriteCnt = oldCache.favouriteCnt;
		}
		if (rating == null) {
			rating = oldCache.rating;
		}
		if (votes == null) {
			votes = oldCache.votes;
		}
		if (myVote == null) {
			myVote = oldCache.myVote;
		}
		if (inventoryItems == 0) {
			inventoryItems = oldCache.inventoryItems;
		}
		if (attributes == null) {
			attributes = oldCache.attributes;
		}
		if (waypoints == null) {
			waypoints = oldCache.waypoints;
		}
		if (spoilers == null) {
			spoilers = oldCache.spoilers;
		}
		if (inventory == null) {
			inventory = oldCache.inventory;
		}
		if (logs == null || logs.isEmpty()) { // keep last known logs if none
			logs = oldCache.logs;
		}

		return this;
	}
	
	public boolean hasTrackables(){
		return inventoryItems > 0;
	}
}
