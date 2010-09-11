package carnero.cgeo;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class cgeoabout extends Activity {
	private Activity activity = null;
	private Resources res = null;
	private cgSettings settings = null;
	private cgBase base = null;

   @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		// init
		activity = this;
		res = this.getResources();
		settings = new cgSettings(this, this.getSharedPreferences(cgSettings.preferences, 0));
		base = new cgBase((cgeoapplication)this.getApplication(), settings, this.getSharedPreferences(cgSettings.preferences, 0));

		// set layout
		setTitle(res.getString(R.string.about));
		if (settings.skin == 1) setContentView(R.layout.about_light);
		else setContentView(R.layout.about_dark);

        init();
	}

	private void init() {
		try {
			PackageManager manager = this.getPackageManager();
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);

			setTitle(res.getString(R.string.about) + " (ver. " + info.versionName + ")");

			manager = null;
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgeoabout.init: Failed to obtain package version.");
		}

		TextView donateMLink = (TextView)findViewById(R.id.donation_more);
		donateMLink.setClickable(true);
		donateMLink.setOnTouchListener(new cgViewTouch(settings, donateMLink, 0));
		donateMLink.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=2Z69QWLRCBE9N&lc=US&item_name=c%3ageo&currency_code=EUR&amount=15&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted")));
			}
		});

		TextView donateLLink = (TextView)findViewById(R.id.donation_less);
		donateLLink.setClickable(true);
		donateLLink.setOnTouchListener(new cgViewTouch(settings, donateLLink, 0));
		donateLLink.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=2Z69QWLRCBE9N&lc=US&item_name=c%3ageo&currency_code=EUR&amount=7&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted")));
			}
		});

		TextView authorLink = (TextView)findViewById(R.id.author);
		authorLink.setClickable(true);
		authorLink.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://carnero.cc/")));
			}
		});

		TextView supportLink = (TextView)findViewById(R.id.support);
		supportLink.setClickable(true);
		supportLink.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:carnero@carnero.cc")));
			}
		});

		TextView websiteLink = (TextView)findViewById(R.id.website);
		websiteLink.setClickable(true);
		websiteLink.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://cgeo.carnero.cc/")));
			}
		});

		TextView facebookLink = (TextView)findViewById(R.id.facebook);
		facebookLink.setClickable(true);
		facebookLink.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.facebook.com/pages/cgeo/297269860090")));
			}
		});

		TextView twitterLink = (TextView)findViewById(R.id.twitter);
		twitterLink.setClickable(true);
		twitterLink.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://twitter.com/android_gc")));
			}
		});

		TextView graphicLink = (TextView)findViewById(R.id.graphic);
		graphicLink.setClickable(true);
		graphicLink.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.jaytech.cz/")));
			}
		});
	}
}
