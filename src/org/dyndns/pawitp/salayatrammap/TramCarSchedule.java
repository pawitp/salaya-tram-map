package org.dyndns.pawitp.salayatrammap;

import java.util.Calendar;

import android.util.Log;

public class TramCarSchedule {
	
	private static final String TAG = "TramCarSchedule";
	
	private static final String TIME_FORMAT = "%02d:%02d";
	
	Integer[][] mSchedule;
	
	public TramCarSchedule(Integer[][] schedule) {
		mSchedule = schedule;
	}
	
	public String getLastTram() {
		return buildTimeString(locateNextTram() - 1);
	}
	
	public String getNextTram() {
		return buildTimeString(locateNextTram());
	}
	
	private int locateNextTram() {
		Calendar now = Calendar.getInstance();
		Calendar tram = Calendar.getInstance();
		
		int i;
		for (i = 0; i < mSchedule.length; i++) {
			tram.set(Calendar.HOUR_OF_DAY, mSchedule[i][0]);
			tram.set(Calendar.MINUTE, mSchedule[i][1]);
			
			if (tram.after(now)) {
				Log.v(TAG, "Found tram: " + i);
				break;
			}
		}
		
		return i;
	}
	
	private String buildTimeString(int i) {
		return String.format(TIME_FORMAT, mSchedule[i][0], mSchedule[i][1]);
	}
	
}