/*
 * Copyright (C) 2010 mAPPn.Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chen.download;

import java.io.File;
import java.util.HashMap;
import java.util.Observable;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.chen.download.lib.DownloadManager;
import com.chen.download.lib.Downloads;

/**
 * 
 * The Client Seesion Object for GfanMobile, contains some necessary
 * information.
 * 
 * @author andrew
 * @date 2010-12-22
 * @since Version 0.5.1
 * 
 */
public class Session extends Observable {

	/** Application Context */
	private Context mContext;

	/** The application debug mode */
	public boolean isDebug;

	/** Download Manager */
	private DownloadManager mDownloadManager;

	/** The singleton instance */
	private static Session mInstance;

	/**
	 * default constructor
	 * 
	 * @param context
	 */
	private Session(Context context) {

		synchronized (this) {
			mContext = context;
			mDownloadManager = new DownloadManager(
					context.getContentResolver(), context.getPackageName());
			mHandler.sendEmptyMessage(CURSOR_CREATED);
		}
	}

	public static Session get(Context context) {
		if (mInstance == null) {
			mInstance = new Session(context);
		}
		return mInstance;
	}

	public DownloadManager getDownloadManager() {
		if (mDownloadManager == null) {
			mDownloadManager = new DownloadManager(
					mContext.getContentResolver(), mContext.getPackageName());
		}
		return mDownloadManager;
	}

	public void close() {
		mDownloadingCursor.unregisterContentObserver(mCursorObserver);
		mDownloadingCursor.close();
		mInstance = null;
	}

	/** 创建下载数据结果集 */
	private static final int CURSOR_CREATED = 0;
	/** 更新下载数据结果集 */
	private static final int CURSOR_CHANGED = 1;
	/** 下载列表更新 */
	private static final int UPDATE_LIST = 3;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case CURSOR_CREATED:
				mDownloadingList = new HashMap<String, DownloadInfo>();
				startQuery();
				break;
			case CURSOR_CHANGED:
				if (mDownloadingCursor == null) {
					return;
				}
				mDownloadingCursor.requery();
				synchronized (this) {
					refreshDownloadApp(mDownloadingCursor);
				}
				break;
			case UPDATE_LIST:
				setChanged();
				notifyObservers(mDownloadingList);
				break;

			default:
				break;
			}
		}
	};

	private HashMap<String, DownloadInfo> mDownloadingList;

	private Cursor mDownloadingCursor;

	public HashMap<String, DownloadInfo> getDownloadingList() {
		return mDownloadingList;
	}

	private ContentObserver mCursorObserver = new ContentObserver(mHandler) {
		@Override
		public void onChange(boolean selfChange) {
			
			Log.d("DownloadManager", "mCursorObserver CURSOR_CHANGED");
			mHandler.sendEmptyMessage(CURSOR_CHANGED);
		}
	};

	/**
	 * 提醒下载列表更新
	 */
	public void updateDownloading() {
		mHandler.sendEmptyMessage(UPDATE_LIST);
	}

	private void startQuery() {

		DbStatusRefreshTask refreshTask = new DbStatusRefreshTask(
				mContext.getContentResolver());
		refreshTask.startQuery(DbStatusRefreshTask.DOWNLOAD, null,
				Downloads.CONTENT_URI, null, null, null, null);
	}

	/**
	 * 本地数据库刷新检查
	 * 
	 */
	private class DbStatusRefreshTask extends AsyncQueryHandler {

		private final static int DOWNLOAD = 0;

		public DbStatusRefreshTask(ContentResolver cr) {
			super(cr);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {

			switch (token) {
			case DOWNLOAD:
				refreshDownloadApp(cursor);
				break;
			default:
				break;
			}
		}
	}

	/*
	 * 刷新正在下载中的应用
	 */
	void refreshDownloadApp(Cursor cursor) {

		Log.d("DownloadManager", "refreshDownloadApp");
		
		// 绑定观察者
		if (mDownloadingCursor == null) {
			mDownloadingCursor = cursor;
			cursor.registerContentObserver(mCursorObserver);
		}

		if (cursor.getCount() > 0) {
			// 检索有结果
			mDownloadingList = new HashMap<String, DownloadInfo>();
		} else {
			mDownloadingList = new HashMap<String, DownloadInfo>();
			Log.d("DownloadManager", "cusor 为空了");
			setChanged();
			notifyObservers(mDownloadingList);
			return;
		}

		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

			DownloadInfo infoItem = new DownloadInfo();
			infoItem.id = cursor.getInt(cursor
					.getColumnIndexOrThrow(DownloadManager.COLUMN_ID));
			infoItem.mStatus = translateStatus(cursor.getInt(cursor
					.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)));

			Log.d("CHEN", "id =" + infoItem.id + "<============>     status ="
					+ infoItem.mStatus);

			infoItem.url = cursor.getString(cursor
					.getColumnIndex(DownloadManager.COLUMN_URI));

			if (DownloadManager.STATUS_RUNNING == infoItem.mStatus
					|| DownloadManager.STATUS_PAUSED == infoItem.mStatus) {
				// downloading progress
				long currentBytes = cursor.getInt(cursor
						.getColumnIndex(Downloads.COLUMN_CURRENT_BYTES));
				long totalBytes = cursor.getInt(cursor
						.getColumnIndex(Downloads.COLUMN_TOTAL_BYTES));
				infoItem.mTotalSize = totalBytes;
				infoItem.mCurrentSize = currentBytes;
				int progress = (int) ((float) currentBytes / (float) totalBytes * 100);
				infoItem.mProgress = progress + "%";
				infoItem.mProgressNumber = progress;
			} else if (infoItem.mStatus == DownloadManager.STATUS_SUCCESSFUL) {
				// download success
				infoItem.mFilePath = cursor.getString(cursor
						.getColumnIndex(Downloads._DATA));
				System.out.println("PATH===========" + infoItem.mFilePath);
				// 检查文件完整性，如果不存在，删除此条记录
				if (!new File(infoItem.mFilePath).exists()) {
					mDownloadingList.remove(infoItem.url);
				}
			}
			mDownloadingList.put(infoItem.url, infoItem);
		}
		setChanged();
		notifyObservers(mDownloadingList);
	}

	private int translateStatus(int status) {
		switch (status) {
		case Downloads.STATUS_PENDING:
			return DownloadManager.STATUS_PENDING;

		case Downloads.STATUS_RUNNING:
			return DownloadManager.STATUS_RUNNING;

		case Downloads.STATUS_PAUSED_BY_APP:
		case Downloads.STATUS_WAITING_TO_RETRY:
		case Downloads.STATUS_WAITING_FOR_NETWORK:
		case Downloads.STATUS_QUEUED_FOR_WIFI:
			return DownloadManager.STATUS_PAUSED;

		case Downloads.STATUS_SUCCESS:
			return DownloadManager.STATUS_SUCCESSFUL;

		default:
			assert Downloads.isStatusError(status);
			return DownloadManager.STATUS_FAILED;
		}
	}

}