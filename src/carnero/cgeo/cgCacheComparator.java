package carnero.cgeo;

import java.util.Comparator;
import android.util.Log;

public class cgCacheComparator implements Comparator {
	private Double latitude = null;
	private Double longitude = null;

	public cgCacheComparator(Double latitudeIn, Double longitudeIn) {
		latitude = latitudeIn;
		longitude = longitudeIn;
	}

	public int compare(Object object1, Object object2) {
		int result = 0;
		try {
			cgCache cache1 = (cgCache)object1;
			cgCache cache2 = (cgCache)object2;

			if (
				(cache1.latitude == null || cache1.longitude == null || cache2.latitude == null || cache2.longitude == null) &&
				cache1.distance != null && cache2.distance != null
			) {
				if (cache1.distance < cache2.distance) return -1;
				else if (cache1.distance > cache2.distance) return 1;
				else return 0;
			} else {
				if (cache1.latitude == null || cache1.longitude == null) return 1;
				if (cache2.latitude == null || cache2.longitude == null) return -1;

				Double distance1 = cgBase.getDistance(latitude, longitude, cache1.latitude, cache1.longitude);
				Double distance2 = cgBase.getDistance(latitude, longitude, cache2.latitude, cache2.longitude);

				if (distance1 < distance2) result =  -1;
				else if (distance1 > distance2) result = 1;
				else result = 0;
			}
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgCacheComparator.compare: " + e.toString());
		}

		return result;
	}
}
