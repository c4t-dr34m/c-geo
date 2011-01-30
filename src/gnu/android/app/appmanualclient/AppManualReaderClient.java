package gnu.android.app.appmanualclient;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

/**
 * The "App Manual Reader" client is a class to be used in applications which
 * want to offer their users manuals through the gnu.android.appmanualreader
 * application. Such applications do not need to include the whole
 * "App Manual Reader" app but instead just have to include only this little
 * package. This package then provides the mechanism to open suitable installed
 * manuals. It does not include any manuals itself.
 * <p>
 * 
 * (c) 2011 Geocrasher (geocrasher@gmx.eu)
 * <p>
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see http://www.gnu.org/licenses/.
 * 
 * @author Geocrasher
 */
public class AppManualReaderClient {

	/**
	 * The URI scheme used to identify application manual URIs when flinging
	 * Intents around within an Android device, in the hope that there are
	 * activities registered which will handle such application manual URIs.
	 * Usually, there won't be just a single activity registered but instead
	 * many, depending on how many manuals are installed on an Android device.
	 */
	public static final String URI_SCHEME_APPMANUAL = "appmanual";

	/**
	 * Standardized topic for opening a manual at its beginning.
	 * 
	 * @see #openManual(String, String, Context)
	 * @see #openManual(String, String, Context, String)
	 */
	public static final String TOPIC_HOME = "andtw-home";
	/**
	 * Standardized topic for opening the index of a manual.
	 * 
	 * @see #openManual(String, String, Context)
	 * @see #openManual(String, String, Context, String)
	 */
	public static final String TOPIC_INDEX = "andtw-index";
	/**
	 * Standardized topic for opening a manual's "about" topic.
	 * 
	 * @see #openManual(String, String, Context)
	 * @see #openManual(String, String, Context, String)
	 */
	public static final String TOPIC_ABOUT_MANUAL = "andtw-about";

	/**
	 * Convenience function to open a manual at a specific topic. See
	 * {@link #openManual(String, String, Context, String)} for a detailed
	 * description.
	 * 
	 * @param manualIdentifier
	 *            the identifier of the manual to open. This identifier must
	 *            uniquely identify the manual as such, independent of the
	 *            particular locale the manual is intended for.
	 * @param topic
	 *            the topic to open. Please do not use spaces for topic names.
	 *            With respect to the TiddlyWiki infrastructure used for manuals
	 *            the topic needs to the tag of a (single) tiddler. This way
	 *            manuals can be localized (especially their topic titles)
	 *            without breaking an app's knowledge about topics. Some
	 *            standardized topics are predefined, such as
	 *            {@link #TOPIC_HOME}, {@link #TOPIC_INDEX}, and
	 *            {@link #TOPIC_ABOUT_MANUAL}.
	 * @param context
	 *            the context (usually an Activity) from which the manual is to
	 *            be opened. In particular, this context is required to derive
	 *            the proper current locale configuration in order to open
	 *            appropriate localized manuals, if installed.
	 * 
	 * @exception ActivityNotFoundException
	 *                there is no suitable manual installed and all combinations
	 *                of locale scope failed to activate any manual.
	 * 
	 * @see #openManual(String, String, Context, String)
	 */
	public static void openManual(String manualIdentifier, String topic,
			Context context) throws ActivityNotFoundException {
		openManual(manualIdentifier, topic, context, null);
	}

