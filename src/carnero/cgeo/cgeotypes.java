package carnero.cgeo;

import android.app.Activity;
import android.os.Bundle;
import android.app.Dialog;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.view.Window;

public class cgeotypes extends Dialog {
	private cgSettings settings = null;
	private cgBase base = null;
	private cgWarning warning = null;
	private LayoutInflater inflater = null;
	private cgeovisit parent = null;
	private String type = null;

	public cgeotypes(Activity contextIn, cgeovisit parentIn, cgSettings settingsIn, cgBase baseIn, String typeIn) {
		super(contextIn);
		
        // init
		settings = new cgSettings(contextIn, contextIn.getSharedPreferences(cgSettings.preferences, 0));
		base = new cgBase((cgeoapplication)contextIn.getApplication(), settings, contextIn.getSharedPreferences(cgSettings.preferences, 0));
		warning = new cgWarning(contextIn);
		type = typeIn.toLowerCase();

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

		if (settings.skin == 1) setContentView(R.layout.types_dark);
		else  setContentView(R.layout.types_light);

		getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);

		if (base.logTypes2.isEmpty()) {
			warning.showToast("Sorry, c:geo can\'t find any log type.");
			dismiss();
		}

		inflater = getLayoutInflater();

		LinearLayout addList = (LinearLayout)findViewById(R.id.types_list);
		for (final int typeOne : base.logTypes2.keySet()) {
			if (
					((type.equals("traditional") || type.equals("multi") || type.equals("mystery") || type.equals("wherigo") || type.equals("virtual")) && (typeOne == 2 || typeOne == 3 || typeOne == 4 || typeOne == 7 || typeOne == 45)) ||
					(type.equals("earth") && (typeOne == 2 || typeOne == 3 || typeOne == 4 || typeOne == 7)) || // without maintenance
					(type.equals("event") && (typeOne == 7 || typeOne == 9 || typeOne == 10)) ||
					(type.equals("webcam") && (typeOne == 3 || typeOne == 4 || typeOne == 7 || typeOne == 11 || typeOne == 45))
				) {
				LinearLayout oneAddPre = null;
				if (settings.skin == 1) oneAddPre = (LinearLayout)inflater.inflate(R.layout.types_button_light, null);
				else oneAddPre = (LinearLayout)inflater.inflate(R.layout.types_button_dark, null);

				Button oneAdd = (Button)oneAddPre.findViewById(R.id.button);

				oneAdd.setText(base.logTypes2.get(typeOne));
				oneAdd.setClickable(true);
				oneAdd.setOnTouchListener(new cgViewTouch(settings, oneAdd));
				oneAdd.setOnClickListener(new buttonListener(typeOne));
				addList.addView(oneAddPre);
			}
		}
	}

	private class buttonListener implements View.OnClickListener {
		private int type = 0;

		public buttonListener(int typeIn) {
			type = typeIn;
		}

		public void onClick(View arg0) {
			if (parent != null) parent.setType(type);
			dismiss();
		}
	}
}
