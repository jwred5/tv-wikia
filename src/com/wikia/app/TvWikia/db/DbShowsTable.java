package com.wikia.app.TvWikia.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.wikia.app.TvWikia.TvWikiaContract.Shows;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.format.DateFormat;
import android.util.Log;

public class DbShowsTable extends DbBaseAdapter {
	private static final String TAG = "DbShowsTable";
	
	// Define a projection that specifies which columns from the database
	// you will actually use after this query.
	private final String[] fullProjection = {
		Shows._ID,
	    Shows.COLUMN_NAME_TITLE,
	    Shows.COLUMN_NAME_WIKIA_URL,
	    Shows.COLUMN_NAME_BANNER_URL,
	    Shows.COLUMN_NAME_TVDB_ID,
	    Shows.COLUMN_NAME_USER_SEASON,
	    Shows.COLUMN_NAME_USER_EPISODE,
	    Shows.COLUMN_NAME_USER_DATE,
	    Shows.COLUMN_NAME_HIDDEN
	};
	
	public DbShowsTable(Context context){
		super(context);
	}
	
	//Figure out which show is loaded by its url
	public Show findShowByUrl(String url){
		//Trim the url to its base domain name
		String[] split = url.split("/http:\\/\\/[^\\/]*\\//");
		String urlBase = split[0] + "%";
		SQLiteDatabase db = openDb();

		final String selection = Shows.COLUMN_NAME_WIKIA_URL + " LIKE ?";
		final String[] selectionArgs = {urlBase};
		Cursor c = db.query(    
				Shows.TABLE_NAME,	// The table to query
				fullProjection,		// The columns to return
			    selection,			// The columns for the WHERE clause
			    selectionArgs,		// The values for the WHERE clause
			    null,				// don't group the rows
			    null,				// don't filter by row groups
			    null				// The sort order
				);
		Show show = null;
		if(c.moveToFirst())
			show = (Show) parseRecord(c);
		closeDb();
		return show;
	}

	@Override
	protected Record parseRecord(Cursor c) {
		return new Show(
				getInteger(c, Shows._ID),
				getText(c, Shows.COLUMN_NAME_TITLE),
				getText(c, Shows.COLUMN_NAME_WIKIA_URL),
				getText(c, Shows.COLUMN_NAME_BANNER_URL),
				getInteger(c, Shows.COLUMN_NAME_TVDB_ID),
				getInteger(c, Shows.COLUMN_NAME_USER_SEASON),
				getInteger(c, Shows.COLUMN_NAME_USER_EPISODE),
				getText(c, Shows.COLUMN_NAME_USER_DATE),
				(getInteger(c, Shows.COLUMN_NAME_HIDDEN) > 0)
			);
	}

	@Override
	protected String myIdColumn() {
		return Shows._ID;
	}

	@Override
	protected String myTableName() {
		return Shows.TABLE_NAME;
	}

	@Override
	protected String[] myFullProjection() {
		return fullProjection;
	}
	
	//Class representing a Show in this App
	public class Show extends Record{
		public final String title;
		public final String wikiaUrl;
		public final String bannerUrl;
		public final int tvdbId;
		public final int userSeason;
		public final int userEpisode;
		public final Date userDate;
		public final boolean hidden;
		
		@SuppressLint("SimpleDateFormat")
		public Show(int id, String title, String wikiaUrl, String bannerUrl, int tvdbId,
				int userSeason, int userEpisode, String userDate, boolean hidden) {
			super(id);
			this.title = title;
			this.wikiaUrl = wikiaUrl;
			this.bannerUrl = bannerUrl;
			this.tvdbId = tvdbId;
			this.userSeason = userSeason;
			this.userEpisode = userEpisode;
			Date uDate = null;
			if(userDate != null && !userDate.equals("")){
				try {
					uDate = new SimpleDateFormat("yyyy-MM-dd").parse(userDate);
				} catch (ParseException e) {
					Log.w(TAG, "Could not parse date from database: " + userDate);
				}
			}
			this.userDate = uDate;
			this.hidden = hidden;
		}
		
		//Compare this Show to another and get the ContentValues of what would need to be updated 
		public ContentValues diff(Record o){
			Show other = (Show) o;
			ContentValues differences = new ContentValues();
			if(!other.title.equals(this.title)){
				differences.put(Shows.COLUMN_NAME_TITLE, other.title);
			}
			if(!other.wikiaUrl.equals(this.wikiaUrl)){
				differences.put(Shows.COLUMN_NAME_BANNER_URL, other.bannerUrl);
			}
			if(!other.bannerUrl.equals(this.bannerUrl)){
				differences.put(Shows.COLUMN_NAME_BANNER_URL, other.bannerUrl);
			}
			if(other.tvdbId != this.tvdbId){
				differences.put(Shows.COLUMN_NAME_TVDB_ID, String.valueOf( other.tvdbId ));
			}
			if(other.userSeason != this.userSeason){
				differences.put(Shows.COLUMN_NAME_USER_SEASON, String.valueOf( other.userSeason ));
			}
			if(other.userEpisode != this.userEpisode){
				differences.put(Shows.COLUMN_NAME_USER_EPISODE, String.valueOf( other.userEpisode ));
			}
			if(!other.userDate.equals(this.userDate)){
				differences.put(Shows.COLUMN_NAME_USER_DATE, DateFormat.format("yyyy-MM-dd", other.userDate).toString());
			}
			if(other.hidden != this.hidden){
				differences.put(Shows.COLUMN_NAME_HIDDEN, String.valueOf( other.hidden ));
			}
			return differences;
		}
		//Compare this Show to another and get the ContentValues of what would need to be updated 
		public ContentValues getContentValues(){
			ContentValues values = new ContentValues();
			values.put(Shows.COLUMN_NAME_TITLE, this.title);
			values.put(Shows.COLUMN_NAME_BANNER_URL, this.bannerUrl);
			values.put(Shows.COLUMN_NAME_BANNER_URL, this.bannerUrl);
			values.put(Shows.COLUMN_NAME_TVDB_ID, String.valueOf( this.tvdbId ));
			values.put(Shows.COLUMN_NAME_USER_SEASON, String.valueOf( this.userSeason ));
			values.put(Shows.COLUMN_NAME_USER_EPISODE, String.valueOf( this.userEpisode ));
			values.put(Shows.COLUMN_NAME_USER_DATE, DateFormat.format("yyyy-MM-dd", this.userDate).toString());
			values.put(Shows.COLUMN_NAME_HIDDEN, String.valueOf( this.hidden ));
			return values;
		}
	}

}
