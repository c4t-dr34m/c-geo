package carnero.cgeo.mapinterfaces;

import android.view.View;

public interface MapFactory {

	public Class getMapClass();

	public int getMapViewId();

	public int getMapLayoutId();
}
