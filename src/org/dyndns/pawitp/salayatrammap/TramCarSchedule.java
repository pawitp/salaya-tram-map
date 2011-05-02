package org.dyndns.pawitp.salayatrammap;

import java.util.Calendar;

import android.util.Log;

public class TramCarSchedule {
	
	private static final String TAG = "TramCarSchedule";
	
	static final long NO_UPDATE = Long.MAX_VALUE;
	
	private static final String TIME_FORMAT = "%02d:%02d";
	private static final long TRAM_ROUND_TIME = 600000; // 10 minutes
	
	Integer[][] mSchedule;
	
	public TramCarSchedule(Integer[][] schedule) {
		mSchedule = schedule;
	}
	
	public String getLastTram() throws NoTramLeftException, NoMoreTramException {
		try {
			int index = locateNextTram() - 1;
			
			if (index < 0) {
				throw new NoTramLeftException();
			}
			
			return buildTimeString(index);
		}
		catch (NoMoreTramException e) {
			// Check if the last round of tram is still running
			int indexLast = mSchedule.length - 1;
			Calendar last = tramTimeToCalendar(mSchedule[indexLast]);
			Calendar now = Calendar.getInstance();
			
			if (now.getTimeInMillis() - last.getTimeInMillis() < TRAM_ROUND_TIME) {
				return buildTimeString(indexLast);
			}
			else {
				throw new NoMoreTramException();
			}
		}
	}
	
	public String getNextTram() throws NoMoreTramException {
		return buildTimeString(locateNextTram());
	}
	
	public long getUpdateTime() {
		Calendar now = Calendar.getInstance();
		try {
			Calendar tram = tramTimeToCalendar(mSchedule[locateNextTram()]); 
			return tram.getTimeInMillis() - now.getTimeInMillis();
		}
		catch (NoMoreTramException e) {
			// Check last round
			int indexLast = mSchedule.length - 1;
			Calendar last = tramTimeToCalendar(mSchedule[indexLast]);
			
			long timeDiff = now.getTimeInMillis() - last.getTimeInMillis();
			if (timeDiff < TRAM_ROUND_TIME) {
				return TRAM_ROUND_TIME  - timeDiff;
			}
			else {
				return NO_UPDATE;
			}
		}
	}
	
	private int locateNextTram() throws NoMoreTramException {
		Log.v(TAG, "Calculating next tram");
		Calendar now = Calendar.getInstance();
		now.set(Calendar.SECOND, 0); // Avoid second differences in comparison
		now.set(Calendar.MILLISECOND, 0);
		Calendar tram = Calendar.getInstance();
		tram.set(Calendar.SECOND, 0);
		tram.set(Calendar.MILLISECOND, 0);
		
		boolean found = false;
		int i;
		for (i = 0; i < mSchedule.length; i++) {
			tram.set(Calendar.HOUR_OF_DAY, mSchedule[i][0]);
			tram.set(Calendar.MINUTE, mSchedule[i][1]);
			
			if (tram.after(now)) {
				Log.v(TAG, "Found tram: " + i);
				found = true;
				break;
			}
		}
		
		if (!found) {
			throw new NoMoreTramException();
		}
		
		return i;
	}
	
	private String buildTimeString(int i) {
		return String.format(TIME_FORMAT, mSchedule[i][0], mSchedule[i][1]);
	}
	
	private Calendar tramTimeToCalendar(Integer[] time) {
		Calendar ret = Calendar.getInstance();
		ret.set(Calendar.HOUR_OF_DAY, time[0]);
		ret.set(Calendar.MINUTE, time[1]);
		ret.set(Calendar.SECOND, 0);
		ret.set(Calendar.MILLISECOND, 0);
		return ret;
	}
}