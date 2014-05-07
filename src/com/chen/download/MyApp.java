/**
 * 
 */
package com.chen.download;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.Application;

import com.chen.download.volley.MyVolley;
import com.chen.download.volley.UIHelperUtil;

public class MyApp extends Application {
	private static MyApp mInstance = null;

	@Override
	public void onCreate() {
		super.onCreate();
		UIHelperUtil.cxt = this;
		mInstance = this;
		MyVolley.init(this);
		ImageLoaderUtil.init(this);
	}

	public static MyApp getInstance() {
		return mInstance;
	}

	private Map<String, Activity> activityList = new HashMap<String, Activity>();

	public void addActivity(Activity activity) {
		activityList.put(activity.getLocalClassName(), activity);
	}

	public void removeActivity(Activity activity) {
		if (activityList.containsKey(activity.getLocalClassName()))
			activityList.remove(activity.getLocalClassName());
	};

	public void clearActivity() {
		Iterator<Entry<String, Activity>> iter = activityList.entrySet()
				.iterator();
		while (iter.hasNext()) {
			Entry<String, Activity> entry = iter.next();
			Activity activity = entry.getValue();
			activity.finish();
		}
		activityList.clear();
	}

	public static void clearCache() {
		ImageLoaderUtil.clearCache();
	}

}
