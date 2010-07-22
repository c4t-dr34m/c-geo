package carnero.cgeo;

import android.app.Activity;
import android.util.Log;
import android.text.Html;
import android.view.Display;
import android.view.WindowManager;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

public class cgHtmlImg implements Html.ImageGetter {
	private Activity activity = null;
	private cgSettings settings = null;
	private String geocode = null;
	private int reason = 0;
	private boolean onlySave = false;

	public cgHtmlImg(Activity activityIn, cgSettings settingsIn, String geocodeIn, int reasonIn, boolean onlySaveIn) {
		activity = activityIn;
		settings = settingsIn;
		geocode = geocodeIn;
		reason = reasonIn;
		onlySave = onlySaveIn;
	}

	@Override
	public BitmapDrawable getDrawable(String url) {
		Bitmap imagePre = null;
		BitmapDrawable image = null;
		String dirName;
		String fileName;

		if (url == null || url.length() == 0) {
			return null;
		}

		String[] urlParts = url.split("\\.");
		String urlExt;
		if (urlParts.length > 1) {
			urlExt = "." + urlParts[(urlParts.length - 1)];
			if (urlExt.length() > 5) {
				urlExt = "";
			}
		} else {
			urlExt = "";
		}

		if (this.geocode != null && this.geocode.length() > 0) {
			dirName = this.settings.getStorage() + this.geocode + "/";
			fileName = this.settings.getStorage() + this.geocode + "/" + cgBase.md5(url) + urlExt;
		} else {
			dirName = this.settings.getStorage() + "_others/";
			fileName = this.settings.getStorage() + "_others/" + cgBase.md5(url) + urlExt;
		}

		File dir = new File(this.settings.getStorage());
		if (dir.exists() == false) {
			dir.mkdirs();
		}
		dir = new File(dirName);
		if (dir.exists() == false) {
			dir.mkdirs();
		}
		dir = null;

		try {
            File file = new File(fileName);
            if (file.exists() == true) {
                if (reason == 1 || file.lastModified() > ((new Date()).getTime() - (24 * 60 * 60 * 1000))) {
                    imagePre = BitmapFactory.decodeFile(fileName);
                }
            }
		} catch (Exception e) {
			Log.w(cgSettings.tag, "cgHtmlImg.getDrawable (reading cache): " + e.toString());
		}

		if (imagePre == null && reason == 0) {
			HttpClient client = null;
			HttpGet getMethod = null;
			HttpResponse httpResponse = null;
			HttpEntity entity = null;
			BufferedHttpEntity bufferedEntity = null;

			for (int i = 0; i < 2; i ++) {
				if (i > 0) Log.w(cgSettings.tag, "cgHtmlImg.getDrawable: Failed to download data, retrying. Attempt #" + (i + 1));

				try {
					client = new DefaultHttpClient();
					getMethod = new HttpGet(url);
					httpResponse = client.execute(getMethod);
					entity = httpResponse.getEntity();
					bufferedEntity = new BufferedHttpEntity(entity);

					if (onlySave == false) {
						long imageSize = bufferedEntity.getContentLength();

						BitmapFactory.Options bfOptions = new BitmapFactory.Options();
						// large images will be downscaled on input to save memory
						if (imageSize > (4 * 1024 * 1024)) {
							bfOptions.inSampleSize = 16;
						} else if (imageSize > (2 * 1024 * 1024)) {
							bfOptions.inSampleSize = 10;
						} else if (imageSize > (1 * 1024 * 1024)) {
							bfOptions.inSampleSize = 6;
						} else if (imageSize > (0.5 * 1024 * 1024)) {
							bfOptions.inSampleSize = 2;
						}

						if (bufferedEntity != null) imagePre = BitmapFactory.decodeStream(bufferedEntity.getContent(), null, bfOptions);

						if (imagePre != null) break;
					} else {
						break;
					}
				} catch (Exception e) {
					Log.e(cgSettings.tag, "cgHtmlImg.getDrawable (downloading from web): " + e.toString());
				}
			}

			try {
				// save to memory/SD cache
				if (bufferedEntity != null) {
					InputStream is = (InputStream)bufferedEntity.getContent();
					FileOutputStream fos = new FileOutputStream(fileName);
					try {
						byte[] buffer = new byte[4096];
						int l;
						while ((l = is.read(buffer)) != -1) {
							fos.write(buffer, 0, l);
						}
					} catch (IOException e) {
						Log.e(cgSettings.tag, "cgHtmlImg.getDrawable (saving to cache): " + e.toString());
					} finally {
						is.close();
						fos.flush();
						fos.close();
					}
				}
			} catch (Exception e) {
				Log.e(cgSettings.tag, "cgHtmlImg.getDrawable (saving to cache): " + e.toString());
			}
		}

		if (onlySave == false) {
			if (imagePre == null) {
				Log.d(cgSettings.tag, "cgHtmlImg.getDrawable: Failed to obtain image");

				imagePre = BitmapFactory.decodeResource(this.activity.getResources(), R.drawable.image_not_loaded);
			}

			Display display = ((WindowManager)this.activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
			int imgWidth = imagePre.getWidth();
			int imgHeight = imagePre.getHeight();
			int maxWidth = display.getWidth() - 25;
			int maxHeight = display.getHeight() - 25;

			Double ratio = new Double(1.0);
			int width = imgWidth;
			int height = imgHeight;

			if (imgWidth > maxWidth || imgHeight > maxHeight) {
				if ((maxWidth / imgWidth) > (maxHeight / imgHeight)) {
					ratio = (double)maxHeight / (double)imgHeight;
				} else {
					ratio = (double)maxWidth / (double)imgWidth;
				}

				width = (int)Math.ceil(imgWidth * ratio);
				height = (int)Math.ceil(imgHeight * ratio);

				try {
					imagePre = Bitmap.createScaledBitmap(imagePre, width, height, true);
				} catch (Exception e) {
					Log.d(cgSettings.tag, "cgHtmlImg.getDrawable: Failed to scale image");
					return null;
				}
			}

			image = new BitmapDrawable(imagePre);
			image.setBounds(new Rect(0, 0, width, height));
			imagePre = null;

			return image;
		}

		return null;
	}
}
