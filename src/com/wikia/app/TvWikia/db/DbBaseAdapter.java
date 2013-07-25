package com.wikia.app.TvWikia.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.wikia.app.TvWikia.TvWikiaContract.Episodes;
import com.wikia.app.TvWikia.TvWikiaContract.Shows;

public class DbBaseAdapter {
	private static final String TAG = "DbBaseAdapter";

	protected static final String DATABASE_NAME = "db.sqlite";
	protected static final int DATABASE_VERSION = 2;
	
	protected Context mContext;
	protected static DatabaseHelper mDbHelper;
	
	private static final String TABLE_CREATE_SHOWS = 
			"CREATE TABLE " + Shows.TABLE_NAME + " (" +
				Shows._ID 						+ " INTEGER PRIMARY KEY AUTOINCREMENT," +
				Shows.COLUMN_NAME_TITLE 		+ " TEXT," +
				Shows.COLUMN_NAME_WIKIA_URL 	+ " TEXT," +
				Shows.COLUMN_NAME_BANNER_URL 	+ " TEXT," +
				Shows.COLUMN_NAME_TVDB_ID 		+ " INTEGER," +
				Shows.COLUMN_NAME_USER_SEASON 	+ " INTEGER," +
				Shows.COLUMN_NAME_USER_EPISODE 	+ " INTEGER," +
				Shows.COLUMN_NAME_USER_DATE		+ " DATE," +
				Shows.COLUMN_NAME_HIDDEN 		+ " BOOLEAN" +
			")";

	private static final String TABLE_CREATE_EPISODES = 
			"CREATE TABLE " + Episodes.TABLE_NAME + " (" +
				Episodes._ID 						+ " INTEGER PRIMARY KEY AUTOINCREMENT," +
				Episodes.COLUMN_NAME_SHOW_ID 		+ " INTEGER," +
				Episodes.COLUMN_NAME_TITLE 			+ " TEXT," +
				Episodes.COLUMN_NAME_SEASON 		+ " INTEGER," +
				Episodes.COLUMN_NAME_EPISODE 		+ " INTEGER," +
				Episodes.COLUMN_NAME_AIRDATE 		+ " TEXT" +
			")";
	
	private static final String TABLE_DELETE_SHOWS =
		    "DROP TABLE IF EXISTS " + Shows.TABLE_NAME;
	
	private static final String TABLE_DELETE_EPISODES =
		    "DROP TABLE IF EXISTS " + Episodes.TABLE_NAME;
	
	public DbBaseAdapter(Context context){
		mContext = context.getApplicationContext();
	}
	
	public SQLiteDatabase openDb(){
		if(mDbHelper == null){
			mDbHelper = new DatabaseHelper(mContext);
		}
		return mDbHelper.getWritableDatabase();
	}
	
	public void closeDb(){
		mDbHelper.close();
	}
	
	//Convenience methods for extracting value from the record the cursor is pointing to
	protected int getInteger(Cursor c, String columnName){
		return c.getInt(c.getColumnIndexOrThrow(columnName));
	}
	protected String getText(Cursor c, String columnName){
		return c.getString(c.getColumnIndexOrThrow(columnName));
	}
	
	// Define static DatabaseHelper so that we will always have one connection to the DB
	// This prevents concurrent write failures.
	protected static class DatabaseHelper extends SQLiteOpenHelper{
		public DatabaseHelper(Context context){
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(TABLE_CREATE_SHOWS);
			db.execSQL(TABLE_CREATE_EPISODES);
			
			ContentValues values = new ContentValues();
			values.put(Shows.COLUMN_NAME_TITLE, "Doctor Who");
			values.put(Shows.COLUMN_NAME_WIKIA_URL, "http://tardis.wikia.com");
			values.put(Shows.COLUMN_NAME_BANNER_URL, "http://images2.wikia.nocookie.net/__cb159/tardis/images/8/89/Wiki-wordmark.png");
			values.put(Shows.COLUMN_NAME_TVDB_ID, "78804");
			values.put(Shows.COLUMN_NAME_USER_SEASON, "-1");
			values.put(Shows.COLUMN_NAME_USER_EPISODE, "-1");
			values.put(Shows.COLUMN_NAME_HIDDEN, "0");
					
			db.insert(Shows.TABLE_NAME, Shows.COLUMN_NAME_TITLE, values);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " +
	                newVersion + ", which will destroy all old data");
	            db.execSQL(TABLE_DELETE_SHOWS);
	            db.execSQL(TABLE_DELETE_EPISODES);
	            onCreate(db);
		}
	}
}
