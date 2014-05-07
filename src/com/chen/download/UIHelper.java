package com.chen.download;

import java.lang.reflect.Field;
import java.util.Map;

import android.util.Log;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.chen.download.volley.GsonObj;
import com.chen.download.volley.GsonRequest;
import com.chen.download.volley.IResponseListener;
import com.chen.download.volley.MyVolley;
import com.chen.download.volley.UIHelperUtil;

public class UIHelper {

	private static String port;

	public static void reqData(int method,
			@SuppressWarnings("rawtypes") final Class cls,
			Map<String, Object> params, Object obj, IResponseListener listener) {

		final UIHelperUtil uhu = UIHelperUtil.getUIHelperUtil(listener);

		getPort(obj, cls);

		@SuppressWarnings({ "rawtypes", "unchecked" })
		GsonRequest jr = new GsonRequest(UIHelperUtil.URL + port, cls, params,
				new Listener() {

					@Override
					public void onResponse(Object arg0) {
						// TODO Auto-generated method stub
						if (arg0 != null) {
							uhu.sendSuccessMessage(arg0);
						} else
							uhu.sendFailureMessage(null);
						uhu.sendFinishMessage();
					}
				}, new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {
						// TODO Auto-generated method stub
						uhu.sendFailureMessage(arg0);
						uhu.sendFinishMessage();
						Log.e("Json",
								arg0 != null ? cls.getName()
										+ arg0.getMessage() : "error");
					}
				}, method);
		jr.setShouldCache(true);
		uhu.sendStartMessage();
		MyVolley.getRequestQueue().add(jr);
	}

	private static void getPort(Object obj,
			@SuppressWarnings("rawtypes") Class cls) {
		GsonObj gsonObj = null;
		try {
			gsonObj = (GsonObj) cls.newInstance();
			if (obj != null) {
				try {
					Field field = cls.getDeclaredField("obj");
					if (field != null) {
						field.setAccessible(true);
						field.set(gsonObj, obj);
					}
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				}
			}
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		port = gsonObj.getInterface();
	}

}
