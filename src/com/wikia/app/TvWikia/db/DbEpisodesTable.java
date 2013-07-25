package com.wikia.app.TvWikia.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
	
	//Get a Episode record by its database id
	public Episode getEpisode(int id){
		SQLiteDatabase db = openDb();
		final String selection = Episodes._ID + " = ?";
		final String[] selectionArgs = {String.valueOf(id)};
		Cursor c = db.query(    
				Episodes.TABLE_NAME,	// The table to query
				fullProjection,		// The columns to return
			    selection,			// The columns for the WHERE clause
			    selectionArgs,		// The values for the WHERE clause
			    null,				// don't group the rows
			    null,				// don't filter by row groups
			    null				// The sort order
				);

		Episode episode = null;
		if(c.moveToFirst())
			episode = parseEpisode(c);
		closeDb();
		return episode;
	}
	
	//Create an ArrayList of all the Episodes in the database
	public ArrayList<Episode> listEpisodes(){
		SQLiteDatabase db = openDb();
		final String sortOrder =
				Episodes._ID + " ASC";
		Cursor c = db.query(    
				Episodes.TABLE_NAME,	// The table to query
				fullProjection,		// The columns to return
			    null,				// The columns for the WHERE clause
			    null,				// The values for the WHERE clause
			    null,				// don't group the rows
			    null,				// don't filter by row groups
			    sortOrder			// The sort order
				);
		ArrayList<Episode> episodes = new ArrayList<Episode>();
		while(c.moveToNext()){
			episodes.add(parseEpisode(c));
		}
		closeDb();
		return episodes;
	}
	
	// Updates the Episode given.
	// Uses the id of the show to look up the record, then updates any values that are different
	// than the database
	public boolean updateEpisode(Episode episode){
		boolean success;
		SQLiteDatabase db = openDb();
		//Get the old record out of the database
		Episode oldEpisode = getEpisode(episode.id);
		//If the old Episode didn't exists, fail
		if(oldEpisode != null){
			ContentValues values = oldEpisode.diff(episode);
			if(values.size() > 0){
				final String selection = Episodes._ID + " = ?";
				final String[] selectionArgs = {String.valueOf(episode.id)};
				int count = db.update(
						Episodes.TABLE_NAME,
					    values,
					    selection,
					    selectionArgs);
				success = (count == 1);
			}
			else
				success = true;
		}
		else{
			success = false;
		}
		closeDb();
		return success;
	}

	// Updates the Episode given.
	// Uses the id of the show to look up the record, then updates any values that are different
	// than the database
	public boolean insertEpisode(Episode episode){
		boolean success;
		SQLiteDatabase db = openDb();
		ContentValues values = new Episode(-1, -1, null, -1, -1, null).diff(episode);
		if(values.size() > 0){
			long count = db.insert(
					Episodes.TABLE_NAME,
				    Episodes.COLUMN_NAME_TITLE,
				    values);
			success = (count > 0);
		}
		else
			success = false;
		closeDb();
		return success;
	}
	/*
	 *  PRIVATE METHODS
	 */
	
	//Convenience method for turning the record the cursor is pointing to into a Episode Object
	private Episode parseEpisode(Cursor c){
		return new Episode(
			getInteger(c, Episodes._ID),
			getInteger(c, Episodes.COLUMN_NAME_SHOW_ID),
			getText(c, Episodes.COLUMN_NAME_TITLE),
			getInteger(c, Episodes.COLUMN_NAME_SEASON),
			getInteger(c, Episodes.COLUMN_NAME_EPISODE),
			getText(c, Episodes.COLUMN_NAME_AIRDATE)
		);
	}
	
	//Class representing a Episode in this App
	public static class Episode{
		public final int id;
		public final int showId;
		public final String title;
		public final int season;
		public final int episode;
		public final Date airdate;
		
		@SuppressLint("SimpleDateFormat")
		public Episode(int id, int showId, String title, int season,
				int episode, String airdate) {
			super();
			this.id = id;
			this.showId = showId;
			this.title = title;
			this.season = season;
			this.episode = episode;
			Date aDate = null;
			if(airdate != null){
				try {
					aDate = new SimpleDateFormat("yyyy-MM-dd").parse(airdate);
				} catch (ParseException e) {
					Log.w(TAG, "Could not parse date from database: " + airdate);
				}
			}
			this.airdate = aDate;
		}
		
		//Compare this Episode to another and get the ContentValues of what would need to be updated 
		public ContentValues diff(Episode other){
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
	}
}
