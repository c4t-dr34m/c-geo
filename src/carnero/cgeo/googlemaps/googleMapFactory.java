package carnero.cgeo.googlemaps;

import carnero.cgeo.mapinterfaces.MapFactory;

public class googleMapFactory implements MapFactory{

	@Override
	public Class getMapClass() {
		return cgeomap.class;
	}

}
