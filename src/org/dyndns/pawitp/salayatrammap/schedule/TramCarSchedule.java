package org.dyndns.pawitp.salayatrammap.schedule;

import android.text.format.Time;
import android.util.Log;

public class TramCarSchedule {
	
	private static final String TAG = "TramCarSchedule";
	
	private static final long TRAM_ROUND_TIME = 600000; // 10 minutes
	
	Integer[][] mSchedule;
	
	public TramCarSchedule(Integer[][] schedule) {
		mSchedule = schedule;
	}
	
	public Time getLastTram() throws NoTramLeftException, NoMoreTramException {
		try {
			int index = locateNextTram() - 1;
			
			if (index < 0) {
				throw new NoTramLeftException();
			}
			
			return tramTimeToTime(mSchedule[index]);
		}
		catch (NoMoreTramException e) {
			// Check if the last round of tram is still running
			int indexLast = mSchedule.length - 1;
			Time last = tramTimeToTime(mSchedule[indexLast]);
			Time now = new Time();
			now.setToNow();
			
			long diff = now.toMillis(false) - last.toMillis(false);
			if (diff < TRAM_ROUND_TIME) {
				return last;
			}
			else {
				throw new NoMoreTramException();
			}
		}
	}
	
	public Time getNextTram() throws NoMoreTramException {
		return tramTimeToTime(mSchedule[locateNextTram()]);
	}
	
	public long getUpdateTime() {
		Time now = new Time();
		now.setToNow();
		
		try {
			Time tram = tramTimeToTime(mSchedule[locateNextTram()]); 
			return tram.toMillis(false) - now.toMillis(false);
		}
		catch (NoMoreTramException e) {
			// Check last round
			int indexLast = mSchedule.length - 1;
			Time last = tramTimeToTime(mSchedule[indexLast]);
			
			long timeDiff = now.toMillis(false) - last.toMillis(false);
			if (timeDiff < TRAM_ROUND_TIME) {
				return TRAM_ROUND_TIME  - timeDiff;
			}
			else {
				// wait till next day
				Time tomorrow = new Time();
				tomorrow.setToNow();
				tomorrow.hour = 24;
				tomorrow.minute = 0;
				tomorrow.second = 0;
				
				return tomorrow.toMillis(false) - now.toMillis(false);
			}
		}
	}
	
	private int locateNextTram() throws NoMoreTramException {
		Log.v(TAG, "Calculating next tram");
		Time now = new Time();
		now.setToNow();
		now.second = 0; // Avoid second differences in comparison
		
		Time tram = new Time(now);
		
		boolean found = false;
		int len = mSchedule.length;
		int i;
		for (i = 0; i < len; i++) {
			tram.hour = mSchedule[i][0];
			tram.minute = mSchedule[i][1];
			
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
	
	private Time tramTimeToTime(Integer[] time) {
		Time ret = new Time();
		ret.setToNow();
		ret.hour = time[0];
		ret.minute = time[1];
		ret.second = 0;
		return ret;
	}
}