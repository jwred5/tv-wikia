package com.wikia.app.TvWikia.db;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import com.wikia.app.TvWikia.db.DbEpisodesTable.Episode;
import com.wikia.app.TvWikia.db.DbShowsTable.Show;
import com.wikia.app.TvWikia.db.EpisodeParser.Entry;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class PopulateShowEpisodesTask extends AsyncTask<Show, Void, Void> {
	private static final String TAG = "PopulateShowEpisodesTask";
	
	private final Context mContext;
	
	public PopulateShowEpisodesTask(Context context){
		this.mContext = context;
	}
	
	@Override
	protected Void doInBackground(Show... shows) {
	    try {
	     	ParseEpisodesFromShow(shows[0]);
	    } catch (Exception e) {
	    	Log.e(TAG, e.toString());
	    }
		return null;
	}
	 // Loads xml from given url
	private void ParseEpisodesFromShow(Show show) throws XmlPullParserException, IOException {
		Log.i(TAG, "Starting ParseEpisodesFromShow");
		InputStream stream = null;
		// Instantiate the parser
		EpisodeParser episodeParser = new EpisodeParser();
		List<Entry> entries = null;
		try {
			Log.i(TAG, "Downloading......");
			String urlString = "DOCTORWHO.xml";
			stream = downloadUrl(urlString);        
			// Makes sure that the InputStream is closed after the app is
			// finished using it.
			entries = episodeParser.parse(stream);
		} finally {
			if (stream != null) {
				stream.close();
			} 
		}
		
		Log.i(TAG, "Generating Records");
		DbEpisodesTable episodesTable = new DbEpisodesTable(mContext);
		for (Entry e : entries) {       
			Log.i(TAG, "Generating Record for " + show.title + " S" + e.season + "E" + e.episode);
			episodesTable.insertEpisode(new Episode(-1, show.id, e.title, e.season, e.episode, e.airdate));
		}
		Log.i(TAG, "Records Generated");
		     return;
	}
	// Given a string representation of a URL, sets up a connection and gets
	// the contents.
	 private InputStream downloadUrl(String urlString) throws IOException {
		 URL url = new URL(urlString);
		 HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		 conn.setReadTimeout(10000 /* milliseconds */);
		 conn.setConnectTimeout(15000 /* milliseconds */);
		 conn.setRequestMethod("GET");
		 conn.setDoInput(true);
		 // Starts the query
	     conn.connect();
	     return conn.getInputStream();
	}
}
