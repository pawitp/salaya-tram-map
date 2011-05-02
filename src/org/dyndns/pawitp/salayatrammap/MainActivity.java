package org.dyndns.pawitp.salayatrammap;

import org.dyndns.pawitp.salayatrammap.schedule.NoMoreTramException;
import org.dyndns.pawitp.salayatrammap.schedule.TramCarSchedule;
import org.dyndns.pawitp.salayatrammap.schedule.TramException;
import org.dyndns.pawitp.salayatrammap.schedule.TramsSchedule;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	static final String TAG = "MainActivity";
	
	TramsSchedule mTramsSchedule;
	Handler mHandler;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		mTramsSchedule = new TramsSchedule();
		mHandler = new Handler();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		runnableUpdateTramsTime.run();
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		
		mHandler.removeCallbacks(runnableUpdateTramsTime);
	}

	private void updateTramTime(int tramId, int idLeft, int idNext) {
		TramCarSchedule schedule = mTramsSchedule.getSchedule(tramId);
		TextView txtLeft = (TextView) findViewById(idLeft);
		TextView txtNext = (TextView) findViewById(idNext);
		
		try {
			txtNext.setText(Utils.formatTime(this, schedule.getNextTram()));
		} catch (NoMoreTramException e) {
			txtNext.setText(R.string.no_tram);
		}
		
		try {
			txtLeft.setText(Utils.formatTime(this, schedule.getLastTram()));
		} catch (TramException e) {
			txtLeft.setText(R.string.no_tram);
		}
	}
	
	private Runnable runnableUpdateTramsTime = new Runnable() {
		
		public void run() {
			Log.v(TAG, "Updating trams time");
			
			mTramsSchedule.updateSchedules();
			
			updateTramTime(TramsSchedule.TRAM_GREEN, R.id.txtTramLeftGreen, R.id.txtTramNextGreen);
			updateTramTime(TramsSchedule.TRAM_BLUE, R.id.txtTramLeftBlue, R.id.txtTramNextBlue);
			updateTramTime(TramsSchedule.TRAM_RED, R.id.txtTramLeftRed, R.id.txtTramNextRed);
			
			long updateTime = mTramsSchedule.getNextUpdateTime();
			
			mHandler.postDelayed(runnableUpdateTramsTime, updateTime);
		}
		
	};
}