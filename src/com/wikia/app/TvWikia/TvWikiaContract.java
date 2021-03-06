package com.wikia.app.TvWikia;

import android.provider.BaseColumns;

//Defines the overall schemas for the databases
public final class TvWikiaContract {
	public TvWikiaContract(){};
	
	public static abstract class Shows implements BaseColumns{
		public static final String TABLE_NAME = "shows";
		public static final String COLUMN_NAME_TITLE = "title";
		public static final String COLUMN_NAME_WIKIA_URL = "wikia_url";
		public static final String COLUMN_NAME_BANNER_URL = "banner_url";
		public static final String COLUMN_NAME_TVDB_ID = "tvdb_id";
		public static final String COLUMN_NAME_USER_SEASON = "user_season";
		public static final String COLUMN_NAME_USER_EPISODE = "user_episode";
		public static final String COLUMN_NAME_USER_DATE = "user_date";
		public static final String COLUMN_NAME_HIDDEN = "hidden";
		
	}

	public static abstract class Episodes implements BaseColumns{
		public static final String TABLE_NAME = "episodes";
		public static final String COLUMN_NAME_SHOW_ID = "show_id";
		public static final String COLUMN_NAME_TITLE = "title";
		public static final String COLUMN_NAME_SEASON = "season";
		public static final String COLUMN_NAME_EPISODE = "episode";
		public static final String COLUMN_NAME_AIRDATE = "airdate";
		
	}
}