	/**
	 * Opens a manual at a specific topic. At least it tries to open a manual.
	 * As manuals are (usually) installed separately and we use late binding in
	 * form of implicit intents, a lot of things can go wrong.
	 * 
	 * We use late binding and the intent architecture in particular as follows:
	 * first, we use our own URI scheme called "appmanual". Second, we use the
	 * host field as a unique manual identifier (such as "c-geo" for the app
	 * manuals for a map which must not be named by the powers that wanna be).
	 * Third, a localized manual is differentiated as a path with a single
	 * element in form of (in this precedence) "/lang_country_variant",
	 * "/lang__variant", "/lang_country", "/lang", or "/". Fourth, the topic to
	 * open is encoded as the a fragment "#topic=mytopic".
	 * 
	 * In order to support localization, manuals can register themselves with
	 * different URIs.
	 * 
	 * @param manualIdentifier
	 *            the identifier of the manual to open. This identifier must
	 *            uniquely identify the manual as such, independent of the
	 *            particular locale the manual is intended for.
	 * @param topic
	 *            the topic to open. Please do not use spaces for topic names.
	 *            With respect to the TiddlyWiki infrastructure used for manuals
	 *            the topic needs to the tag of a (single) tiddler. This way
	 *            manuals can be localized (especially their topic titles)
	 *            without breaking an app's knowledge about topics. Some
	 *            standardized topics are predefined, such as
	 *            {@link #TOPIC_HOME}, {@link #TOPIC_INDEX}, and
	 *            {@link #TOPIC_ABOUT_MANUAL}.
	 * @param context
	 *            the context (usually an Activity) from which the manual is to
	 *            be opened. In particular, this context is required to derive
	 *            the proper current locale configuration in order to open
	 *            appropriate localized manuals, if installed.
	 * @param fallbackUri
	 *            either <code>null</code> or a fallback URI to be used in case
	 *            the user has not installed any suitable manual.
	 * 
	 * @exception ActivityNotFoundException
	 *                there is no suitable manual installed and all combinations
	 *                of locale scope failed to activate any manual and no
	 *                {@literal fallbackUri} was given.
	 */
	public static void openManual(String manualIdentifier, String topic,
			Context context, String fallbackUri)
			throws ActivityNotFoundException {
		//
		// The path of an "appmanual:" URI consists simply of the locale
		// information. This allows manual packages to register themselves
		// for both very specific locales as well as very broad ones.
		//
		String localePath = "/"
				+ context.getResources().getConfiguration().locale.toString();
		//
		// We later need this intent in order to try to launch an appropriate
		// manual (respectively its manual viewer). And yes, we need to set
		// the intent's category explicitly, even as we will later use
		// startActivity(): if we don't do this, the proper activity won't be
		// started albeit the filter almost matches. That dirty behavior (it is
		// documented wrong) had cost me half a day until I noticed some
		// informational log entry generated from the ActivityManager. Grrrr!
		//
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		//
		// Try to open the manual in the following order (subject to
		// availability):
		// 1. manualIdentifier_lang_country_variant (can also be
		// manualIdentifier_lang__variant in some cases)
		// 2. manualIdentifier_lang_country
		// 3. manualIdentifier_lang
		// 4. manualIdentifier
		// Of course, manuals are free to register more than one Intent,
		// in particular, the should register also the plain manualIdentifier
		// as a suitable fallback strategy. Even when installing multiple
		// manuals this doesn't matter, as the user then can choose which
		// one to use on a single or permanent basis.
		//
		String logTag = "appmanualclient";
		for ( ;; ) {
			Uri uri = Uri.parse(URI_SCHEME_APPMANUAL + "://" + manualIdentifier
					+ localePath + "#topic='" + topic + "'");
			// Note: we do not use a MIME type for this.
			intent.setData(uri);
			try {
				if ( Log.isLoggable(logTag, Log.INFO) ) {
					Log.i(logTag,
							"Trying to activate manual: uri=" + uri.toString());
				}
				context.startActivity(intent);
				//
				// We could successfully activate the manual activity, so no
				// further trials are required.
				//
				return;
			} catch ( ActivityNotFoundException noActivity ) {
				//
				// As long as we still have some locale information, reduce it
				// and try again a broader locale.
				//
				if ( localePath.length() > 1 ) {
					int underscore = localePath.lastIndexOf('_');
					if ( underscore > 0 ) {
						localePath = localePath.substring(0, underscore);
						//
						// Handle the case where we have a locale variant, yet
						// no locale country, thus two underscores in immediate
						// series. Get rid of both.
						//
						if ( localePath.endsWith("_") ) {
							localePath = localePath
									.substring(0, underscore - 1);
						}
					} else {
						//
						// Ready for the last round: try without any locale
						// modifiers.
						//
						localePath = "/";
					}
				} else {
					//
					// We've tried all combinations, so we've run out of them
					// and bail out.
					//
					break;
				}
			}
			//
			// Okay, go for the next round, we've updated (or rather trimmed)
			// the localeIdent, so let us try this.
			//
		}
		//
		// If we reach this code point then no suitable activity could be found
		// and activated. In case the caller specified a fallback URI we will
		// try to open that. As this will activate a suitable browser and this
		// is an asynchronous activity we won't get back any negative results,
		// such as 404's. Here we will only see such problems that prevented the
		// start of a suitable browsing activity.
		//
		if ( fallbackUri != null ) {
			intent = new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUri));
			intent.addCategory(Intent.CATEGORY_BROWSABLE);
			context.startActivity(intent);
		}
		//
		// We could not activate any manual and there was no fallback URI to
		// open, so we finally bail out unsuccessful with an exception.
		//
		throw new ActivityNotFoundException();
	}
}
