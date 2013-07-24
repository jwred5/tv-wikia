package com.wikia.app.TvWikia.db;

import java.util.ArrayList;

import com.wikia.app.TvWikia.TvWikiaContract.Shows;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DbShowsTable extends DbBaseAdapter {
	
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
	    Shows.COLUMN_NAME_HIDDEN
	};
	
	public DbShowsTable(Context context){
		super(context);
	}
	
	//Get a Show record by its database id
	public Show getShow(int id){
		SQLiteDatabase db = openDb();
		final String selection = Shows._ID + " = ?";
		final String[] selectionArgs = {String.valueOf(id)};
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
			show = parseShow(c);
		closeDb();
		return show;
	}
	
	//Create an ArrayList of all the Shows in the database
	public ArrayList<Show> listShows(){
		SQLiteDatabase db = openDb();
		final String sortOrder =
			    Shows._ID + " ASC";
		Cursor c = db.query(    
				Shows.TABLE_NAME,	// The table to query
				fullProjection,		// The columns to return
			    null,				// The columns for the WHERE clause
			    null,				// The values for the WHERE clause
			    null,				// don't group the rows
			    null,				// don't filter by row groups
			    sortOrder			// The sort order
				);
		ArrayList<Show> shows = new ArrayList<Show>();
		while(c.moveToNext()){
			shows.add(parseShow(c));
		}
		closeDb();
		return shows;
	}
	
	// Updates the show given.
	// Uses the id of the show to look up the record, then updates any values that are different
	// than the database
	public boolean updateShow(Show show){
		boolean success;
		SQLiteDatabase db = openDb();
		//Get the old record out of the database
		Show oldShow = getShow(show.id);
		//If the old show didn't exists, fail
		if(oldShow != null){
			ContentValues values = oldShow.diff(show);
			if(values.size() > 0){
				final String selection = Shows._ID + " = ?";
				final String[] selectionArgs = {String.valueOf(show.id)};
				int count = db.update(
					    Shows.TABLE_NAME,
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
			show = parseShow(c);
		closeDb();
		return show;
	}
	
	
	/*
	 *  PRIVATE METHODS
	 */
	
	//Convenience method for turning the record the cursor is pointing to into a Show Object
	private Show parseShow(Cursor c){
		return new Show(
			getInteger(c, Shows._ID),
			getText(c, Shows.COLUMN_NAME_TITLE),
			getText(c, Shows.COLUMN_NAME_WIKIA_URL),
			getText(c, Shows.COLUMN_NAME_BANNER_URL),
			getInteger(c, Shows.COLUMN_NAME_TVDB_ID),
			getInteger(c, Shows.COLUMN_NAME_USER_SEASON),
			getInteger(c, Shows.COLUMN_NAME_USER_EPISODE),
			(getInteger(c, Shows.COLUMN_NAME_HIDDEN) > 0)
		);
	}
	
	//Convenience methods for extracting value from the record the cursor is pointing to
	private int getInteger(Cursor c, String columnName){
		return c.getInt(c.getColumnIndexOrThrow(columnName));
	}
	private String getText(Cursor c, String columnName){
		return c.getString(c.getColumnIndexOrThrow(columnName));
	}
	
	//Class representing a Show in this App
	public static class Show{
		public final int id;
		public final String title;
		public final String wikiaUrl;
		public final String bannerUrl;
		public final int tvdbId;
		public final int userSeason;
		public final int userEpisode;
		public final boolean hidden;
		
		public Show(int id, String title, String wikiaUrl, String bannerUrl, int tvdbId,
				int userSeason, int userEpisode, boolean hidden) {
			super();
			this.id = id;
			this.title = title;
			this.wikiaUrl = wikiaUrl;
			this.bannerUrl = bannerUrl;
			this.tvdbId = tvdbId;
			this.userSeason = userSeason;
			this.userEpisode = userEpisode;
			this.hidden = hidden;
		}
		
		//Compare this Show to another and get the ContentValues of what would need to be updated 
		public ContentValues diff(Show other){
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
			if(other.hidden != this.hidden){
				differences.put(Shows.COLUMN_NAME_HIDDEN, String.valueOf( other.hidden ));
			}
			return differences;
		}
	}
}
