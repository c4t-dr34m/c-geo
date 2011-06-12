package carnero.cgeo.mapinterfaces;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public interface ActivityImpl {

	Resources getResources();

	Activity getActivity();

	void superOnCreate(Bundle savedInstanceState);

	void superOnResume();

	void superOnDestroy();

	boolean superOnCreateOptionsMenu(Menu menu);

	boolean superOnPrepareOptionsMenu(Menu menu);

	boolean superOnOptionsItemSelected(MenuItem item);

}
