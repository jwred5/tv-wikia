package com.wikia.app.TvWikia.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.SparseArray;

import com.wikia.app.TvWikia.TvWikiaContract.Episodes;
import com.wikia.app.TvWikia.TvWikiaContract.Shows;

public abstract class DbBaseAdapter {
	private static final String TAG = "DbBaseAdapter";

	protected static final String DATABASE_NAME = "db.sqlite";
	protected static final int DATABASE_VERSION = 1;
	
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
		synchronized(this){
			if(mDbHelper == null){
				mDbHelper = new DatabaseHelper(mContext);
			}
			return mDbHelper.getWritableDatabase();
		}
	}

	public void closeDb(){
		closeDb(false);
	}
	public void closeDb(boolean force){
		synchronized(this){
			
			if(mDbHelper != null){
				if(force){
					mDbHelper.close();
					mDbHelper = null;
				}
				else if(mDbHelper.closeCheck()){
					mDbHelper = null;
				}
			}
		}
	}

	protected abstract Record parseRecord(Cursor c);
	protected abstract String myIdColumn();
	protected abstract String myTableName();
	protected abstract String[] myFullProjection();
	
	//Get a Record by its database id
	public Record get(int id){
		SQLiteDatabase db = openDb();
		Record r = getRecord(db, id);
		closeDb();
		return r;
	}
	private Record getRecord(SQLiteDatabase db, int id){
		final String selection = myIdColumn() + " = ?";
		final String[] selectionArgs = {String.valueOf(id)};
		Cursor c = db.query(    
				myTableName(),		// The table to query
				myFullProjection(),	// The columns to return
			    selection,			// The columns for the WHERE clause
			    selectionArgs,		// The values for the WHERE clause
			    null,				// don't group the rows
			    null,				// don't filter by row groups
			    null				// The sort order
				);

		Record record = null;
		if(c.moveToFirst())
			record = parseRecord(c);
		return record;
	}
	
	//Get a HashMap of Records given a list of integers
	public SparseArray<Record> get(int[] ids){
		SparseArray<Record> records = new SparseArray<Record>();
		final String selection = myIdColumn() + "(" + makePlaceholders(ids.length) + ")";
		String[] selectionArgs = new String[ids.length];
		for(int i=0; i<ids.length;i++){
			selectionArgs[i] = String.valueOf(ids[i]);
		}
		SQLiteDatabase db = openDb();
		Cursor c = db.query(    
				myTableName(),		// The table to query
				myFullProjection(),	// The columns to return
				selection,				// The columns for the WHERE clause
				selectionArgs,				// The values for the WHERE clause
			    null,				// don't group the rows
			    null,				// don't filter by row groups
			    null			// The sort order
				);
		while(c.moveToNext()){
			Record r = parseRecord(c);
			records.put(r.id, r);
		}
		closeDb();
		return records;
	}
	
	//Create an ArrayList of all the Records in the database
	public ArrayList<Record> list(){
		SQLiteDatabase db = openDb();
		final String sortOrder = myIdColumn() + " ASC";
		Cursor c = db.query(    
				myTableName(),		// The table to query
				myFullProjection(),	// The columns to return
			    null,				// The columns for the WHERE clause
			    null,				// The values for the WHERE clause
			    null,				// don't group the rows
			    null,				// don't filter by row groups
			    sortOrder			// The sort order
				);
		ArrayList<Record> records = new ArrayList<Record>();
		while(c.moveToNext()){
			records.add(parseRecord(c));
		}
		closeDb();
		return records;
	}
	
	// Updates the Record given.
	// Uses the id of the show to look up the record, then updates any values that are different
	// than the database
	public boolean update(Record record){
		boolean success;
		SQLiteDatabase db = openDb();
		success = updateRecord(db, record);
		closeDb();
		return success;
	}	
	public int update(List<Record> records){
		int updated = 0;
		SQLiteDatabase db = openDb();
		for(Record record : records){
			if( updateRecord(db, record) ){
				updated++;
			}
		}
		closeDb();
		return updated;
	}
	private boolean updateRecord(SQLiteDatabase db, Record record){
		Log.i(TAG, "Updating Record");
		boolean success;
		//Get the old record out of the database
		Record oldRecord = get(record.id);
		//If the old Record didn't exists, fail
		if(oldRecord != null){
			ContentValues values = oldRecord.diff(record);
			if(values.size() > 0){
				final String selection = myIdColumn() + " = ?";
				final String[] selectionArgs = {String.valueOf(record.id)};
				int count = db.update(
						myTableName(),
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
		return success;
	}

	// Inserts the Record given.
	public boolean insert(Record record){
		boolean success;
		SQLiteDatabase db = openDb();
		success = insertRecord(db, record);
		closeDb();
		return success;
	}
	public int insert(List<Record> records){
		int updated = 0;
		SQLiteDatabase db = openDb();
		for(Record record : records){
			if( insertRecord(db, record) ){
				updated++;
			}
		}
		closeDb();
		return updated;
	}
	private boolean insertRecord(SQLiteDatabase db, Record record){
		Log.i(TAG, "Inserting Record");
		boolean success;
		ContentValues values = record.getContentValues();
		if(values.size() > 0){
			long count = db.insert(
					myTableName(),
				    "",
				    values);
			success = (count > 0);
		}
		else
			success = false;
		return success;
	}
	
	//Convenience methods for extracting value from the record the cursor is pointing to
	protected int getInteger(Cursor c, String columnName){
		return c.getInt(c.getColumnIndexOrThrow(columnName));
	}
	protected String getText(Cursor c, String columnName){
		return c.getString(c.getColumnIndexOrThrow(columnName));
	}
	//Private convenience method to make a replacement string with n questions marks
	private String makePlaceholders(int len){
		String placeholder = "?";
		for(int i=1; i<len; i++){
			placeholder = placeholder + ",";
		}
		return placeholder;
	}
	public static abstract class Record{
		public final int id;

		Record(int id){
			this.id = id;
		}
		
		protected abstract ContentValues diff(Record other);
		protected abstract ContentValues getContentValues();
	}
	
	// Define static DatabaseHelper so that we will always have one connection to the DB
	// This prevents concurrent write failures.
	protected static class DatabaseHelper extends SQLiteOpenHelper{
		private int databaseRefs = 0;
		
		public DatabaseHelper(Context context){
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		//Keep track of how many database references are out there
		@Override
		public SQLiteDatabase getWritableDatabase(){
			Log.i(TAG, "Opening Database Reference (Currently open: " + databaseRefs + ")");
			databaseRefs++;
			return super.getWritableDatabase();
		}
		
		//Check if there are any open references before closing
		public boolean closeCheck(){
			Log.i(TAG, "Closing Database Reference (Currently open: " + databaseRefs + ")");
			databaseRefs--;
			if(databaseRefs <= 0){
				Log.i(TAG, "No more references.  Closing Database completely");
				super.close();
				return true;
			}
			return false;
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
		@Override
		public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Downgrading database from version " + oldVersion + " to " +
	                newVersion + ", which will destroy all old data");
	            db.execSQL(TABLE_DELETE_SHOWS);
	            db.execSQL(TABLE_DELETE_EPISODES);
	            onCreate(db);
		}
	}
}
