package com.wikia.app.TvWikia;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;

public class BrowserActivity extends Activity {

	WebView webview;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Create the WebView
		setupWebView();
		
		//Set the view to the webview
		setContentView(webview);
		
		//Load the data into the view
		Intent intent = getIntent();
        final String action = intent.getAction();
        final String url;
        if (Intent.ACTION_VIEW.equals(action)) {
        	//Get the URL passed in to the intent
        	url = intent.getDataString();
        	//Load the URL
    		webview.loadUrl(url);
            
        }
        else{
        	//Load the no URL message
        	webview.loadData("<div>No page sent</div>", "text/html", null);
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
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		}
			
		return super.onOptionsItemSelected(item);
	}

}
