package com.wikia.app.TvWikia;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class EpisodeSettingsFragment extends Fragment implements OnClickListener {

	private static final String TAG = "EpisodeSettingsFragment";

	public EpisodeSettingsFragment() {
		// Required empty public constructor
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_episode_settings, container,
				false);
	}
	
	public void setSeasonValue(String value){
		TextView seasonValue = (TextView) getView().findViewById(R.id.season_value);
		seasonValue.setText(value);
		
	}
	
	public void setEpisodeValue(String value){
		TextView episodeValue = (TextView) getView().findViewById(R.id.episode_value);
		episodeValue.setText(value);
		
	}
	
	public void onClick(View v){
		Log.i(TAG, "onClick handler invoked");
		EpisodeSettingsListener activity = (EpisodeSettingsListener) getActivity();
		activity.onClick(v);
	}
	
	public interface EpisodeSettingsListener extends OnClickListener{
		void onFinishEditSeasonDialog(String value);
	}
	
	public static class EditSeasonDialog extends DialogFragment{
		private EditText mEditText;
		private EpisodeSettingsListener mListener;
		
	    public EditSeasonDialog() {
	        // Empty constructor required for DialogFragment
	    }
	    @Override
	    public void onAttach(Activity activity) {
	        super.onAttach(activity);
	        // Verify that the host activity implements the callback interface
	        try {
	            // Instantiate the NoticeDialogListener so we can send events to the host
	            mListener = (EpisodeSettingsListener) activity;
	        } catch (ClassCastException e) {
	            // The activity doesn't implement the interface, throw exception
	            throw new ClassCastException(activity.toString()
	                    + " must implement EpisodeSettingsListener");
	        }
	    }
	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	    	Activity activity = getActivity();
	    	LayoutInflater inflater = activity.getLayoutInflater();
	        View view = inflater.inflate(R.layout.fragment_season_selector, null);
	        mEditText = (EditText) view.findViewById(R.id.txt_season_selector);
	        // Use the Builder class for convenient dialog construction
	        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
	        builder.setTitle("Select Season")
	        		.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            	   		public void onClick(DialogInterface dialog, int id) {
							mListener.onFinishEditSeasonDialog(mEditText.getText().toString());
							dialog.dismiss();
        	   			}
	               })
	               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                       dialog.dismiss();
	                   }
	               });
	        builder.setView(view);
	        // Create the AlertDialog object and return it
	        return builder.create();
	    }
	}
}
