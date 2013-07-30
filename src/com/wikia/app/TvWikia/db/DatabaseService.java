package com.wikia.app.TvWikia.db;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import com.wikia.app.TvWikia.db.DbBaseAdapter.Record;
import com.wikia.app.TvWikia.db.DbEpisodesTable.Episode;
import com.wikia.app.TvWikia.db.DbShowsTable.Show;
import com.wikia.app.TvWikia.db.EpisodeParser.Entry;

import android.app.Service;
import android.content.Intent;
import android.util.Log;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;


public class DatabaseService extends Service {
	private static final String TAG = "DatabaseService";
	
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	
	private final class ServiceHandler extends Handler{
		public ServiceHandler(Looper looper){
			super(looper);
		}
		
		@Override
		public void handleMessage(Message msg){
			Log.i(TAG, "Starting to handle Message");
			ArrayList<Record> shows = new DbShowsTable(getBaseContext()).list();
			for(Record s : shows){
				try{
					ParseEpisodesFromShow((Show) s);
				}
				catch(Exception e){
					Log.e(TAG, e.toString());
				}
			}
			Log.i(TAG, "Finished handling Message");
			stopSelf(msg.arg1);
		}
	}
	
	@Override
	public void onCreate(){
		HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);;
		thread.start();
		
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		Log.i(TAG, "Service Starting");
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		mServiceHandler.sendMessage(msg);
		
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent){
		return null;
	}
	
	@Override
	public void onDestroy(){
		Log.i(TAG, "Destroying DatabaseService");
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
		DbEpisodesTable episodesTable = new DbEpisodesTable(getBaseContext());
		ArrayList<Record> updates = new ArrayList<Record>();
		ArrayList<Record> inserts = new ArrayList<Record>();
		for (Entry e : entries) {
			//ignore episodes that haven't aired yet
			if(e.airdate != null && !e.airdate.equals("")){
				//Check if this episode is already in our table
				int episodeId = episodesTable.getEpisodeId(show.id, e.season, e.episode);
				if(episodeId < 0){
					Log.i(TAG, "Generating Record for " + show.title + " S" + e.season + "E" + e.episode);
					inserts.add(new Episode(-1, show.id, e.title, e.season, e.episode, e.airdate));
				}
				else{
					Log.i(TAG, "Updating Record for " + show.title + " S" + e.season + "E" + e.episode);
					updates.add(new Episode(episodeId, show.id, e.title, e.season, e.episode, e.airdate));
				}
			}
		}
		episodesTable.update(updates);
		episodesTable.insert(inserts);
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