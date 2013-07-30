package com.wikia.app.TvWikia;

import com.wikia.app.TvWikia.db.DatabaseService;
import com.wikia.app.TvWikia.db.DatabaseService.DatabaseBinder;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class DatabaseServiceConnection implements ServiceConnection {

	private DatabaseListener mListener;
	
	public interface DatabaseListener{
		public void onServiceConnected();
		public void setService(DatabaseService service);
	}
	
	public DatabaseServiceConnection(DatabaseListener listener){
		this.mListener = listener;
	}
	
    @Override
    public void onServiceConnected(ComponentName className,
            IBinder service) {
        // We've bound to LocalService, cast the IBinder and get LocalService instance
        DatabaseBinder binder = (DatabaseBinder) service;
        mListener.setService(binder.getService());
        mListener.onServiceConnected();
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {        
    	mListener.setService(null);
    }
}
