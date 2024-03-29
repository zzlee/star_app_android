/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 * 
 * https://github.com/phonegap-build/GAPlugin/blob/master/src/android/GAPlugin.java
 *
 */

package com.phonegap.plugins.analytics;
import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;
import org.json.JSONArray;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Log;
import com.google.analytics.tracking.android.Tracker;

public class GAPlugin extends CordovaPlugin {
	private static final String TAG = " GAPlugin";
	private com.google.analytics.tracking.android.EasyTracker instance;
	private Tracker tracker;
	public GAPlugin(){
		instance = com.google.analytics.tracking.android.EasyTracker
				.getInstance();
	}
	public boolean execute(String action, JSONArray args, CallbackContext callback) {
		
		GoogleAnalytics ga = GoogleAnalytics.getInstance(cordova.getActivity());
		GoogleAnalytics.getInstance(cordova.getActivity()).setDebug(true);
		tracker = ga.getDefaultTracker(); 
		
		if (action.equals("initGA")) {
			try {
//				start();
				Log.v(TAG + " GA-id : " + args.getString(0));
				tracker = ga.getTracker(args.getString(0));
				GAServiceManager.getInstance().setDispatchPeriod(args.getInt(1));
				ga.setDefaultTracker(tracker);
				callback.success("initGA - id = " + args.getString(0) + "; interval = " + args.getInt(1) + " seconds");
				
				return true;
			} catch (final Exception e) {
				callback.error(e.getMessage());
			}
		} else if (action.equals("exitGA")) {
			try {
//				stop();
				GAServiceManager.getInstance().dispatch();
				callback.success("exitGA");
				return true;
			} catch (final Exception e) {
				callback.error(e.getMessage());
			}
		} else if (action.equals("trackEvent")) {
			try {
				tracker.sendEvent(args.getString(0), args.getString(1), args.getString(2), args.getLong(3));
				callback.success("trackEvent - category = " + args.getString(0) + "; action = " + args.getString(1) + "; label = " + args.getString(2) + "; value = " + args.getInt(3));
				return true;
			} catch (final Exception e) {
				callback.error(e.getMessage());
			}
		} else if (action.equals("trackPage")) {
			try {
				tracker.sendView(args.getString(0));
				callback.success("trackPage - url = " + args.getString(0));
				return true;
			} catch (final Exception e) {
				callback.error(e.getMessage());
			}
		} else if (action.equals("setVariable")) {
			try {
				tracker.setCustomDimension(args.getInt(0), args.getString(1));
				callback.success("setVariable passed - index = " + args.getInt(0) + "; value = " + args.getString(1));
				return true;
			} catch (final Exception e) {
				callback.error(e.getMessage());
			}
		}
		else if (action.equals("setDimension")) {
			try {
				tracker.setCustomDimension(args.getInt(0), args.getString(1));
				callback.success("setDimension passed - index = " + args.getInt(0) + "; value = " + args.getString(1));
				return true;
			} catch (final Exception e) {
				callback.error(e.getMessage());
			}
		}
		else if (action.equals("setMetric")) {
			try {
				tracker.setCustomMetric(args.getInt(0), args.getLong(1));
				callback.success("setVariable passed - index = " + args.getInt(2) + "; key = " + args.getString(0) + "; value = " + args.getString(1));
				return true;
			} catch (final Exception e) {
				callback.error(e.getMessage());
			}
		}
		return false;
	}
	
//	private void start() {
//		instance.activityStart(this.cordova.getActivity());
//		tracker = EasyTracker.getTracker();
//	}
//
//	private void stop() {
//		instance.activityStop(this.cordova.getActivity());
//	}
}