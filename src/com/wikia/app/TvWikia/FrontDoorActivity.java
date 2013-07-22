package com.wikia.app.TvWikia;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;

public class FrontDoorActivity extends Activity {

	ImageButton imageButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_front_door);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.front_door, menu);
		return true;
	}

	public void openBrowser(View arg0){
    	Intent intent = new Intent(Intent.ACTION_VIEW);
    	intent.setComponent(new ComponentName(getBaseContext(), BrowserActivity.class));
    	intent.setData(Uri.parse("http://tardis.wikia.com"));
    	startActivity(intent);
	}
}
