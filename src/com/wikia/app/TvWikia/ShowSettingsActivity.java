package com.wikia.app.TvWikia;

import com.wikia.app.TvWikia.EpisodeSettingsFragment.EditSeasonDialog;
import com.wikia.app.TvWikia.EpisodeSettingsFragment.EpisodeSettingsListener;
import com.wikia.app.TvWikia.db.DbShowsTable;
import com.wikia.app.TvWikia.db.DbShowsTable.Show;

import android.os.Bundle;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class ShowSettingsActivity extends FragmentActivity implements EpisodeSettingsListener{

	private static final String TAG = "ShowSettingsActivity";
	private Show mShow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		intent.getIntExtra(BrowserActivity.SHOW_ID_MESSAGE, -1);

		int showId = intent.getIntExtra(BrowserActivity.SHOW_ID_MESSAGE, -1);
		DbShowsTable showsTable = new DbShowsTable(getBaseContext());
		if(showId > 0){
			mShow = (Show) showsTable.get(showId);
		}
		if(mShow == null){
			Log.w(TAG, "Could not identify what show this is (id: " + showId + ")");
		}
		else{
			Log.i(TAG, "We found out that this show is " + mShow.title + " (id: " + showId + ")");
			
			//Lookup what date in the past we need to show from
		}
		
		setContentView(R.layout.activity_show_settings);

		// Add the fragment to the view        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.show_settings_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create an instance of EpisodeSelectorFragment
            EpisodeSettingsFragment firstFragment = new EpisodeSettingsFragment();
            
            // In case this activity was started with special instructions from an Intent,
            // pass the Intent's extras to the fragment as arguments
            firstFragment.setArguments(getIntent().getExtras());
            
            // Add the fragment to the 'show_settings_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.show_settings_container, firstFragment, "episodeSelector").commit();
        }
	}
	
	public void onResume(){
		super.onResume();
		EpisodeSettingsFragment fragment = (EpisodeSettingsFragment) getSupportFragmentManager().findFragmentByTag("episodeSelector");
		fragment.setSeasonValue(String.valueOf(mShow.userSeason));
		fragment.setEpisodeValue(String.valueOf(mShow.userEpisode));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_settings, menu);
		return true;
	} 

	@Override
	public void onFinishEditSeasonDialog(String season) {
		EpisodeSettingsFragment fragment = (EpisodeSettingsFragment) getSupportFragmentManager().findFragmentByTag("episodeSelector");
		fragment.setSeasonValue(season);
	}

	@Override
	public void onClick(View v) {
		Log.i(TAG, "onClick handler invoked");
		if(v.getId() == R.id.season_label || v.getId() == R.id.season_value){
	        FragmentManager fm = getSupportFragmentManager();
	        EditSeasonDialog editNameDialog = new EditSeasonDialog();
	        editNameDialog.show(fm, "fragment_edit_season");
		}
	}
}
