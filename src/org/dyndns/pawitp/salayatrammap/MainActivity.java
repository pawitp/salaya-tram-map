package org.dyndns.pawitp.salayatrammap;

import org.dyndns.pawitp.salayatrammap.map.MapView;
import org.dyndns.pawitp.salayatrammap.schedule.NoMoreTramException;
import org.dyndns.pawitp.salayatrammap.schedule.TramCarSchedule;
import org.dyndns.pawitp.salayatrammap.schedule.TramException;
import org.dyndns.pawitp.salayatrammap.schedule.TramsSchedule;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	static final String TAG = "MainActivity";
	
	private TramsSchedule mTramsSchedule;
	private Handler mHandler;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
		
		mTramsSchedule = new TramsSchedule(this);
		mHandler = new Handler();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			((MapView) findViewById(R.id.mapView)).showStopInfo(Integer.valueOf(intent.getDataString()));
		} else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			Toast.makeText(this, R.string.use_search_suggestion, Toast.LENGTH_SHORT).show();
		}
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

	@Override
	public void onBackPressed() {
		MapView mapView = (MapView) findViewById(R.id.mapView);
		if (mapView.isShowingStopInfo()) {
			mapView.hideStopInfo();
		}
		else {
			super.onBackPressed();
		}
	}
	
	public void holidayToggle(View view) {
		mTramsSchedule.setHoliday(!mTramsSchedule.isHoliday());
		
		mHandler.removeCallbacks(runnableUpdateTramsTime);
		runnableUpdateTramsTime.run();
	}
	
	public void btnSearchOnClick(View view) {
		onSearchRequested();
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
			
			// Update "Holiday" label
			TextView lblHoliday = (TextView) findViewById(R.id.lblScheduleType);
			lblHoliday.setText(mTramsSchedule.isHoliday() ? R.string.holiday_schedule : R.string.normal_schedule);
			
			long updateTime = mTramsSchedule.getNextUpdateTime();
			mHandler.postDelayed(runnableUpdateTramsTime, updateTime);
		}
		
	};
}