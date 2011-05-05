package org.dyndns.pawitp.salayatrammap.schedule;

import android.text.format.Time;
import android.util.Log;

public class TramsSchedule {
	
	private static final String TAG = "TramsSchedule";
	
	public static final int TRAM_GREEN = 0;
	public static final int TRAM_BLUE = 1;
	public static final int TRAM_RED = 2;
	
	private static final int TRAMS_COUNT = 3;
	private TramCarSchedule[] mTramCarSchedules = new TramCarSchedule[TRAMS_COUNT];
	
	private static final Integer[][] OTHER_NORMAL_SCHEDULE = {
		{6, 30}, {6, 40}, {6, 50}, {7, 00}, {7, 10}, {7, 20}, {7, 30}, {7, 40}, {7, 50},
		{8, 00}, {8, 10}, {8, 20}, {8, 30}, {8, 40}, {8, 50}, {9, 00}, {9, 20}, {9, 40},
		{10, 00}, {10, 20}, {10, 40}, {11, 00}, {11, 10}, {11, 20}, {11, 30}, {11, 40},
		{11, 50}, {12, 00}, {12, 10}, {12, 20}, {12, 30}, {12, 40}, {12, 50}, {13, 00},
		{13, 10}, {13, 20}, {13, 30}, {13, 40}, {13, 50}, {14, 00}, {14, 20}, {14, 40},
		{15, 00}, {15, 20}, {15, 40}, {16, 00}, {16, 10}, {16, 20}, {16, 30}, {16, 40},
		{16, 50}, {17, 00}, {17, 10}, {17, 20}, {17, 30}, {17, 40}, {17, 50}, {18, 00},
		{18, 10}, {18, 20}, {18, 30}, {18, 40}, {19, 00}, {19, 20}, {19, 40}, {20, 00},
	};

	private static final Integer[][] OTHER_HOLIDAY_SCHEDULE = {
		{8, 00}, {8, 20}, {8, 40}, {9, 00}, {9, 20}, {9, 40}, {10, 00}, {10, 20}, {10, 40},
		{11, 00}, {11, 20}, {11, 40}, {12, 00}, {12, 20}, {12, 40}, {13, 00}, {13, 20},
		{13, 40}, {14, 00}, {14, 20}, {14, 40}, {15, 00}, {15, 20}, {15, 40}, {16, 00},
		{16, 20}, {16, 40}, {17, 00}, {17, 20}, {17, 40}, {18, 00},
	};

	private static final Integer[][] GREEN_NORMAL_SCHEDULE = {
		{6, 35}, {6, 45}, {6, 55}, {7, 05}, {7, 15}, {7, 25}, {7, 35}, {7, 45}, {7, 55},
		{8, 05}, {8, 15}, {8, 25}, {8, 35}, {8, 45}, {8, 55}, {9, 05}, {9, 25}, {9, 45},
		{10, 05}, {10, 25}, {10, 45}, {11, 05}, {11, 15}, {11, 25}, {11, 35}, {11, 45},
		{11, 55}, {12, 05}, {12, 15}, {12, 25}, {12, 35}, {12, 45}, {12, 55}, {13, 05},
		{13, 15}, {13, 25}, {13, 35}, {13, 45}, {13, 55}, {14, 05}, {14, 25}, {14, 45},
		{15, 05}, {15, 25}, {15, 45}, {16, 05}, {16, 15}, {16, 25}, {16, 35}, {16, 45},
		{16, 55}, {17, 05}, {17, 15}, {17, 25}, {17, 35}, {17, 45}, {17, 55}, {18, 05},
		{18, 15}, {18, 25}, {18, 35}, {18, 45}, {19, 05}, {19, 25}, {19, 45}, {20, 05},
	};

	private static final Integer[][] GREEN_HOLIDAY_SCHEDULE = {
		{8, 05}, {8, 25}, {8, 45}, {9, 05}, {9, 25}, {9, 45}, {10, 05}, {10, 25}, {10, 45},
		{11, 05}, {11, 25}, {11, 45}, {12, 05}, {12, 25}, {12, 45}, {13, 05}, {13, 25},
		{13, 45}, {14, 05}, {14, 25}, {14, 45}, {15, 05}, {15, 25}, {15, 45}, {16, 05},
		{16, 25}, {16, 45}, {17, 05}, {17, 25}, {17, 45}, {18, 05},
	};
	
	public TramsSchedule() {
		mTramCarSchedules[TRAM_GREEN] = new TramCarSchedule();
		mTramCarSchedules[TRAM_BLUE] = new TramCarSchedule();
		mTramCarSchedules[TRAM_RED] = new TramCarSchedule();
	}
	
	public TramCarSchedule getSchedule(int tram) {
		return mTramCarSchedules[tram];
	}
	
	public long getNextUpdateTime() {
		long updateTime = Long.MAX_VALUE;
		
		for (TramCarSchedule schedule : mTramCarSchedules) {
			long tramUpdateTime = schedule.getUpdateTime();
			if (tramUpdateTime < updateTime) {
				updateTime = tramUpdateTime;
			}
		}
		
		Log.v(TAG, "Updating in: " + updateTime);
		return updateTime;
	}
	
	public boolean isHoliday() {
		// TODO: Public holidays
		Time time = new Time();
		time.setToNow();
		
		if (time.weekDay == Time.SATURDAY || time.weekDay == Time.SUNDAY) {
			return true;
		}
		else {
			return false;
		}
	}
	
	// Always call before using any of my functions!
	// this functions also triggers calculation of next tram (for use with next update time)
	public void updateSchedules() {
		if (!isHoliday()) {
			mTramCarSchedules[TRAM_GREEN].updateSchedule(GREEN_NORMAL_SCHEDULE);
			mTramCarSchedules[TRAM_BLUE].updateSchedule(OTHER_NORMAL_SCHEDULE);
			mTramCarSchedules[TRAM_RED].updateSchedule(OTHER_NORMAL_SCHEDULE);	
		}
		else {
			mTramCarSchedules[TRAM_GREEN].updateSchedule(GREEN_HOLIDAY_SCHEDULE);
			mTramCarSchedules[TRAM_BLUE].updateSchedule(OTHER_HOLIDAY_SCHEDULE);
			mTramCarSchedules[TRAM_RED].updateSchedule(OTHER_HOLIDAY_SCHEDULE);
		}
	}
	
}
