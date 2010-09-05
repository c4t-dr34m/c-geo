package carnero.cgeo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;

public class cgeogpxes extends Activity {
	private ArrayList<File> files = new ArrayList<File>();
    private cgeoapplication app = null;
	private cgSettings settings = null;
	private cgBase base = null;
	private cgWarning warning = null;
    private Context activity = null;
	private LayoutInflater inflater = null;
	private LinearLayout addList = null;
	private ProgressDialog waitDialog = null;
	private ProgressDialog parseDialog = null;
	private int imported = 0;

	final private Handler changeWaitDialogHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.obj != null && waitDialog != null) {
				waitDialog.setMessage("searching for .gpx files\nin " + (String)msg.obj);
			}
		}
	};
	
	final private Handler changeParseDialogHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.obj != null && parseDialog != null) {
				parseDialog.setMessage("loading caches from .gpx file\nstored: " + (Integer)msg.obj);
			}
		}
	};

	final private Handler loadFilesHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				if (addList == null) addList = (LinearLayout)findViewById(R.id.gpx_list);

				if (files == null || files.isEmpty()) {
					if (waitDialog != null) waitDialog.dismiss();

					warning.showToast("Sorry, c:geo found no .gpx files.");

					finish();
					return;
				} else {
					LinearLayout oneFilePre = null;

					for (File file : files) {
						if (settings.skin == 1) oneFilePre = (LinearLayout)inflater.inflate(R.layout.gpxes_button_light, null);
						else oneFilePre = (LinearLayout)inflater.inflate(R.layout.gpxes_button_dark, null);

						LinearLayout oneFile = (LinearLayout)oneFilePre.findViewById(R.id.button);

						oneFile.setClickable(true);
						oneFile.setOnTouchListener(new cgViewTouch(settings, oneFile, 0));
						oneFile.setOnClickListener(new loadFileListener(file));

						((TextView)oneFile.findViewById(R.id.filepath)).setText(file.getParent());
						((TextView)oneFile.findViewById(R.id.filename)).setText(file.getName());
						
						addList.addView(oneFilePre);
					}
				}

				if (waitDialog != null) waitDialog.dismiss();
			} catch (Exception e) {
				if (waitDialog != null) waitDialog.dismiss();
				Log.e(cgSettings.tag, "cgeogpxes.loadFilesHandler: " + e.toString());
			}
		}
	};

	final private Handler loadCachesHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				if (parseDialog != null) parseDialog.dismiss();

				warning.helpDialog("import", imported + " caches imported");
				imported = 0;
			} catch (Exception e) {
				if (parseDialog != null) parseDialog.dismiss();
			}
		}
	};

   @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		// init
		activity = this;
        app = (cgeoapplication)this.getApplication();
        settings = new cgSettings(this, getSharedPreferences(cgSettings.preferences, 0));
        base = new cgBase(app, settings, getSharedPreferences(cgSettings.preferences, 0));
        warning = new cgWarning(this);

		// set layout
		setTitle("import gpx");
		if (settings.skin == 1) setContentView(R.layout.gpxes_light);
		else setContentView(R.layout.gpxes_dark);
		inflater = getLayoutInflater();

		waitDialog = ProgressDialog.show(this, "searching", "searching for .gpx files", true);
		waitDialog.setCancelable(false);

		(new loadFiles()).start();
	}

	private class loadFiles extends Thread {
		@Override
		public void run() {
			ArrayList<File> list = new ArrayList<File>();

			try {
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) == true) {
					listDir(list, Environment.getExternalStorageDirectory());
				} else {
					Log.w(cgSettings.tag, "No external media mounted.");
				}
			} catch (Exception e) {
				Log.e(cgSettings.tag, "cgeogpxes.loadFiles.run: " + e.toString());
			}

			final Message msg = new Message();
			msg.obj = "loaded directories";
			changeWaitDialogHandler.sendMessage(msg);

			files.addAll(list);
			list.clear();

			loadFilesHandler.sendMessage(new Message());
	   }
   }

	private void listDir(ArrayList<File> list, File directory) {
	   if (directory == null || directory.isDirectory() == false || directory.canRead() == false) return;

	   final File[] listPre = directory.listFiles();

	   if (listPre != null && listPre.length > 0) {
		   final int listCnt = listPre.length;

		   for (int i = 0; i < listCnt; i ++) {
			   if (listPre[i].canRead() == true && listPre[i].isFile() == true) {
					final String[] nameParts = listPre[i].getName().split("\\.");
					if (nameParts.length > 1) {
						final String extension = nameParts[(nameParts.length - 1)].toLowerCase();

						if (extension.equals("gpx") == false) continue;
					} else {
						continue; // file has no extension
					}

				   list.add(listPre[i]); // add file to list
			   } else if (listPre[i].canRead() == true && listPre[i].isDirectory() == true) {
				   final Message msg = new Message();
				   String name = listPre[i].getName();
				   if (name.length() > 16) name = name.substring(0, 14) + "...";
				   msg.obj = name;
				   changeWaitDialogHandler.sendMessage(msg);

				   listDir(list, listPre[i]); // go deeper
			   }
		   }
	   }

	   return;
	}

	private class loadFileListener implements View.OnClickListener {
		File file = null;

		public loadFileListener(File fileIn) {
			file = fileIn;
		}

		public void onClick(View view) {
			if (waitDialog != null) waitDialog.dismiss();

			parseDialog = ProgressDialog.show(activity, "reading file", "loading caches from .gpx file", true);
			parseDialog.setCancelable(false);

			new loadCaches(file).start();
		}
	}

	private class loadCaches extends Thread {
		File file = null;

		public loadCaches(File fileIn) {
			file = fileIn;
		}

		@Override
		public void run() {
			final long searchId = base.parseGPX(app, file, changeParseDialogHandler);

			imported = app.getCount(searchId);

			loadCachesHandler.sendMessage(new Message());
	   }
   }
}
