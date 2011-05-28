package carnero.cgeo;

import java.util.Comparator;
import android.util.Log;

/**
 * sorts caches by the users own voting (if available at all)
 * @author bananeweizen
 *
 */
public class cgCacheVoteComparator implements Comparator<cgCache> {

	public int compare(cgCache cache1, cgCache cache2) {
		try {
			// if there is no vote available, put that cache at the end of the list
			int vote1 = 0;
			if (cache1.vote != null) {
				vote1 = cache1.vote;
			}

			int vote2 = 0;
			if (cache2.vote != null) {
				vote2 = cache2.vote;
			}

			if (vote1 < vote2) {
				return 1;
			} else if (vote2 < vote1) {
				return -1;
			} else {
				return 0;
			}
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgCacheVoteComparator.compare: " + e.toString());
		}
		return 0;
	}
}
