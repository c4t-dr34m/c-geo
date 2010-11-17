package carnero.cgeo;

import android.app.Activity;
import android.os.Bundle;
import android.app.Dialog;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.DatePicker;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import java.util.Calendar;

public class cgeodate extends Dialog {
	private cgSettings settings = null;
	private cgBase base = null;
	private cgWarning warning = null;
	private cgLogForm parent = null;
	private Calendar date = Calendar.getInstance();

	public cgeodate(Activity contextIn, cgLogForm parentIn, Calendar dateIn) {
		super(contextIn);

		// init
		settings = new cgSettings(contextIn, contextIn.getSharedPreferences(cgSettings.preferences, 0));
		base = new cgBase((cgeoapplication)contextIn.getApplication(), settings, contextIn.getSharedPreferences(cgSettings.preferences, 0));
		warning = new cgWarning(contextIn);
		date = dateIn;

		parent = parentIn;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		try {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		} catch (Exception e) {
			// nothing
		}
		
		if (settings.skin == 1) setContentView(R.layout.date_dark);
		else  setContentView(R.layout.date_light);

		getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);

		DatePicker picker = (DatePicker)findViewById(R.id.picker);
		picker.init(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE), new pickerListener());
	}

   public class pickerListener implements DatePicker.OnDateChangedListener {
	   @Override
	   public void onDateChanged(DatePicker picker, int year, int month, int day) {
		   if (parent != null) {
				date.set(year, month, day);

				parent.setDate(date);
		   }
	   }
   }
}
