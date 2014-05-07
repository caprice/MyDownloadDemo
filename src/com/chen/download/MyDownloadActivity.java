package com.chen.download;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.chen.download.bean.GameInfo;
import com.chen.download.bean.GameInfo.GameInfoitem;
import com.chen.download.lib.DownloadManager;
import com.chen.download.lib.DownloadManager.Request;
import com.chen.download.volley.IResponseListener;

public class MyDownloadActivity extends Activity {

	private ListView listView;

	private final static String PATH = Environment
			.getExternalStorageDirectory().getPath() + "/MyDownloadTest/";

	private LvAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_download);

		listView = (ListView) findViewById(R.id.listview);
		adapter = new LvAdapter();
		listView.setAdapter(adapter);

		File file = new File(PATH);
		if (!file.exists()) {
			file.mkdirs();
		}

		getData();

	}

	private void getData() {
		Map<String, Object> params = new LinkedHashMap<String, Object>();
		params.put("type", "2");
		params.put("cate_id", "7");
		// params.put("page", page + "");
		UIHelper.reqData(Method.GET, GameInfo.class, params, null,
				new IResponseListener() {

					@Override
					public void onSuccess(Object o) {
						// TODO Auto-generated method stub
						GameInfo data = (GameInfo) o;
						if (data.flg == 1) {
							List<GameInfoitem> item = data.data;
							if (item != null && item.size() > 0) {
								adapter.addAll(item);
							}
						}
					}

					@Override
					public void onReqStart() {
						// TODO Auto-generated method stub
					}

					@Override
					public void onFinish() {
						// TODO Auto-generated method stub
					}

					@Override
					public void onFailure(Object o) {
						// TODO Auto-generated method stub
					}
				});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Session.get(this).close();
	}

	static class ListInfo {

		public ListInfo(String url) {
			// TODO Auto-generated constructor stub
			this.url = url;
		}

		public String url;
		public long id;
		public int download_state = DownloadManager.STATUS_NORMAL;
		public int download_progress;
	}

	private class LvAdapter extends BaseAdapter implements Observer {

		private DownloadManager mDownloadManager;
		private HashMap<String, DownloadInfo> mDownloadingTask;

		public LvAdapter() {
			// TODO Auto-generated constructor stub
			Session session = Session.get(MyDownloadActivity.this);
			session.addObserver(this);
			mDownloadManager = session.getDownloadManager();
			mDownloadingTask = session.getDownloadingList();
		}

		private List<GameInfoitem> list = new ArrayList<GameInfoitem>();

		public void addAll(List<GameInfoitem> o) {
			list.addAll(o);
			notifyDataSetChanged();
		}

		public void clear() {
			list.clear();
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			// TODO Auto-generated method stub
			ViewHolder holder;
			if (arg1 == null) {
				holder = new ViewHolder();
				arg1 = LayoutInflater.from(MyDownloadActivity.this).inflate(
						R.layout.item_download, null);
				holder.tv_percent = (TextView) arg1
						.findViewById(R.id.tv_percent);
				holder.iv = (ImageView) arg1.findViewById(R.id.iv);
				holder.pb = (ProgressBar) arg1.findViewById(R.id.progress);
				holder.btn = (Button) arg1.findViewById(R.id.btn);
				holder.btn_canel = (Button) arg1.findViewById(R.id.btn_canel);
				arg1.setTag(holder);
			} else {
				holder = (ViewHolder) arg1.getTag();
			}
			final GameInfoitem item = list.get(arg0);
			String url = item.url;
			ImageLoaderUtil.displayImage(item.game_ico, holder.iv);
			if (mDownloadingTask != null && mDownloadingTask.containsKey(url)) {
				DownloadInfo info = mDownloadingTask.get(url);
				// 下载过程中，刷新进度
				item.download_progress = info.mProgressNumber;
				item.download_state = info.mStatus;
				item.id = info.id;
				System.out.println("下载   " + arg0 + "======="
						+ item.download_state);
			} else {
				item.download_state = DownloadManager.STATUS_NORMAL;
				item.download_progress = 0;
				item.id = 0;
				System.out.println("空闲   " + arg0 + "======="
						+ item.download_state);
			}
			if (item.download_state == DownloadManager.STATUS_PAUSED) {
				holder.pb.setIndeterminate(true);
			} else {
				holder.pb.setIndeterminate(false);
			}

			holder.btn.setTag(arg0);
			switch (item.download_state) {
			case DownloadManager.STATUS_NORMAL:
				holder.btn.setText("下载");
				holder.btn_canel.setVisibility(View.GONE);
				holder.pb.setProgress(0);
				holder.tv_percent.setText("");
				break;
			case DownloadManager.STATUS_FAILED:
				holder.btn.setText("下载");
				holder.btn_canel.setVisibility(View.GONE);
				holder.pb.setProgress(0);
				holder.tv_percent.setText("下载失败");
				break;
			case DownloadManager.STATUS_PAUSED:
				holder.btn_canel.setVisibility(View.VISIBLE);
				holder.btn.setText("继续");
				holder.tv_percent
						.setText("下载暂停" + item.download_progress + "%");
				break;
			case DownloadManager.STATUS_PENDING:
				holder.btn.setText("暂停");
				holder.btn_canel.setVisibility(View.VISIBLE);
				holder.tv_percent.setText("等待开始下载...");
				holder.pb.setProgress(item.download_progress);
				break;
			case DownloadManager.STATUS_RUNNING:
				holder.btn.setText("暂停");
				holder.btn_canel.setVisibility(View.VISIBLE);
				holder.tv_percent
						.setText("正在下载" + item.download_progress + "%");
				holder.pb.setProgress(item.download_progress);
				break;
			case DownloadManager.STATUS_SUCCESSFUL:
				holder.btn.setText("已下载");
				holder.tv_percent.setText("下载完成");
				holder.pb.setProgress(item.download_progress);
				holder.btn_canel.setVisibility(View.GONE);
				break;
			default:
				break;
			}
			holder.btn.setOnClickListener(mOperationListener);
			holder.btn_canel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mDownloadManager.markRowDeleted(item.id);
				}
			});
			return arg1;
		}

		private OnClickListener mOperationListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int position = (Integer) v.getTag();
				GameInfoitem item = list.get(position);
				int status = item.download_state;
				switch (status) {
				case DownloadManager.STATUS_NORMAL:
					startDownload(item.url);
					break;
				case DownloadManager.STATUS_FAILED:
					mDownloadManager.restartDownload(item.id);
					break;
				case DownloadManager.STATUS_PAUSED:
					mDownloadManager.resumeDownload(item.id);
					break;
				case DownloadManager.STATUS_PENDING:
				case DownloadManager.STATUS_RUNNING:
					mDownloadManager.pauseDownload(item.id);
					break;
				case DownloadManager.STATUS_SUCCESSFUL:
					break;
				default:
					break;
				}
				notifyDataSetChanged();
			}
		};

		private class ViewHolder {
			TextView tv_percent;
			ProgressBar pb;
			Button btn;
			Button btn_canel;
			ImageView iv;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void update(Observable observable, Object data) {
			if (data instanceof HashMap) {
				mDownloadingTask = (HashMap<String, DownloadInfo>) data;
				notifyDataSetChanged();
			} else {
				notifyDataSetChanged();
			}
		}

		private void startDownload(String url) {
			Uri srcUri = Uri.parse(url);
			String name = url.substring(url.lastIndexOf("/"));
			DownloadManager.Request request = new Request(srcUri);
			request.setShowRunningNotification(true);
			request.setDestinationInExternalPublicDir("MyDownloadTest", name);
			// request.setDescription("Just for test");
			mDownloadManager.enqueue(request);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.my_download, menu);
		return true;
	}

}
