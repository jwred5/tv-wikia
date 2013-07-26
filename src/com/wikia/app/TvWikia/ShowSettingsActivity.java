package com.wikia.app.TvWikia;

import com.wikia.app.TvWikia.db.DbShowsTable;
import com.wikia.app.TvWikia.db.DbShowsTable.Show;

import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ShowSettingsActivity extends Activity {

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

            // Create an instance of ExampleFragment
            EpisodeSelectorFragment firstFragment = new EpisodeSelectorFragment();
            
            // In case this activity was started with special instructions from an Intent,
            // pass the Intent's extras to the fragment as arguments
            firstFragment.setArguments(getIntent().getExtras());
            
            // Add the fragment to the 'fragment_container' FrameLayout
            getFragmentManager().beginTransaction()
                    .add(R.id.show_settings_container, firstFragment, "episodeSelector").commit();
        }
	}
	
	public void onResume(){
		super.onResume();
		EpisodeSelectorFragment fragment = (EpisodeSelectorFragment) getFragmentManager().findFragmentByTag("episodeSelector");
		fragment.setSeasonValue(String.valueOf(mShow.userSeason));
		fragment.setEpisodeValue(String.valueOf(mShow.userEpisode));
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void addEpisodesSelectorFragment(EpisodeSelectorFragment fragment){
		/*
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		EpisodeSelectorFragment fragment = new EpisodeSelectorFragment();
		fragmentTransaction.add(R.id.show_settings_container, fragment);
		fragmentTransaction.commit();
		
		fragment.setSeasonValue(String.valueOf(mShow.userSeason));
		fragment.setEpisodeValue(String.valueOf(mShow.userEpisode));
		*/
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_settings, menu);
		return true;
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class EpisodeSelectorFragment extends Fragment{
		
		private Show mShow;
		
		public static EpisodeSelectorFragment newInstance(int showId){
			Bundle b = new Bundle();
			b.putInt("showId", showId);
			
			EpisodeSelectorFragment f = new EpisodeSelectorFragment();
			f.setArguments(b);
			
			return f;
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			
			
			//int showId = getArguments().getInt("showId");
			/*
			int showId = 1;
			Log.i(TAG, "Looking for show " + showId);
			if(mShow != null && mShow.id != showId){
				mShow = (Show) new DbShowsTable(getActivity()).get(showId);
			}
			
			Log.i(TAG, "Inflating Fragment with show " + mShow.id);
			*/
			View view = inflater.inflate(R.layout.fragment_episode_selector,
					container, false);
			//Set the inital values
			TextView seasonValue = (TextView) view.findViewById(R.id.season_selector_value);
			seasonValue.setText("1");
			TextView episodeValue = (TextView) view.findViewById(R.id.episode_selector_value);
			episodeValue.setText("22");

			return view;
		}
		
		public void setSeasonValue(String value){
			TextView seasonValue = (TextView) getView().findViewById(R.id.season_selector_value);
			seasonValue.setText(value);
			
		}
		public void setEpisodeValue(String value){
			TextView episodeValue = (TextView) getView().findViewById(R.id.episode_selector_value);
			episodeValue.setText(value);
			
		}
	}

}
