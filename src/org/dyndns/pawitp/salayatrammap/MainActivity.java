package org.dyndns.pawitp.salayatrammap;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	TramsSchedule mTramsSchedule;
	Handler mHandler;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		mTramsSchedule = new TramsSchedule();
		mHandler = new Handler();
		
		// TODO: Update every minute

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
		
		txtLeft.setText(schedule.getLastTram());
		txtNext.setText(String.format(getString(R.string.next_tram), schedule.getNextTram()));
	}
	
	private Runnable runnableUpdateTramsTime = new Runnable() {
		
		public void run() {
			updateTramTime(TramsSchedule.TRAM_GREEN, R.id.txtTramLeftGreen, R.id.txtTramNextGreen);
			updateTramTime(TramsSchedule.TRAM_BLUE, R.id.txtTramLeftBlue, R.id.txtTramNextBlue);
			updateTramTime(TramsSchedule.TRAM_RED, R.id.txtTramLeftRed, R.id.txtTramNextRed);
			
			mHandler.postDelayed(runnableUpdateTramsTime, mTramsSchedule.getNextUpdateTime());
		}
		
	};
}