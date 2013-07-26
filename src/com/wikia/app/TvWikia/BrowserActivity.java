package com.wikia.app.TvWikia;

import com.wikia.app.TvWikia.db.DbShowsTable;
import com.wikia.app.TvWikia.db.DbShowsTable.Show;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.support.v4.app.NavUtils;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;

public class BrowserActivity extends Activity {

	private static final String TAG = "BrowserActivity";
	
	public static final String SHOW_ID_MESSAGE = "SHOW_ID";
	
	WebView webview;
	private Show mShow;
	private String currentUrl;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Figure out what show we are looking at
		Intent intent = getIntent();
		
        final String action = intent.getAction();
    	//Get the URL passed in to the intent
        final String url = intent.getDataString();
        final DbShowsTable showsTable = new DbShowsTable(getBaseContext());
        
		int showId = intent.getIntExtra(SHOW_ID_MESSAGE, -1);
		if(showId > 0){
			mShow = (Show) showsTable.get(showId);
		}
		else{
			//See if we can determine it from the URL.findShowByUrl(url);
			mShow = showsTable.findShowByUrl(url);
		}
		if(mShow == null){
			Log.w(TAG, "Could not identify what show this is (id: " + showId + ", url: " + url +")");
		}
		else{
			Log.i(TAG, "We found out that this show is " + mShow.title + " (id: " + showId + ", url: " + url +")");
			
			//Lookup what date in the past we need to show from
		}
		//Create the WebView
		setupWebView();
		
		//Set the view to the webview
		setContentView(webview);
		
		//Load the data into the view
        if (Intent.ACTION_VIEW.equals(action)) {
        	//Load the URL
    		webview.loadUrl(url);
            currentUrl = url;
        }
        else{
        	//Load the no URL message
        	webview.loadData("<div>No page sent</div>", "text/html", null);
        	currentUrl = null;
        }
		// Show the Up button in the action bar.
		setupActionBar();
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}
	
	//Set up the WebView with settings
	@SuppressLint("SetJavaScriptEnabled")
	private void setupWebView(){
		
		//Create the webview
		webview = new WebView(this);
		
		//Enable Javascript
		webview.getSettings().setJavaScriptEnabled(true);
		
		//Override Urls so that they don't ask to switch apps
		webview.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url){
				view.loadUrl(url);
				currentUrl = url;
				return true;
			}
			
		});
		//Show progress of page load
		final Activity activity = this;
		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		webview.setWebChromeClient(new WebChromeClient(){
			@Override
			public void onProgressChanged(WebView view, int progress){
				activity.setProgress(progress * 1000);
			}
		});
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.browser, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.action_settings:
			Intent intent = new Intent(this, ShowSettingsActivity.class);
			intent.putExtra(SHOW_ID_MESSAGE, mShow.id);
			startActivity(intent);
			return true;
		}
			
		return super.onOptionsItemSelected(item);
	}

}
