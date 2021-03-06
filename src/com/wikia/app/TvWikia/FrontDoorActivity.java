package com.wikia.app.TvWikia;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.wikia.app.TvWikia.DatabaseServiceConnection.DatabaseListener;
import com.wikia.app.TvWikia.db.DatabaseService;
import com.wikia.app.TvWikia.db.DbBaseAdapter.Record;
import com.wikia.app.TvWikia.db.DbShowsTable.Show;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;

public class FrontDoorActivity extends Activity implements DatabaseListener {

	protected static final String TAG = "FrontDoorActivity";
	private final DatabaseServiceConnection mConnection = new DatabaseServiceConnection(this);
	protected DatabaseService mService;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_front_door);
		
		// Bind to DatabaseService
        Intent intent = new Intent(this, DatabaseService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        
        //Start the service, which will make it update the database.  Different and unrelated to binding
        startService(intent);
	}

	@Override
	public void setService(DatabaseService service){
		this.mService = service;
	}

	@Override
	public void onStop(){
		super.onStop();
		try{
		unbindService(mConnection);
		}
		catch(Exception e){
			Log.i(TAG, e.toString());
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		}
			
		return super.onOptionsItemSelected(item);
	}
	@Override
	public void onServiceConnected(){
		// Get all shows
		ArrayList<Record> shows = mService.getAllShows();
		
		// Create a banner for each show
		for(Record s : shows){
			new CreateBannerTask().execute((Show) s);
		}
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.front_door, menu);
		return true;
	}
	
	private void appendButton(final ViewGroup layout, final Show show, final Bitmap banner){
		Log.i(TAG, "Appending button for Show ID " + show.id + " to layout");
		
	    //Create the button
		ImageButton b = new ImageButton(getBaseContext());
		
		//Load the banner image defined in the database for this show
		b.setImageBitmap(banner);

		//Define the listener for this button to redirect to the Browser upon click
		OnClickListener buttonClickListener;
		buttonClickListener = new OnClickListener(){

	        @Override
	        public void onClick(View v) {
	        	Log.i(TAG, "Clicked Show ID " + show.id);
	        	Intent intent = new Intent(Intent.ACTION_VIEW);
	        	intent.setComponent(new ComponentName(getBaseContext(), BrowserActivity.class));
	        	intent.setData(Uri.parse(show.wikiaUrl));
	        	intent.putExtra(BrowserActivity.SHOW_ID_MESSAGE, show.id);
	        	startActivity(intent);
	        }
	    };
	    
		//Bind the click event of the button to the listener
		b.setOnClickListener(buttonClickListener);

		b.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT
			)
		);
		//Add the button to the layout
		layout.addView(b);
		Log.i(TAG, "Appended button for Show ID " + show.id + " to layout");
	}
	
    // Implementation of AsyncTask used to download XML feed from stackoverflow.com.
    private class CreateBannerTask extends AsyncTask<Show, Void, Bitmap> {
    	
    	private Show show;
    	
        @Override
        protected Bitmap doInBackground(Show... shows) {
            try {
            	show = shows[0];
                return downloadBitmap(show.bannerUrl);
            } catch (IOException e) {
            	Log.e(TAG, getResources().getString(R.string.connection_error) + e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
        	if(result != null){
	    		ViewGroup layout = (ViewGroup) findViewById(R.id.front_door_layout);
	        	appendButton(layout, show, result);
        	}
        }
    }
	// Utility method to download image from the internet
	private static Bitmap downloadBitmap(String url) throws IOException {
		HttpUriRequest request = new HttpGet(url);
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(request);
		
		StatusLine statusLine = response.getStatusLine();
		int statusCode = statusLine.getStatusCode();
		if (statusCode == 200) {
		  HttpEntity entity = response.getEntity();
		  byte[] bytes = EntityUtils.toByteArray(entity);
		
		  Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,
		      bytes.length);
		  return bitmap;
		} else {
		  throw new IOException("Download failed, HTTP response code "
		  + statusCode + " - " + statusLine.getReasonPhrase());
		}
	}
}
