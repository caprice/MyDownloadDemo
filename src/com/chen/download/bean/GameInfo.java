package com.chen.download.bean;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;

import com.chen.download.lib.DownloadManager;
import com.chen.download.volley.GsonObj;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;

public class GameInfo implements GsonObj, Serializable {

	private static final long serialVersionUID = -2034838654151599403L;
	@Expose
	public int flg;
	@Expose
	public List<GameInfoitem> data;
	
	
	@Override
	public String getInterface() {
		return "game/categoryList?";
	}

	@Override
	public Type getTypeToken() {
		return new TypeToken<GameInfo>() {
		}.getType();
	}
	
	public class GameInfoitem implements  Serializable {
		@Expose
		public String game_id;
		@Expose
		public String game_ico;
		@Expose
		public String game_name;
		@Expose
		public String url;
		
		public long id;
		public int download_state = DownloadManager.STATUS_NORMAL;
		public int download_progress;
	}
}