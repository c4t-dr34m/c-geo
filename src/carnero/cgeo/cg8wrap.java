package carnero.cgeo;

import android.app.Activity;

public class cg8wrap {
	private cg8 cg8 = null;

	static {
		try {
			Class.forName("cg8");
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void check() {
		// nothing
	}

	public cg8wrap(Activity activityIn) {
		cg8 = new cg8(activityIn);
	}

	public int getRotation() {
		return cg8.getRotation();
	}
}
