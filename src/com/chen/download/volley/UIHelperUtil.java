package com.chen.download.volley;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class UIHelperUtil {

	public static Context cxt;
	public static String URL;
	private static Properties pro;

	protected static final int command_start = 1;
	protected static final int command_failure = 2;
	protected static final int command_success = 3;
	protected static final int command_finish = 4;
	private IResponseListener listener;
	private Object response;
	private Handler handler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				UIHelperUtil.this.listener.onReqStart();
				break;
			case 3:
				UIHelperUtil.this.listener.onSuccess(UIHelperUtil.this
						.getResponse());
				break;
			case 2:
				UIHelperUtil.this.listener.onFailure(UIHelperUtil.this
						.getResponse());
				break;
			case 4:
				UIHelperUtil.this.listener.onFinish();
				break;
			}
		}
	};

	public UIHelperUtil() {
		if (URL == null) {
			URL = getUrl(cxt);
		}
	}

	public static String getUrl(Context cxt) {
		initPropertis(cxt);
		return pro.getProperty("com.asktun.json.api.url");
	}

	private static void initPropertis(Context cxt) {
		InputStream fis = null;
		if (pro == null) {
			try {
				fis = cxt.getResources().getAssets().open("pro.properties");
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			pro = new Properties();
			try {
				pro.load(fis);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static UIHelperUtil getUIHelperUtil(IResponseListener listener) {
		UIHelperUtil uhu = new UIHelperUtil();
		uhu.listener = listener;
		return uhu;
	}

	protected void sendMessage(int state) {
		if (this.listener != null) {
			this.handler.sendEmptyMessage(state);
		}
	}

	public void sendStartMessage() {
		sendMessage(1);
	}

	public void sendSuccessMessage(Object object) {
		setResponse(object);
		sendMessage(3);
	}

	public void sendFailureMessage(Object object) {
		setResponse(object);
		sendMessage(2);
	}

	public void sendFinishMessage() {
		sendMessage(4);
	}

	public Object getResponse() {
		return this.response;
	}

	public void setResponse(Object response) {
		this.response = response;
	}
}