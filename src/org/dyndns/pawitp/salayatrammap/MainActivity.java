package org.dyndns.pawitp.salayatrammap;

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
			txtLeft.setText(schedule.getLastTram());
		} catch (TramException e) {
			txtLeft.setText(R.string.no_tram);
		}
		
		String strNext;
		try {
			strNext = schedule.getNextTram();
		} catch (NoMoreTramException e) {
			strNext = getString(R.string.no_tram);
		}
		txtNext.setText(String.format(getString(R.string.next_tram), strNext));
	}
	
	private Runnable runnableUpdateTramsTime = new Runnable() {
		
		public void run() {
			Log.v(TAG, "Updating trams time");
			
			updateTramTime(TramsSchedule.TRAM_GREEN, R.id.txtTramLeftGreen, R.id.txtTramNextGreen);
			updateTramTime(TramsSchedule.TRAM_BLUE, R.id.txtTramLeftBlue, R.id.txtTramNextBlue);
			updateTramTime(TramsSchedule.TRAM_RED, R.id.txtTramLeftRed, R.id.txtTramNextRed);
			
			long updateTime = mTramsSchedule.getNextUpdateTime();
			
			if (updateTime != TramCarSchedule.NO_UPDATE) {
				mHandler.postDelayed(runnableUpdateTramsTime, updateTime);
			}
			else {
				// TODO: Set update for next day
			}
		}
		
	};
}