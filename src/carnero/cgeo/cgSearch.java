package carnero.cgeo;

import java.util.ArrayList;

public class cgSearch {
	private Long id = null;
	private ArrayList<String> geocodes = new ArrayList<String>();

	public int errorRetrieve = 0;
	public String error = null;
	public String url = "";
	public String viewstate = "";
	public String viewstate1 = "";
	public int totalCnt = 0;

	public cgSearch() {
		id = System.currentTimeMillis();
	}

	public Long getCurrentId() {
		return id;
	}

	public ArrayList<String> getGeocodes() {
		return geocodes;
	}

	public int getCount() {
		return geocodes.size();
	}

	public void addGeocode(String geocode) {
		if (geocodes == null) {
			geocodes = new ArrayList<String>();
		}

		geocodes.add(geocode);
	}
}
