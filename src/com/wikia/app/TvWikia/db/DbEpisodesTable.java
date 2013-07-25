package com.wikia.app.TvWikia.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.wikia.app.TvWikia.TvWikiaContract.Episodes;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.format.DateFormat;
import android.util.Log;

public class DbEpisodesTable extends DbBaseAdapter {
	private static final String TAG = "DbEpisodesTable";
	
	// Define a projection that specifies which columns from the database
	// you will actually use after this query.
	private final String[] fullProjection = {
	    Episodes._ID,
	    Episodes.COLUMN_NAME_SHOW_ID,
	    Episodes.COLUMN_NAME_TITLE,
	    Episodes.COLUMN_NAME_SEASON,
	    Episodes.COLUMN_NAME_EPISODE,
	    Episodes.COLUMN_NAME_AIRDATE
	};
	
	public DbEpisodesTable(Context context){
		super(context);
	}
	
	//Find the internal id of the episode by its show, season, and episode
	public int getEpisodeId(int showId, int season, int episode){

		SQLiteDatabase db = openDb();
		final String selection = Episodes.COLUMN_NAME_SHOW_ID + " = ? AND " + Episodes.COLUMN_NAME_SEASON + " = ? AND " + Episodes.COLUMN_NAME_EPISODE + " = ?";
		final String[] selectionArgs = {String.valueOf(showId), String.valueOf(season), String.valueOf(episode)};
		final String[] idProjection = {Episodes._ID};
		Cursor c = db.query(    
				Episodes.TABLE_NAME,	// The table to query
				idProjection,		// The columns to return
			    selection,			// The columns for the WHERE clause
			    selectionArgs,		// The values for the WHERE clause
			    null,				// don't group the rows
			    null,				// don't filter by row groups
			    null				// The sort order
				);

		int episodeId = -1;
		if(c.moveToFirst())
			episodeId = getInteger(c, Episodes._ID);
		closeDb();
		return episodeId;
	}

	@Override
	protected Record parseRecord(Cursor c) {
		return new Episode(
				getInteger(c, Episodes._ID),
				getInteger(c, Episodes.COLUMN_NAME_SHOW_ID),
				getText(c, Episodes.COLUMN_NAME_TITLE),
				getInteger(c, Episodes.COLUMN_NAME_SEASON),
				getInteger(c, Episodes.COLUMN_NAME_EPISODE),
				getText(c, Episodes.COLUMN_NAME_AIRDATE)
			);
	}
	@Override
	protected String myIdColumn() {
		return Episodes._ID;
	}

	@Override
	protected String myTableName() {
		return Episodes.TABLE_NAME;
	}

	@Override
	protected String[] myFullProjection() {
		return fullProjection;
	}
	/*
	 *  PRIVATE METHODS
	 */
	
	//Class representing a Episode in this App
	public class Episode extends Record{
		public final int showId;
		public final String title;
		public final int season;
		public final int episode;
		public final Date airdate;
		
		@SuppressLint("SimpleDateFormat")
		public Episode(int id, int showId, String title, int season,
				int episode, String airdate) {
			super(id);
			this.showId = showId;
			this.title = title;
			this.season = season;
			this.episode = episode;
			Date aDate = null;
			if(airdate != null && !airdate.equals("")){
				try {
					aDate = new SimpleDateFormat("yyyy-MM-dd").parse(airdate);
				} catch (ParseException e) {
					Log.w(TAG, "Could not parse date from database: " + airdate);
				}
			}
			this.airdate = aDate;
		}
		
		
		//Compare this Episode to another and get the ContentValues of what would need to be updated 
		protected ContentValues diff(Record o){
			Episode other = (Episode) o;
			ContentValues differences = new ContentValues();
			if(other.showId != this.showId){
				differences.put(Episodes.COLUMN_NAME_SHOW_ID, String.valueOf( other.showId ));
			}
			if(!other.title.equals(this.title)){
				differences.put(Episodes.COLUMN_NAME_TITLE, other.title);
			}
			if(other.season != this.season){
				differences.put(Episodes.COLUMN_NAME_SEASON, String.valueOf( other.season ));
			}
			if(other.episode != this.episode){
				differences.put(Episodes.COLUMN_NAME_EPISODE, String.valueOf( other.episode ));
			}
			if(!other.airdate.equals(this.airdate)){
				differences.put(Episodes.COLUMN_NAME_AIRDATE, DateFormat.format("yyyy-MM-dd", other.airdate).toString());
			}
			return differences;
		}


		@Override
		protected ContentValues getContentValues() {
			ContentValues differences = new ContentValues();
			differences.put(Episodes.COLUMN_NAME_SHOW_ID, String.valueOf( this.showId ));
			differences.put(Episodes.COLUMN_NAME_TITLE, this.title);
			differences.put(Episodes.COLUMN_NAME_SEASON, String.valueOf( this.season ));
			differences.put(Episodes.COLUMN_NAME_EPISODE, String.valueOf( this.episode ));
			differences.put(Episodes.COLUMN_NAME_AIRDATE,
					(this.airdate != null && !this.airdate.equals(""))?
						DateFormat.format("yyyy-MM-dd", this.airdate).toString() : null
			);
			return differences;
		}
	}
}
